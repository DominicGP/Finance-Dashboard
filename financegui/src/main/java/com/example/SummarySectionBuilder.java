package com.example;

import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SummarySectionBuilder {
    
    private ComboBox<Integer> summaryYearBox;
    private ComboBox<Integer> spendingYearBox;

     /**
     * Creates the summary entry field for year and month and summary statements
     * 
     * @return VBox element that contains month and year entry fields and summary statements
     */
    public VBox buildSummarySection(){

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Spending by Category");
        pieChart.setVisible(false); // Hide initially

        // Summary statements
        VBox summaryStatements = new VBox(10);
        // Summary entry fields
        HBox summaryEnter = new HBox(10);
        // Summary section
        VBox summarySection = new VBox(10, summaryEnter, summaryStatements, pieChart);

        // Month entry field, hardcoded months
        ComboBox<String> monthBox = new ComboBox<>();
        monthBox.setPromptText("Month");
        monthBox.getItems().addAll(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        );

        // Year entry field
        summaryYearBox = new ComboBox<>();
        summaryYearBox.setPromptText("Year");
        // Uses function to get current used years from the transactions
        DatabaseManager dbManager = new DatabaseManager();
        Set<Integer> years = dbManager.extractYears();
        summaryYearBox.getItems().addAll(years);

        // Show summary button
        Button showSummaryButton = new Button("Show Summary");

        // Summary statements & placeholders
        Label incomeLabel = new Label();
        Label creditLabel = new Label();
        Label balanceLabel = new Label();
        incomeLabel.setText(String.format(" "));
        creditLabel.setText(String.format(" "));
        balanceLabel.setText(String.format(" "));

        summaryStatements.getChildren().addAll(incomeLabel, creditLabel, balanceLabel);

        // Summary button logic
        showSummaryButton.setOnAction(e -> {
            summaryStatements.getChildren().clear(); // Clear previous labels

            // Get all transactions
            List<Transaction> transactions = dbManager.getTransactions();
            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expenses = BigDecimal.ZERO;
            
            // If a month and year have been selected
            if (monthBox.getValue() != null && summaryYearBox.getValue() != null) {
                // Convert the mont value to its respective month number
                // Ex. January = 1
                int targetMonth = Month.valueOf(monthBox.getValue().toUpperCase()).getValue();
                // Filter the transactions to the needed month and year
                List<Transaction> filtered = transactions.stream()
                    .filter(t -> t.getDate().getYear() == summaryYearBox.getValue() && t.getDate().getMonthValue() == targetMonth)
                    .collect(Collectors.toList());
                // Build pie chart
                updateSummaryChart(pieChart, filtered);
                pieChart.setVisible(true);
                // Go through the filtered transactions and determine whether is an expense or income
                for (Transaction t : filtered) {
                    if(t.getCategory().isCredit()){
                        income = income.add(t.getAmount());
                    } else {
                        expenses = expenses.add(t.getAmount());
                    }
                }
                // Update summary statements
                BigDecimal balance = income.subtract(expenses);
                incomeLabel.setText(String.format("Total Income: $%.2f", income));
                creditLabel.setText(String.format("Total Expenses: $%.2f", expenses));
                balanceLabel.setText(String.format("Net Balance: $%.2f", balance));
                summaryStatements.getChildren().addAll(incomeLabel, creditLabel, balanceLabel);
            }
        
        });
        // Summary entry field and button 
        summaryEnter.getChildren().addAll(monthBox,summaryYearBox, showSummaryButton);

        VBox spendingGraph = buildSpendingGraph();

        summarySection.getChildren().addAll(spendingGraph);

        return summarySection;
    }

    /**
     * Build a pie chart used the filtered dates based on categorical spending
     * 
     * @param filtered list of transactions within the specified month/year
     * @return A pie chart of categorical spending
     */
    private PieChart updateSummaryChart(PieChart pieChart, List<Transaction> filtered) {

        // Sum the categories spending totals
        Map<Transaction.Category, BigDecimal> totals = new HashMap<>();
        for (Transaction t : filtered) {
            totals.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
        }

        // Add summed categories to pie chart data
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (var entry : totals.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey().name(), entry.getValue().doubleValue()));
        }

        pieChart.setData(pieData);

        return pieChart;
    }


    /**
     * Builds a bar chart of monthly spending
     * 
     * @return VBox element that has the bar chart and input
     */
    private VBox buildSpendingGraph() {

        // Axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount ($)");

        // Usually has 13 entries, only want first 12 months, fixes label bugs
        String[] monthLabels = Arrays.copyOfRange(
            new DateFormatSymbols().getMonths(), 0, 12
        );
        xAxis.setCategories(FXCollections.observableArrayList(Arrays.asList(monthLabels)));

        // Barchart placeholder for formatting
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Monthly Income vs Expenses");
        barChart.getStylesheets().add(getClass().getResource("chart-style.css").toExternalForm()); // Bar colours
        barChart.setVisible(false); 

        HBox spendingInput = new HBox(10);
        VBox spendingGraph = new VBox (10, spendingInput, barChart);

        DatabaseManager dbManager = new DatabaseManager();

        // Year input box
        spendingYearBox = new ComboBox<>();
        spendingYearBox.setPromptText("Year");
        spendingYearBox.getItems().addAll(dbManager.extractYears());

        // Show spending button
        Button showSpendingButton = new Button("Show Monthly Spending");

        // Spending button logic
        showSpendingButton.setOnAction(e -> {
            List<Transaction> transactions = dbManager.getTransactions();
            if (spendingYearBox.getValue() != null) {
                // Filter the transactions to the needed year
                List<Transaction> filtered = transactions.stream()
                    .filter(t -> t.getDate().getYear() == spendingYearBox.getValue())
                    .collect(Collectors.toList());
                updateBarChart(barChart, filtered);
                barChart.setVisible(true);
            }
        });

        spendingInput.getChildren().addAll(spendingYearBox, showSpendingButton);

        return spendingGraph;
        
    }

    /**
     * Updates the data of the bar chart 
     * 
     * @param barChart Bar chart used to update
     * @param filtered Transactions used to create the data
     * @return Updated bar chart
     */
    private BarChart<String, Number> updateBarChart(BarChart<String, Number> barChart, List<Transaction> filtered) {

        // Income and expense totals arrays
        BigDecimal[] incomeTotals = new BigDecimal[12];
        BigDecimal[] expenseTotals = new BigDecimal[12];
        Arrays.fill(incomeTotals, BigDecimal.ZERO);
        Arrays.fill(expenseTotals, BigDecimal.ZERO);

        // Go through each transaction and determine whether its a credit or debit and update the total
        for (Transaction t : filtered) {
            int month = t.getDate().getMonthValue() - 1; // 0-based index
            if (t.getCategory().isCredit()) {
                incomeTotals[month] = incomeTotals[month].add(t.getAmount());
            } else {
                expenseTotals[month] = expenseTotals[month].add(t.getAmount());
            }
        }

        // Income and expense data
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");

        String[] monthLabels = new DateFormatSymbols().getMonths(); // Jan-Dec
        for (int i = 0; i < 12; i++) {
            incomeSeries.getData().add(new XYChart.Data<>(monthLabels[i], incomeTotals[i]));
            expenseSeries.getData().add(new XYChart.Data<>(monthLabels[i], expenseTotals[i]));
        }

        // Add data to exisiting chart
        ObservableList<XYChart.Series<String, Number>> dataSeriesList = FXCollections.observableArrayList();
        dataSeriesList.add(incomeSeries);
        dataSeriesList.add(expenseSeries);
        barChart.setData(dataSeriesList);
            
       return barChart;

    }

    /**
     * Used to get the combobox 'summaryYearBox', contains the applicable years for the summary chart
     * 
     * @return summaryYearBox
     */
    public ComboBox<Integer> getSummaryYearBox() {
        return summaryYearBox;
    }

    /**
     * Used to get the combobox 'spendingYearBox', contains the applicable years for the spending chart
     * 
     * @return spendingYearBox
     */
    public ComboBox<Integer> getSpendingYearBox() {
        return spendingYearBox;
    }
    
}
