package com.family_tasks.user;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.group.GroupEntity;
import com.family_tasks.dto.user.UserEntity;
import com.family_tasks.dto.user.UserUpdateRequest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.family_tasks.UrlConstant.GET_USER_URI;
import static com.family_tasks.ValidationConstants.USER_NAME_MAX_LENGTH;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestValuesUtils.randomInt;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UpdateUserTests extends AbstractTaskTrackerTest {

    @Test
    public void updateUser_whenUserHasNoGroup() {

        UserEntity user = buildUserEntity(null);
        insertUserIntoDB(user);
        int userId = user.getId();

        UserUpdateRequest updateRequest = buildUpdateUserRequest(null).build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put(GET_USER_URI + "/" + userId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(userId))
                .body("name", equalTo(updateRequest.getName()))
                .body("admin", equalTo(updateRequest.getAdmin()))
                .body("groupId", nullValue())
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void updateUser_whenUserIsOwner() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();
        int ownerId = group.getOwnerId();

        UserUpdateRequest updateRequest = buildUpdateUserRequest(groupId).build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put(GET_USER_URI + "/" + ownerId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(ownerId))
                .body("name", equalTo(updateRequest.getName()))
                .body("admin", equalTo(updateRequest.getAdmin()))
                .body("groupId", notNullValue())
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void updateUser_whenUserIsAddedIntoGroup() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();

        int newUserId = insertUserIntoDB(buildUserEntity(groupId));

        UserUpdateRequest updateRequest = buildUpdateUserRequest(groupId).build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put(GET_USER_URI + "/" + newUserId)
                .then()
                .statusCode(200)
                .body("userId", equalTo(newUserId))
                .body("name", equalTo(updateRequest.getName()))
                .body("admin", equalTo(updateRequest.getAdmin()))
                .body("groupId", equalTo(groupId))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void attemptToUpdateUser_whenUserDoesNotExist_thenReturn400() {

        int nonExistentUserId = randomInt();

        UserUpdateRequest updateRequest = buildUpdateUserRequest(null).build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put(GET_USER_URI + "/" + nonExistentUserId)
                .then()
                .statusCode(404)
                .body("errorMessage", equalTo(String.format(USER_NOT_EXIST, nonExistentUserId)))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void attemptToUpdateUser_whenUserNameTooLong_thenReturn400() {

        GroupEntity group = createUserWithGroup();
        int userId = group.getOwnerId();

        UserUpdateRequest updateRequest = buildUpdateUserRequest(null)
                .name(randomString(USER_NAME_MAX_LENGTH + 1))
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put(GET_USER_URI + "/" + userId)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(USER_NAME_TOO_LONG)))
                .extract()
                .response();

        response.prettyPrint();
    }

    @ParameterizedTest(name = "Invalid userId format: \"{0}\"")
    @MethodSource("invalidUserIdProvider")
    public void attemptToUpdateUser_withInvalidUserId_thenReturn400(String invalidUserId) {

        buildUpdateUserRequest(null).build();

        Response resp = given()
                .when()
                .put(GET_USER_URI + "/" + invalidUserId)
                .then()
                .statusCode(400)
                .body("errorMessage",
                        equalTo(String.format(INCORRECT_REQUEST_FORMAT, invalidUserId)))
                .extract().response();

        resp.prettyPrint();
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("missingRequiredField")
    public void attemptToUpdateUser_withMissingRequiredField_returns400(String description, UserUpdateRequest request, String expectedError) {

        UserEntity user = buildUserEntity(null);
        insertUserIntoDB(user);
        int userId = user.getId();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put(GET_USER_URI + "/" + userId)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(expectedError))
                .extract()
                .response();

        response.prettyPrint();
    }

    private static Stream<Arguments> missingRequiredField() {
        return Stream.of(
                Arguments.of(
                        "Missing user name",
                        buildUpdateUserRequest(null).name(null).build(),
                        USER_NAME_NOT_SPECIFIED
                ),
                Arguments.of(
                        "Missing user name",
                        buildUpdateUserRequest(null).name("").build(),
                        USER_NAME_NOT_SPECIFIED
                ),
                Arguments.of(
                        "Missing status",
                        buildUpdateUserRequest(null).admin(null).build(),
                        IS_ADMIN_NOT_SPECIFIED
                )
        );
    }

    @Test
    public void attemptToUpdateUser_whenGroupNotExist_thenReturn400() {

        int groupId = randomInt();

        UserEntity user = buildUserEntity(null);
        insertUserIntoDB(user);
        int userId = user.getId();

        UserUpdateRequest updateRequest = buildUpdateUserRequest(groupId).build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put(GET_USER_URI + "/" + userId)
                .then()
                .statusCode(400)
                .body("errorMessage",
                        equalTo(String.format(GROUP_NOT_EXIST, groupId)))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void attemptToUpdateUser_whenRequestBodyIsMissing_thenReturn400() {

        UserEntity user = buildUserEntity(null);
        insertUserIntoDB(user);
        int userId = user.getId();

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .put(GET_USER_URI + "/" + userId)
                .then()
                .statusCode(400)
                .body("errorMessage",
                        equalTo(String.format(INCORRECT_REQUEST_FORMAT, userId)))
                .extract()
                .response();

        response.prettyPrint();
    }

    private static UserUpdateRequest.UserUpdateRequestBuilder buildUpdateUserRequest(Integer groupId) {
        return UserUpdateRequest.builder()
                .groupId(groupId)
                .name("updated_user_" + randomString(5))
                .admin(false);
    }
}
