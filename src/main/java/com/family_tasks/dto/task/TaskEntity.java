package com.family_tasks.dto.task;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    private String taskId;
    private String name;
    private String status;
    private String description;
    private String priority;
    private int reporterId;
    private List<Integer> executorIds;
    private boolean confidential;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void addExecutor(Integer executorId) {
        if (executorIds == null) {
            executorIds = new ArrayList<>();
        }
        executorIds.add(executorId);
    }

    public void addExecutors(List<Integer> executors) {
        if (executorIds == null) {
            executorIds = new ArrayList<>();
        }
        executorIds.addAll(executors);
    }
}