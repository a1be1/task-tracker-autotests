package com.family_tasks.utils;

import com.family_tasks.AbstractTaskTrackerTest;
import com.family_tasks.dto.task.TaskEntity;
import com.family_tasks.dto.user.User;
import com.family_tasks.dto.user.UserEntity;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

//TODO make it more readable
public class TestDataBaseUtils {

    public static void executeDbQuery(String query) {
        Properties props = new Properties();
        Connection conn = null;
        try (InputStream is = AbstractTaskTrackerTest.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(is);
            String JDBC_URL = props.getProperty("POSTGRES_URL");
            String JDBC_USER = props.getProperty("POSTGRES_USER");
            String JDBC_PASS = props.getProperty("POSTGRES_PASSWORD");

            conn = DriverManager.getConnection(JDBC_URL.replace("db:5432", "localhost:5432"), JDBC_USER, JDBC_PASS);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static ResultSet getQueryResult(String query) {
        Properties props = new Properties();
        try (InputStream is = AbstractTaskTrackerTest.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(is);
            String JDBC_URL = props.getProperty("POSTGRES_URL").replace("db:5432", "localhost:5432");
            String JDBC_USER = props.getProperty("POSTGRES_USER");
            String JDBC_PASS = props.getProperty("POSTGRES_PASSWORD");

            Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return stmt.executeQuery(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int insertUserIntoDB(UserEntity userEntity) {
        Properties props = new Properties();
        Connection conn = null;
        int generatedID = -1;
        try (InputStream is = AbstractTaskTrackerTest.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(is);
            String JDBC_URL = props.getProperty("POSTGRES_URL");
            String JDBC_USER = props.getProperty("POSTGRES_USER");
            String JDBC_PASS = props.getProperty("POSTGRES_PASSWORD");

            conn = DriverManager.getConnection(JDBC_URL.replace("db:5432", "localhost:5432"), JDBC_USER, JDBC_PASS);
            String query = "INSERT INTO users(name, admin, created_at, updated_at) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, userEntity.getName());
            stmt.setBoolean(2, userEntity.getAdmin());
            stmt.setTimestamp(3, Timestamp.valueOf(userEntity.getCreatedAt()));
            stmt.setTimestamp(4, Timestamp.valueOf(userEntity.getUpdatedAt()));

            stmt.executeUpdate();

            ResultSet cursor = stmt.getGeneratedKeys();
            cursor.next();
            generatedID = cursor.getInt(1);
            userEntity.builder().id(generatedID).build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return generatedID;
    }

    public static void insertTaskIntoDB(TaskEntity taskEntity) {
        Properties props = new Properties();
        Connection conn = null;
        try (InputStream is = AbstractTaskTrackerTest.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(is);
            String JDBC_URL = props.getProperty("POSTGRES_URL");
            String JDBC_USER = props.getProperty("POSTGRES_USER");
            String JDBC_PASS = props.getProperty("POSTGRES_PASSWORD");

            conn = DriverManager.getConnection(JDBC_URL.replace("db:5432", "localhost:5432"), JDBC_USER, JDBC_PASS);
            String query = """
                    INSERT INTO tasks (id, name, description, priority, status, reporter_id,
                    confidential, deadline, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, taskEntity.getTaskId());
            stmt.setString(2, taskEntity.getName());
            stmt.setString(3, taskEntity.getDescription());
            stmt.setString(4, taskEntity.getPriority());
            stmt.setString(5, taskEntity.getStatus());
            stmt.setInt(6, taskEntity.getReporterId());
            stmt.setBoolean(7, taskEntity.isConfidential());
            stmt.setTimestamp(8, Timestamp.valueOf(taskEntity.getDeadline().atStartOfDay()));
            stmt.setTimestamp(9, Timestamp.valueOf(taskEntity.getCreatedAt()));
            stmt.setTimestamp(10, Timestamp.valueOf(taskEntity.getUpdatedAt()));

            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}