package com.gateway.workflow.controllers;

import com.gateway.workflow.dtos.DarHistoryAggDto;
import com.gateway.workflow.dtos.DarHistoryDto;
import com.gateway.workflow.services.DataRequestService;
import com.gateway.workflow.util.DarHistoryAggUtil;
import com.gateway.workflow.util.DarHistoryUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.gateway.workflow.util.DarHistoryUtil.*;
import static com.gateway.workflow.util.DarHistoryAggUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@WebMvcTest(DataRequestController.class)
public class DataRequestControllerTest implements DarHistoryUtil, DarHistoryAggUtil {

    private HttpHeaders headers;

    private DarHistoryDto darHistoryDto01;
    private DarHistoryDto darHistoryDto02;
    private DarHistoryAggDto darHistoryAggDto;

    private Date date01 = convertToDate(LocalDateTime.now());
    private Date date02 = convertToDate(LocalDateTime.now().plusSeconds(45));

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataRequestService dataRequestService;

    @Before
    public void setUp() throws Exception {
        headers = new HttpHeaders();

        darHistoryDto01 = buildDarHistoryDto(DATA_REQUEST_STATUS_01, DATA_REQUEST_DATETIME_01, DATA_REQUEST_PUBLISHER_01, DATA_REQUEST_ARCHIVED_01, date01);
        darHistoryDto02 = buildDarHistoryDto(DATA_REQUEST_STATUS_02, DATA_REQUEST_DATETIME_02, DATA_REQUEST_PUBLISHER_02, DATA_REQUEST_ARCHIVED_02, date02);
        List<DarHistoryDto> darHistoryList = new ArrayList<DarHistoryDto>();
        darHistoryList.add(darHistoryDto01);
        darHistoryList.add(darHistoryDto02);

        darHistoryAggDto = buildDarHistoryAggDto(DATA_REQUEST_ID, (long) TIME_IN_STATUS, darHistoryList);

        given(dataRequestService.getDarRequestHistory(any())).willReturn(darHistoryAggDto);
    }

    @Test
    public void shouldRetrieveTheHistoryForTheProcess() throws Exception {
        mockMvc.perform(get("/api/gateway/workflow/v1/history/requests/{businessKey}", DATA_REQUEST_ID))
                .andExpect(jsonPath("$.dataRequestId", is(DATA_REQUEST_ID)))
                .andExpect(jsonPath("$.timeInStatus", is(TIME_IN_STATUS)))
                .andExpect(jsonPath("$.darHistoryList[0].dataRequestStatus", is(DATA_REQUEST_STATUS_01)))
                .andExpect(jsonPath("$.darHistoryList[0].dataRequestDateTime", is(DATA_REQUEST_DATETIME_01)))
                .andExpect(jsonPath("$.darHistoryList[0].dataRequestPublisher", is(DATA_REQUEST_PUBLISHER_01)))
                .andExpect(jsonPath("$.darHistoryList[0].dataRequestArchived", is(DATA_REQUEST_ARCHIVED_01)))
                //.andExpect(jsonPath("$.darHistoryList[0].dataTimeStamp", is(date01)))
                .andExpect(jsonPath("$.darHistoryList[1].dataRequestStatus", is(DATA_REQUEST_STATUS_02)))
                .andExpect(jsonPath("$.darHistoryList[1].dataRequestDateTime", is(DATA_REQUEST_DATETIME_02)))
                .andExpect(jsonPath("$.darHistoryList[1].dataRequestPublisher", is(DATA_REQUEST_PUBLISHER_02)))
                .andExpect(jsonPath("$.darHistoryList[1].dataRequestArchived", is(DATA_REQUEST_ARCHIVED_02)));
                //.andExpect(jsonPath("$.darHistoryList[1].dataTimeStamp", is(date02)));

        verify(dataRequestService, times(1)).getDarRequestHistory(any());
        reset(dataRequestService);
    }

    public Date convertToDate(LocalDateTime dateToConvert) {
        return java.sql.Timestamp.valueOf(dateToConvert);
    }
}
