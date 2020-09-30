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
    private String dataRequestId;
    private Boolean managerApproved;
}
