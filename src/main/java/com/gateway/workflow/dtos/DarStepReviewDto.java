package com.gateway.workflow.dtos;


import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@ToString
public class DarStepReviewDto {

    private String dataRequestId;
    private String notifyReviewer;
    private List<ReviewerDto> reviewerDtoList;

}
