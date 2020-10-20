package com.gateway.workflow.services;

import com.gateway.workflow.dtos.DarHistoryAggDto;
import com.gateway.workflow.dtos.DarHistoryDto;
import com.gateway.workflow.dtos.DarStepReviewDto;
import com.gateway.workflow.dtos.ManagerApprovedDto;
import javassist.NotFoundException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.gateway.workflow.util.ConstantPropertiesUtil.*;

@Service
public class DataRequestServiceImpl implements DataRequestService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Override
    public DarStepReviewDto completeManagerStepAndCreateStepDefinition(String businessKey, DarStepReviewDto darStepReviewDto) throws NotFoundException {
        Task task = getTask(businessKey);

        Map<String, Object> processVars = new HashMap<>();
        processVars.put("applicationStatus", darStepReviewDto.getDataRequestStatus());
        processVars.put("publisher", darStepReviewDto.getDataRequestPublisher());
        processVars.put("stepName", darStepReviewDto.getDataRequestStepName());
        processVars.put("notifyReviewer", darStepReviewDto.getNotifyReviewerSLA());
        processVars.put("managerApproved", darStepReviewDto.getManagerApproved());
        processVars.put("phaseApproved", darStepReviewDto.getPhaseApproved());
        processVars.put("finalPhaseApproved", darStepReviewDto.getFinalPhaseApproved());
        processVars.put("reviewerList", darStepReviewDto.getReviewerList());

        taskService.complete(task.getId(), processVars);

        return darStepReviewDto;
    }

    @Override
    public ManagerApprovedDto managerCompletedReview(String businessKey, ManagerApprovedDto managerApprovedDto) throws NotFoundException {
        Task task = getTask(businessKey);

        Map<String, Object> processVars = new HashMap<>();
        processVars.put("applicationStatus", managerApprovedDto.getDataRequestStatus());
        processVars.put("managerId", managerApprovedDto.getDataRequestManagerId());
        processVars.put("publisher", managerApprovedDto.getDataRequestPublisher());
        processVars.put("managerApproved", managerApprovedDto.getManagerApproved());
        processVars.put("phaseApproved", managerApprovedDto.getPhaseApproved());
        processVars.put("finalPhaseApproved", managerApprovedDto.getFinalPhaseApproved());

        taskService.delegateTask(task.getId(), managerApprovedDto.getDataRequestManagerId());
        taskService.complete(task.getId(), processVars);

        return managerApprovedDto;
    }

    @Override
    public DarStepReviewDto completeReviewerStep(String businessKey, DarStepReviewDto darStepReviewDto) throws NotFoundException {
        Task userTask = getUserTasks(businessKey).stream()
                .filter(x -> darStepReviewDto.getDataRequestUserId().equals(x.getAssignee()))
                .findFirst()
                .orElse(null);

        if(userTask == null && !darStepReviewDto.getPhaseApproved() && !darStepReviewDto.getManagerApproved()) {
            throw new NotFoundException(String.format("No assignee was found matching %s", darStepReviewDto.getDataRequestUserId()));
        }

        //Manager Override
        if(darStepReviewDto.getManagerApproved() && (darStepReviewDto.getPhaseApproved() || darStepReviewDto.getFinalPhaseApproved())) {
            Task delegateUserTask = getTask(businessKey);
            taskService.delegateTask(delegateUserTask.getId(), darStepReviewDto.getDataRequestUserId());
            userTask = delegateUserTask;
        }

        //If not phase not approved
        if(!darStepReviewDto.getPhaseApproved() || (darStepReviewDto.getPhaseApproved() && darStepReviewDto.getFinalPhaseApproved())) {
            Map<String, Object> processVars = new HashMap<>();
            processVars.put("userId", darStepReviewDto.getDataRequestUserId());
            processVars.put("managerApproved", darStepReviewDto.getManagerApproved());
            processVars.put("phaseApproved", darStepReviewDto.getPhaseApproved());
            processVars.put("finalPhaseApproved", darStepReviewDto.getFinalPhaseApproved());

            taskService.complete(userTask.getId(), processVars);
        }

        if(darStepReviewDto.getPhaseApproved() && darStepReviewDto.getReviewerList() == null && !darStepReviewDto.getFinalPhaseApproved())
            throw new IllegalArgumentException(
                    String.format("%s has no records. Please sure reviewers are added before continuing",
                            darStepReviewDto.getReviewerList()));

        //If phase approved and reviewer is greater than zero
        if(darStepReviewDto.getPhaseApproved() && !darStepReviewDto.getFinalPhaseApproved()) {
            createNextStepDefinition(businessKey, darStepReviewDto);
        }

        return darStepReviewDto;
    }

    @Override
    public DarHistoryAggDto getDarRequestHistory(String businessKey) throws NotFoundException {
        String process = getProcessId(businessKey);
        List<String> hdq = historyService.createHistoricDetailQuery()
                .processInstanceId(process)
                .list()
                .stream().map(x -> ((HistoricDetailVariableInstanceUpdateEntity)x).getActivityInstanceId()).distinct().collect(Collectors.toList());

        List<DarHistoryDto> darHistoryDtoList = new ArrayList<>();
        List<Date> timeStamp = new ArrayList<>();
        for (String actInstId: hdq) {
            DarHistoryDto temp = getProcessHistory(actInstId);

            if(temp != null) {
                timeStamp.add(temp.getDataTimeStamp());
                darHistoryDtoList.add(temp);
            }
        }

        return DarHistoryAggDto.builder()
                .darHistoryList(darHistoryDtoList)
                .dataRequestId(businessKey)
                .timeInStatus(timeStamp.size() > 1 ? timeStamp.get(1).getTime() - timeStamp.get(0).getTime() : 0)
                .build();
    }

    private DarHistoryDto getProcessHistory(String actInstId) {
        Optional<HistoricDetail> historicDetailOptionalStatus = historyService.createHistoricDetailQuery()
                .activityInstanceId(actInstId).orderByTime().asc().variableUpdates().list().stream()
                .filter(x -> ((HistoricDetailVariableInstanceUpdateEntity)x).getName().equals(DATA_REQUEST_STATUS))
                .reduce((first, second) -> second);

        Optional<HistoricDetail> historicDetailOptionalDateTime = historyService.createHistoricDetailQuery()
                .activityInstanceId(actInstId).orderByTime().asc().variableUpdates().list().stream()
                .filter(x -> ((HistoricDetailVariableInstanceUpdateEntity)x).getName().equals(DATA_REQUEST_DATETIME))
                .reduce((first, second) -> second);

        Optional<HistoricDetail> historicDetailOptionalPublisher = historyService.createHistoricDetailQuery()
                .activityInstanceId(actInstId).orderByTime().asc().variableUpdates().list().stream()
                .filter(x -> ((HistoricDetailVariableInstanceUpdateEntity)x).getName().equals(DATA_REQUEST_PUBLISHER))
                .reduce((first, second) -> second);

        Optional<HistoricDetail> historicDetailOptionalArchived = historyService.createHistoricDetailQuery()
                .activityInstanceId(actInstId).orderByTime().asc().variableUpdates().list().stream()
                .filter(x -> ((HistoricDetailVariableInstanceUpdateEntity)x).getName().equals(DATA_REQUEST_ARCHIVED))
                .reduce((first, second) -> second);

        Optional<DarHistoryDto> darHistoryDtoOptional = historicDetailOptionalStatus.map(x -> DarHistoryDto.builder()
                .dataRequestStatus((String)((HistoricDetailVariableInstanceUpdateEntity) x).getValue())
                .dataTimeStamp((Date)((HistoricDetailVariableInstanceUpdateEntity)x).getTimestamp())
                .build());

        historicDetailOptionalDateTime.map(x -> ((String)((HistoricDetailVariableInstanceUpdateEntity) x).getValue()))
                .ifPresent(meta ->  darHistoryDtoOptional.get().setDataRequestDateTime(meta));

        historicDetailOptionalPublisher.map(x -> ((String)((HistoricDetailVariableInstanceUpdateEntity) x).getValue()))
                .ifPresent(meta ->  darHistoryDtoOptional.get().setDataRequestPublisher(meta));

        historicDetailOptionalArchived.map(x -> ((Boolean)((HistoricDetailVariableInstanceUpdateEntity) x).getValue()))
                .ifPresent(meta ->  darHistoryDtoOptional.get().setDataRequestArchived(meta));

        return  darHistoryDtoOptional.orElse(null);
    }

    private Task getTask(String businessKey) throws NotFoundException {
        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).list();
        if (tasks == null || tasks.size() == 0) {
            throw new NotFoundException(String.format("No task with businessKey %s exists!", businessKey));
        }
        return tasks.get(0);
    }

    private List<Task> getUserTasks(String businessKey) throws NotFoundException {
        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).list();
        if (tasks == null || tasks.size() == 0) {
            throw new NotFoundException(String.format("No task with businessKey %s exists!", businessKey));
        }
        return tasks;
    }

    private String getProcessId(String businessKey) throws NotFoundException {
        String historicProcessId = getHistoricProcessTask(businessKey);
        String activeProcessId = getProcessTask(businessKey);

        if (historicProcessId.isEmpty() && activeProcessId.isEmpty()) {
            throw new NotFoundException(String.format("No task could be found with businessKey %s", businessKey));
        }

        return activeProcessId.isEmpty() ? historicProcessId : activeProcessId;
    }

    private String getProcessTask(String businessKey) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).orderByTaskCreateTime().asc().list();

        if(tasks.size() == 0) {
           return "";
        }

        return tasks.get(0).getProcessInstanceId();
    }

    private String getHistoricProcessTask(String businessKey){
        List<HistoricProcessInstance> historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();

        if(historicProcessInstance == null) {
            return "";
        }

        return historicProcessInstance.get(0).getRootProcessInstanceId();
    }

    private MessageCorrelationResult createNextStepDefinition(String businessKey, DarStepReviewDto darStepReviewDto) {
        return runtimeService.createMessageCorrelation(UPDATE_STEP)
                .processInstanceBusinessKey(businessKey)
                .setVariable("userId", darStepReviewDto.getDataRequestUserId())
                .setVariable("publisher", darStepReviewDto.getDataRequestPublisher())
                .setVariable("stepName", darStepReviewDto.getDataRequestStepName())
                .setVariable("notifyReviewerSLA", darStepReviewDto.getNotifyReviewerSLA())
                .setVariable("managerApproved", darStepReviewDto.getManagerApproved())
                .setVariable("phaseApproved", darStepReviewDto.getPhaseApproved())
                .setVariable("finalPhaseApproved", darStepReviewDto.getFinalPhaseApproved())
                .setVariable("reviewerList", darStepReviewDto.getReviewerList())
                .correlateWithResult();
    }
}
