package com.family_tasks;

import com.family_tasks.dto.task.TaskEntity;
import com.family_tasks.dto.group.GroupEntity;
import com.family_tasks.dto.user.User;
import com.family_tasks.dto.user.UserEntity;
import com.family_tasks.enums.TaskPriority;
import com.family_tasks.enums.TaskStatus;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.family_tasks.ValidationConstants.USER_NAME_MAX_LENGTH;
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestValuesUtils.randomString;

public abstract class AbstractTaskTrackerTest {

    @BeforeAll
    static void setup() {

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.get("TASK_TRACKER_BASE_URL");
    }

    @AfterAll
    public static void clearDB() {
        executeDbQuery("DELETE FROM executors_tasks");
        executeDbQuery("DELETE FROM rewards");
        executeDbQuery("DELETE FROM tasks");
        executeDbQuery("DELETE FROM groups");
        executeDbQuery("DELETE FROM users");
    }

    protected GroupEntity buildGroupEntity(Integer ownerId) {
        return GroupEntity.builder()
                .ownerId(ownerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
    }

    protected User.UserBuilder buildUser() {
        return User.builder()
                .name(randomString(USER_NAME_MAX_LENGTH))
                .admin(true);
    }

    protected UserEntity buildUserEntity(Integer groupId) {
        return UserEntity.builder()
                .admin(true)
                .name("user_" + randomString(6))
                .groupId(groupId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    protected GroupEntity createUserWithGroup() {

        UserEntity owner = buildUserEntity(null);
        int ownerId = insertUserIntoDB(owner);

        GroupEntity group = buildGroupEntity(ownerId);
        int groupId = insertGroupIntoDB(group);

        owner.setGroupId(groupId);
        updateUserGroupIdInDB(owner);
        return group;
    }

    protected TaskEntity buildTaskEntity(Integer userId) {
        return TaskEntity.builder()
                .taskId(UUID.randomUUID().toString())
                .name("task_" + randomString(5))
                .description("desc_" + randomString(10))
                .reporterId(userId)
                .priority(TaskPriority.LOW.name())
                .status(TaskStatus.TO_DO.name())
                .confidential(false)
                .rewardsPoints(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deadline(LocalDate.now().plusDays(7))
                .build();
    }

}