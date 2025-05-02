package com.example.cashmate.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cashmate.database.Budget;

import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets")
    List<Budget> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE id = :id")
    Budget getBudgetById(long id);

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    List<Budget> getBudgetsForMonthYear(int month, int year);

    @Query("SELECT * FROM budgets WHERE category = :category AND month = :month AND year = :year")
    Budget getBudgetByCategoryAndMonthYear(String category, int month, int year);

    @Query("SELECT COUNT(*) FROM budgets WHERE category = :category AND month = :month AND year = :year")
    int countBudgetsForCategoryAndMonthYear(String category, int month, int year);
}