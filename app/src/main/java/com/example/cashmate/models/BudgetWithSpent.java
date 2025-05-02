package com.example.cashmate.models;

public class BudgetWithSpent {
    private long id;
    private String category;
    private double budgetAmount;
    private double spentAmount;
    private int month;
    private int year;

    public BudgetWithSpent(long id, String category, double budgetAmount, double spentAmount, int month, int year) {
        this.id = id;
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
        this.month = month;
        this.year = year;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingAmount() {
        return budgetAmount - spentAmount;
    }

    public int getProgress() {
        if (budgetAmount <= 0) {
            return 0;
        }

        return (int) Math.min(100, (spentAmount / budgetAmount) * 100);
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}