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

    private String applicationStatus;
    private String publisher;
    private String stepName;
    private String notifyReviewer;
    private Boolean finalStep;
    private List<String> reviewerList;

}