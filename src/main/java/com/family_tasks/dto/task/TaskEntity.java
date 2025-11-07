package com.family_tasks.dto.task;

import com.family_tasks.enums.TaskStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder(toBuilder = true)
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    private static final Set<String> ACTIVE_STATUSES = Set.of(TaskStatus.TO_DO.name(), TaskStatus.IN_PROGRESS.name());
    private static final Set<String> COMPLETED_STATUSES = Set.of(TaskStatus.COMPLETED.name());

    private String taskId;
    private String name;
    private String status;
    private String description;
    private String priority;
    private Integer reporterId;
    private boolean confidential;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isActiveTask() {
        return ACTIVE_STATUSES.contains(this.status);
    }

    public boolean isCompletedTask() {
        return COMPLETED_STATUSES.contains(this.status);
    }
}