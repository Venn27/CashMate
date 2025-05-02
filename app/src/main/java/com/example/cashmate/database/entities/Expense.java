package com.example.cashmate.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "expenses")
@TypeConverters(DateConverter.class)
public class Expense {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private double amount;
    private String category;
    private Date date;
    private String notes;

    public Expense(double amount, String category, Date date, String notes) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.notes = notes;
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
}
