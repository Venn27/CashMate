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
import com.example.cashmate.database.BusinessTransaction;
import com.example.cashmate.database.dao.BusinessTransactionDao;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusinessTransactionActivity extends AppCompatActivity {

    private TextInputLayout tilProductName, tilQuantity, tilPrice, tilTransactionType, tilDate, tilDescription;
    private TextInputEditText etProductName, etQuantity, etPrice, etDate, etDescription;
    private AutoCompleteTextView actTransactionType;
    private MaterialButton btnSave;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_transaction);

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

        // Setup transaction type dropdown
        setupTransactionTypeDropdown();

        // Setup save button
        btnSave.setOnClickListener(v -> saveBusinessTransaction());
    }

    private void initViews() {
        tilProductName = findViewById(R.id.til_product_name);
        tilQuantity = findViewById(R.id.til_quantity);
        tilPrice = findViewById(R.id.til_price);
        tilTransactionType = findViewById(R.id.til_transaction_type);
        tilDate = findViewById(R.id.til_date);
        tilDescription = findViewById(R.id.til_description);

        etProductName = findViewById(R.id.et_product_name);
        etQuantity = findViewById(R.id.et_quantity);
        etPrice = findViewById(R.id.et_price);
        etDate = findViewById(R.id.et_date);
        etDescription = findViewById(R.id.et_description);

        actTransactionType = findViewById(R.id.act_transaction_type);

        btnSave = findViewById(R.id.btn_save);
    }

    private void setupTransactionTypeDropdown() {
        String[] types = getResources().getStringArray(R.array.business_transaction_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, types);
        actTransactionType.setAdapter(adapter);
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

    private void saveBusinessTransaction() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        // Get input values
        String productName = etProductName.getText().toString().trim();
        int quantity = Integer.parseInt(etQuantity.getText().toString().trim());
        double price = Double.parseDouble(etPrice.getText().toString().trim());
        String transactionType = actTransactionType.getText().toString().trim();
        Date date = calendar.getTime();
        String description = etDescription.getText().toString().trim();

        // Calculate total amount
        double totalAmount = quantity * price;

        // Create business transaction object
        final BusinessTransaction transaction = new BusinessTransaction(
                productName, quantity, price, totalAmount, transactionType, date, description);

        // Save to database
        executorService.execute(() -> {
            BusinessTransactionDao dao = database.businessTransactionDao();
            dao.insert(transaction);

            runOnUiThread(() -> {
                Toast.makeText(
                        BusinessTransactionActivity.this,
                        R.string.transaction_saved_successfully,
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            });
        });
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate product name
        if (etProductName.getText().toString().trim().isEmpty()) {
            tilProductName.setError(getString(R.string.error_product_name_required));
            isValid = false;
        } else {
            tilProductName.setError(null);
        }

        // Validate quantity
        if (etQuantity.getText().toString().trim().isEmpty()) {
            tilQuantity.setError(getString(R.string.error_quantity_required));
            isValid = false;
        } else {
            tilQuantity.setError(null);
        }

        // Validate price
        if (etPrice.getText().toString().trim().isEmpty()) {
            tilPrice.setError(getString(R.string.error_price_required));
            isValid = false;
        } else {
            tilPrice.setError(null);
        }

        // Validate transaction type
        if (actTransactionType.getText().toString().trim().isEmpty()) {
            tilTransactionType.setError(getString(R.string.error_transaction_type_required));
            isValid = false;
        } else {
            tilTransactionType.setError(null);
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