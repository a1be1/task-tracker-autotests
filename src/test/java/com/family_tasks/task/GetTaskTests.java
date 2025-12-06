package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskEntity;
import com.family_tasks.dto.user.GroupEntity;
import com.family_tasks.enums.TaskPriority;
import com.family_tasks.enums.TaskStatus;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.family_tasks.UrlConstant.TASKS_URI;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class GetTaskTests extends AbstractTaskTrackerTest {

    @EnumSource(value = TaskPriority.class)
    @ParameterizedTest
    public void getTaskById_shouldReturnTaskWithGivenPriority(TaskPriority priority) {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        TaskEntity taskEntity = buildTaskEntity(reporterId);
        taskEntity.setPriority(priority.name());
        insertTaskIntoDB(taskEntity);

        String taskId = taskEntity.getTaskId();

        Response response = given()
                .queryParam("userId", reporterId)
                .when()
                .get(TASKS_URI + "/" + taskId)
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

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        TaskEntity taskEntity = buildTaskEntity(reporterId);
        taskEntity.setStatus(status.name());
        insertTaskIntoDB(taskEntity);

        String taskId = taskEntity.getTaskId();

        Response response = given()
                .queryParam("userId", reporterId)
                .when()
                .get(TASKS_URI + "/" + taskId)
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

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        TaskEntity taskEntity = buildTaskEntity(reporterId);
        taskEntity.setConfidential(true);
        insertTaskIntoDB(taskEntity);

        String taskId = taskEntity.getTaskId();

        Response response = given()
                .queryParam("userId", reporterId)
                .when()
                .get(TASKS_URI + "/" + taskId)
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

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();

        int executorId = insertUserIntoDB(buildUserEntity(groupId));

        TaskEntity taskEntity = buildTaskEntity(reporterId);
        taskEntity.setConfidential(true);
        insertTaskIntoDB(taskEntity);

        String taskId = taskEntity.getTaskId();

        insertTaskExecutors(taskId, List.of(executorId));

        Response response = given()
                .queryParam("userId", executorId)
                .when()
                .get(TASKS_URI + "/" + taskId)
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

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();

        GroupEntity otherGroup = createUserWithGroup();
        int notAllowedUserId = otherGroup.getOwnerId();

        assertNotEquals(groupId, otherGroup.getGroupId(), "notAllowedUser must be from a different group");

        TaskEntity taskEntity = buildTaskEntity(reporterId);
        taskEntity.setConfidential(true);
        insertTaskIntoDB(taskEntity);

        String taskId = taskEntity.getTaskId();

        Response response = given()
                .queryParam("userId", notAllowedUserId)
                .when()
                .get(TASKS_URI + "/" + taskId)
                .then()
                .statusCode(404)
                .body("errorMessage", equalTo(String.format(TASK_NOT_EXIST, taskId)))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void getTaskById_missingUserId_thenBadRequest() {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        TaskEntity taskEntity = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskEntity);
        String taskId = taskEntity.getTaskId();

        Response response = given()
                .when()
                .get(TASKS_URI + "/" + taskId)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(USER_NOT_SPECIFIED))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void getTaskById_invalidUserId_thenBadRequest() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        TaskEntity taskEntity = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskEntity);
        String taskId = taskEntity.getTaskId();

        int invalidUserId = reporterId + 2;

        Response response = given()
                .queryParam("userId", invalidUserId)
                .when()
                .get(TASKS_URI + "/" + taskId)
                .then()
                .statusCode(404)
                .body("errorMessage", equalTo(String.format(USER_NOT_EXIST, invalidUserId)))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void getTaskById_invalidTaskId_thenBadRequest() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        String invalidTaskId = UUID.randomUUID().toString();

        Response response = given()
                .queryParam("userId", reporterId)
                .when()
                .get(TASKS_URI + "/" + invalidTaskId)
                .then()
                .statusCode(404)
                .body("errorMessage", equalTo(String.format(TASK_NOT_EXIST, invalidTaskId)))
                .extract()
                .response();

        response.prettyPrint();
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
}
