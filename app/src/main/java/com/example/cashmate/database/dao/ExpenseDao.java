package com.example.cashmate.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cashmate.database.Expense;
import com.example.cashmate.models.Transaction;
import com.example.cashmate.models.TransactionTuple;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    long insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    List<Expense> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE id = :id")
    Expense getExpenseById(long id);

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    List<Expense> getRecentExpenses(int limit);

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<Expense> getExpensesInDateRange(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM expenses")
    double getTotalExpense();

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    double getTotalExpenseInDateRange(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM expenses WHERE strftime('%m', datetime(date/1000, 'unixepoch')) = :month + 1 " +
            "AND strftime('%Y', datetime(date/1000, 'unixepoch')) = :year")
    double getTotalExpenseForMonthYear(int month, int year);

    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category AND " +
            "strftime('%m', datetime(date/1000, 'unixepoch')) = :month + 1 " +
            "AND strftime('%Y', datetime(date/1000, 'unixepoch')) = :year")
    double getTotalExpenseForCategoryInMonthYear(String category, int month, int year);

    @Query("SELECT id, amount, category, date, notes, 0 as isIncome FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    List<TransactionTuple> getExpensesAsTransactionTuples(long startDate, long endDate);

    @Query("SELECT id, amount, category, date, notes, 0 as isIncome FROM expenses ORDER BY date DESC LIMIT :limit")
    List<TransactionTuple> getRecentExpensesAsTransactionTuples(int limit);

    // Metode bantuan untuk mengkonversi TransactionTuple ke Transaction
    default List<Transaction> getExpensesAsTransactions(long startDate, long endDate) {
        List<Transaction> transactions = new java.util.ArrayList<>();
        List<TransactionTuple> tuples = getExpensesAsTransactionTuples(startDate, endDate);
        for (TransactionTuple tuple : tuples) {
            transactions.add(tuple.toTransaction());
        }
        return transactions;
    }

    // Metode bantuan untuk mengkonversi TransactionTuple ke Transaction
    default List<Transaction> getRecentExpensesAsTransactions(int limit) {
        List<Transaction> transactions = new java.util.ArrayList<>();
        List<TransactionTuple> tuples = getRecentExpensesAsTransactionTuples(limit);
        for (TransactionTuple tuple : tuples) {
            transactions.add(tuple.toTransaction());
        }
        return transactions;
    }
}