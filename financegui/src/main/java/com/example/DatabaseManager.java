package com.example;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:transactions.db";

    // Helper method to get a new connection
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private int getNextId(Connection conn) {
        String sql = "SELECT MAX(id) AS max_id FROM transactions";
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            int maxId = rs.getInt("max_id");  // will return 0 if no rows
            return maxId + 1;

        } catch (SQLException e) {
            System.out.println("Error getting max ID: " + e.getMessage());
            return 1; // fallback in case of error
        }
    }

    public Transaction addTransaction(LocalDate date, Transaction.Category category, String description,
            BigDecimal amount) {
        String sql = "INSERT INTO transactions(id, date, category, description, amount) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int nextId = getNextId(conn);  // fetch next ID
            pstmt.setInt(1, nextId);
            pstmt.setString(2, date.toString()); // LocalDate → TEXT (ISO-8601, e.g., "2025-06-24")
            pstmt.setString(3, category.name()); // enum → TEXT (e.g., "FOOD")
            pstmt.setString(4, description);
            pstmt.setBigDecimal(5, amount); // BigDecimal → REAL in SQLite
            pstmt.executeUpdate();
            return new Transaction(nextId, date, category, description, amount); 
        } catch (SQLException e) {
            System.out.println("Insert error: " + e.getMessage());
            return null;
        }

        
    }

    public List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions;";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                Transaction.Category category = Transaction.Category.valueOf(rs.getString("category"));
                String description = rs.getString("description");
                BigDecimal amount = rs.getBigDecimal("amount");

                Transaction transaction = new Transaction(id, date, category, description, amount);
                transactions.add(transaction);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching transactions: " + e.getMessage());
        }

        return transactions;

    }

    public void deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?;";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete error: " + e.getMessage());
        }
    }

    public List<Transaction> getSpendingByCategory(Transaction.Category category) {
        String sql = "SELECT * FROM transactions WHERE category = ?";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                Transaction.Category rowCategory = Transaction.Category.valueOf(rs.getString("category"));
                String description = rs.getString("description");
                BigDecimal amount = rs.getBigDecimal("amount");

                Transaction transaction = new Transaction(id, date, rowCategory, description, amount);
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.out.println("Get spending by category reading error: " + e.getMessage());
        }

        return transactions;

    }

    public List<Transaction> getSpendingOverTime(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM transactions WHERE date BETWEEN ? AND ?";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                Transaction.Category rowCategory = Transaction.Category.valueOf(rs.getString("category"));
                String description = rs.getString("description");
                BigDecimal amount = rs.getBigDecimal("amount");

                Transaction transaction = new Transaction(id, date, rowCategory, description, amount);
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.out.println("Get spending over time reading error: " + e.getMessage());
        }

        return transactions;
    }

}
