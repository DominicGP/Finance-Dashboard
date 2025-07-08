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

/*
 * Class that handles any interactions that are done with the database
 * Ex. Adding, deleting, querying, etc.
 */
public class DatabaseManager {
    // Database URL
    private static final String URL = "jdbc:sqlite:transactions.db";

    /**
     * Helper method that establishes a connection to the database
    */
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Helper method that gets the next id for a new transaction
     * 
     * @param conn A connection to the database
     * @return Returns max current id + 1 
    */
    private int getNextId(Connection conn) {
        // SQL query statement
        String sql = "SELECT MAX(id) AS max_id FROM transactions";
        // Try to find the max id
        try (Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            // Will return 0 if no rows
            int maxId = rs.getInt("max_id"); 
            return maxId + 1; // Return id + 1

        } catch (SQLException e) {
            System.out.println("Error getting max ID: " + e.getMessage());
            return 1; // Fallback in case of error
        }
    }

    /**
     * Adds a transaction to the database
     * 
     * @param date The date of the transaction
     * @param category The category of the transaction
     * @param description The description of the transaction
     * @param amount The amount of the transaction
     * @return The created transaction 
    */
    public Transaction addTransaction(LocalDate date, Transaction.Category category, String description,
            BigDecimal amount) {
        // SQL statement
        String sql = "INSERT INTO transactions(id, date, category, description, amount) VALUES (?, ?, ?, ?, ?)";
        //Try to add transaction to database
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int nextId = getNextId(conn);  // Fetch next ID
            pstmt.setInt(1, nextId);
            pstmt.setString(2, date.toString()); // LocalDate → TEXT (ISO-8601, e.g., "2025-06-24")
            pstmt.setString(3, category.name()); // enum → TEXT (e.g., "FOOD")
            pstmt.setString(4, description);
            pstmt.setBigDecimal(5, amount);
            pstmt.executeUpdate();
            return new Transaction(nextId, date, category, description, amount); 
        } catch (SQLException e) {
            System.out.println("Insert error: " + e.getMessage());
            return null;
        }

        
    }
    /**
     * Returns a list of all the transactions in the database
     * Used for showing the default state of the finance GUI
     * 
     * @return A list of all transactions
     */
    public List<Transaction> getTransactions() {
        // Transaction list
        List<Transaction> transactions = new ArrayList<>();
        // SQL statement
        String sql = "SELECT * FROM transactions;";

        // Try to connect to the database and get the transactions
        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            // Loop through the rows and creates transactions to add to the list
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

    /**
     * Used to delete a transaction from the database
     * 
     * @param id ID used to identify the specific transaction
     */
    public void deleteTransaction(int id) {
        // SQL statement
        String sql = "DELETE FROM transactions WHERE id = ?;";
        // Try to connect and run prepared statement
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete error: " + e.getMessage());
        }
    }

    /**
     * Used to filter the transactions by the given user category
     * 
     * @param category User input category used to filter transactions by
     * @return A list of filtered transactions
     */
    public List<Transaction> getSpendingByCategory(Transaction.Category category) {
        // SQL statement
        String sql = "SELECT * FROM transactions WHERE category = ?";
        // Transaction List
        List<Transaction> transactions = new ArrayList<>();

        // Try to connect and run prepared statement
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.name());
            ResultSet rs = pstmt.executeQuery();
            // Loop through the rows and creates transactions to add to the list
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
    /**
     * Filters the transactions between the given start and end date
     * 
     * @param startDate User inputted start date
     * @param endDate User inputted end date
     * @return List of transactions that fall between the two dates
     */
    public List<Transaction> getSpendingOverTime(LocalDate startDate, LocalDate endDate) {
        // SQL statement
        String sql = "SELECT * FROM transactions WHERE date BETWEEN ? AND ?";
        // Transaction List
        List<Transaction> transactions = new ArrayList<>();
        // Try to connect and run prepared statement
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();
            // Loop through the rows and creates transactions to add to the list
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

    /**
     * Gets a list of transaction that involve an amount higher than the given input
     * 
     * @param userAmount User given inputted amount
     * @return A list of all transaction that are greater than the given amount
     */
    public List<Transaction> getTransactionsAbove(BigDecimal userAmount) {
        // SQL statement
        String sql = "SELECT * FROM transactions WHERE amount > ?";
        // Transaction List
        List<Transaction> transactions = new ArrayList<>();
        // Try to connect and run prepared statement
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, userAmount);
            ResultSet rs = pstmt.executeQuery();
            // Loop through the rows and creates transactions to add to the list
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
            System.out.println("Get transaction above reading error: " + e.getMessage());
        }
        return transactions;
    }

    /**
     * Gets a list of transaction that involve an amount lower than the given input
     * 
     * @param userAmount User given inputted amount
     * @return A list of all transaction that are lesser than the given amount
     */
    public List<Transaction> getTransactionsBelow(BigDecimal userAmount) {
        // SQL statement
        String sql = "SELECT * FROM transactions WHERE amount < ?";
        // Transaction List
        List<Transaction> transactions = new ArrayList<>();
        // Try to connect and run prepared statement
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, userAmount);
            ResultSet rs = pstmt.executeQuery();
            // Loop through the rows and creates transactions to add to the list
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
            System.out.println("Get transaction below reading error: " + e.getMessage());
        }
        return transactions;
    }

}
