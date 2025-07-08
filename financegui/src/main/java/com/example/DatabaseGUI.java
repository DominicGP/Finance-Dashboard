package com.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// mvn javafx:run

/**
 * JavaFX application for viewing and managing financial transactions
 */
public class DatabaseGUI extends Application {

    /**
     * Creates the main GUI window
     * 
     * @param primaryStage Layout of the window/elements
    */
    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(buildMainLayout(), 1000, 800);
        primaryStage.setTitle("Transaction Manager");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    /**
     * Builds the main layout for the gui window
     * 
     * @return A final VBox of the gui elements
     * 
    */
    private VBox buildMainLayout() {

        // Builds transation table
        TableView<Transaction> table = buildTransactionTable();

        // Builds table entry section and filtering section
        GridPane tableEntry = buildTableEntry(table);
        HBox filteringEntry = buildFilterEntry(table);

        //Combines above elements into one section
        VBox layout = new VBox(15, table, tableEntry, filteringEntry);
        // Padding of 10 on all sides
        layout.setPadding(new Insets(10));

        return layout;
    }

    /**
     * Creates a table for the transactions
     * Columns are: ID, Date, Category, Description, Amount
     * 
     * @return A table of the transactions
     */
    private TableView<Transaction>buildTransactionTable() {
        
        // New table
        TableView<Transaction> table = new TableView<>();

        // Table columns, setcallvalueFactory allows the database to know exactly what
        // column should have what value via getter methods
        TableColumn<Transaction, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Transaction, BigDecimal> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // Add columns to table
        table.getColumns().addAll(idCol, dateCol, categoryCol, descCol, amountCol);

        // Access database and get all transactions
        DatabaseManager dbManager = new DatabaseManager();
        List<Transaction> transactions = dbManager.getTransactions();
        table.getItems().addAll(transactions);

        return table;

    }

    /**
     * Combines the table, transaction entry, and add transaction button 
     * @param table Table that makes up the transactions
     * @return A gridpane element of the transaction table and entry elements
     */
    private GridPane buildTableEntry(TableView<Transaction> table) {

        // Create gridpane
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));

        // Labels
        Label dateLabel = new Label("Date");
        Label categoryLabel = new Label("Category");
        Label descLabel = new Label("Description");
        Label amountLabel = new Label("Amount");

        Label[] labels = {dateLabel, categoryLabel, descLabel, amountLabel};

        // Inputs
        DatePicker datePicker = new DatePicker();
        ComboBox<Transaction.Category> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(Transaction.Category.values());
        TextField descField = new TextField();
        TextField amountField = new TextField();

        Region[] inputs = {datePicker, categoryBox, descField, amountField};

        // Add transaction button and logic
        Button addTransactionButton = new Button("Add Transaction");
        addTransactionButton.setOnAction(e -> {
            LocalDate date = datePicker.getValue();
            Transaction.Category category = categoryBox.getValue();
            String description = descField.getText();
            BigDecimal amount = new BigDecimal(amountField.getText());
            DatabaseManager dbManager = new DatabaseManager();
            Transaction t = dbManager.addTransaction(date, category, description, amount);
            table.getItems().add(t);
        });

        // Formatting grid so labels are above input fields
        for (int i = 0; i < labels.length; i++) {
            grid.add(labels[i], i, 0); // Add label to row 0
            grid.add(inputs[i], i, 1); // Add input below to row 1

            // Stretch input fields to fill space
            inputs[i].setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(inputs[i], Priority.ALWAYS);

            // Center labels
            labels[i].setMaxWidth(Double.MAX_VALUE);
            labels[i].setAlignment(Pos.CENTER);
            GridPane.setHgrow(labels[i], Priority.ALWAYS);
        }

        grid.add(addTransactionButton, 4, 1); // Add button to the end of row 0

        return grid;

    }

    /**
     * Builds the filter entry elements and logic 
     * @param table Table that makes up the transactions
     * @return A HBox element of the filter entry fields and button
     */
    private HBox buildFilterEntry(TableView<Transaction> table){

        // Filter HBox
        HBox filterBar = new HBox(10);
        filterBar.setPadding(new Insets(0,0,0,10));
        // Filter type combo box
        ComboBox<String> filterTypeBox = new ComboBox<>();
        filterTypeBox.getItems().addAll("Default", "Category", "Date Range", "Amount Greater Than", "Amount Less Than");

        // Dynamic filter HBox
        HBox dynamicInputs = new HBox();
        // Delete entry HBox
        HBox deleteBar = buildDeleteEntry();
        // Apply filter button
        Button applyFilterButton = new Button("Apply Filter");

        // Hbox filter elements 
        filterBar.getChildren().addAll(filterTypeBox, dynamicInputs, applyFilterButton, deleteBar);

        // Filter combo box actions
        filterTypeBox.setOnAction(e -> {
            // Filter input type
            String selected = filterTypeBox.getValue();
            // Dynamic fields that change dependent on the filter input
            dynamicInputs.getChildren().clear();

            switch (selected) {
                // When category, add category input field
                case "Category" -> {
                    ComboBox<Transaction.Category> categoryInput = new ComboBox<>();
                    categoryInput.getItems().addAll(Transaction.Category.values());
                    dynamicInputs.getChildren().add(categoryInput);
                    }
                // When date, add a start and end date input field
                case "Date Range" -> {
                    DatePicker startDate = new DatePicker();
                    DatePicker endDate = new DatePicker();
                    dynamicInputs.getChildren().addAll(new Label("Start:"), startDate, new Label("End:"), endDate);
                    }
                // When amount, add amount input field
                case "Amount Greater Than", "Amount Less Than" -> {
                    TextField amountInput = new TextField();
                    amountInput.setPromptText("Amount");
                    dynamicInputs.getChildren().add(amountInput);
                    }
            }
        });

        // Apply filter button logic
        applyFilterButton.setOnAction(e -> {
            // Filter input type
            String selected = filterTypeBox.getValue();
            // Open database manager for filtering results
            DatabaseManager dbManager = new DatabaseManager();
            switch (selected) {
                // When default is selected, show all transactions
                case "Default" -> {
                    // Get all transactions
                    List<Transaction> transactions = dbManager.getTransactions();
                    // Replaces existing items
                    table.getItems().setAll(transactions);
                }
                // When category is selected, filter by category type
                case "Category" -> {
                    // Get category input field
                    ComboBox<Transaction.Category> categoryInput = (ComboBox<Transaction.Category>) dynamicInputs.getChildren().get(0);
                    // Get category value
                    Transaction.Category category = categoryInput.getValue();
                    // Filter by category
                    List<Transaction> transactions = dbManager.getSpendingByCategory(category);
                    // Replaces existing items
                    table.getItems().setAll(transactions);
                }
                case "Date Range" -> {
                    // Get start and end dates
                    DatePicker startDate = (DatePicker) dynamicInputs.getChildren().get(1);
                    DatePicker endDate = (DatePicker) dynamicInputs.getChildren().get(3);
                    // Get transactions between the dates
                    List<Transaction> transactions = dbManager.getSpendingOverTime(
                        startDate.getValue(), endDate.getValue()
                    );
                    // Replaces existing items
                    table.getItems().setAll(transactions);
                }
                case "Amount Greater Than" -> {
                    // Get user amount
                    TextField amountInput = (TextField) dynamicInputs.getChildren().get(0);
                    BigDecimal amount = new BigDecimal(amountInput.getText());
                    // Get transactions greater than user amount
                    List<Transaction> transactions = dbManager.getTransactionsAbove(amount);
                    table.getItems().setAll(transactions);
                }
                case "Amount Less Than" -> {
                    // Get user amount
                    TextField amountInput = (TextField) dynamicInputs.getChildren().get(0);
                    BigDecimal amount = new BigDecimal(amountInput.getText());
                    // Get transactions less than user amount
                    List<Transaction> transactions = dbManager.getTransactionsBelow(amount);
                    table.getItems().setAll(transactions);
                }
            }
        });

        return filterBar;
    }
    
    /**
     * Creates the delete entry field of the button and input field of the ID
     * 
     * @return Hbox element that contains the button and entry field
     */
    private HBox buildDeleteEntry(){

        HBox deleteBar = new HBox(10);
        TextField idField = new TextField();
        idField.setPromptText("ID");

        Button deleteTransactionButton = new Button("Delete Transaction");

        deleteBar.getChildren().addAll(idField, deleteTransactionButton);

        deleteTransactionButton.setOnAction(e -> {
            int id = Integer.parseInt(idField.getText());
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.deleteTransaction(id);

            });

        return deleteBar;
    }

    public static void main(String[] args) {
        launch(args); // required to start JavaFX
    }
}
