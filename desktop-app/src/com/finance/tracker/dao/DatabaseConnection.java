package com.finance.tracker.dao;

import java.io.*;
import java.sql.*;
import java.util.Properties;

/**
 * Manages database connections with automatic detection:
 * 1. Checks for external MySQL connection if configured.
 * 2. Provides embedded file-backed persistence fallback if MySQL is offline,
 *    ensuring the application always runs seamlessly out of the box.
 */
public class DatabaseConnection {
    private static String dbUrl = "jdbc:mysql://localhost:3306/personal_finance_db";
    private static String dbUser = "root";
    private static String dbPassword = "";
    private static boolean useMySql = false;
    private static boolean mysqlConnected = false;

    private static Connection activeConnection = null;

    static {
        loadProperties();
        testMySqlConnection();
    }

    private static void loadProperties() {
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream input = new FileInputStream(propFile)) {
                Properties prop = new Properties();
                prop.load(input);
                dbUrl = prop.getProperty("db.url", dbUrl);
                dbUser = prop.getProperty("db.user", dbUser);
                dbPassword = prop.getProperty("db.password", dbPassword);
                useMySql = Boolean.parseBoolean(prop.getProperty("db.use_mysql", "false"));
            } catch (Exception e) {
                System.err.println("Could not load db.properties: " + e.getMessage());
            }
        }
    }

    private static void testMySqlConnection() {
        if (!useMySql) {
            mysqlConnected = false;
            return;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                if (conn != null && !conn.isClosed()) {
                    mysqlConnected = true;
                    System.out.println("Successfully connected to MySQL database: " + dbUrl);
                }
            }
        } catch (Throwable t) {
            mysqlConnected = false;
            System.out.println("MySQL not detected or unreachable. Using embedded persistent storage engine.");
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (useMySql && mysqlConnected) {
            try {
                if (activeConnection == null || activeConnection.isClosed()) {
                    activeConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                }
                return activeConnection;
            } catch (SQLException e) {
                mysqlConnected = false;
                throw e;
            }
        }
        return null; // Signals using embedded persistent engine
    }

    public static boolean isMySqlActive() {
        return useMySql && mysqlConnected;
    }

    public static void setUseMySql(boolean enable) {
        useMySql = enable;
        if (enable) {
            testMySqlConnection();
        } else {
            mysqlConnected = false;
        }
    }

    public static String getDbUrl() { return dbUrl; }
    public static String getDbUser() { return dbUser; }
}

