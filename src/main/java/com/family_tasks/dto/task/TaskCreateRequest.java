package com.family_tasks.dto.task;

import lombok.*;

import java.util.Set;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {
    private String name;
    private String description;
    private String priority;
    private Integer reporterId;
    private Set<Integer> executorIds;
    private Boolean confidential;
    private String deadline;
}

