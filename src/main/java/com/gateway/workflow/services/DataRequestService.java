package com.gateway.workflow.services;

import com.gateway.workflow.dtos.DarHistoryDto;
import javassist.NotFoundException;

public interface DataRequestService {

    DarHistoryDto getDarRequestHistory(String businessKey, DarHistoryDto darHistoryDto) throws NotFoundException;
}
