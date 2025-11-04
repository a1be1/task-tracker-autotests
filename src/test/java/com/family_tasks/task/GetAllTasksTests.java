package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskEntity;
import com.family_tasks.dto.user.UserEntity;
import com.family_tasks.enums.TaskFilter;
import com.family_tasks.enums.TaskPriority;
import com.family_tasks.enums.TaskStatus;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.family_tasks.UrlConstant.GET_TASKS_URI;
import static com.family_tasks.ValidationMessage.*;
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

        int reporterId = insertUserIntoDB(buildUserEntity());

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
                .get(GET_TASKS_URI)
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
        assertThat(statuses.size(), equalTo(tasks.size()));
        response.prettyPrint();
    }

    @Test
    public void getAllClosedTasks() {

        int reporterId = insertUserIntoDB(buildUserEntity());

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
                .get(GET_TASKS_URI)
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
        assertThat(statuses, contains("CANCELLED"));
        response.prettyPrint();

    }

    @Test
    public void getTasks_whenIsExecutorActiveTask() {
        int reporterId = insertUserIntoDB(buildUserEntity());
        int executorId = insertUserIntoDB(buildUserEntity());

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

        List<TaskEntity> activeTasks = new ArrayList<>();
        for (TaskEntity t : tasks) {
            if (t.isActiveTask()) {
                activeTasks.add(t);
            }
        }

        Response response = given()
                .queryParam("userId", executorId)
                .queryParam("filter", TaskFilter.IS_EXECUTOR_ACTIVE_TASK.name())
                .when()
                .get(GET_TASKS_URI)
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
        int reporterId = insertUserIntoDB(buildUserEntity());

        List<TaskEntity> tasks = createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        List<TaskEntity> activeTasks = new ArrayList<>();
        for (TaskEntity t : tasks) {
            if (t.isActiveTask()) {
                activeTasks.add(t);
            }
        }

        Response response = given()
                .queryParam("userId", reporterId)
                .queryParam("filter", TaskFilter.IS_REPORTER_ACTIVE_TASK.name())
                .when()
                .get(GET_TASKS_URI)
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
        int reporterId = insertUserIntoDB(buildUserEntity());
        int executorId = insertUserIntoDB(buildUserEntity());

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

        List<TaskEntity> completedTasks = new ArrayList<>();
        for (TaskEntity t : tasks) {
            if (t.isCompletedTask()) {
                completedTasks.add(t);
            }
        }

        Response response = given()
                .queryParam("userId", executorId)
                .queryParam("filter", TaskFilter.IS_EXECUTOR_COMPLETED_TASK.name())
                .when()
                .get(GET_TASKS_URI)
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
        System.out.println("Statuses (IS_EXECUTOR_ACTIVE_TASK): " + statuses);
        response.prettyPrint();
    }

    @Test
    public void getTasks_whenIsReporterCompletedTask() {
        int reporterId = insertUserIntoDB(buildUserEntity());

        List<TaskEntity> tasks = createTasksForStatusesAndInsertIntoDB(
                reporterId,
                TaskStatus.TO_DO,
                TaskStatus.IN_PROGRESS,
                TaskStatus.COMPLETED,
                TaskStatus.CANCELLED
        );

        List<TaskEntity> completedTasks = new ArrayList<>();
        for (TaskEntity t : tasks) {
            if (t.isCompletedTask()) {
                completedTasks.add(t);
            }
        }

        Response response = given()
                .queryParam("userId", reporterId)
                .queryParam("filter", TaskFilter.IS_REPORTER_COMPLETED_TASK.name())
                .when()
                .get(GET_TASKS_URI)
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
        System.out.println("Statuses (IS_REPORTER_ACTIVE_TASK): " + statuses);
        response.prettyPrint();
    }

    @Test
    public void getTasks_whenUserHasNoTasks_thenReturnEmptyList() {
        int userId = insertUserIntoDB(buildUserEntity());

        Response response = given()
                .queryParam("userId", userId)
                .queryParam("filter", TaskFilter.ALL_AVAILABLE.name())
                .when()
                .get(GET_TASKS_URI)
                .then()
                .statusCode(200)
                .extract().response();

        List<String> taskIds = response.jsonPath().getList("taskId");
        assertTrue(taskIds == null || taskIds.isEmpty(), "Expected empty task list for new user");
        response.prettyPrint();

    }

    @Test
    public void getAllAvailableTasks_withMissingUserId_thenBadRequest() {

        int reporterId = insertUserIntoDB(buildUserEntity());

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
                .get(GET_TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(USER_NOT_SPECIFIED)))
                .extract().response();

        response.prettyPrint();
    }

    @Test
    public void getAllAvailableTasks_withInvalidUserId_thenBadRequest() {

        int reporterId = insertUserIntoDB(buildUserEntity());
        int invalidUserId = reporterId +2;

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
                .get(GET_TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(USER_NOT_EXIST,invalidUserId)))
                .extract().response();

        response.prettyPrint();
    }

    @Test
    public void getAllAvailableTasks_withInvalidFilter_thenBadRequest() {

        int reporterId = insertUserIntoDB(buildUserEntity());

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
                .get(GET_TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(TASK_FILTER_INVALID)))
                .extract().response();

        response.prettyPrint();
    }

    @Test
    public void getAllAvailableTasks_withMissingFilter_thenBadRequest() {

        int reporterId = insertUserIntoDB(buildUserEntity());

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
                .get(GET_TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(TASK_FILTER_NOT_SPECIFIED)))
                .extract().response();

        response.prettyPrint();
    }

    @AfterEach
    public void clearDB() {
        executeDbQuery("DELETE FROM executors_tasks");
        executeDbQuery("DELETE FROM tasks");
        executeDbQuery("DELETE FROM groups");
        executeDbQuery("DELETE FROM users");
    }

    public static UserEntity buildUserEntity() {
        return UserEntity.builder()
                .admin(true)
                .name("user_" + randomString(6))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static TaskEntity buildTaskEntity(Integer userId) {
        return TaskEntity.builder()
                .taskId(UUID.randomUUID().toString())
                .name("task_" + randomString(5))
                .description("desc_" + randomString(10))
                .reporterId(userId)
                .priority(TaskPriority.LOW.name())
                .status(TaskStatus.TO_DO.name())
                .confidential(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deadline(LocalDate.now().plusDays(7))
                .build();
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
