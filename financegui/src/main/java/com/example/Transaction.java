package com.example;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {


    public enum Category {
    FOOD,
    TRANSPORTATION,
    ENTERTAINMENT,
    UTILITIES,
    HEALTHCARE,
    GROCERIES,
    RENT,
    SALARY,
    INVESTMENT,
    EDUCATION,
    TRAVEL,
    INSURANCE,
    SUBSCRIPTIONS,
    DONATION,
    MISCELLANEOUS
}

    private final int id;
    private final LocalDate date;
    private final Category category;
    private final String description; 
    private final BigDecimal amount; 


    public Transaction (int id, LocalDate date, Category category, String description, BigDecimal amount) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount; 

    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }



}