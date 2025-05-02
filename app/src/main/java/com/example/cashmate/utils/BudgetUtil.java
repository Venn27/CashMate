package com.example.cashmate.utils;

import com.example.cashmate.database.AppDatabase;
import com.example.cashmate.database.Budget;
import com.example.cashmate.database.dao.BudgetDao;
import com.example.cashmate.database.dao.ExpenseDao;

import java.util.Calendar;

public class BudgetUtil {

    private final AppDatabase database;

    public BudgetUtil(AppDatabase database) {
        this.database = database;
    }

    /**
     * Check if adding a new expense would exceed the budget for the category
     * @param category expense category
     * @param amount expense amount
     * @return true if budget would be exceeded, false otherwise
     */
    public boolean checkBudgetExceeded(String category, double amount) {
        BudgetDao budgetDao = database.budgetDao();
        ExpenseDao expenseDao = database.expenseDao();

        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        // Get budget for category
        Budget budget = budgetDao.getBudgetByCategoryAndMonthYear(category, currentMonth, currentYear);

        // If no budget exists for this category, return false
        if (budget == null) {
            return false;
        }

        // Get total spent for category in current month
        double totalSpent = expenseDao.getTotalExpenseForCategoryInMonthYear(
                category, currentMonth, currentYear);

        // Check if adding new expense would exceed budget
        return (totalSpent + amount) > budget.getAmount();
    }

    /**
     * Get the remaining budget for a category
     * @param category expense category
     * @return remaining budget amount, or -1 if no budget exists
     */
    public double getRemainingBudget(String category) {
        BudgetDao budgetDao = database.budgetDao();
        ExpenseDao expenseDao = database.expenseDao();

        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        // Get budget for category
        Budget budget = budgetDao.getBudgetByCategoryAndMonthYear(category, currentMonth, currentYear);

        // If no budget exists for this category, return -1
        if (budget == null) {
            return -1;
        }

        // Get total spent for category in current month
        double totalSpent = expenseDao.getTotalExpenseForCategoryInMonthYear(
                category, currentMonth, currentYear);

        // Return remaining budget
        return budget.getAmount() - totalSpent;
    }
}