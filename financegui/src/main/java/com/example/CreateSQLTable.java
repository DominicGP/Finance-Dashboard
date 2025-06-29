package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// mvn compile
// mvn exec:java -Dexec.mainClass="com.example.CreateSQLTable"

public class CreateSQLTable {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:transactions.db"; // creates this file if it doesn't exist

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                // Create table if it doesn't exist
                String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                        "id INTEGER PRIMARY KEY," +
                        "date TEXT NOT NULL," +
                        "category TEXT NOT NULL," +
                        "description TEXT," +
                        "amount REAL NOT NULL" +
                        ");";

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
                System.out.println("Database connection opened and table created (if needed).");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
