package com.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

public class TableBuilder {

    private final SummarySectionBuilder summaryBuilder;
    
    /** 
     * This allows the table to interact with the summary sections comboboxes
     * 
     * @param summaryBuilder Instance of the summary section whose components are needed
     */
    public TableBuilder(SummarySectionBuilder summaryBuilder) {
        this.summaryBuilder = summaryBuilder;
    }

    /**
     * Creates a table for the transactions
     * Columns are: ID, Date, Category, Description, Amount
     * 
     * @return A table of the transactions
     */
    public TableView<Transaction> build() {
        
        // New table
        TableView<Transaction> table = new TableView<>();
        table.setEditable(true);

        DatabaseManager dbManager = new DatabaseManager();

        // Table columns, setcallvalueFactory allows the database to know exactly what
        // column should have what value via getter methods
        TableColumn<Transaction, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        // Date values in table edit logic
        dateCol.setCellFactory(column -> new TableCell<>() {
            // Creates a new date picker for edit
            private final DatePicker datePicker = new DatePicker();

            {
                // Updates date value for transactions
                datePicker.setOnAction(e -> {
                    LocalDate newDate = datePicker.getValue();
                    int row = getIndex();
                    if (row >= 0 && row < getTableView().getItems().size()) {
                        Transaction t = getTableView().getItems().get(row);
                        t.setDate(newDate);
                        dbManager.updateTransaction(t);
                    }
                });

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
            // Allows datepicker to appear in table cells
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    datePicker.setValue(date);
                    setGraphic(datePicker);
                }
            }
        });

        TableColumn<Transaction, Transaction.Category> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        ObservableList<Transaction.Category> categories = FXCollections.observableArrayList(Transaction.Category.values());
        // Category values in table edit logic
        categoryCol.setCellFactory(ComboBoxTableCell.forTableColumn(categories));
        categoryCol.setOnEditCommit(event -> {
            Transaction t = event.getRowValue();
            t.setCategory(event.getNewValue());
            dbManager.updateTransaction(t);
        });

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        // Description values in table edit logic
        descCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descCol.setOnEditCommit(event -> {
            Transaction t = event.getRowValue();
            t.setDescription(event.getNewValue());
            dbManager.updateTransaction(t);
        });

        TableColumn<Transaction, BigDecimal> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        // Converter that allows big decimal to be converted to string and back due to editing functionality
        amountCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<BigDecimal>() {
            @Override
            public String toString(BigDecimal value) {
                return value != null ? value.toPlainString() : "";
            }
            @Override
            public BigDecimal fromString(String s) {
                try {
                    return new BigDecimal(s);
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO; // Or handle error properly
                }
            }
        }));
        // Amount values in table edit logic
        amountCol.setOnEditCommit(event -> {
            Transaction t = event.getRowValue();
            t.setAmount(event.getNewValue());

            dbManager.updateTransaction(t);
        });

        
        // Add columns to table
        table.getColumns().addAll(idCol, dateCol, categoryCol, descCol, amountCol);

        List<Transaction> transactions = dbManager.getTransactions();
        table.getItems().addAll(transactions);

        return table;

    }

    /**
     * Combines the table, transaction entry, and add transaction button 
     * @param table Table that makes up the transactions
     * @return A gridpane element of the transaction table and entry elements
     */
    public GridPane buildTableEntry(TableView<Transaction> table) {

        // Create gridpane
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(5);

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
            // Get input data and add transaction to database
            LocalDate date = datePicker.getValue();
            Transaction.Category category = categoryBox.getValue();
            String description = descField.getText();
            String amountText = amountField.getText();

            if (amountText == null || amountText.trim().isEmpty() ||
                date == null || category == null || description.trim().isEmpty()) {
                showAlert("All fields must be filled.");
                return;
            }
                
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountText.trim());
            } catch (NumberFormatException er) {
                showAlert("Amount value must be a valid number.");
                return;
            }
                
            

            DatabaseManager dbManager = new DatabaseManager();
            Transaction t = dbManager.addTransaction(date, category, description, amount);
            table.getItems().add(t);

            // Update year boxes, and clear input form
            MainGUI databaseGUI = new MainGUI();
            ComboBox<Integer> summaryYearBox = summaryBuilder.getSummaryYearBox();
            ComboBox<Integer> spendingYearBox = summaryBuilder.getSpendingYearBox();
            databaseGUI.updateYearComboBox(summaryYearBox);
            databaseGUI.updateYearComboBox(spendingYearBox);
            datePicker.setValue(null);
            categoryBox.setValue(null);
            descField.clear();
            amountField.clear();

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

    // Helper function
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
