package com.gateway.workflow.services;

import com.gateway.workflow.dtos.DarHistoryAggDto;
import com.gateway.workflow.dtos.DarStepReviewDto;
import javassist.NotFoundException;

public interface DataRequestService {

    DarStepReviewDto completeManagerStepAndCreateStepDefinition(String businessKey, DarStepReviewDto darStepReviewDto) throws NotFoundException;

    DarHistoryAggDto getDarRequestHistory(String businessKey) throws NotFoundException;
}
