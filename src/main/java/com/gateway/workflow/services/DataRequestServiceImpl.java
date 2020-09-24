package com.gateway.workflow.services;

import com.gateway.workflow.dtos.DarHistoryAggDto;
import com.gateway.workflow.dtos.DarHistoryDto;
import com.gateway.workflow.dtos.DarStepReviewDto;
import javassist.NotFoundException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
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
        String process = getProcessId(businessKey);
        Task task = getTask(businessKey);

        List<String> reviewers = new ArrayList<>();
        reviewers.add("Test1");
        reviewers.add("Test2");

        Map<String, Object> processVars = new HashMap<>();
        processVars.put("reviewerList", reviewers);
        processVars.put("notifyReviewer", darStepReviewDto.getNotifyReviewer());

        processEngine.getTaskService().complete(task.getId(), processVars);

        return DarStepReviewDto.builder().build();
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
            timeStamp.add(temp.getDataTimeStamp());
            darHistoryDtoList.add(temp);
        }

        return DarHistoryAggDto.builder()
                .darHistoryList(darHistoryDtoList)
                .dataRequestId(businessKey)
                .timeInStatus(timeStamp.get(1).getTime() - timeStamp.get(0).getTime())
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

        return  darHistoryDtoOptional.orElse(DarHistoryDto.builder().build());
    }

    private Task getTask(String ticketId) throws NotFoundException {
        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(ticketId).list();
        if (tasks == null || tasks.size() == 0) {
            throw new NotFoundException(String.format("No task with ticketId %s exists!", ticketId));
        }
        return tasks.get(0);
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
}
