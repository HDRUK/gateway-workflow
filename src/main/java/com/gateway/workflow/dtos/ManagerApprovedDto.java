package com.gateway.workflow.dtos;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@ToString
public class ManagerApprovedDto {

    private String dataRequestStatus;
    private String dataRequestManagerId;
    private String dataRequestPublisher;
    @Builder.Default
    private Boolean managerApproved = false;
    @Builder.Default
    private Boolean phaseApproved = false;
    @Builder.Default
    private Boolean finalPhaseApproved = false;
}
