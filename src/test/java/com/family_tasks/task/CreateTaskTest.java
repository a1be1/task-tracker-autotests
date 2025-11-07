package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskEntity;
import com.family_tasks.dto.user.GroupEntity;
import com.family_tasks.dto.user.UserEntity;
import com.family_tasks.enums.TaskPriority;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.family_tasks.UrlConstant.GET_TASKS_URI; // POST to /v1/tasks
import static com.family_tasks.utils.TestDataBaseUtils.insertGroupIntoDB;
import static com.family_tasks.utils.TestDataBaseUtils.insertUserIntoDB;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateTaskTests extends AbstractTaskTrackerTest {

    /**
     * Create task with required fields only -> 201
     * Required (per DTO / spec): name, priority, reporterId, confidential
     */
    @Test
    public void createTask_withRequiredFieldsOnly_shouldReturn201() {
        // Arrange: create a user + group to get a valid reporterId
        int ownerId = insertUserIntoDB(UserEntity.builder()
                .name("user_" + randomString(6))
                .admin(true)
                .groupId(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        int groupId = insertGroupIntoDB(GroupEntity.builder()
                .ownerId(ownerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build());

        // Update user with groupId if your API/DB requires (often not needed for simple reporterId use)

        // Build MINIMAL task payload (required fields only)
        TaskEntity minimal = TaskEntity.builder()
                .name("task_" + randomString(8))
                .priority(TaskPriority.LOW.name())
                .reporterId(ownerId)
                .confidential(false)
                .build();

        // Act + Assert
        Response response = given()
                // Some endpoints in this project use ?userId=... — keep it for consistency
                .queryParam("userId", ownerId)
                .body(minimal)
                .when()
                .post(GET_TASKS_URI)          // POST /v1/tasks
                .then()
                .statusCode(201)
                .body("taskId", notNullValue())
                .body("name", equalTo(minimal.getName()))
                .body("priority", equalTo(minimal.getPriority()))
                .body("reporterId", equalTo(minimal.getReporterId()))
                .body("confidential", equalTo(minimal.isConfidential()))
                .extract()
                .response();

        // Optionally: print created id for debugging
        System.out.println("Created taskId = " + response.path("taskId"));
    }
}
