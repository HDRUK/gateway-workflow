package com.gateway.workflow.controllers;

import com.gateway.workflow.dtos.DarHistoryAggDto;
import com.gateway.workflow.services.DataRequestService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import static org.springframework.http.HttpStatus.*;

@RestController
public class DataRequestController extends BaseController {

    @Autowired
    DataRequestService dataRequestService;

    @GetMapping(value = "/hdr/request")
    @ResponseStatus(OK)
    public String getDataRequestTest() {
        return "You've reached the data request controller ";
    }

    @GetMapping(value = "/history/requests/{businessKey}")
    @ResponseStatus(OK)
    public DarHistoryAggDto darRequestHistory(@PathVariable("businessKey") String businessKey) throws NotFoundException {
        return dataRequestService.getDarRequestHistory(businessKey);
    }
}
