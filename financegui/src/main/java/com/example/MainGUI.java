package com.example;


import java.util.Collections;
import java.util.Set;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
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
public class MainGUI extends Application {

    private VBox transactionLayout;
    private VBox summaryLayout;

    /**
     * Creates the main GUI window
     * 
     * @param primaryStage Layout of the window/elements
    */
    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(buildMainLayout(primaryStage), 1900, 900);
        primaryStage.setTitle("Transaction Manager");
        primaryStage.setScene(scene);
        primaryStage.show();

         // Bind widths to always be 60% and 40% of the scene width
        transactionLayout.prefWidthProperty().bind(scene.widthProperty().multiply(0.6));
        summaryLayout.prefWidthProperty().bind(scene.widthProperty().multiply(0.4));

    }

    /**
     * Builds the main layout for the gui window
     * 
     * @return A final VBox of the gui elements
     * 
    */
    private HBox buildMainLayout(Stage primaryStage) {

        // Overall page layout
        HBox pageLayout = new HBox(10);

        // Summary section 
        SummarySectionBuilder summaryBuilder = new SummarySectionBuilder();
        VBox summarySection = summaryBuilder.buildSummarySection();
        summaryLayout = new VBox(15, summarySection);
        summaryLayout.setPadding(new Insets(10)); // Padding of 10 on all sides

        // Builds transation table
        TableBuilder tableGUI = new TableBuilder(summaryBuilder);
        TableView<Transaction> table = tableGUI.build();

        // Builds table entry section and filtering section
        GridPane tableEntry = tableGUI.buildTableEntry(table);

        // Filter bar setion
        FilterBarBuilder filterGUI = new FilterBarBuilder(summaryBuilder);
        HBox filteringEntry = filterGUI.buildFilterEntry(table);
        tableEntry.prefWidthProperty().bind(table.widthProperty());
        filteringEntry.prefWidthProperty().bind(table.widthProperty());

        // Options bar section
        HBox optionLayouts = new OptionsBarBuilder().buildOptionsBar(primaryStage);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        //Transaction layout
        transactionLayout = new VBox(15, table, tableEntry, filteringEntry, spacer, optionLayouts);
        transactionLayout.setPadding(new Insets(10)); // Padding of 10 on all sides


        pageLayout.getChildren().addAll(transactionLayout, summaryLayout);


        return pageLayout;
    }

    /**
     * Updates the year boxes dropdowns after adding a transaction or deleting one
     * 
     * @param yearBox Comboxbox that contains the years
     */
    public void updateYearComboBox(ComboBox<Integer> yearBox) {

        if (yearBox == null) {
            System.out.println("Warning: yearBox is null. Skipping update.");
            return;
        }

        DatabaseManager dbManager = new DatabaseManager();
        Set<Integer> years = dbManager.extractYears();

        // Preserve current selection
        Integer currentSelection = yearBox.getValue(); 
        yearBox.getItems().setAll(years);

        if (years.contains(currentSelection)) {
            // Restore selection if valid
            yearBox.setValue(currentSelection);       
        } else if (!years.isEmpty()) {
            // Pick the most recent valid year
            yearBox.setValue(Collections.max(years));  
        }

}


    public static void main(String[] args) {
        launch(args); // required to start JavaFX
    }
}
