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

public class DatabaseGUI extends Application {
    private static final String URL = "jdbc:sqlite:transactions.db";

    @Override
    public void start(Stage primaryStage) {

        Scene scene = new Scene(buildMainLayout(), 1000, 800);
        primaryStage.setTitle("Transaction Manager");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private VBox buildMainLayout() {

        TableView<Transaction> table = buildTransactionTable();

        GridPane tableEntry = buildTableEntry(table);
        HBox filteringEntry = buildFilterEntry();

        VBox layout = new VBox(15, table, tableEntry, filteringEntry);
        layout.setPadding(new Insets(10));
        tableEntry.prefWidthProperty().bind(table.widthProperty());

        return layout;
    }

    private TableView<Transaction>buildTransactionTable() {
        
        TableView<Transaction> table = new TableView<>();

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

        DatabaseManager dbManager = new DatabaseManager();
        List<Transaction> transactions = dbManager.getTransactions();
        table.getItems().addAll(transactions);

        return table;

    }

    private GridPane buildTableEntry(TableView<Transaction> table) {

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

        // Add to grid
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

        grid.add(addTransactionButton, 4, 1); // Add label to row 0

        return grid;

    }

    private HBox buildFilterEntry(){

        HBox filterBar = new HBox(10);
        ComboBox<String> filterTypeBox = new ComboBox<>();
        filterTypeBox.getItems().addAll("Category", "Date Range", "Amount Greater Than", "Amount Less Than");

        HBox dynamicInputs = new HBox();
        Button applyFilterButton = new Button("Apply Filter");

        filterBar.getChildren().addAll(filterTypeBox, dynamicInputs, applyFilterButton);

        filterTypeBox.setOnAction(e -> {
            String selected = filterTypeBox.getValue();
            dynamicInputs.getChildren().clear();

            switch (selected) {
                case "Category" -> {
                    ComboBox<Transaction.Category> categoryInput = new ComboBox<>();
                    categoryInput.getItems().addAll(Transaction.Category.values());
                    dynamicInputs.getChildren().add(categoryInput);
                    }

                case "Date Range" -> {
                    DatePicker startDate = new DatePicker();
                    DatePicker endDate = new DatePicker();
                    dynamicInputs.getChildren().addAll(new Label("Start:"), startDate, new Label("End:"), endDate);
                    }

                case "Amount Greater Than", "Amount Less Than" -> {
                    TextField amountInput = new TextField();
                    amountInput.setPromptText("Amount");
                    dynamicInputs.getChildren().add(amountInput);
                    }
            }
        });


        return filterBar;
    }
    

    public static void main(String[] args) {
        launch(args); // required to start JavaFX
    }
}
