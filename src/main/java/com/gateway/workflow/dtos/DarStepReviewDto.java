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
    private String applicationStatus;
    private String dateSubmitted;
    private String userId;
    private Boolean archived;
    private String publisher;
    private String notifyReviewer;
    private List<String> reviewerList;

}
