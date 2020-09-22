package com.gateway.workflow.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

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
    private Date dataTimeStamp;
}
