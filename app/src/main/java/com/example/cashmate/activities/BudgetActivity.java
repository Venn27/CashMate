package com.example.cashmate.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashmate.R;
import com.example.cashmate.adapters.BudgetAdapter;
import com.example.cashmate.database.AppDatabase;
import com.example.cashmate.database.Budget;
import com.example.cashmate.database.dao.BudgetDao;
import com.example.cashmate.database.dao.ExpenseDao;
import com.example.cashmate.models.BudgetWithSpent;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetActivity extends AppCompatActivity {

    private TextInputLayout tilCategory, tilAmount;
    private TextInputEditText etAmount;
    private AutoCompleteTextView actCategory;
    private MaterialButton btnSave;
    private RecyclerView rvBudgets;

    private AppDatabase database;
    private ExecutorService executorService;
    private BudgetAdapter budgetAdapter;
    private List<BudgetWithSpent> budgets = new ArrayList<>();

    private int currentMonth, currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_budget);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();

        // Initialize database
        database = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);

        // Setup category dropdown
        setupCategoryDropdown();

        // Setup recycler view
        setupRecyclerView();

        // Load budgets
        loadBudgets();

        // Setup save button
        btnSave.setOnClickListener(v -> saveBudget());
    }

    private void initViews() {
        tilCategory = findViewById(R.id.til_category);
        tilAmount = findViewById(R.id.til_amount);

        etAmount = findViewById(R.id.et_amount);

        actCategory = findViewById(R.id.act_category);

        btnSave = findViewById(R.id.btn_save);

        rvBudgets = findViewById(R.id.rv_budgets);
    }

    private void setupCategoryDropdown() {
        String[] categories = getResources().getStringArray(R.array.expense_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categories);
        actCategory.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        budgetAdapter = new BudgetAdapter(budgets);
        rvBudgets.setLayoutManager(new LinearLayoutManager(this));
        rvBudgets.setAdapter(budgetAdapter);
    }

    private void loadBudgets() {
        executorService.execute(() -> {
            BudgetDao budgetDao = database.budgetDao();
            ExpenseDao expenseDao = database.expenseDao();

            List<Budget> budgetList = budgetDao.getBudgetsForMonthYear(currentMonth, currentYear);
            List<BudgetWithSpent> budgetWithSpentList = new ArrayList<>();

            for (Budget budget : budgetList) {
                double spent = expenseDao.getTotalExpenseForCategoryInMonthYear(
                        budget.getCategory(), currentMonth, currentYear);

                BudgetWithSpent budgetWithSpent = new BudgetWithSpent(
                        budget.getId(),
                        budget.getCategory(),
                        budget.getAmount(),
                        spent,
                        budget.getMonth(),
                        budget.getYear()
                );

                budgetWithSpentList.add(budgetWithSpent);
            }

            runOnUiThread(() -> {
                budgets.clear();
                budgets.addAll(budgetWithSpentList);
                budgetAdapter.notifyDataSetChanged();
            });
        });
    }

    private void saveBudget() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        // Get input values
        String category = actCategory.getText().toString().trim();
        double amount = Double.parseDouble(etAmount.getText().toString().trim());

        // Check if budget already exists for this category, month, and year
        executorService.execute(() -> {
            BudgetDao budgetDao = database.budgetDao();
            ExpenseDao expenseDao = database.expenseDao();

            // Get the total expense for the selected category
            double totalSpent = expenseDao.getTotalExpenseForCategoryInMonthYear(category, currentMonth, currentYear);

            // Check if budget already exists
            Budget existingBudget = budgetDao.getBudgetByCategoryAndMonthYear(category, currentMonth, currentYear);
            if (existingBudget != null) {
                // Update the existing budget by adjusting the remaining amount
                double updatedAmount = existingBudget.getAmount() - totalSpent;
                existingBudget.setAmount(updatedAmount);

                // Update the budget
                budgetDao.update(existingBudget);

                runOnUiThread(() -> {
                    Toast.makeText(
                            BudgetActivity.this,
                            getString(R.string.budget_updated, category),
                            Toast.LENGTH_SHORT
                    ).show();

                    // Clear inputs
                    actCategory.setText("");
                    etAmount.setText("");

                    // Reload budgets
                    loadBudgets();
                });
            } else {
                // Create a new budget if no existing budget is found
                Budget budget = new Budget(category, amount, currentMonth, currentYear);
                budgetDao.insert(budget);

                runOnUiThread(() -> {
                    Toast.makeText(
                            BudgetActivity.this,
                            R.string.budget_saved_successfully,
                            Toast.LENGTH_SHORT
                    ).show();

                    // Clear inputs
                    actCategory.setText("");
                    etAmount.setText("");

                    // Reload budgets
                    loadBudgets();
                });
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate category
        if (actCategory.getText().toString().trim().isEmpty()) {
            tilCategory.setError(getString(R.string.error_category_required));
            isValid = false;
        } else {
            tilCategory.setError(null);
        }

        // Validate amount
        if (etAmount.getText().toString().trim().isEmpty()) {
            tilAmount.setError(getString(R.string.error_amount_required));
            isValid = false;
        } else {
            tilAmount.setError(null);
        }

        return isValid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}