package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskEntity;
import com.family_tasks.dto.user.UserEntity;
import com.family_tasks.enums.TaskPriority;
import com.family_tasks.enums.TaskStatus;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.family_tasks.UrlConstant.GET_TASKS_URI;
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestDataBaseUtils.executeDbQuery;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class GetAllTasksTests extends AbstractTaskTrackerTest {

    @Test
    public void getAllTasksByUser() {
        int userId = insertUserIntoDB(buildUserEntity());
        TaskEntity taskEntity = buildTaskEntity(userId);
        insertTaskIntoDB(taskEntity);
        String taskId = taskEntity.getTaskId();
        Response response = given()
                .queryParam("userId", userId)
                .queryParam("filter", "ALL_AVAILABLE")
                .when()
                .get(GET_TASKS_URI)
                .then()
                .statusCode(200)
                .extract()
                .response();

        System.out.println(response.asPrettyString());

        response.then().body("find { it.taskId == '%s' }", withArgs(taskId), notNullValue());
        response.then().body("find { it.taskId == '%s' }.name", withArgs(taskId), equalTo(taskEntity.getName()));
        response.then().body("find { it.taskId == '%s' }.status", withArgs(taskId), equalTo("TO_DO"));
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
}
