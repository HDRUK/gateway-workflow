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

    /**
     * <p>Move from manager review to step review. Create a series of new tasks based reviewerList</p>
     * @param businessKey The DarId passed in from the execute method
     * @param darStepReviewDto The DTO which contains the values for the next phase the flow.
     * @return The passed in DTO
     * @throws NotFoundException A NotFoundException is thrown when the user task cannot be found.
     * */
    @Override
    public DarStepReviewDto completeManagerStepAndCreateStepDefinition(String businessKey, DarStepReviewDto darStepReviewDto) throws NotFoundException {
        // Get the user task by the business key
        Task task = getTask(businessKey);

        // Create a HashMap to populate the task values.
        Map<String, Object> processVars = new HashMap<>();
        processVars.put("applicationStatus", darStepReviewDto.getDataRequestStatus());
        processVars.put("publisher", darStepReviewDto.getDataRequestPublisher());
        processVars.put("stepName", darStepReviewDto.getDataRequestStepName());
        processVars.put("notifyReviewer", darStepReviewDto.getNotifyReviewerSLA());
        processVars.put("managerApproved", darStepReviewDto.getManagerApproved());
        processVars.put("phaseApproved", darStepReviewDto.getPhaseApproved());
        processVars.put("finalPhaseApproved", darStepReviewDto.getFinalPhaseApproved());
        processVars.put("reviewerList", darStepReviewDto.getReviewerList());

        //Complete the current task and start the next one.
        taskService.complete(task.getId(), processVars);

        return darStepReviewDto;
    }

    /**
     * <p>Allows the manager to override the phase</p>
     * @param businessKey The DarId passed in from the execute method
     * @param managerApprovedDto The DTO which contains the values for the manager override.
     * @return The passed in DTO
     * @throws NotFoundException A NotFoundException is thrown when the user task cannot be found.
     * */
    @Override
    public ManagerApprovedDto managerCompletedReview(String businessKey, ManagerApprovedDto managerApprovedDto) throws NotFoundException {
        // Get the user task by the business key
        Task task = getTask(businessKey);

        // Create a HashMap to populate the task values.
        Map<String, Object> processVars = new HashMap<>();
        processVars.put("applicationStatus", managerApprovedDto.getDataRequestStatus());
        processVars.put("managerId", managerApprovedDto.getDataRequestManagerId());
        processVars.put("publisher", managerApprovedDto.getDataRequestPublisher());
        processVars.put("managerApproved", managerApprovedDto.getManagerApproved());
        processVars.put("phaseApproved", managerApprovedDto.getPhaseApproved());
        processVars.put("finalPhaseApproved", managerApprovedDto.getFinalPhaseApproved());

        // Delegate the task to the manager
        taskService.delegateTask(task.getId(), managerApprovedDto.getDataRequestManagerId());

        // Then complete the task
        taskService.complete(task.getId(), processVars);

        return managerApprovedDto;
    }

    /**
     * <p>Completes a reviewer step and based based in values will either start the next step or move to final review</p>
     * @param businessKey The DarId passed in from the execute method
     * @param darStepReviewDto The DTO which contains the values a review phase.
     * @return The passed in DTO
     * @throws NotFoundException A NotFoundException is thrown when the user task cannot be found.
     * */
    @Override
    public DarStepReviewDto completeReviewerStep(String businessKey, DarStepReviewDto darStepReviewDto) throws NotFoundException {
        // Find the user task matching the business key and the assignee
        Task userTask = getUserTasks(businessKey).stream()
                .filter(x -> darStepReviewDto.getDataRequestUserId().equals(x.getAssignee()))
                .findFirst()
                .orElse(null);

        // If no task could be found then throw an error
        if(userTask == null && !darStepReviewDto.getPhaseApproved() && !darStepReviewDto.getManagerApproved()) {
            throw new NotFoundException(String.format("No assignee was found matching %s", darStepReviewDto.getDataRequestUserId()));
        }

        // Manager Override - Delegate the task to the manager if the following conditions are met.
        if(darStepReviewDto.getManagerApproved() && (darStepReviewDto.getPhaseApproved() || darStepReviewDto.getFinalPhaseApproved())) {
            Task delegateUserTask = getTask(businessKey);
            taskService.delegateTask(delegateUserTask.getId(), darStepReviewDto.getDataRequestUserId());
            userTask = delegateUserTask;
        }

        // Check for phase approval and/or final phase approved
        if(!darStepReviewDto.getPhaseApproved() || (darStepReviewDto.getPhaseApproved() && darStepReviewDto.getFinalPhaseApproved())) {
            Map<String, Object> processVars = new HashMap<>();
            processVars.put("userId", darStepReviewDto.getDataRequestUserId());
            processVars.put("managerApproved", darStepReviewDto.getManagerApproved());
            processVars.put("phaseApproved", darStepReviewDto.getPhaseApproved());
            processVars.put("finalPhaseApproved", darStepReviewDto.getFinalPhaseApproved());

            // Complete the task
            taskService.complete(userTask.getId(), processVars);
        }

        // If the following conditions are met then throw an error
        if(darStepReviewDto.getPhaseApproved() && darStepReviewDto.getReviewerList() == null && !darStepReviewDto.getFinalPhaseApproved())
            throw new IllegalArgumentException(
                    String.format("%s has no records. Please sure reviewers are added before continuing",
                            darStepReviewDto.getReviewerList()));

        // If phase approved and is not final phase approved, then create the next step definition
        if(darStepReviewDto.getPhaseApproved() && !darStepReviewDto.getFinalPhaseApproved()) {
            createNextStepDefinition(businessKey, darStepReviewDto);
        }

        return darStepReviewDto;
    }

    /**
     * <p>Aggregate the time in steps</p>
     * @param businessKey The DarId passed in from the execute method
     * @return The DarHistory Dto
     * @throws NotFoundException A NotFoundException is thrown when the user task cannot be found.
     * */
    @Override
    public DarHistoryAggDto getDarRequestHistory(String businessKey) throws NotFoundException {
        // Get the processId via the businessKey
        String process = getProcessId(businessKey);

        // Get a list of all activities based on the process Id
        List<String> hdq = historyService.createHistoricDetailQuery()
                .processInstanceId(process)
                .list()
                .stream().map(x -> ((HistoricDetailVariableInstanceUpdateEntity)x).getActivityInstanceId()).distinct().collect(Collectors.toList());

        List<DarHistoryDto> darHistoryDtoList = new ArrayList<>();
        List<Date> timeStamp = new ArrayList<>();

        // Iterate over the history and and calculate the time difference
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

    /**
     * <p>Filter the history results by the activity Id</p>
     * @param actInstId The activity ID
     * @return Return the history via the DarHistoryDto
     * */
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

    /**
     * <p>Get the task via the businessKey</p>
     * @param businessKey The DAR ID
     * @return Return the camunda task
     * */
    private Task getTask(String businessKey) throws NotFoundException {
        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).list();
        if (tasks == null || tasks.size() == 0) {
            throw new NotFoundException(String.format("No task with businessKey %s exists!", businessKey));
        }
        // Get the first task from the list.
        return tasks.get(0);
    }

    /**
     * <p>Get a list of user tasks</p>
     * @param businessKey The DAR Id
     * @return Return a list of user tasks
     * */
    private List<Task> getUserTasks(String businessKey) throws NotFoundException {
        // Get a list of tasks using the businessKey
        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).list();

        // Check if the list empty and return an error if it is.
        if (tasks == null || tasks.size() == 0) {
            throw new NotFoundException(String.format("No task with businessKey %s exists!", businessKey));
        }

        // Return a list of tasks
        return tasks;
    }

    /**
     * <p>Get process id using the business key</p>
     * @param businessKey The DAR Id
     * @return Returns the process Id
     * */
    private String getProcessId(String businessKey) throws NotFoundException {
        // Tries to get the historic processId or the active processId
        String historicProcessId = getHistoricProcessTask(businessKey);
        String activeProcessId = getProcessTask(businessKey);

        // If both strings are empty then throw an error
        if (historicProcessId.isEmpty() && activeProcessId.isEmpty()) {
            throw new NotFoundException(String.format("No task could be found with businessKey %s", businessKey));
        }

        // Determine which Id to return
        return activeProcessId.equals("") ? historicProcessId : activeProcessId;
    }

    /**
     * <p>Get the latest processInstanceId via the businessKey</p>
     * @param businessKey The DAR Id
     * @return The latest processInstanceId
     * */
    private String getProcessTask(String businessKey) {
        // Get a list of active processes via the business key and sort them by time created.
        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).orderByTaskCreateTime().asc().list();

        // If the list is empty return an empty string. An error should not be thrown here as this is handled by 'getProcessId'
        if(tasks.size() == 0) {
           return "";
        }

        // Return the latest processInstanceId
        return tasks.get(0).getProcessInstanceId();
    }

    /**
     * <p>Get the historic processInstanceId</p>
     * @param businessKey The DAR Id
     * @return The historic processId
     * */
    private String getHistoricProcessTask(String businessKey) {
        // Get a list of historic processInstanceIds via the businessKey
        List<HistoricProcessInstance> historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();

        // If the list is empty return an empty string. An error should not be thrown here as this is handled by 'getProcessId'
        if(historicProcessInstance == null) {
            return "";
        }
        // Return the first processInstanceId
        return historicProcessInstance.get(0).getRootProcessInstanceId();
    }

    /**
     * <p>Create a message correlation, this is used to create the next step definition during the Step-Review phase</p>
     * @param businessKey The DAR Id
     * @param darStepReviewDto The object which contains the values to start the next review phase.
     * @return A new messageCorrelationResult that will be executed from - completeReviewerStep
     * */
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