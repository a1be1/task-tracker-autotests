package com.family_tasks.task;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskEntity;
import com.family_tasks.dto.task.TaskUpdateRequest;
import com.family_tasks.dto.user.GroupEntity;
import com.family_tasks.enums.TaskPriority;
import com.family_tasks.enums.TaskStatus;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static com.family_tasks.UrlConstant.GET_TASKS_URI;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.task.GetTaskTests.*;
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

        TaskUpdateRequest updateRequest = buildUpdateTaskRequest(Set.of(executorId, reporterId));

        Response response = given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .body("taskId", equalTo(taskId))
                .body("name", equalTo(updateRequest.getName()))
                .body("status", equalTo(updateRequest.getStatus()))
                .body("priority", equalTo(updateRequest.getPriority()))
                .body("executorIds", containsInAnyOrder(executorId, reporterId))
                .body("description", equalTo(updateRequest.getDescription()))
                .body("deadline", equalTo(updateRequest.getDeadline()))
                .extract()
                .response();

        Set<Integer> expectedExecutors = Set.of(executorId, reporterId);

        assertTaskUpdatedCorrectly(response, taskToUpdate, reporterId, expectedExecutors);

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

        TaskUpdateRequest updateRequest = buildUpdateTaskRequest(Set.of(executorId, reporterId));

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("userId", reporterId)
                .body(updateRequest)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .body("taskId", equalTo(taskId))
                .body("name", equalTo(updateRequest.getName()))
                .body("status", equalTo(updateRequest.getStatus()))
                .body("priority", equalTo(updateRequest.getPriority()))
                .body("executorIds", containsInAnyOrder(executorId, reporterId))
                .body("description", equalTo(updateRequest.getDescription()))
                .body("deadline", equalTo(updateRequest.getDeadline()))
                .extract()
                .response();

        Set<Integer> expectedExecutors = Set.of(executorId, reporterId);

        assertTaskUpdatedCorrectly(response, taskToUpdate, reporterId, expectedExecutors);

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

        TaskUpdateRequest updateRequest = buildUpdateTaskRequest(Set.of(executorId, reporterId));

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("userId", reporterId)
                .body(updateRequest)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .body("taskId", equalTo(taskId))
                .body("name", equalTo(updateRequest.getName()))
                .body("status", equalTo(updateRequest.getStatus()))
                .body("priority", equalTo(updateRequest.getPriority()))
                .body("executorIds", containsInAnyOrder(executorId, reporterId))
                .body("description", equalTo(updateRequest.getDescription()))
                .body("deadline", equalTo(updateRequest.getDeadline()))
                .extract()
                .response();

        Set<Integer> expectedExecutors = Set.of(executorId, reporterId);

        assertTaskUpdatedCorrectly(response, taskToUpdate, reporterId, expectedExecutors);

        System.out.println("Update all fields test when task has status COMPLETED: ");
        response.prettyPrint();
    }

    @Test
    public void updateAllTaskFields_sendRequestTwice() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();
        int executorId = insertUserIntoDB(buildUserEntity(groupId));

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskToUpdate);

        String taskId = taskToUpdate.getTaskId();

        TaskUpdateRequest updateRequest = buildUpdateTaskRequest(Set.of(executorId));

        Response response1 = sendAndVerifyUpdateTask(reporterId, taskId, updateRequest, Set.of(executorId));
        assertTaskUpdatedCorrectly2(response1, taskToUpdate, updateRequest, reporterId);

        Response response2 = sendAndVerifyUpdateTask(reporterId, taskId, updateRequest, Set.of(executorId));
        assertTaskUpdatedCorrectly2(response2, taskToUpdate, updateRequest, reporterId);

        System.out.println("Send the same update request twice: ");
        response1.prettyPrint();
        response2.prettyPrint();
    }

    @Test
    public void updateAllTaskFields_whenTaskNotExist() {
        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();
        int executorId = insertUserIntoDB(buildUserEntity(groupId));

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskToUpdate);

        String nonExistentTask = taskToUpdate.getTaskId()+2;

        TaskUpdateRequest updateRequest = buildUpdateTaskRequest(Set.of(executorId));

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

    @ParameterizedTest(name = "{1}")
    @CsvFileSource(resources = "/missing_required_fields.csv", numLinesToSkip = 1, delimiter = ';')
    public void updateTask_withMissingRequiredField_returnsBadRequest(
            String fieldToRemove, String caseDescription, String expectedError) {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int reporterId = group.getOwnerId();
        int executorId = insertUserIntoDB(buildUserEntity(groupId));

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskToUpdate);

        String taskId = taskToUpdate.getTaskId();
        TaskUpdateRequest updateRequest = buildUpdateTaskRequest(Set.of(executorId));

        switch (fieldToRemove) {
            case "name" -> updateRequest.setName(null);
            case "status" -> updateRequest.setStatus(null);
            case "priority" -> updateRequest.setPriority(null);
            case "confidential" -> updateRequest.setConfidential(null);
        }

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("userId", reporterId)
                .body(updateRequest)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(expectedError))
                .extract()
                .response();

        System.out.printf("Case: %s%n", caseDescription);
        response.prettyPrint();
    }

    @ParameterizedTest(name = "{1}")
    @CsvFileSource(resources = "/invalid_update_requests.csv", numLinesToSkip = 1,delimiter = ';')
    public void updateTask_withWrongDataType_returnsBadRequest(String invalidJson, String caseDescription) {

        GroupEntity group = createUserWithGroup();
        int reporterId = group.getOwnerId();

        TaskEntity taskToUpdate = buildTaskEntity(reporterId);
        insertTaskIntoDB(taskToUpdate);

        String taskId = taskToUpdate.getTaskId();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(INCORRECT_REQUEST_FORMAT)))
                .extract()
                .response();

        System.out.printf("Case: %s%nInvalid JSON:%n%s%n", caseDescription, invalidJson);
        response.prettyPrint();
    }

    @AfterAll
    public static void clearDB() {
        executeDbQuery("DELETE FROM executors_tasks");
        executeDbQuery("DELETE FROM tasks");
        executeDbQuery("DELETE FROM groups");
        executeDbQuery("DELETE FROM users");
    }

    private TaskUpdateRequest buildUpdateTaskRequest(Set<Integer> executorIds) {
        return TaskUpdateRequest.builder()
                .status(TaskStatus.IN_PROGRESS.name())
                .name("updated_task_" + randomString(5))
                .description("updated_desc_" + randomString(10))
                .priority(TaskPriority.HIGH.name())
                .executorIds(executorIds)
                .confidential(true)
                .deadline(LocalDate.now().plusDays(2).toString())
                .build();
    }

    private Response sendAndVerifyUpdateTask(
            int userId,
            String taskId,
            TaskUpdateRequest updateRequest,
            Set<Integer> expectedExecutorIds
    ) {
        return given()
                .contentType(ContentType.JSON)
                .queryParam("userId", userId)
                .body(updateRequest)
                .when()
                .put(GET_TASKS_URI + "/" + taskId)
                .then()
                .statusCode(200)
                .body("taskId", equalTo(taskId))
                .body("name", equalTo(updateRequest.getName()))
                .body("status", equalTo(updateRequest.getStatus()))
                .body("priority", equalTo(updateRequest.getPriority()))
                .body("executorIds", containsInAnyOrder(expectedExecutorIds.toArray()))
                .body("description", equalTo(updateRequest.getDescription()))
                .body("deadline", equalTo(updateRequest.getDeadline()))
                .extract()
                .response();
    }

    private void assertTaskUpdatedCorrectly(
            Response response,
            TaskEntity originalTask,
            int reporterId,
            Set<Integer> expectedExecutors
    ) {
        String actualName = response.path("name");
        String actualStatus = response.path("status");
        String actualPriority = response.path("priority");
        String actualDeadline = response.path("deadline");
        Integer actualReporterId = response.path("reporterId");
        String actualTaskId = response.path("taskId");
        Set<Integer> actualExecutors = new HashSet<>(response.path("executorIds"));

        assertNotEquals(originalTask.getName(), actualName, "Name should be updated");
        assertNotEquals(originalTask.getStatus(), actualStatus, "Status should be updated");
        assertNotEquals(originalTask.getPriority(), actualPriority, "Priority should be updated");
        assertNotEquals(originalTask.getDeadline(), actualDeadline, "Deadline should match request payload");
        assertEquals(reporterId, actualReporterId, "Reporter ID should remain unchanged");
        assertEquals(originalTask.getTaskId(), actualTaskId, "TaskId should remain unchanged");
        assertEquals(expectedExecutors, actualExecutors, "Executors should match expected set");
    }

    private void assertTaskUpdatedCorrectly2(
            Response response,
            TaskEntity originalTask,
            TaskUpdateRequest updateRequest,
            int reporterId
    ) {
        String actualName = response.path("name");
        String actualStatus = response.path("status");
        String actualPriority = response.path("priority");
        String actualDeadline = response.path("deadline");
        Integer actualReporterId = response.path("reporterId");
        String actualTaskId = response.path("taskId");
        Set<Integer> actualExecutors = new HashSet<>(response.path("executorIds"));

        assertNotEquals(originalTask.getName(), actualName, "Name should be updated");
        assertEquals(updateRequest.getName(), actualName, "Name should match updateRequest");

        assertNotEquals(originalTask.getStatus(), actualStatus, "Status should be updated");
        assertEquals(updateRequest.getStatus(), actualStatus, "Status should match updateRequest");

        assertNotEquals(originalTask.getPriority(), actualPriority, "Priority should be updated");
        assertEquals(updateRequest.getPriority(), actualPriority, "Priority should match updateRequest");

        assertNotEquals(originalTask.getDeadline(), actualDeadline, "Deadline should be updated");
        assertEquals(updateRequest.getDeadline(), actualDeadline, "Deadline should match updateRequest");

        assertEquals(reporterId, actualReporterId, "Reporter ID should remain unchanged");
        assertEquals(originalTask.getTaskId(), actualTaskId, "TaskId should remain unchanged");

        Set<Integer> expectedExecutors = updateRequest.getExecutorIds();
        assertEquals(expectedExecutors, actualExecutors, "Executors should match updateRequest");
    }
}
