package com.example.cashmate.models;

public class Category {
    private String id;
    private String name;
    private int iconResourceId;  // Ubah nama field untuk mencerminkan nama metode getter
    private boolean isExpenseCategory;

    public Category(String id, String name, int iconResourceId, boolean isExpenseCategory) {
        this.id = id;
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.isExpenseCategory = isExpenseCategory;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Gunakan nama metode yang sesuai dengan yang diharapkan oleh CategoryAdapter
    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }

    public boolean isExpenseCategory() {
        return isExpenseCategory;
    }

    public void setExpenseCategory(boolean expenseCategory) {
        isExpenseCategory = expenseCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return id.equals(category.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}