package com.example.cashmate.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.cashmate.database.dao.BudgetDao;
import com.example.cashmate.database.dao.BusinessTransactionDao;
import com.example.cashmate.database.dao.ExpenseDao;
import com.example.cashmate.database.dao.IncomeDao;

@Database(entities = {com.example.cashmate.database.Income.class, com.example.cashmate.database.Expense.class, com.example.cashmate.database.Budget.class, com.example.cashmate.database.BusinessTransaction.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "cashmate.db";

    public abstract IncomeDao incomeDao();
    public abstract ExpenseDao expenseDao();
    public abstract BudgetDao budgetDao();
    public abstract BusinessTransactionDao businessTransactionDao();

    // Metode yang sudah ada
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Tambahkan metode alias untuk kompatibilitas
    public static AppDatabase getInstance(final Context context) {
        return getDatabase(context);
    }
}