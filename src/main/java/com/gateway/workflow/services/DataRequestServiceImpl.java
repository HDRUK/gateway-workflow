package com.gateway.workflow.services;

import com.gateway.workflow.dtos.DarHistoryAggDto;
import com.gateway.workflow.dtos.DarHistoryDto;
import javassist.NotFoundException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gateway.workflow.util.ConstantPropertiesUtil.*;

@Service
public class DataRequestServiceImpl implements DataRequestService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Override
    public DarHistoryAggDto getDarRequestHistory(String businessKey) throws NotFoundException {
        String historicProcessId = getHistoricProcessTask(businessKey);
        String activeProcessId = getProcessTask(businessKey);

        if (historicProcessId.isEmpty() && activeProcessId.isEmpty()) {
            throw new NotFoundException(String.format("No task could be found with businessKey %s", businessKey));
        }

        String process = activeProcessId.isEmpty() ? historicProcessId : activeProcessId;
        List<String> hdq = historyService.createHistoricDetailQuery()
                .processInstanceId(process)
                .list()
                .stream().map(x -> ((HistoricDetailVariableInstanceUpdateEntity)x).getActivityInstanceId()).distinct().collect(Collectors.toList());

        List<DarHistoryDto> darHistoryDtoList = new ArrayList<>();

        List<Date> timeStamp = new ArrayList<>();

        for (String actInstId: hdq) {
            DarHistoryDto temp = getProcessHistory(actInstId);
            timeStamp.add(temp.getTimeStamp());
            darHistoryDtoList.add(temp);
        }

        Long timeInStatus = (timeStamp.get(1).getTime() - timeStamp.get(0).getTime());

        return DarHistoryAggDto.builder()
                .darHistoryList(darHistoryDtoList)
                .dataRequestId(businessKey)
                .timeInStatus(timeInStatus)
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
                .timeStamp((Date)((HistoricDetailVariableInstanceUpdateEntity)x).getTimestamp())
                .dataActivityId((String)((HistoricDetailVariableInstanceUpdateEntity)x).getActivityInstanceId())
                .build());

        historicDetailOptionalDateTime.map(x -> ((String)((HistoricDetailVariableInstanceUpdateEntity) x).getValue()))
                .ifPresent(meta ->  darHistoryDtoOptional.get().setDataRequestDateTime(meta));

        historicDetailOptionalPublisher.map(x -> ((String)((HistoricDetailVariableInstanceUpdateEntity) x).getValue()))
                .ifPresent(meta ->  darHistoryDtoOptional.get().setDataRequestPublisher(meta));

        historicDetailOptionalArchived.map(x -> ((Boolean)((HistoricDetailVariableInstanceUpdateEntity) x).getValue()))
                .ifPresent(meta ->  darHistoryDtoOptional.get().setDataRequestArchived(meta));

        return  darHistoryDtoOptional.orElse(DarHistoryDto.builder().build());
    }

    private String getProcessTask(String businessKey) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).orderByTaskCreateTime().asc().list();

        if(tasks.size() == 0) {
           return "";
        }

        return tasks.get(0).getProcessInstanceId();
    }

    private String getHistoricProcessTask(String businessKey){
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();

        if(historicProcessInstance == null) {
            return "";
        }

        return historicProcessInstance.getRootProcessInstanceId();
    }
}
