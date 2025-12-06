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

import java.util.Set;
import java.util.stream.Stream;

import static com.family_tasks.UrlConstant.TASKS_URI;
import static com.family_tasks.ValidationConstants.TASK_NAME_MAX_LENGTH;
import static com.family_tasks.ValidationMessage.TASK_NAME_NOT_SPECIFIED;
import static com.family_tasks.ValidationMessage.TASK_NAME_TOO_LONG;
import static com.family_tasks.ValidationMessage.TASK_PRIORITY_INVALID;
import static com.family_tasks.ValidationMessage.TASK_PRIORITY_NULL;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

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
    public void createTask_priorityLow_shouldReturn200() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        TaskCreateRequest request = taskCreateRequest(reporterId)
                .priority(TaskPriority.LOW.name())
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
    public void createTask_priorityEmpty_shouldReturn400() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        TaskCreateRequest request = taskCreateRequest(reporterId)
                .priority("")
                .build();

        Response response = given()
                .header("Content-Type", "application/json")
                .body(request)
                .when()
                .post(TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(TASK_PRIORITY_NULL))
                .extract()
                .response();

        response.prettyPrint();
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("invalidCreateTaskProvider")
    public void whenInvalidInput_createTask(String testName,
                                            TaskCreateRequest request,
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

        System.out.println(testName);
        response.prettyPrint();
    }

    private static Stream<Arguments> invalidCreateTaskProvider() {
        return Stream.of(
                Arguments.of(
                        "When invalid priority",
                        taskCreateRequest(1)
                                .priority("INVALID_PRIORITY")
                                .build(),
                        TASK_PRIORITY_INVALID
                ),
                Arguments.of(
                        "When task's priority null",
                        taskCreateRequest(1)
                                .priority(null)
                                .build(),
                        TASK_PRIORITY_NULL
                ),
                Arguments.of(
                        "When task's priority empty",
                        taskCreateRequest(1)
                                .priority("")
                                .build(),
                        TASK_PRIORITY_NULL
                )
        );
    }
}
