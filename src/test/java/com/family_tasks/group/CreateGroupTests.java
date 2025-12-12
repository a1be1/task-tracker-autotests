package com.family_tasks.group;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.group.GroupEntity;
import com.family_tasks.dto.user.UserEntity;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static com.family_tasks.UrlConstant.GROUP_URL;
import static com.family_tasks.ValidationMessage.*;
import static com.family_tasks.utils.TestDataBaseUtils.insertUserIntoDB;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateGroupTests extends AbstractTaskTrackerTest {

    @Test
    public void createGroupTest() {

        UserEntity owner = buildUserEntity(null);
        int ownerId = insertUserIntoDB(owner);

        GroupEntity group = GroupEntity.builder()
                .ownerId(ownerId)
                .build();

        Response response = given()
                .contentType("application/json")
                .body(group)
                .when()
                .post(GROUP_URL)
                .then()
                .statusCode(200)
                .body("ownerId", equalTo(ownerId))
                .body("groupId", notNullValue())
                .extract()
                .response();

        System.out.println("Group created with id " + response.getBody().path("groupId"));
        response.prettyPrint();
    }

    @Test
    public void createGroup_whenUserIsAlreadyOwner_thenBadRequest() {

        GroupEntity group1 = createUserWithGroup();
        int ownerId = group1.getOwnerId();

        GroupEntity group2 = GroupEntity.builder()
                .ownerId(ownerId)
                .build();

        Response response = given()
                .contentType("application/json")
                .body(group2)
                .when()
                .post(GROUP_URL)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(USER_ALREADY_IS_OWNER,ownerId)))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void createGroup_whenUserFromAnotherGroup_thenBadRequest() {

        GroupEntity group1 = createUserWithGroup();
        int groupId = group1.getGroupId();
        int foreignUser = insertUserIntoDB(buildUserEntity(groupId));

        GroupEntity group2 = GroupEntity.builder()
                .ownerId(foreignUser)
                .build();

        Response response = given()
                .contentType("application/json")
                .body(group2)
                .when()
                .post(GROUP_URL)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(USER_ALREADY_HAS_GROUP,foreignUser)))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void createGroup_whenOwnerIdIsNull_thenBadRequest() {

        GroupEntity group2 = GroupEntity.builder()
                .ownerId(null)
                .build();

        Response response = given()
                .contentType("application/json")
                .body(group2)
                .when()
                .post(GROUP_URL)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(GROUP_OWNER_NOT_SPECIFIED)))
                .extract()
                .response();

        response.prettyPrint();
    }

    @Test
    public void createGroup_withInvalidOwnerId_thenBadRequest() {

        UserEntity owner = buildUserEntity(null);
        int ownerId = insertUserIntoDB(owner);

        int invalidOwnerId = ownerId +1;

        GroupEntity group2 = GroupEntity.builder()
                .ownerId(invalidOwnerId)
                .build();

        Response response = given()
                .contentType("application/json")
                .body(group2)
                .when()
                .post(GROUP_URL)
                .then()
                .statusCode(400)
                .body("errorMessage", equalTo(String.format(USER_NOT_EXIST, invalidOwnerId)))
                .extract()
                .response();

        response.prettyPrint();
    }
}
