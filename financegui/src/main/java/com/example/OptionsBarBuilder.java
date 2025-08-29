package com.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class OptionsBarBuilder {

     /**
      * Builds the options bar, which consists of the export to csv, and export to excel buttons
      * 
      * @param primaryStage The primary stage used to display file chooser dialogs or popups
      * @return An HBox containing the options bar with the export buttons
      */
    public HBox buildOptionsBar(Stage primaryStage) {
        
        // Get transactions
        DatabaseManager dbManager = new DatabaseManager();
        List<Transaction> transactions = dbManager.getTransactions();

        // Buttons
        Button exportCSV = new Button("Export to CSV");
        Button exportExcel = new Button("Export to Excel");

        exportCSV.setOnAction(e -> {
            exportToCSV(transactions, primaryStage);
        });

        exportExcel.setOnAction(e -> {
            exportToExcel(transactions, primaryStage);
        });

        HBox optionBar = new HBox(10, exportCSV, exportExcel);

        return optionBar;
    }


    /**
     * Logic for the export to csv button, takes transactions database and converts it to csv
     * 
     * @param transactions List of transactions
     * @param primaryStage The primary stage used to display file chooser dialogs or popups
     */
    private void exportToCSV(List<Transaction> transactions, Stage primaryStage) {
        // Create a filer chooser, set up as a csv file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.setInitialFileName("transactions.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(primaryStage); // or any parent window

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
            // Write CSV headers
            writer.println("ID,Date,Category,Description,Amount");

                // Write transaction rows
                for (Transaction t : transactions) {
                    writer.printf("%d,%s,%s,%s,%.2f%n",
                        t.getId(),
                        t.getDate(), 
                        t.getCategory().name(),
                        t.getDescription().replace(",", " "), 
                        t.getAmount()
                    );
                }

                System.out.println("Export successful.");
            } catch (IOException ex) {
                System.err.println("Export to CSV Error");
            }
        }
    }

    /**
     * Logic for the export to excel button, takes transactions database and converts it to excel
     * 
     * @param transactions List of transactions
     * @param primaryStage The primary stage used to display file chooser dialogs or popups
     */
    private void exportToExcel(List<Transaction> transactions, Stage primaryStage) {
        // Create a filer chooser, set up as an excel file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.setInitialFileName("transactions.xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Transactions");

                // Header rows
                Row header = sheet.createRow(0);
                String[] headers = {"ID", "Date", "Category", "Description", "Amount"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(headers[i]);
                }
                // Write transaction rows
                for (int i = 0; i < transactions.size(); i++) {
                    Transaction t = transactions.get(i);
                    Row row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(t.getId());
                    row.createCell(1).setCellValue(t.getDate().toString());
                    row.createCell(2).setCellValue(t.getCategory().name());
                    row.createCell(3).setCellValue(t.getDescription());
                    row.createCell(4).setCellValue(t.getAmount().doubleValue());
                }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Write to file
                try (FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                }

            } catch (IOException ex) {
                System.err.println("Export to Excel Error");
            }
        }
    }

    
}
