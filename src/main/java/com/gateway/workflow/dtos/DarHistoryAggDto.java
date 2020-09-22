package com.gateway.workflow.dtos;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@ToString
public class DarHistoryAggDto {

    private String dataRequestId;
    private Long timeInStatus;
    private List<DarHistoryDto> darHistoryList;

}
