package com.example.cashmate.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cashmate.R;
import com.example.cashmate.database.AppDatabase;
import com.example.cashmate.database.Income;
import com.example.cashmate.database.dao.IncomeDao;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncomeActivity extends AppCompatActivity {

    private TextInputLayout tilAmount, tilSource, tilDate, tilNotes;
    private TextInputEditText etAmount, etDate, etNotes;
    private AutoCompleteTextView actSource;
    private MaterialButton btnSave;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();

        // Initialize database
        database = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        // Set current date as default
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        updateDateField();

        // Setup date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Setup source dropdown
        setupSourceDropdown();

        // Setup save button
        btnSave.setOnClickListener(v -> saveIncome());
    }

    private void initViews() {
        tilAmount = findViewById(R.id.til_amount);
        tilSource = findViewById(R.id.til_source);
        tilDate = findViewById(R.id.til_date);
        tilNotes = findViewById(R.id.til_notes);

        etAmount = findViewById(R.id.et_amount);
        etDate = findViewById(R.id.et_date);
        etNotes = findViewById(R.id.et_notes);

        actSource = findViewById(R.id.act_source);

        btnSave = findViewById(R.id.btn_save);
    }

    private void setupSourceDropdown() {
        String[] sources = getResources().getStringArray(R.array.income_sources);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, sources);
        actSource.setAdapter(adapter);
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

    private void saveIncome() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        // Get input values
        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        String source = actSource.getText().toString().trim();
        Date date = calendar.getTime();
        String notes = etNotes.getText().toString().trim();

        // Create income object
        final Income income = new Income(amount, source, date, notes);

        // Save to database
        executorService.execute(() -> {
            IncomeDao incomeDao = database.incomeDao();
            incomeDao.insert(income);

            runOnUiThread(() -> {
                Toast.makeText(
                        IncomeActivity.this,
                        R.string.income_saved_successfully,
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            });
        });
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

        // Validate source
        if (actSource.getText().toString().trim().isEmpty()) {
            tilSource.setError(getString(R.string.error_source_required));
            isValid = false;
        } else {
            tilSource.setError(null);
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