package com.family_tasks.utils.task;

import io.restassured.response.Response;

import java.util.HashSet;
import java.util.Set;

public class TaskResponseWrapper {

    private final Response response;

    public TaskResponseWrapper(Response response) {
        this.response = response;
    }

    public String getName() {
        return response.path("name");
    }

    public String getStatus() {
        return response.path("status");
    }

    public String getPriority() {
        return response.path("priority");
    }

    public String getDeadline() {
        return response.path("deadline");
    }

    public Integer getReporterId() {
        return response.path("reporterId");
    }

    public String getTaskId() {
        return response.path("taskId");
    }

    public String getDescription() {
        return response.path("description");
    }

    public Set<Integer> getExecutorIds() {
        return new HashSet<>(response.path("executorIds"));
    }

    public String getUpdatedAt() {
        return response.path("updatedAt");
    }

    public String getCreatedAt() {
        return response.path("createdAt");
    }
}
