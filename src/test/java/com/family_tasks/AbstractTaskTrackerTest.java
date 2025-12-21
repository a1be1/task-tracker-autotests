package com.family_tasks;

import com.family_tasks.dto.task.TaskCreateRequest;
import com.family_tasks.dto.user.GroupEntity;
import com.family_tasks.dto.user.UserEntity;
import com.family_tasks.enums.TaskPriority;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static com.family_tasks.ValidationConstants.*;
import static com.family_tasks.utils.TestDataBaseUtils.*;
import static com.family_tasks.utils.TestValuesUtils.randomString;

public abstract class AbstractTaskTrackerTest {

    @BeforeAll
    static void setup() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Just triggers loading; base URL is configured elsewhere (RestAssured, etc.)
        dotenv.get("TASK_TRACKER_BASE_URL");
    }

    @AfterEach
    public void clearDB() {
        executeDbQuery("DELETE FROM executors_tasks");
        executeDbQuery("DELETE FROM tasks");
        executeDbQuery("DELETE FROM groups");
        executeDbQuery("DELETE FROM users");
    }

    /**
     * Common user builder for tests.
     */
    protected UserEntity buildUserEntity(Integer groupId) {
        return UserEntity.builder()
                .admin(true)
                .name("user_" + randomString(6))
                .groupId(groupId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Common group builder for tests.
     */
    protected GroupEntity buildGroupEntity(Integer ownerId) {
        return GroupEntity.builder()
                .ownerId(ownerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deletedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates user + group in DB and returns the persisted group entity.
     * GroupEntity has both ownerId and groupId populated.
     */
    protected GroupEntity createUserWithGroup() {
        UserEntity owner = buildUserEntity(null);
        int ownerId = insertUserIntoDB(owner);

        GroupEntity group = buildGroupEntity(ownerId);
        int groupId = insertGroupIntoDB(group);

        owner.setGroupId(groupId);
        updateUserGroupIdInDB(owner);

        // ensure groupId is set (depending on your mapper this may already be true)
        group.setGroupId(groupId);
        return group;
    }

    /**
     * Common builder for TaskCreateRequest used in multiple tests.
     * Static so it can be used from static @MethodSource.
     */
    protected static TaskCreateRequest.TaskCreateRequestBuilder taskCreateRequest(Integer reporterId) {
        return TaskCreateRequest.builder()
                .name(randomString(TASK_NAME_MAX_LENGTH))
                .description(randomString(TASK_DESCRIPTION_MAX_LENGTH))
                .priority(TaskPriority.LOW.name())
                .reporterId(reporterId)
                .executorIds(Set.of())
                .confidential(true)
                .deadline(LocalDate.now().toString());
    }
}
