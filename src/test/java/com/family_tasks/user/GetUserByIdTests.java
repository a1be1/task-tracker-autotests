package com.family_tasks.user;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.group.GroupEntity;
import com.family_tasks.dto.user.UserEntity;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.family_tasks.UrlConstant.GET_USER_URI;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.utils.TestDataBaseUtils.insertUserIntoDB;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetUserByIdTests extends AbstractTaskTrackerTest {

    @Test
    public void getUserById() {

        UserEntity user = buildUserEntity(null);
        insertUserIntoDB(user);

        int userId = user.getId();

        Response resp = given()
                .contentType("application/json")
                .when()
                .get(GET_USER_URI + "/" + userId)
                .then()
                .statusCode(200)
                .body("name", equalTo(user.getName()))
                .body("admin", equalTo(user.getAdmin()))
                .extract().response();

        resp.prettyPrint();
    }

    @Test
    public void getUser_whenUserRegistersViaLink() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();

        UserEntity user = buildUserEntity(groupId);
        insertUserIntoDB(user);

        int userId = user.getId();

        Response resp = given()
                .contentType("application/json")
                .when()
                .get(GET_USER_URI + "/" + userId)
                .then()
                .statusCode(200)
                .body("name", equalTo(user.getName()))
                .body("admin", equalTo(user.getAdmin()))
                .body("groupId", equalTo(groupId))
                .extract().response();

        resp.prettyPrint();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void getUser_whenUserIdDoesBoundaryValue_thenReturns404(int userId) {

        given()
                .when()
                .get(GET_USER_URI + "/" + userId)
                .then()
                .statusCode(404)
                .body("errorMessage", equalTo(String.format(USER_NOT_EXIST, userId)));
    }

    @Test
    public void getUser_whenUserIdDoesNotExist_thenReturns404() {

        int nonExistUserId = Integer.MAX_VALUE;

        Response resp = given()
                .contentType("application/json")
                .when()
                .get(GET_USER_URI + "/" + nonExistUserId)
                .then()
                .statusCode(404)
                .body("errorMessage", equalTo(String.format(USER_NOT_EXIST, nonExistUserId)))
                .extract().response();

        resp.prettyPrint();
    }

    @Test
    public void getUser_whenUserIdIsBlank_thenResourceNotFound() {

        Response resp = given()
                .when()
                .get(GET_USER_URI + "/")
                .then()
                .statusCode(404)
                .extract().response();

        resp.prettyPrint();
    }


    @ParameterizedTest(name = "Invalid userId format: \"{0}\"")
    @MethodSource("invalidUserIdProvider")
    public void getUser_whenUserIdHasInvalidFormat_thenReturns400(String invalidUserId) {

        Response resp = given()
                .when()
                .get(GET_USER_URI + "/" + invalidUserId)
                .then()
                .statusCode(400)
                .body("errorMessage",
                        equalTo(String.format(ID_HAS_INVALID_FORMAT, invalidUserId)))
                .extract().response();

        resp.prettyPrint();
    }

    private static Stream<Arguments> invalidUserIdProvider() {
        return Stream.of(
                Arguments.of("null"),
                Arguments.of("abc"),
                Arguments.of("12abc"),
                Arguments.of("!@#"),
                Arguments.of("0x12")
        );
    }

    @ParameterizedTest
    @MethodSource("sqlInjectionUserIds")
    void getUser_whenUserIdIsSqlInjection_thenReturns400or404(String userId) {

        Response resp = given()
                .pathParam("userId", userId)
                .when()
                .get(GET_USER_URI + "/{userId}")
                .then()
                .statusCode(anyOf(is(400), is(404)))
                .body(not(hasKey("stackTrace")))
                .body(not(hasKey("exception")))
                .extract().response();

        resp.prettyPrint();

    }

    static Stream<String> sqlInjectionUserIds() {
        return Stream.of(
                "1 OR 1=1",
                "1; DROP TABLE users",
                "' OR '1'='1",
                "\" OR \"1\"=\"1",
                "1--",
                "1/*"
        );
    }
}
