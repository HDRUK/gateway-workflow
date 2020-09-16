package com.gateway.workflow.services;

import com.gateway.workflow.dtos.DarHistoryDto;
import javassist.NotFoundException;
import org.camunda.bpm.engine.history.HistoricDetailQuery;

public interface DataRequestService {

    HistoricDetailQuery getDarRequestHistory(String businessKey) throws NotFoundException;
}
