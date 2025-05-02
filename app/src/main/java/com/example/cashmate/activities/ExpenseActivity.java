package com.example.cashmate.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cashmate.R;
import com.example.cashmate.database.AppDatabase;
import com.example.cashmate.database.Expense;
import com.example.cashmate.database.dao.ExpenseDao;
import com.example.cashmate.utils.BudgetUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseActivity extends AppCompatActivity {

    private TextInputLayout tilAmount, tilCategory, tilDate, tilNotes;
    private TextInputEditText etAmount, etDate, etNotes;
    private AutoCompleteTextView actCategory;
    private MaterialButton btnSave;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private AppDatabase database;
    private ExecutorService executorService;
    private BudgetUtil budgetUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();

        // Initialize database and utils
        database = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();
        budgetUtil = new BudgetUtil(database);

        // Set current date as default
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        updateDateField();

        // Setup date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Setup category dropdown
        setupCategoryDropdown();

        // Setup save button
        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void initViews() {
        tilAmount = findViewById(R.id.til_amount);
        tilCategory = findViewById(R.id.til_category);
        tilDate = findViewById(R.id.til_date);
        tilNotes = findViewById(R.id.til_notes);

        etAmount = findViewById(R.id.et_amount);
        etDate = findViewById(R.id.et_date);
        etNotes = findViewById(R.id.et_notes);

        actCategory = findViewById(R.id.act_category);

        btnSave = findViewById(R.id.btn_save);
    }

    private void setupCategoryDropdown() {
        String[] categories = getResources().getStringArray(R.array.expense_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categories);
        actCategory.setAdapter(adapter);
    }

    private void showDatePicker() {
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateField();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateField() {
        etDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void saveExpense() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        // Get input values
        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        String category = actCategory.getText().toString().trim();
        Date date = calendar.getTime();
        String notes = etNotes.getText().toString().trim();

        // Check budget limits
        executorService.execute(() -> {
            boolean exceedsBudget = budgetUtil.checkBudgetExceeded(category, amount);

            if (exceedsBudget) {
                runOnUiThread(() -> showBudgetExceededDialog(category, amount));
            } else {
                saveExpenseToDatabase(amount, category, date, notes);
            }
        });
    }

    private void saveExpenseToDatabase(double amount, String category, Date date, String notes) {
        // Create expense object
        final Expense expense = new Expense(amount, category, date, notes);

        // Save to database
        executorService.execute(() -> {
            ExpenseDao expenseDao = database.expenseDao();
            expenseDao.insert(expense);

            runOnUiThread(() -> {
                Toast.makeText(
                        ExpenseActivity.this,
                        R.string.expense_saved_successfully,
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            });
        });
    }

    private void showBudgetExceededDialog(String category, double amount) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.budget_exceeded)
                .setMessage(getString(R.string.budget_exceeded_message, category))
                .setPositiveButton(R.string.save_anyway, (dialog, which) -> {
                    saveExpenseToDatabase(amount, category, calendar.getTime(),
                            etNotes.getText().toString().trim());
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate amount
        if (etAmount.getText().toString().trim().isEmpty()) {
            tilAmount.setError(getString(R.string.error_amount_required));
            isValid = false;
        } else {
            tilAmount.setError(null);
        }

        // Validate category
        if (actCategory.getText().toString().trim().isEmpty()) {
            tilCategory.setError(getString(R.string.error_category_required));
            isValid = false;
        } else {
            tilCategory.setError(null);
        }

        // Validate date
        if (etDate.getText().toString().trim().isEmpty()) {
            tilDate.setError(getString(R.string.error_date_required));
            isValid = false;
        } else {
            tilDate.setError(null);
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