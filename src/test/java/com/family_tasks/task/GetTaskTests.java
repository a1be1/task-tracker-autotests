package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.sql.ResultSet;

import static com.family_tasks.UrlConstant.GET_TASKS_URI;
import static com.family_tasks.utils.TestDataBaseUtils.executeDbQuery;
import static com.family_tasks.utils.TestDataBaseUtils.getQueryResult;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class GetTaskTests extends AbstractTaskTrackerTest {

    private static int userId;
    private static String taskId;
    private String taskName;
    private String taskDescription;

    @BeforeEach
    void preCondition() throws Exception {

        String userName = "user_" + randomString(6);
        executeDbQuery("""
            INSERT INTO users (name, admin, created_at, updated_at)
            VALUES ('%s', true, now(), now());
        """.formatted(userName));

        userId = getLastInsertedUserId();

        taskId = randomString(8);
        taskName = "task_" + randomString(5);
        taskDescription = "desc_" + randomString(10);

        executeDbQuery("""
            INSERT INTO tasks (id, name, description, priority, status, reporter_id,
                               confidential, deadline, created_at, updated_at)
            VALUES ('%s', '%s', '%s', 'LOW', 'TO_DO',
                    %d, false, current_date + interval '7 days', now(), now());
        """.formatted(taskId, taskName, taskDescription, userId));
    }

    @Test()
    public void getTaskByIdPositiveTest() {
        Response response = given()
                .queryParam("userId", userId)
                .when()
                .get(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        response.then().body("taskId", equalTo(taskId));
        response.then().body("name", equalTo(taskName));
        response.then().body("status", equalTo("TO_DO"));

        System.out.println(response.asPrettyString());
    }

    @Test
    public void getAllTasksByUser() {
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
        response.then().body("find { it.taskId == '%s' }.name", withArgs(taskId), equalTo(taskName));
        response.then().body("find { it.taskId == '%s' }.status", withArgs(taskId), equalTo("TO_DO"));
    }

    private int getLastInsertedUserId() throws Exception {
        try (ResultSet rs = getQueryResult("SELECT id FROM users ORDER BY id DESC LIMIT 1;")) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        throw new RuntimeException("Couldn't get userId");
    }

    @AfterEach
    public void clearDB() {
        executeDbQuery("DELETE FROM tasks WHERE id = '%s';".formatted(taskId));
        executeDbQuery("DELETE FROM users WHERE id = %d;".formatted(userId));
    }
}
