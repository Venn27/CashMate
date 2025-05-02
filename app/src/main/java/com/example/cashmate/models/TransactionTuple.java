package com.example.cashmate.models;

import androidx.room.TypeConverters;
import com.example.cashmate.database.DateConverter;

import java.util.Date;

@TypeConverters(DateConverter.class)
public class TransactionTuple {
    public long id;
    public double amount;
    public String category;
    public long date;  // Menyimpan date sebagai long, bukan Date
    public String notes;
    public int isIncome;  // SQLite tidak mendukung boolean, gunakan int (0 = false, 1 = true)

    // Konversi ke Transaction
    public Transaction toTransaction() {
        return new Transaction(id, amount, category, new Date(date), notes, isIncome == 1);
    }
}