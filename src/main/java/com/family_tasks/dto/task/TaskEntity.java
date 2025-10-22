package com.family_tasks.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskApiResponse {

    private String taskId;
    private String name;
    private String status;
    private String description;
    private String priority;
    private int reporterId;
    private List<Integer> executorIds;
    private boolean confidential;
    private String deadline;
    private String createdAt;
    private String updatedAt;
}
