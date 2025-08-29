package com.example;

import java.math.BigDecimal;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class FilterBarBuilder {

    private final SummarySectionBuilder summaryBuilder;
    
    /** 
     * This allows the filter bar to interact with the summary sections comboboxes
     * 
     * @param summaryBuilder Instance of the summary section whose components are needed
     */
    public FilterBarBuilder(SummarySectionBuilder summaryBuilder) {
        this.summaryBuilder = summaryBuilder;
    }

    /**
     * Builds the filter entry elements and logic 
     * 
     * @param table Table that makes up the transactions
     * @return A HBox element of the filter entry fields and button
     */
    public HBox buildFilterEntry(TableView<Transaction> table){
        

        // Filter HBox
        HBox filterBar = new HBox(10);
        // Filter type combo box
        ComboBox<String> filterTypeBox = new ComboBox<>();
        filterTypeBox.getItems().addAll("Default", "Category", "Date Range", "Amount Greater Than", "Amount Less Than");

        // Dynamic filter HBox
        HBox dynamicInputs = new HBox();
        // Delete entry HBox
        HBox deleteBar = buildDeleteEntry(table);
        // Apply filter button
        Button applyFilterButton = new Button("Apply Filter");

        // Keeps delete button right aligned
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Hbox filter elements 
        filterBar.getChildren().addAll(filterTypeBox, dynamicInputs, applyFilterButton, spacer, deleteBar);

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
                    startDate.setPromptText("Start Date");
                    endDate.setPromptText("End Date");

                    Label startLabel = new Label("Start:");
                    Label endLabel = new Label("End:");

                    // Add spacing to the right of each label
                    HBox.setMargin(startLabel, new Insets(5, 10, 0, 0)); 
                    HBox.setMargin(endLabel, new Insets(5, 10, 0, 10));

                    dynamicInputs.getChildren().addAll(startLabel, startDate, endLabel, endDate);
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
     * Creates the delete entry field, made up of the button and input field of the ID
     * 
     * @return HBox element that contains the button and entry field
     */
    private HBox buildDeleteEntry(TableView<Transaction> table){

        // Delete entry HBox
        HBox deleteBar = new HBox(10);
        // Delete entry input field
        TextField idField = new TextField();
        idField.setPromptText("ID");
        // Delete button
        Button deleteTransactionButton = new Button("Delete Transaction");

        deleteBar.getChildren().addAll(idField, deleteTransactionButton);
        // Delete button logic
        deleteTransactionButton.setOnAction(e -> {
            // Delete transaction by ID
            int id = Integer.parseInt(idField.getText());
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.deleteTransaction(id);
            // Update transaction list
            List<Transaction> transactions = dbManager.getTransactions();
            table.getItems().setAll(transactions);
            // Update year boxes
            MainGUI mainGUI = new MainGUI();
            ComboBox<Integer> summaryYearBox = summaryBuilder.getSummaryYearBox();
            ComboBox<Integer> spendingYearBox = summaryBuilder.getSpendingYearBox();
            mainGUI.updateYearComboBox(summaryYearBox);
            mainGUI.updateYearComboBox(spendingYearBox);

            });

        return deleteBar;
    }
    
}
