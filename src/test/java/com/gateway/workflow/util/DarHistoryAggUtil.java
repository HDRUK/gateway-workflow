package com.gateway.workflow.util;

import com.gateway.workflow.dtos.DarHistoryAggDto;
import com.gateway.workflow.dtos.DarHistoryDto;

import java.util.ArrayList;
import java.util.List;

public interface DarHistoryAggUtil {

    String DATA_REQUEST_ID = "673b597b-976e-4bb7-aea1-618c1fac8c2d";
    int TIME_IN_STATUS = 44089;
    List<DarHistoryDto> DAR_HISTORY_LIST = new ArrayList<>();

    static DarHistoryAggDto buildDarHistoryAggDto (String dataRequestId, Long timeInStatus, List<DarHistoryDto> darHistoryList) {
        return DarHistoryAggDto.builder()
                .dataRequestId(dataRequestId)
                .timeInStatus(timeInStatus)
                .darHistoryList(darHistoryList)
                .build();
    }
}
