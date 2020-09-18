package com.gateway.workflow.dtos;

import lombok.*;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@ToString
public class DarHistoryDto {

    private String dataRequestStatus;
    private String dataRequestDateTime;
    private String dataRequestPublisher;
    private Boolean dataRequestArchived;
    private Date timeStamp;
}
