package com.gateway.workflow.services;

import com.gateway.workflow.dtos.DarHistoryAggDto;
import com.gateway.workflow.dtos.DarStepReviewDto;
import com.gateway.workflow.dtos.ManagerApprovedDto;
import javassist.NotFoundException;

public interface DataRequestService {

    DarStepReviewDto completeManagerStepAndCreateStepDefinition(String businessKey, DarStepReviewDto darStepReviewDto) throws NotFoundException;

    ManagerApprovedDto managerCompletedReview(String businessKey, ManagerApprovedDto managerApprovedDto) throws NotFoundException;

    DarStepReviewDto completeReviewerStep(String businessKey, DarStepReviewDto darStepReviewDto) throws NotFoundException;

    DarHistoryAggDto getDarRequestHistory(String businessKey) throws NotFoundException;
}
