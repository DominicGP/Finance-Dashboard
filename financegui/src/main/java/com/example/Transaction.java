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
    MISCELLANEOUS;

    public boolean isCredit() {
        return this == SALARY || this == INVESTMENT;
    }
}

    private final int id;
    private LocalDate date;
    private Category category;
    private String description; 
    private BigDecimal amount; 


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

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount; 
    }




}