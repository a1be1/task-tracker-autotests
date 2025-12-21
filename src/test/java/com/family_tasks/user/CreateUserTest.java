package com.family_tasks.user;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.group.GroupEntity;
import com.family_tasks.dto.user.User;
import com.family_tasks.dto.user.UserEntity;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static com.family_tasks.UrlConstant.CREATE_USER_URI;
import static com.family_tasks.ValidationConstants.USER_NAME_MAX_LENGTH;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


public class CreateUserTest extends AbstractTaskTrackerTest {

    @Test
    void createUser() {
        {
            User user = buildUser()
                    .build();

            Response resp = given()
                    .contentType("application/json")
                    .body(user)
                    .when()
                    .post(CREATE_USER_URI)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo(user.getName()))
                    .body("admin", equalTo(user.getAdmin()))
                    .extract().response();

            resp.prettyPrint();
        }
    }

    @Test
    void createUserWhenNameIsNull() {
        {
            User user = buildUser()
                    .name(null)
                    .build();

            Response resp = given()
                    .contentType("application/json")
                    .body(user)
                    .when()
                    .post(CREATE_USER_URI)
                    .then()
                    .statusCode(400)
                    .body("errorMessage", equalTo(USER_NAME_NOT_SPECIFIED))
                    .extract().response();

            resp.prettyPrint();
        }
    }

    @Test
    void createUserWhenNameIsEmpty() {

        User user = buildUser()
                .name("")
                .build();

        Response resp = given()
                .contentType("application/json")
                .body(user)
                .when()
                .post(CREATE_USER_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(USER_NAME_NOT_SPECIFIED))
                .extract().response();

        resp.prettyPrint();
    }

    @Test
    void createUserWhenNameIsTooLong() {
        {
            User user = buildUser()
                    .name(randomString(USER_NAME_MAX_LENGTH + 1))
                    .build();

            Response resp = given()
                    .contentType("application/json")
                    .body(user)
                    .when()
                    .post(CREATE_USER_URI)
                    .then()
                    .statusCode(400)
                    .body("errorMessage", equalTo(USER_NAME_TOO_LONG))
                    .extract().response();

            resp.prettyPrint();
        }
    }

    @Test
    void createUserWhenIsAdminIsNull() {
        {
            User user = buildUser()
                    .admin(null)
                    .build();

            Response resp = given()
                    .contentType("application/json")
                    .body(user)
                    .when()
                    .post(CREATE_USER_URI)
                    .then()
                    .statusCode(400)
                    .body("errorMessage", equalTo(IS_ADMIN_NOT_SPECIFIED))
                    .extract().response();

            resp.prettyPrint();
        }
    }

    @Test
    public void createUser_whenUserRegistersViaLink() {

        GroupEntity group = createUserWithGroup();
        int groupId = group.getGroupId();

        UserEntity user = buildUserEntity(groupId);

        Response resp = given()
                .contentType("application/json")
                .body(user)
                .when()
                .post(CREATE_USER_URI)
                .then()
                .statusCode(200)
                .body("name", equalTo(user.getName()))
                .body("admin", equalTo(user.getAdmin()))
                .body("groupId", equalTo(groupId))
                .extract().response();

        resp.prettyPrint();
    }
}