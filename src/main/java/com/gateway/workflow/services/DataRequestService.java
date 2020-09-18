package com.gateway.workflow.services;

import com.gateway.workflow.dtos.DarHistoryAggDto;
import javassist.NotFoundException;

public interface DataRequestService {

    DarHistoryAggDto getDarRequestHistory(String businessKey) throws NotFoundException;
}
