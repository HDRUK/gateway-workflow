package com.gateway.workflow.services;

import com.gateway.workflow.dtos.DarHistoryAggDto;
import com.gateway.workflow.dtos.DarStepReviewDto;
import com.gateway.workflow.dtos.ManagerApprovedDto;
import javassist.NotFoundException;

public interface DataRequestService {

    DarStepReviewDto completeManagerStepAndCreateStepDefinition(String businessKey, DarStepReviewDto darStepReviewDto) throws NotFoundException;

    ManagerApprovedDto managerCompleted(String businessKey, ManagerApprovedDto managerApprovedDto) throws NotFoundException;

    Boolean completeUserTask(String businessKey, String userId) throws NotFoundException;

    DarHistoryAggDto getDarRequestHistory(String businessKey) throws NotFoundException;
}
