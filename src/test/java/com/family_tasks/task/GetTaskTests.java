package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskEntity;
import com.family_tasks.dto.user.UserEntity;
import com.family_tasks.enums.TaskPriority;
import com.family_tasks.enums.TaskStatus;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
import static org.hamcrest.Matchers.*;

public class GetTaskTests extends AbstractTaskTrackerTest {

    @EnumSource(value = TaskPriority.class)
    @ParameterizedTest
    public void getTaskById_shouldReturnTaskWithGivenPriority(TaskPriority priority) {

        int userId = insertUserIntoDB(buildUserEntity());

        TaskEntity taskEntity = buildTaskEntity(userId);
        taskEntity.setPriority(priority.name());
        insertTaskIntoDB(taskEntity);

        String taskId = taskEntity.getTaskId();

        Response response = given()
                .queryParam("userId", userId)
                .when()
                .get(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .body("taskId", equalTo(taskId))
                .body("name", equalTo(taskEntity.getName()))
                .body("status", equalTo(taskEntity.getStatus()))
                .body("priority", equalTo(taskEntity.getPriority()))
                .body("reporterId", equalTo(taskEntity.getReporterId()))
                .body("description", equalTo(taskEntity.getDescription()))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue())
                .body("deadline", equalTo(taskEntity.getDeadline().toString()))
                .extract()
                .response();

        response.prettyPrint();
    }

    @EnumSource(value = TaskStatus.class)
    @ParameterizedTest
    public void getTaskById_shouldReturnTaskWithGivenStatus(TaskStatus status) {
        int userId = insertUserIntoDB(buildUserEntity());

        TaskEntity taskEntity = buildTaskEntity(userId);
        taskEntity.setStatus(status.name());
        insertTaskIntoDB(taskEntity);

        String taskId = taskEntity.getTaskId();

        Response response = given()
                .queryParam("userId", userId)
                .when()
                .get(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .body("taskId", equalTo(taskId))
                .body("name", equalTo(taskEntity.getName()))
                .body("status", equalTo(taskEntity.getStatus()))
                .body("priority", equalTo(taskEntity.getPriority()))
                .body("reporterId", equalTo(taskEntity.getReporterId()))
                .body("description", equalTo(taskEntity.getDescription()))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue())
                .body("deadline", equalTo(taskEntity.getDeadline().toString()))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void getTaskById_shouldReturnConfidentialTask_forReporter() {
        int userId = insertUserIntoDB(buildUserEntity());
        int reporterId = insertUserIntoDB(buildUserEntity());

        TaskEntity taskEntity = buildTaskEntity(userId);
        insertTaskIntoDB(taskEntity);

        String taskId = taskEntity.getTaskId();

        Response response = given()
                .queryParam("userId", reporterId)
                .when()
                .get(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .body("taskId", equalTo(taskId))
                .body("name", equalTo(taskEntity.getName()))
                .body("status", equalTo(taskEntity.getStatus()))
                .body("priority", equalTo(taskEntity.getPriority()))
                .body("reporterId", equalTo(taskEntity.getReporterId()))
                .body("description", equalTo(taskEntity.getDescription()))
                .body("confidential", equalTo(taskEntity.isConfidential()))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue())
                .body("deadline", equalTo(taskEntity.getDeadline().toString()))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void getTaskById_withConfidentialTrue_forExecutor() {

        int reporterId = insertUserIntoDB(buildUserEntity());
        int executorId = insertUserIntoDB(buildUserEntity());

        TaskEntity taskEntity = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskEntity);

        String taskId = taskEntity.getTaskId();

        insertTaskExecutors(taskId, List.of(executorId));

        Response response = given()
                .queryParam("userId", executorId)
                .when()
                .get(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .body("taskId", equalTo(taskId))
                .body("name", equalTo(taskEntity.getName()))
                .body("status", equalTo(taskEntity.getStatus()))
                .body("priority", equalTo(taskEntity.getPriority()))
                .body("reporterId", equalTo(taskEntity.getReporterId()))
                .body("executorIds", hasItem(executorId))
                .body("description", equalTo(taskEntity.getDescription()))
                .body("confidential", equalTo(taskEntity.isConfidential()))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue())
                .body("deadline", equalTo(taskEntity.getDeadline().toString()))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void getTaskById_withConfidentialTrueForNotAllowedUser_thenAccessDenied () {

        int reporterId = insertUserIntoDB(buildUserEntity());
        int executorId = insertUserIntoDB(buildUserEntity());
        int notAllowedUserId = insertUserIntoDB(buildUserEntity());

        TaskEntity taskEntity = buildTaskEntity(reporterId);
        taskEntity.setConfidential(true);

        insertTaskIntoDB(taskEntity);

        String taskId = taskEntity.getTaskId();

        insertTaskExecutors(taskId, List.of(reporterId, executorId));

        Response response = given()
                .queryParam("userId", notAllowedUserId)
                .when()
                .get(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(404)
                .body("errorMessage", equalTo(String.format(TASK_NOT_EXIST,taskId)))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void getTaskById_missingUserId_thenBadRequest() {
        int reporterId = insertUserIntoDB(buildUserEntity());
        TaskEntity taskEntity = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskEntity);
        String taskId = taskEntity.getTaskId();

        Response response = given()
                .when()
                .get(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(USER_NOT_SPECIFIED))
                .extract()
                .response();

        response.prettyPrint();

    }

    @Test
    public void getTaskById_invalidUserId_thenBadRequest() {
        int reporterId = insertUserIntoDB(buildUserEntity());
        TaskEntity taskEntity = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskEntity);
        String taskId = taskEntity.getTaskId();

        int invalidUserId = reporterId +2;

        Response response = given()
                .queryParam("userId", invalidUserId)
                .when()
                .get(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(404)
                .body("errorMessage", equalTo(String.format(USER_NOT_EXIST,invalidUserId)))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void getTaskById_invalidTaskId_thenBadRequest() {

        int userId = insertUserIntoDB(buildUserEntity());

        String invalidTaskId = UUID.randomUUID().toString();

        Response response = given()
                .queryParam("userId", userId)
                .when()
                .get(GET_TASKS_URI + "/" + invalidTaskId)
                .then()
                .statusCode(404)
                .body("errorMessage", equalTo(String.format(TASK_NOT_EXIST, invalidTaskId)))
                .extract()
                .response();

        response.prettyPrint();
    }

    @AfterAll
    public static void clearDB() {
        executeDbQuery("DELETE FROM executors_tasks");
        executeDbQuery("DELETE FROM tasks");
        executeDbQuery("DELETE FROM users");
    }

    private UserEntity buildUserEntity() {
        return UserEntity.builder()
                .admin(true)
                .name("user_" + randomString(6))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private TaskEntity buildTaskEntity(Integer userId) {
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

    private List<Integer> createTestUsers(int count) {
        List<Integer> userIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UserEntity user = buildUserEntity();
            int userId = insertUserIntoDB(user);
            userIds.add(userId);
        }
        return userIds;
    }

}