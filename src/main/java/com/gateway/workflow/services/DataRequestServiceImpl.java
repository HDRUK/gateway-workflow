package com.gateway.workflow.services;

import com.gateway.workflow.dtos.DarHistoryDto;
import javassist.NotFoundException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricDetailQuery;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DataRequestServiceImpl implements DataRequestService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Override
    public DarHistoryDto getDarRequestHistory(String businessKey, DarHistoryDto darHistoryDto) throws NotFoundException {
        String processId = getProcessTask(businessKey).getProcessInstanceId();
        HistoricDetailQuery hdq = historyService.createHistoricDetailQuery().processInstanceId(processId).variableUpdates();

        return new DarHistoryDto();
    }

    private DarHistoryDto getProcessHistory(String processId) {
        Optional<HistoricDetail> historicDetail = historyService.createHistoricDetailQuery()
                .processInstanceId(processId).orderByTime().asc().variableUpdates().list().stream()
                .filter(x -> ((HistoricDetailVariableInstanceUpdateEntity)x).getName().equals(""))
                .reduce((first, second) -> second);

        return new DarHistoryDto();
    }

    private Task getProcessTask(String businessKey) throws NotFoundException {
        List<Task> tasks = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).orderByTaskCreateTime().asc().list();
        if (tasks.isEmpty() || tasks.size() == 0) {
            throw new NotFoundException(String.format("No task could be found with businessKey %s", businessKey));
        }
        return tasks.get(0);
    }
}
