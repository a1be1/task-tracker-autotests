package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskEntity;
import com.family_tasks.dto.task.TaskUpdateRequest;
import com.family_tasks.dto.user.GroupEntity;
import com.family_tasks.enums.TaskPriority;
import com.family_tasks.enums.TaskStatus;
import com.family_tasks.utils.task.TaskResponseWrapper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.family_tasks.UrlConstant.GET_TASKS_URI;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.task.GetTaskTests.*;
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class UpdateTaskTests extends AbstractTaskTrackerTest {

    @Test
    public void updateAllTaskFields() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();
        int executorId = insertUserIntoDB(buildUserEntity(groupId));

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskToUpdate);

        String taskId = taskToUpdate.getTaskId();

        TaskUpdateRequest updateRequest = buildUpdateTaskRequest()
                .executorIds(Set.of(executorId, reporterId))
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        TaskResponseWrapper taskResp = new TaskResponseWrapper(response);

        assertEquals(taskId, taskResp.getTaskId());
        assertEquals(updateRequest.getName(), taskResp.getName());
        assertEquals(updateRequest.getStatus(), taskResp.getStatus());
        assertEquals(updateRequest.getPriority(), taskResp.getPriority());
        assertEquals(updateRequest.getDescription(), taskResp.getDescription());
        assertEquals(updateRequest.getDeadline(), taskResp.getDeadline());
        assertThat(taskResp.getExecutorIds())
                .containsExactlyInAnyOrderElementsOf(Set.of(executorId, reporterId));
        assertThat(taskResp.getCreatedAt()).isNotNull();
        assertThat(taskResp.getUpdatedAt()).isNotNull();

        System.out.println("Update all fields test: ");
        response.prettyPrint();
    }

    @Test
    public void updateCompletedTask() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();
        int executorId = insertUserIntoDB(buildUserEntity(groupId));

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        taskToUpdate.setStatus(TaskStatus.COMPLETED.name());
        insertTaskIntoDB(taskToUpdate);

        String taskId = taskToUpdate.getTaskId();

        TaskUpdateRequest updateRequest = buildUpdateTaskRequest()
                .executorIds(Set.of(executorId, reporterId))
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("userId", reporterId)
                .body(updateRequest)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertTaskUpdatedCorrectly(response, updateRequest, reporterId, taskId);

        System.out.println("Update all fields test when task has status COMPLETED: ");
        response.prettyPrint();
    }

    @Test
    public void updateCancelledTask() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();
        int executorId = insertUserIntoDB(buildUserEntity(groupId));

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        taskToUpdate.setStatus(TaskStatus.CANCELLED.name());
        insertTaskIntoDB(taskToUpdate);

        String taskId = taskToUpdate.getTaskId();

        TaskUpdateRequest updateRequest = buildUpdateTaskRequest()
                .executorIds(Set.of(executorId, reporterId))
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("userId", reporterId)
                .body(updateRequest)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertTaskUpdatedCorrectly(response, updateRequest, reporterId, taskId);

        System.out.println("Update all fields test when task has status COMPLETED: ");
        response.prettyPrint();
    }

    @Test
    public void updateAllTaskFields_whenTaskNotExist() {
        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();
        int executorId = insertUserIntoDB(buildUserEntity(groupId));

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskToUpdate);

        String nonExistentTask = taskToUpdate.getTaskId() + 2;

        TaskUpdateRequest updateRequest = buildUpdateTaskRequest()
                .executorIds(Set.of(executorId, reporterId))
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("userId", reporterId)
                .body(updateRequest)
                .when()
                .put(GET_TASKS_URI + "/" + nonExistentTask)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(TASK_NOT_EXIST, nonExistentTask)))
                .extract()
                .response();

        System.out.println("Attempt to update NON-EXISTENT task: ");
        response.prettyPrint();
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("missingRequiredField")
    public void updateTask_withMissingRequiredField_returnsBadRequest(String testName, TaskUpdateRequest request, String expectedError) {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskToUpdate);

        String taskId = taskToUpdate.getTaskId();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(expectedError))
                .extract()
                .response();

        System.out.println("Running test case: " + testName);
        response.prettyPrint();
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("invalidUpdateData")
    public void updateTask_withWrongDataType_returnsBadRequest(String testName, TaskUpdateRequest request, String expectedError) {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskToUpdate);

        String taskId = taskToUpdate.getTaskId();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(expectedError))
                .extract()
                .response();

        System.out.println("Running test case: " + testName);
        response.prettyPrint();
    }

    private static Stream<Arguments> invalidUpdateData() {
        return Stream.of(
                Arguments.of("Invalid deadline format",
                        buildUpdateTaskRequest().deadline("deadline").build(),
                        INCORRECT_REQUEST_FORMAT
                ),
                Arguments.of(
                        "Invalid status value",
                        buildUpdateTaskRequest().status("NOT_A_STATUS").build(),
                        TASK_STATUS_INVALID
                ),
                Arguments.of(
                        "Invalid priority value",
                        buildUpdateTaskRequest().priority("NOT_A_PRIORITY").build(),
                        TASK_PRIORITY_INVALID
                )
        );
    }

    private static Stream<Arguments> missingRequiredField() {
        return Stream.of(
                Arguments.of(
                        "Missing task name",
                        buildUpdateTaskRequest().name(null).build(),
                        TASK_NAME_NOT_SPECIFIED
                ),
                Arguments.of(
                        "Missing status",
                        buildUpdateTaskRequest().status(null).build(),
                        TASK_STATUS_NULL
                ),
                Arguments.of(
                        "Missing priority",
                        buildUpdateTaskRequest().priority(null).build(),
                        TASK_PRIORITY_NULL
                ),
                Arguments.of(
                        "Missing confidential flag",
                        buildUpdateTaskRequest().confidential(null).build(),
                        TASK_CONFIDENTIAL_STATUS_NOT_SPECIFIED
                )
        );
    }

    private static TaskUpdateRequest.TaskUpdateRequestBuilder buildUpdateTaskRequest() {
        return TaskUpdateRequest.builder()
                .status(TaskStatus.IN_PROGRESS.name())
                .name("updated_task_" + randomString(5))
                .description("updated_desc_" + randomString(10))
                .priority(TaskPriority.HIGH.name())
                .executorIds(null)
                .confidential(true)
                .deadline(LocalDate.now().plusDays(2).toString());
    }

    private void assertTaskUpdatedCorrectly(
            Response response,
            TaskUpdateRequest updateRequest,
            int reporterId,
            String taskId
    ) {
        assertEquals(updateRequest.getName(), response.path("name"));
        assertEquals(updateRequest.getStatus(), response.path("status"));
        assertEquals(updateRequest.getPriority(), response.path("priority"));
        assertEquals(updateRequest.getDeadline(), response.path("deadline"));
        assertEquals(updateRequest.getDescription(), response.path("description"));
        assertEquals(updateRequest.getExecutorIds(), new HashSet<>(response.path("executorIds")));

        assertEquals(reporterId, (Integer) response.path("reporterId"));
        assertEquals(taskId, response.path("taskId"));
    }

    @AfterAll
    public static void clearDB() {
        executeDbQuery("DELETE FROM executors_tasks");
        executeDbQuery("DELETE FROM rewards");
        executeDbQuery("DELETE FROM tasks");
        executeDbQuery("DELETE FROM groups");
        executeDbQuery("DELETE FROM users");
    }
}
