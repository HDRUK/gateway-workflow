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

    private String dataRequestStatus;
    private String dataRequestUserId;
    private String dataRequestPublisher;
    private String dataRequestStepName;
    private String notifyReviewerSLA;
    @Builder.Default
    private Boolean managerApproved = false;
    @Builder.Default
    private Boolean phaseApproved = false;
    @Builder.Default
    private Boolean finalPhaseApproved = false;
    private List<String> reviewerList;
}