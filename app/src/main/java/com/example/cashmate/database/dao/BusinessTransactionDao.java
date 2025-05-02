package com.example.cashmate.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BusinessTransactionDao {
    @Insert
    long insert(com.example.cashmate.database.BusinessTransaction transaction);

    @Update
    void update(com.example.cashmate.database.BusinessTransaction transaction);

    @Delete
    void delete(com.example.cashmate.database.BusinessTransaction transaction);

    @Query("SELECT * FROM business_transactions ORDER BY date DESC")
    List<com.example.cashmate.database.BusinessTransaction> getAllTransactions();

    @Query("SELECT * FROM business_transactions WHERE id = :id")
    com.example.cashmate.database.BusinessTransaction getTransactionById(long id);

    @Query("SELECT * FROM business_transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<com.example.cashmate.database.BusinessTransaction> getTransactionsInDateRange(long startDate, long endDate);

    @Query("SELECT * FROM business_transactions WHERE transactionType = :type ORDER BY date DESC")
    List<com.example.cashmate.database.BusinessTransaction> getTransactionsByType(String type);

    @Query("SELECT * FROM business_transactions WHERE transactionType = :type AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<com.example.cashmate.database.BusinessTransaction> getTransactionsByTypeInDateRange(String type, long startDate, long endDate);

    @Query("SELECT SUM(totalAmount) FROM business_transactions WHERE transactionType = :type")
    double getTotalAmountForType(String type);

    @Query("SELECT SUM(totalAmount) FROM business_transactions WHERE transactionType = :type AND date BETWEEN :startDate AND :endDate")
    double getTotalAmountForTypeInDateRange(String type, long startDate, long endDate);
}