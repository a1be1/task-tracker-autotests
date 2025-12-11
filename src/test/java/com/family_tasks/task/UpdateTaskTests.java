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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.family_tasks.UrlConstant.GET_TASKS_URI;
import static com.family_tasks.ValidationConstants.*;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                .executorIds(Set.of(executorId))
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
        assertEquals(updateRequest.getRewardsPoints(), taskResp.getRewardsPoints());
        assertThat(taskResp.getExecutorIds())
                .containsExactlyInAnyOrderElementsOf(Set.of(executorId));
        assertThat(taskResp.getCreatedAt()).isNotNull();
        assertThat(taskResp.getUpdatedAt()).isNotNull();

        System.out.println("Update all fields test: ");
        response.prettyPrint();
    }

    @EnumSource(value = TaskStatus.class)
    @ParameterizedTest
    public void updateTaskWithGivenStatus(TaskStatus status) {

        GroupEntity group = createUserWithGroup();

        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();
        int executorId = insertUserIntoDB(buildUserEntity(groupId));

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        taskToUpdate.setStatus(status.name());
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

        System.out.println("Update all fields test for different statuses: ");
        response.prettyPrint();
    }

    @Test
    public void updateAllTaskFields_whenTaskNotExist_returnsBadRequest() {

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

    @Test
    public void updateAllTaskFields_whenExecutorNotExist_returnsBadRequest() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();

        int reporterId = group.getOwnerId();
        int executorId = insertUserIntoDB(buildUserEntity(groupId));
        int nonExistentExecutorId = executorId + 2;

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskToUpdate);

        String taskId = taskToUpdate.getTaskId();

        TaskUpdateRequest updateRequest = buildUpdateTaskRequest()
                .executorIds(Set.of(nonExistentExecutorId))
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(USER_NOT_EXIST, nonExistentExecutorId)))
                .extract()
                .response();

        System.out.println("Update all fields test when executor does not exist: ");
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
                ),
                Arguments.of(
                        "Invalid rewardsPoints value",
                        buildUpdateTaskRequest().rewardsPoints(0).build(),
                        REWARDS_POINTS_POSITIVE
                ),
                Arguments.of(
                        "Task description too long",
                        buildUpdateTaskRequest().description("A".repeat(TASK_DESCRIPTION_MAX_LENGTH + 1)).build(),
                        TASK_DESCRIPTION_TOO_LONG
                ),
                Arguments.of(
                        "Task description too short",
                        buildUpdateTaskRequest().description("A".repeat(TASK_DESCRIPTION_MIN_LENGTH - 1)).build(),
                        TASK_DESCRIPTION_TOO_SHORT
                ),
                Arguments.of(
                        "Task name too long",
                        buildUpdateTaskRequest().name("A".repeat(TASK_NAME_MAX_LENGTH + 1)).build(),
                        TASK_NAME_TOO_LONG
                )
        );
    }

    @Test
    public void shouldNotAllowAssigningExecutorFromAnotherGroup() {

        GroupEntity groupA = createUserWithGroup();
        int reporterId = groupA.getOwnerId();

        GroupEntity groupB = createUserWithGroup();
        int executorFromOtherGroup = groupB.getOwnerId();

        TaskEntity task = buildTaskEntity(reporterId);
        insertTaskIntoDB(task);

        TaskUpdateRequest updateRequest = buildUpdateTaskRequest()
                .executorIds(Set.of(executorFromOtherGroup))
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("userId", reporterId)
                .body(updateRequest)
                .when()
                .put(GET_TASKS_URI + "/" + task.getTaskId())
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(CREATE_OR_UPDATE_TASK_FOR_OWN_GROUP)))
                .extract()
                .response();

        System.out.println("Attempt to update a task by assigning an executor from another group: ");
        response.prettyPrint();
    }

    private static TaskUpdateRequest.TaskUpdateRequestBuilder buildUpdateTaskRequest() {
        return TaskUpdateRequest.builder()
                .status(TaskStatus.IN_PROGRESS.name())
                .name("updated_task_" + randomString(5))
                .description("updated_desc_" + randomString(10))
                .priority(TaskPriority.HIGH.name())
                .executorIds(Set.of())
                .confidential(true)
                .deadline(LocalDate.now().plusDays(2).toString())
                .rewardsPoints(null);
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
        assertEquals(updateRequest.getRewardsPoints(), response.path("rewardsPoints"));
        assertEquals(reporterId, (Integer) response.path("reporterId"));
        assertEquals(taskId, response.path("taskId"));
    }

}
