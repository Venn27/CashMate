package com.example.cashmate.models;

import java.util.Date;

public class Transaction {
    private long id;
    private double amount;
    private String category;
    private Date date;
    private String notes;
    private boolean isIncome;

    public Transaction(long id, double amount, String category, Date date, String notes, boolean isIncome) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.notes = notes;
        this.isIncome = isIncome;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public void setIncome(boolean income) {
        isIncome = income;
    }
}