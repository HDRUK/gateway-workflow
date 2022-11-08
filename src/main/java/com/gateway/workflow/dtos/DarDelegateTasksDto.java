package com.gateway.workflow.dtos;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@ToString
public class DarDelegateTasksDto {

    private List<String> delegateTasks;
    private String previousAssignee;
    private String newAssignee;
    private Integer taskCount;
}
