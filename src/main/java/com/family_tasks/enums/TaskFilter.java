package com.family_tasks.enums;


import lombok.Getter;

@Getter
public enum TaskFilter {
    IS_REPORTER_ACTIVE_TASK,
    IS_EXECUTOR_ACTIVE_TASK,
    IS_REPORTER_COMPLETED_TASK,
    IS_EXECUTOR_COMPLETED_TASK,
    ALL_AVAILABLE,
    ALL_CLOSED;
}