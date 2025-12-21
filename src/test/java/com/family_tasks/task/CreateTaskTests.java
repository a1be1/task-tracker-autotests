package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskCreateRequest;
import com.family_tasks.dto.user.GroupEntity;
import com.family_tasks.enums.TaskPriority;
import com.family_tasks.enums.TaskStatus;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static com.family_tasks.UrlConstant.TASKS_URI;
import static com.family_tasks.ValidationConstants.*;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.utils.TestValuesUtils.randomInt;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static com.family_tasks.utils.TestDataBaseUtils.*;


public class CreateTaskTests extends AbstractTaskTrackerTest {

    @EnumSource(value = TaskPriority.class)
    @ParameterizedTest
    public void createTask_withRequiredFieldsOnly_shouldReturn200(TaskPriority priority) {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        TaskCreateRequest request = taskCreateRequest(reporterId)
                .priority(priority.name())
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(200)
                .body("taskId", notNullValue())
                .body("name", equalTo(request.getName()))
                .body("priority", equalTo(request.getPriority()))
                .body("reporterId", equalTo(request.getReporterId()))
                .body("confidential", equalTo(request.getConfidential()))
                .body("description", equalTo(request.getDescription()))
                .body("status", equalTo(TaskStatus.TO_DO.name()))
                .body("deadline", equalTo(request.getDeadline()))
                .body("executorIds", empty())
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue())
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void createTask_nameLength100_shouldReturn200() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        String longName = randomString(TASK_NAME_MAX_LENGTH);

        TaskCreateRequest request = taskCreateRequest(reporterId)
                .name(longName)
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(200)
                .body("taskId", notNullValue())
                .body("name", equalTo(request.getName()))
                .body("priority", equalTo(request.getPriority()))
                .body("reporterId", equalTo(request.getReporterId()))
                .body("confidential", equalTo(request.getConfidential()))
                .body("description", equalTo(request.getDescription()))
                .body("status", equalTo(TaskStatus.TO_DO.name()))
                .body("deadline", equalTo(request.getDeadline()))
                .body("executorIds", empty())
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue())
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void createTask_missingRequiredField_name_thenBadRequest() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

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

    @Test
    public void createTask_nameLength101_shouldReturn400() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        String tooLongName = randomString(TASK_NAME_MAX_LENGTH + 1);

        TaskCreateRequest request = taskCreateRequest(reporterId)
                .name(tooLongName)
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(TASK_NAME_TOO_LONG))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void createTask_withValidDeadline_shouldReturn200() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        String validDeadline = LocalDate.now().toString();

        TaskCreateRequest request = taskCreateRequest(reporterId)
                .deadline(validDeadline)
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(200)
                .body("deadline", equalTo(validDeadline))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void createTask_deadlineInvalidFormat_shouldReturn400() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        // Wrong format: DD-MM-YYYY
        TaskCreateRequest request = taskCreateRequest(reporterId)
                .deadline("31-12-2025")
                .build();

        Response response = given()
                .header("Content-Type", "application/json")
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", notNullValue())
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void createTask_executorIdsUnique_shouldReturn200() {
        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();

        int executorId1 = insertUserIntoDB(buildUserEntity(groupId));
        int executorId2 = insertUserIntoDB(buildUserEntity(groupId));
        int executorId3 = insertUserIntoDB(buildUserEntity(groupId));

        Set<Integer> executorIds = Set.of(executorId1, executorId2, executorId3);

        TaskCreateRequest request = taskCreateRequest(reporterId)
                .executorIds(executorIds)
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(200)
                .body("taskId", notNullValue())
                .body("name", equalTo(request.getName()))
                .body("priority", equalTo(request.getPriority()))
                .body("reporterId", equalTo(request.getReporterId()))
                .body("confidential", equalTo(request.getConfidential()))
                .body("description", equalTo(request.getDescription()))
                .body("status", equalTo(TaskStatus.TO_DO.name()))
                .body("deadline", equalTo(request.getDeadline()))
                .body("executorIds", containsInAnyOrder(executorId1, executorId2, executorId3))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue())
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void createTask_wrongTypes_thenBadRequest() {

        String invalidJson = """
                {
                    "name": "Test task",
                    "description": "desc",
                    "priority": "LOW",
                    "reporterId": "abc",
                    "executorIds": ["1", 2],
                    "confidential": "true",
                    "deadline": "2025-12-31"
                }
                """;

        Response response = given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(400)   // EXPECTED
                .extract()
                .response();

        System.out.println("Wrong types test:");
        response.prettyPrint();
    }

    @Test
    public void createTask_descriptionMinLength10_shouldReturn201() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        String minDesc = "1234567890"; // exactly 10 chars

        TaskCreateRequest request = taskCreateRequest(reporterId)
                .description(minDesc)
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(200)
                .body("description", equalTo(minDesc))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void createTask_descriptionAtMaxLength_shouldReturn201() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        String maxDesc = randomString(TASK_DESCRIPTION_MAX_LENGTH);

        TaskCreateRequest request = taskCreateRequest(reporterId)
                .description(maxDesc)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(200);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateTaskProvider")
    public void whenInvalidInput_createTask(TaskCreateRequest request,
                                            String expectedMessage) {

        Response response = given()
                .header("Content-Type", "application/json")
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(expectedMessage))
                .extract()
                .response();

        response.prettyPrint();
    }

    private static Stream<Arguments> invalidCreateTaskProvider() {
        return Stream.of(
                Arguments.of(
                        taskCreateRequest(randomInt())
                                .priority("INVALID_PRIORITY")
                                .build(),
                        TASK_PRIORITY_INVALID
                ),
                Arguments.of(
                        taskCreateRequest(randomInt())
                                .priority(null)
                                .build(),
                        TASK_PRIORITY_NULL
                ),

                Arguments.of(
                        taskCreateRequest(randomInt())
                                .description(randomString(TASK_DESCRIPTION_MAX_LENGTH + 1))
                                .build(),
                        TASK_DESCRIPTION_TOO_LONG
                ),

                Arguments.of(
                        taskCreateRequest(randomInt())
                                .description(randomString(TASK_DESCRIPTION_MIN_LENGTH - 1))
                                .build(),
                        TASK_DESCRIPTION_TOO_SHORT
                ),

                Arguments.of(
                        taskCreateRequest(randomInt())
                                .deadline(LocalDate.now().minusDays(1).toString())
                                .build(),
                        TASK_DEADLINE_DATE_NOT_FUTURE
                ),

                Arguments.of(
                        taskCreateRequest(randomInt())
                                .confidential(null)
                                .build(),
                        TASK_CONFIDENTIAL_STATUS_NOT_SPECIFIED
                ),

                Arguments.of(
                        taskCreateRequest(randomInt())
                                .reporterId(null)
                                .build(),
                        TASK_REPORTER_NULL
                )
        );
    }

}
