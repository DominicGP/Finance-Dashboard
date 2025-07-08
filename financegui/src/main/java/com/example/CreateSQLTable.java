package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// mvn compile
// mvn exec:java -Dexec.mainClass="com.example.CreateSQLTable"

/**
 * Creates an SQL table that is used for the finance dashboard
 */
public class CreateSQLTable {
        /**
         * Entry point for this program
         * @param args Command-line arguments (not used in this program)
         */
        public static void main(String[] args) {
        // Creates the database file if it doesn't exist
        String url = "jdbc:sqlite:transactions.db"; 
        // Tries to connect to database
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                // SQL statement to create the transactions table if it doesn't exist
                String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                        "id INTEGER PRIMARY KEY," +
                        "date TEXT NOT NULL," +
                        "category TEXT NOT NULL," +
                        "description TEXT," +
                        "amount REAL NOT NULL" +
                        ");";
                // Attempts to create table
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
