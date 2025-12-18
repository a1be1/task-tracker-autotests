package com.family_tasks.user;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.group.GroupEntity;
import com.family_tasks.dto.user.UserEntity;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.family_tasks.UrlConstant.GET_USER_URI;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestValuesUtils.randomInt;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.equalTo;

public class GetAllUsersTests extends AbstractTaskTrackerTest {

    @Test
    public void getAllUsers_whenGroupHasOnlyOwner() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();

        int ownerId = group.getOwnerId();
        String ownerName = getUserNameFromDB(ownerId);

        Response response = given()
                .queryParam("groupId", groupId)
                .when()
                .get(GET_USER_URI)
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("find { it.userId == %s }.groupId", withArgs(ownerId), equalTo(groupId))
                .body("find { it.userId == %s }.userId", withArgs(ownerId), equalTo(ownerId))
                .body("find { it.userId == %s }.name", withArgs(ownerId), equalTo(ownerName))
                .body("find { it.userId == %s }.admin", withArgs(ownerId), equalTo(true))
                .extract().response();

        response.prettyPrint();
    }

    @Test
    public void getAllUsers_whenGroupHasMultipleUsers() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();

        int additionalUsersCount = randomInt(1, 5);
        List<UserEntity> users = createAndInsertUsersForGroup(groupId, additionalUsersCount);

        Response response = given()
                .queryParam("groupId", groupId)
                .when()
                .get(GET_USER_URI)
                .then()
                .statusCode(200)
                .extract().response();

        response.prettyPrint();

        for (UserEntity user : users) {
            response.then()
                    .body("find { it.userId == %s }.groupId", withArgs(user.getId()), equalTo(groupId))
                    .body("find { it.userId == %s }.userId", withArgs(user.getId()), equalTo(user.getId()))
                    .body("find { it.userId == %s }.name", withArgs(user.getId()), equalTo(user.getName()))
                    .body("find { it.userId == %s }.admin", withArgs(user.getId()), equalTo(true));
        }

        int expectedSize = users.size() + 1; // users + owner
        response.then().body("size()", equalTo(expectedSize));
    }

    @Test
    public void getAllUsers_whenMisingGroupId_thenReturns400() {

        Response response = given()
                .when()
                .get(GET_USER_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(GROUP_NOT_SPECIFIED)))
                .extract().response();

        response.prettyPrint();
    }

    @Test
    public void getAllUsers_whenGroupIdDoesNotExist_thenReturns400() {

        int nonExistentGroupId = randomInt();

        Response response = given()
                .queryParam("groupId", nonExistentGroupId)
                .when()
                .get(GET_USER_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(GROUP_NOT_EXIST, nonExistentGroupId)))
                .extract().response();

        response.prettyPrint();
    }

    @ParameterizedTest(name = "Invalid groupId format: \"{0}\"")
    @MethodSource("invalidGroupIdProvider")
    public void getAllUsers_whenGroupIdHasInvalidFormat_thenReturns400(String invalidGroupId) {
        Response resp = given()
                .queryParam("groupId", invalidGroupId)
                .when()
                .get(GET_USER_URI)
                .then()
                .statusCode(400)
                .body("errorMessage",
                        equalTo(String.format(ID_HAS_INVALID_FORMAT)))
                .extract().response();

        resp.prettyPrint();
    }

    private static Stream<Arguments> invalidGroupIdProvider() {
        return Stream.of(
                Arguments.of("null"),
                Arguments.of("abc"),
                Arguments.of("12abc"),
                Arguments.of("!@#"),
                Arguments.of("0x12")
        );
    }
}
