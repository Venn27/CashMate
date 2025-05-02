package com.example.cashmate.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cashmate.database.Income;
import com.example.cashmate.models.Transaction;
import com.example.cashmate.models.TransactionTuple;

import java.util.List;

@Dao
public interface IncomeDao {
    @Insert
    long insert(com.example.cashmate.database.Income income);

    @Update
    void update(com.example.cashmate.database.Income income);

    @Delete
    void delete(com.example.cashmate.database.Income income);

    @Query("SELECT * FROM incomes ORDER BY date DESC")
    List<com.example.cashmate.database.Income> getAllIncomes();

    @Query("SELECT * FROM incomes WHERE id = :id")
    com.example.cashmate.database.Income getIncomeById(long id);

    @Query("SELECT * FROM incomes ORDER BY date DESC LIMIT :limit")
    List<com.example.cashmate.database.Income> getRecentIncomes(int limit);

    @Query("SELECT * FROM incomes WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<Income> getIncomesInDateRange(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM incomes")
    double getTotalIncome();

    @Query("SELECT SUM(amount) FROM incomes WHERE date BETWEEN :startDate AND :endDate")
    double getTotalIncomeInDateRange(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM incomes WHERE strftime('%m', datetime(date/1000, 'unixepoch')) = :month + 1 " +
            "AND strftime('%Y', datetime(date/1000, 'unixepoch')) = :year")
    double getTotalIncomeForMonthYear(int month, int year);

    @Query("SELECT id, amount, source as category, date, notes, 1 as isIncome FROM incomes WHERE date BETWEEN :startDate AND :endDate")
    List<TransactionTuple> getIncomesAsTransactionTuples(long startDate, long endDate);

    @Query("SELECT id, amount, source as category, date, notes, 1 as isIncome FROM incomes ORDER BY date DESC LIMIT :limit")
    List<TransactionTuple> getRecentIncomesAsTransactionTuples(int limit);

    // Metode bantuan untuk mengkonversi TransactionTuple ke Transaction
    default List<Transaction> getIncomesAsTransactions(long startDate, long endDate) {
        List<Transaction> transactions = new java.util.ArrayList<>();
        List<TransactionTuple> tuples = getIncomesAsTransactionTuples(startDate, endDate);
        for (TransactionTuple tuple : tuples) {
            transactions.add(tuple.toTransaction());
        }
        return transactions;
    }

    // Metode bantuan untuk mengkonversi TransactionTuple ke Transaction
    default List<Transaction> getRecentIncomesAsTransactions(int limit) {
        List<Transaction> transactions = new java.util.ArrayList<>();
        List<TransactionTuple> tuples = getRecentIncomesAsTransactionTuples(limit);
        for (TransactionTuple tuple : tuples) {
            transactions.add(tuple.toTransaction());
        }
        return transactions;
    }
}