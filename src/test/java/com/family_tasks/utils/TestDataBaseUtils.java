package com.family_tasks.utils;

import com.family_tasks.AbstractTaskTrackerTest;

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
}