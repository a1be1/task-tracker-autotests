package com.family_tasks.utils;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskEntity;
import com.family_tasks.dto.group.GroupEntity;
import com.family_tasks.dto.user.UserEntity;

import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class TestDataBaseUtils {

    private static Connection getConnection() throws Exception {
        Properties props = new Properties();
        try (InputStream is = AbstractTaskTrackerTest.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            props.load(is);
            String JDBC_URL = props.getProperty("POSTGRES_URL").replace("db:5432", "localhost:5432");
            String JDBC_USER = props.getProperty("POSTGRES_USER");
            String JDBC_PASS = props.getProperty("POSTGRES_PASSWORD");
            return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
        }
    }

    public static void executeDbQuery(String query) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
        } catch (Exception e) {
            throw new RuntimeException("DB query execution failed: " + query, e);
        }
    }

    public static <T> T query(String sql, ResultSetHandler<T> handler) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return handler.handle(rs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query: " + sql, e);
        }
    }

    public static int insertGroupIntoDB(GroupEntity group) {
        String sql = """
                INSERT INTO groups (owner_id, created_at, updated_at, deleted_at)
                                      VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, group.getOwnerId());
            stmt.setTimestamp(2, Timestamp.valueOf(group.getCreatedAt()));
            stmt.setTimestamp(3, Timestamp.valueOf(group.getUpdatedAt()));
            stmt.setTimestamp(4, Timestamp.valueOf(group.getDeletedAt()));
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    group.setGroupId(id);
                    return id;
                }
            }
            return -1;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert group", e);
        }
    }

    public static int insertUserIntoDB(UserEntity userEntity) {
        String sql = """
                INSERT INTO users (name, admin, group_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, userEntity.getName());
            stmt.setBoolean(2, userEntity.getAdmin());
            stmt.setObject(3, userEntity.getGroupId());
            stmt.setTimestamp(4, Timestamp.valueOf(userEntity.getCreatedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(userEntity.getUpdatedAt()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    userEntity.setId(id);
                    return id;
                }
            }
            return -1;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert user", e);
        }
    }

    public static void updateUserGroupIdInDB(UserEntity user) {
        String sql = "UPDATE users SET group_id = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.getGroupId());
            stmt.setInt(2, user.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user's groupId", e);
        }
    }

    public static void insertTaskIntoDB(TaskEntity taskEntity) {
        String sql = """
                INSERT INTO tasks (id, name, description, priority, status, reporter_id,
                                   confidential, rewards_points, deadline, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, taskEntity.getTaskId());
            stmt.setString(2, taskEntity.getName());
            stmt.setString(3, taskEntity.getDescription());
            stmt.setString(4, taskEntity.getPriority());
            stmt.setString(5, taskEntity.getStatus());
            stmt.setInt(6, taskEntity.getReporterId());
            stmt.setBoolean(7, taskEntity.isConfidential());

            if (taskEntity.getRewardsPoints() == null) {
                stmt.setNull(8, Types.INTEGER);
            } else {
                stmt.setInt(8, taskEntity.getRewardsPoints());
            }
            stmt.setTimestamp(9, Timestamp.valueOf(taskEntity.getDeadline().atStartOfDay()));
            stmt.setTimestamp(10, Timestamp.valueOf(taskEntity.getCreatedAt()));
            stmt.setTimestamp(11, Timestamp.valueOf(taskEntity.getUpdatedAt()));

            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert task", e);
        }
    }

    public static void insertTaskExecutors(String taskId, List<Integer> executorIds) {
        String executorSql = "INSERT INTO executors_tasks (task_id, user_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(executorSql)) {

            for (Integer executorId : executorIds) {
                stmt.setString(1, taskId);
                stmt.setInt(2, executorId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert task executors", e);
        }
    }

    public static String getUserNameFromDB(int userId) {
        String sql = "SELECT name FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                } else {
                    throw new RuntimeException("User not found in DB with id: " + userId);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to get user name from DB", e);
        }
    }

    @FunctionalInterface
    public interface ResultSetHandler<T> {
        T handle(ResultSet resultSet) throws SQLException;
    }
}