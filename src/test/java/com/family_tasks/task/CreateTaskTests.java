package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.user.GroupEntity;
import com.family_tasks.dto.user.UserEntity;
import com.family_tasks.enums.TaskPriority;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.family_tasks.UrlConstant.CREATE_TASKS_URI;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.task.GetTaskTests.createUserWithGroup;
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateTaskTests extends AbstractTaskTrackerTest {

    @Test
    public void createTask_withRequiredFieldsOnly_shouldReturn200() {
        // Arrange – create a valid user and group
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        // Build request body — matches successful Swagger 200 example
        String json = String.format("""
        {
          "name": "task_%s",
          "description": "Valid task description",
          "priority": "LOW",
          "reporterId": %d,
          "executorIds": [%d],
          "confidential": false,
          "deadline": "2025-12-31"
        }
    """, randomString(5), reporterId, reporterId);

        // Act – send the POST
        Response response = given()
                .log().all()
                .queryParam("userId", reporterId)
                .contentType("application/json")
                .accept("application/json")
                .body(json)
                .when()
                .post(CREATE_TASKS_URI);

        // Debug output
        System.out.println("STATUS = " + response.getStatusCode());
        response.prettyPrint();

        // Assert – backend actually returns 200
        response.then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("taskId", notNullValue())
                .body("name", startsWith("task_"))
                .body("priority", equalTo("LOW"))
                .body("reporterId", equalTo(reporterId))
                .body("confidential", equalTo(false))
                .body("description", equalTo("Valid task description"))
                .body("deadline", equalTo("2025-12-31"))
                .body("status", equalTo("TO_DO"));
    }


    @Test
    public void createTask_missingRequiredField_name_thenBadRequest() {
        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        String json = String.format("""
        {
          "priority": "%s",
          "reporterId": %d,
          "confidential": false
        }
    """, TaskPriority.LOW.name(), reporterId);

        Response response = given()
                .header("Content-Type", "application/json")
                .body(json)
                .when()
                .post(CREATE_TASKS_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", containsStringIgnoringCase("name"))
                .extract()
                .response();

        response.prettyPrint();
    }

    @AfterEach
    public void clearDB() {
        executeDbQuery("DELETE FROM executors_tasks");
        executeDbQuery("DELETE FROM tasks");
        executeDbQuery("DELETE FROM groups");
        executeDbQuery("DELETE FROM users");
    }
}
