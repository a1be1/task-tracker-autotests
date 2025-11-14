package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskCreateRequest;
import com.family_tasks.dto.user.GroupEntity;
import com.family_tasks.dto.user.UserEntity;
import com.family_tasks.enums.TaskPriority;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static com.family_tasks.UrlConstant.TASKS_URI;
import static com.family_tasks.ValidationConstants.USER_NAME_MIN_LENGTH;
import static com.family_tasks.ValidationMessage.TASK_NAME_NOT_SPECIFIED;
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateTaskTests extends AbstractTaskTrackerTest {

    @Test
    public void createTask_withRequiredFieldsOnly_shouldReturn200() {
        int reporterId = createUserWithGroup();

        TaskCreateRequest request = taskCreateRequest(reporterId)
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(200)
                .body("taskId", notNullValue()) // проверка, что сервер вернул id задачи
                .body("name", equalTo(request.getName()))
                .body("priority", equalTo(request.getPriority()))
                .body("reporterId", equalTo(request.getReporterId()))
                .body("confidential", equalTo(request.getConfidential()))
                .body("description", equalTo(request.getDescription()))
                .body("status", equalTo("TO_DO")) // предполагаем, что статус всегда TO_DO при создании
                .body("deadline", equalTo(request.getDeadline()))
                .body("executorIds", empty())
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue())
                .extract()
                .response();

        System.out.println("STATUS = " + response.getStatusCode());
        response.prettyPrint();
    }

    @Test
    public void createTask_missingRequiredField_name_thenBadRequest() {
        // Arrange – create a valid user and group
        int reporterId = createUserWithGroup();
        TaskCreateRequest request = taskCreateRequest(reporterId)
                .name(null)
                .build();

        Response response = given()
                .header("Content-Type", "application/json")
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(TASK_NAME_NOT_SPECIFIED))
                .extract()
                .response();

        response.prettyPrint();
    }

    private TaskCreateRequest.TaskCreateRequestBuilder taskCreateRequest(Integer reporterId) {
        return TaskCreateRequest.builder()
                .name("name_" + randomString(USER_NAME_MIN_LENGTH))
                .description(null)
                .priority(TaskPriority.LOW.name())
                .reporterId(reporterId)
                .executorIds(Set.of())
                .confidential(false)
                .deadline(LocalDate.now().toString());
    }

    public UserEntity buildUserEntity(Integer groupId) {
        return UserEntity.builder()
                .admin(true)
                .name("user_" + randomString(6))
                .groupId(groupId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static GroupEntity buildGroupEntity(Integer ownerId) {
        return GroupEntity.builder()
                .ownerId(ownerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
    }

    private Integer createUserWithGroup() {

        UserEntity owner = buildUserEntity(null);
        int ownerId = insertUserIntoDB(owner);

        GroupEntity group = buildGroupEntity(ownerId);
        int groupId = insertGroupIntoDB(group);

        owner.setGroupId(groupId);
        updateUserGroupIdInDB(owner);
        return owner.getId();
    }

    @AfterEach
    public void clearDB() {
        executeDbQuery("DELETE FROM executors_tasks");
        executeDbQuery("DELETE FROM tasks");
        executeDbQuery("DELETE FROM groups");
        executeDbQuery("DELETE FROM users");
    }
}