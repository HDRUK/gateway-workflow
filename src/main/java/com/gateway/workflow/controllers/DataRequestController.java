package com.gateway.workflow.controllers;

import com.gateway.workflow.dtos.DarHistoryAggDto;
import com.gateway.workflow.dtos.DarStepReviewDto;
import com.gateway.workflow.dtos.ManagerApprovedDto;
import com.gateway.workflow.services.DataRequestService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.*;

@RestController
public class DataRequestController extends BaseController {

    @Autowired
    DataRequestService dataRequestService;

    @GetMapping(value = "/history/requests/{businessKey}")
    @ResponseStatus(OK)
    public DarHistoryAggDto darRequestHistory(@PathVariable("businessKey") String businessKey) throws NotFoundException {
        return dataRequestService.getDarRequestHistory(businessKey);
    }

    @PostMapping(value = "/complete/review/{businessKey}")
    @ResponseStatus(OK)
    public DarStepReviewDto darStartStepReview(@PathVariable("businessKey") String businessKey, @Valid @RequestBody DarStepReviewDto darStepReviewDto) throws NotFoundException {
        return dataRequestService.completeManagerStepAndCreateStepDefinition(businessKey, darStepReviewDto);
    }

    @PostMapping(value = "/reviewer/complete/{businessKey}")
    @ResponseStatus(OK)
    public DarStepReviewDto darReviewerStep(@PathVariable("businessKey") String businessKey, @Valid @RequestBody DarStepReviewDto darStepReviewDto) throws NotFoundException {
        return dataRequestService.completeReviewerStep(businessKey, darStepReviewDto);
    }

    @PostMapping(value = "/manager/completed/{businessKey}")
    @ResponseStatus(OK)
    public ManagerApprovedDto darManagerApproval(@PathVariable("businessKey") String businessKey, @Valid @RequestBody ManagerApprovedDto managerApprovedDto) throws NotFoundException {
        return dataRequestService.managerCompletedReview(businessKey, managerApprovedDto);
    }
}
