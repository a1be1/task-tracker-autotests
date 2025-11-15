package com.family_tasks.user;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.user.User;
import com.family_tasks.utils.TestDataBaseUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static com.family_tasks.UrlConstant.USER_URI;
import static com.family_tasks.ValidationConstants.USER_NAME_MAX_LENGTH;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.utils.TestValuesUtils.randomString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


public class CreateUserTest extends AbstractTaskTrackerTest {

    @AfterAll
    public static void afterAll() {
        TestDataBaseUtils.executeDbQuery("DELETE FROM users;");
    }

    @Test
    void createUser() {
        {
            User user = buildUser()
                    .build();

            given()
                    .contentType("application/json")
                    .body(user)
                    .when()
                    .post(USER_URI)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo(user.getName()))
                    .body("admin", equalTo(user.getAdmin()));
        }
    }

    @Test
    void createUserWhenNameIsNull() {
        {
            User user = buildUser()
                    .name(null)
                    .build();

            given()
                    .contentType("application/json")
                    .body(user)
                    .when()
                    .post(USER_URI)
                    .then()
                    .statusCode(400)
                    .body("errorMessage", equalTo(USER_NAME_NOT_SPECIFIED));
        }
    }

    @Test
    void createUserWhenNameIsEmpty() {

        User user = buildUser()
                .name("")
                .build();

        given()
                .contentType("application/json")
                .body(user)
                .when()
                .post(USER_URI)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(USER_NAME_NOT_SPECIFIED));
    }

    @Test
    void createUserWhenNameIsTooLong() {
        {
            User user = buildUser()
                    .name(randomString(USER_NAME_MAX_LENGTH + 1))
                    .build();

            given()
                    .contentType("application/json")
                    .body(user)
                    .when()
                    .post(USER_URI)
                    .then()
                    .statusCode(400)
                    .body("errorMessage", equalTo(USER_NAME_TOO_LONG));
        }
    }

    @Test
    void createUserWhenIsAdminIsNull() {
        {
            User user = buildUser()
                    .admin(null)
                    .build();

            given()
                    .contentType("application/json")
                    .body(user)
                    .when()
                    .post(USER_URI)
                    .then()
                    .statusCode(400)
                    .body("errorMessage", equalTo(IS_ADMIN_NOT_SPECIFIED));
        }
    }

    private User.UserBuilder buildUser() {
        return User.builder()
                .name(randomString(USER_NAME_MAX_LENGTH))
                .admin(true);
    }
}