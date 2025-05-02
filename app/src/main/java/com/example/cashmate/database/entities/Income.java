package com.example.cashmate.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "incomes")
@TypeConverters(DateConverter.class)
public class Income {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private double amount;
    private String source;
    private Date date;
    private String notes;

    public Income(double amount, String source, Date date, String notes) {
        this.amount = amount;
        this.source = source;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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