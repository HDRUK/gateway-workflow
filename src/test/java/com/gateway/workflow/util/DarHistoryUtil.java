package com.gateway.workflow.util;

import com.gateway.workflow.dtos.DarHistoryDto;

import java.util.Date;

public interface DarHistoryUtil {

    String DATA_REQUEST_STATUS_01 = "InProgress";
    String DATA_REQUEST_DATETIME_01 = "16/09/2020 00:00:00";
    String DATA_REQUEST_PUBLISHER_01 = "409e2967-0bae-4586-9398-8c6f9d976286";
    Boolean DATA_REQUEST_ARCHIVED_01 = null;
    Date DATA_TIMESTAMP_01 = new Date();

    String DATA_REQUEST_STATUS_02 = "Approve";
    String DATA_REQUEST_DATETIME_02 = "18/09/2020 00:00:00";
    String DATA_REQUEST_PUBLISHER_02 = "409e2967-0bae-4586-9398-8c6f9d976286";
    Boolean DATA_REQUEST_ARCHIVED_02 = false;
    Date DATA_TIMESTAMP_02 = new Date();

    static DarHistoryDto buildDarHistoryDto (String dataRequestStatus , String dataRequestDateTime, String dataRequestPublisher, Boolean dataRequestArchived, Date dataTimeStamp) {
        return DarHistoryDto.builder()
                .dataRequestStatus(dataRequestStatus)
                .dataRequestDateTime(dataRequestDateTime)
                .dataRequestPublisher(dataRequestPublisher)
                .dataRequestArchived(dataRequestArchived)
                .dataTimeStamp(dataTimeStamp)
                .build();
    }
}
