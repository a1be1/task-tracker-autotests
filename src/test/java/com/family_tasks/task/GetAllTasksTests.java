package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskEntity;
import com.family_tasks.dto.group.GroupEntity;
import com.family_tasks.enums.TaskFilter;
import com.family_tasks.enums.TaskPriority;
import com.family_tasks.enums.TaskStatus;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import static com.family_tasks.utils.TestDataBaseUtils.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.family_tasks.UrlConstant.TASKS_URI;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.utils.TestDataBaseUtils.*;   // ← REQUIRED
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GetAllTasksTests extends AbstractTaskTrackerTest {

    @Test
    public void getAllAvailableTasks_returnsAllTasks() {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        List<TaskEntity> tasks = createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        Response response = given()
                .queryParam("userId", reporterId)
                .queryParam("filter", TaskFilter.ALL_AVAILABLE.name())
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(200)
                .extract().response();

        for (TaskEntity task : tasks) {
            response.then()
                    .body("find { it.taskId == '%s' }.name", withArgs(task.getTaskId()), equalTo(task.getName()))
                    .body("find { it.taskId == '%s' }.status", withArgs(task.getTaskId()), equalTo(task.getStatus()))
                    .body("find { it.taskId == '%s' }.priority", withArgs(task.getTaskId()), equalTo(task.getPriority()))
                    .body("find { it.taskId == '%s' }.reporterId", withArgs(task.getTaskId()), equalTo(task.getReporterId()))
                    .body("find { it.taskId == '%s' }.description", withArgs(task.getTaskId()), equalTo(task.getDescription()))
                    .body("find { it.taskId == '%s' }.confidential", withArgs(task.getTaskId()), equalTo(task.isConfidential()))
                    .body("find { it.taskId == '%s' }.createdAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.updatedAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.deadline", withArgs(task.getTaskId()), equalTo(task.getDeadline().toString()));
        }

        List<String> statuses = response.jsonPath().getList("status");
        System.out.println("Statuses (ALL_AVAILABLE): " + statuses);
        assertThat(statuses, contains("TO_DO", "IN_PROGRESS", "COMPLETED", "CANCELLED"));
        assertThat(statuses.size(), equalTo(tasks.size()));
        response.prettyPrint();
    }

    @Test
    public void getAllClosedTasks() {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        List<TaskEntity> tasks = createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        Response response = given()
                .queryParam("userId", reporterId)
                .queryParam("filter", TaskFilter.ALL_CLOSED.name())
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(200)
                .extract().response();

        for (TaskEntity task : tasks) {
            if (!task.getStatus().equals(TaskStatus.CANCELLED.name())) {
                continue;
            }

            response.then()
                    .body("find { it.taskId == '%s' }.name", withArgs(task.getTaskId()), equalTo(task.getName()))
                    .body("find { it.taskId == '%s' }.status", withArgs(task.getTaskId()), equalTo(task.getStatus()))
                    .body("find { it.taskId == '%s' }.priority", withArgs(task.getTaskId()), equalTo(task.getPriority()))
                    .body("find { it.taskId == '%s' }.reporterId", withArgs(task.getTaskId()), equalTo(task.getReporterId()))
                    .body("find { it.taskId == '%s' }.description", withArgs(task.getTaskId()), equalTo(task.getDescription()))
                    .body("find { it.taskId == '%s' }.confidential", withArgs(task.getTaskId()), equalTo(task.isConfidential()))
                    .body("find { it.taskId == '%s' }.createdAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.updatedAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.deadline", withArgs(task.getTaskId()), equalTo(task.getDeadline().toString()));
        }

        List<String> statuses = response.jsonPath().getList("status");
        System.out.println("Statuses (ALL_CLOSED): " + statuses);
        assertThat(statuses, everyItem(equalTo("CANCELLED")));
        response.prettyPrint();
    }

    @Test
    public void getTasks_whenIsExecutorActiveTask() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();

        int executorId = insertUserIntoDB(buildUserEntity(groupId));

        List<TaskEntity> tasks = createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        for (TaskEntity task : tasks) {
            insertTaskExecutors(task.getTaskId(), List.of(executorId));
        }

        Response response = given()
                .queryParam("userId", executorId)
                .queryParam("filter", TaskFilter.IS_EXECUTOR_ACTIVE_TASK.name())
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(200)
                .extract().response();

        for (TaskEntity task : tasks) {
            if (!task.isActiveTask()) {
                continue;
            }

            response.then()
                    .body("find { it.taskId == '%s' }.name", withArgs(task.getTaskId()), equalTo(task.getName()))
                    .body("find { it.taskId == '%s' }.status", withArgs(task.getTaskId()), equalTo(task.getStatus()))
                    .body("find { it.taskId == '%s' }.priority", withArgs(task.getTaskId()), equalTo(task.getPriority()))
                    .body("find { it.taskId == '%s' }.reporterId", withArgs(task.getTaskId()), equalTo(task.getReporterId()))
                    .body("find { it.taskId == '%s' }.description", withArgs(task.getTaskId()), equalTo(task.getDescription()))
                    .body("find { it.taskId == '%s' }.confidential", withArgs(task.getTaskId()), equalTo(task.isConfidential()))
                    .body("find { it.taskId == '%s' }.createdAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.updatedAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.deadline", withArgs(task.getTaskId()), equalTo(task.getDeadline().toString()));
        }

        List<String> statuses = response.jsonPath().getList("status");
        assertThat(statuses, contains("TO_DO", "IN_PROGRESS"));
        System.out.println("Statuses (IS_EXECUTOR_ACTIVE_TASK): " + statuses);
        response.prettyPrint();
    }

    @Test
    public void getTasks_whenIsReporterActiveTask() {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        List<TaskEntity> tasks = createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        Response response = given()
                .queryParam("userId", reporterId)
                .queryParam("filter", TaskFilter.IS_REPORTER_ACTIVE_TASK.name())
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(200)
                .extract().response();

        for (TaskEntity task : tasks) {
            if (!task.isActiveTask()) {
                continue;
            }

            response.then()
                    .body("find { it.taskId == '%s' }.name", withArgs(task.getTaskId()), equalTo(task.getName()))
                    .body("find { it.taskId == '%s' }.status", withArgs(task.getTaskId()), equalTo(task.getStatus()))
                    .body("find { it.taskId == '%s' }.priority", withArgs(task.getTaskId()), equalTo(task.getPriority()))
                    .body("find { it.taskId == '%s' }.reporterId", withArgs(task.getTaskId()), equalTo(task.getReporterId()))
                    .body("find { it.taskId == '%s' }.description", withArgs(task.getTaskId()), equalTo(task.getDescription()))
                    .body("find { it.taskId == '%s' }.confidential", withArgs(task.getTaskId()), equalTo(task.isConfidential()))
                    .body("find { it.taskId == '%s' }.createdAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.updatedAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.deadline", withArgs(task.getTaskId()), equalTo(task.getDeadline().toString()));
        }

        List<String> statuses = response.jsonPath().getList("status");
        assertThat(statuses, contains("TO_DO", "IN_PROGRESS"));
        System.out.println("Statuses (IS_REPORTER_ACTIVE_TASK): " + statuses);
        response.prettyPrint();
    }

    @Test
    public void getTasks_whenIsExecutorCompletedTask() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();

        int executorId = insertUserIntoDB(buildUserEntity(groupId));

        List<TaskEntity> tasks = createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        for (TaskEntity task : tasks) {
            insertTaskExecutors(task.getTaskId(), List.of(executorId));
        }

        Response response = given()
                .queryParam("userId", executorId)
                .queryParam("filter", TaskFilter.IS_EXECUTOR_COMPLETED_TASK.name())
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(200)
                .extract().response();

        for (TaskEntity task : tasks) {
            if (!task.isCompletedTask()) {
                continue;
            }

            response.then()
                    .body("find { it.taskId == '%s' }.name", withArgs(task.getTaskId()), equalTo(task.getName()))
                    .body("find { it.taskId == '%s' }.status", withArgs(task.getTaskId()), equalTo(task.getStatus()))
                    .body("find { it.taskId == '%s' }.priority", withArgs(task.getTaskId()), equalTo(task.getPriority()))
                    .body("find { it.taskId == '%s' }.reporterId", withArgs(task.getTaskId()), equalTo(task.getReporterId()))
                    .body("find { it.taskId == '%s' }.description", withArgs(task.getTaskId()), equalTo(task.getDescription()))
                    .body("find { it.taskId == '%s' }.confidential", withArgs(task.getTaskId()), equalTo(task.isConfidential()))
                    .body("find { it.taskId == '%s' }.createdAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.updatedAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.deadline", withArgs(task.getTaskId()), equalTo(task.getDeadline().toString()));
        }

        List<String> statuses = response.jsonPath().getList("status");
        assertThat(statuses, contains("COMPLETED"));
        System.out.println("Statuses (IS_EXECUTOR_COMPLETED_TASK): " + statuses);
        response.prettyPrint();
    }

    @Test
    public void getTasks_whenIsReporterCompletedTask() {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        List<TaskEntity> tasks = createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        Response response = given()
                .queryParam("userId", reporterId)
                .queryParam("filter", TaskFilter.IS_REPORTER_COMPLETED_TASK.name())
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(200)
                .extract().response();

        for (TaskEntity task : tasks) {
            if (!task.isCompletedTask()) {
                continue;
            }

            response.then()
                    .body("find { it.taskId == '%s' }.name", withArgs(task.getTaskId()), equalTo(task.getName()))
                    .body("find { it.taskId == '%s' }.status", withArgs(task.getTaskId()), equalTo(task.getStatus()))
                    .body("find { it.taskId == '%s' }.priority", withArgs(task.getTaskId()), equalTo(task.getPriority()))
                    .body("find { it.taskId == '%s' }.reporterId", withArgs(task.getTaskId()), equalTo(task.getReporterId()))
                    .body("find { it.taskId == '%s' }.description", withArgs(task.getTaskId()), equalTo(task.getDescription()))
                    .body("find { it.taskId == '%s' }.confidential", withArgs(task.getTaskId()), equalTo(task.isConfidential()))
                    .body("find { it.taskId == '%s' }.createdAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.updatedAt", withArgs(task.getTaskId()), notNullValue())
                    .body("find { it.taskId == '%s' }.deadline", withArgs(task.getTaskId()), equalTo(task.getDeadline().toString()));
        }

        List<String> statuses = response.jsonPath().getList("status");
        assertThat(statuses, contains("COMPLETED"));
        System.out.println("Statuses (IS_REPORTER_COMPLETED_TASK): " + statuses);
        response.prettyPrint();
    }

    @Test
    public void getAllAvailableTasks_whenUserHasNoTasks_thenReturnEmptyList() {

        GroupEntity group = createUserWithGroup();
        int userId = group.getOwnerId();

        Response response = given()
                .queryParam("userId", userId)
                .queryParam("filter", TaskFilter.ALL_AVAILABLE.name())
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(200)
                .extract().response();

        List<String> taskIds = response.jsonPath().getList("taskId");
        assertTrue(taskIds.isEmpty(), "Expected no tasks for new user");
        System.out.println("Get ALL_AVAILABLE tasks when user has no tasks: ");
        response.prettyPrint();
    }

    @Test
    public void getAllAvailableTasks_withMissingUserId_thenBadRequest() {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        Response response = given()
                .queryParam("filter", TaskFilter.ALL_AVAILABLE.name())
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(USER_NOT_SPECIFIED)))
                .extract().response();

        System.out.println("Get ALL_AVAILABLE tasks with missing userId: ");
        response.prettyPrint();
    }

    @Test
    public void getAllAvailableTasks_withInvalidUserId_thenBadRequest() {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        int invalidUserId = reporterId + 2;

        createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        Response response = given()
                .queryParam("userId", invalidUserId)
                .queryParam("filter", TaskFilter.ALL_AVAILABLE.name())
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(USER_NOT_EXIST, invalidUserId)))
                .extract().response();

        System.out.println("Get ALL_AVAILABLE tasks with invalid userId: ");
        response.prettyPrint();
    }

    @Test
    public void getAllAvailableTasks_withInvalidFilter_thenBadRequest() {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        Response response = given()
                .queryParam("userId", reporterId)
                .queryParam("filter", "ALL")
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(TASK_FILTER_INVALID)))
                .extract().response();

        System.out.println("Get ALL_AVAILABLE tasks with invalid filter: ");
        response.prettyPrint();
    }

    @Test
    public void getAllAvailableTasks_withMissingFilter_thenBadRequest() {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        Response response = given()
                .queryParam("userId", reporterId)
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(TASK_FILTER_NOT_SPECIFIED)))
                .extract().response();

        System.out.println("Get ALL_AVAILABLE tasks with missing filter: ");
        response.prettyPrint();
    }

    @Test
    public void getAllAvailableTasks_withoutGroup_thenReturnEmptyList() {

        int reporterId = insertUserIntoDB(buildUserEntity(null));

        createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        Response response = given()
                .queryParam("userId", reporterId)
                .queryParam("filter", TaskFilter.ALL_AVAILABLE.name())
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(200)
                .extract().response();

        List<?> tasks = response.jsonPath().getList("$");
        assertTrue(tasks.isEmpty(), "Expected empty list when user has no group");
        System.out.println("Get ALL_AVAILABLE tasks without group: ");
        response.prettyPrint();
    }

    @Test
    public void getAllClosedTasks_withoutGroup_thenReturnEmptyList() {

        int reporterId = insertUserIntoDB(buildUserEntity(null));

        createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        Response response = given()
                .queryParam("userId", reporterId)
                .queryParam("filter", TaskFilter.ALL_CLOSED.name())
                .when()
                .get(TASKS_URI)
                .then()
                .statusCode(200)
                .extract().response();

        List<?> tasks = response.jsonPath().getList("$");
        assertTrue(tasks.isEmpty(), "Expected empty list when user has no group");
        System.out.println("Get ALL_CLOSED tasks without group: ");
        response.prettyPrint();
    }

    private List<TaskEntity> createTasksForStatusesAndInsertIntoDB(int userId, TaskStatus... statuses) {
        List<TaskEntity> tasks = new ArrayList<>();

        for (TaskStatus status : statuses) {
            TaskEntity task = TaskEntity.builder()
                    .taskId(UUID.randomUUID().toString())
                    .name("task_" + randomString(5))
                    .description("desc_" + randomString(10))
                    .reporterId(userId)
                    .priority(TaskPriority.LOW.name())
                    .status(status.name())
                    .confidential(false)
                    .rewardsPoints(null)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .deadline(LocalDate.now().plusDays(7))
                    .build();

            insertTaskIntoDB(task);
            tasks.add(task);
        }

        return tasks;
    }
}
