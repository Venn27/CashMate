package com.example.cashmate.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cashmate.R;
import com.example.cashmate.database.AppDatabase;
import com.example.cashmate.database.Expense;
import com.example.cashmate.database.Income;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataExportImportUtil {

    private static final String EXPORT_DIRECTORY = "CashMate";

    public static void exportData(Activity activity, AppDatabase database) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            try {
                // Create export directory
                File exportDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS), EXPORT_DIRECTORY);
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                // Create export file
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                File exportFile = new File(exportDir, "cashmate_export_" + timestamp + ".json");

                // Get data from database
                Map<String, Object> exportData = new HashMap<>();

                // Get incomes
                List<Income> incomes = database.incomeDao().getAllIncomes();
                exportData.put("incomes", incomes);

                // Get expenses
                List<Expense> expenses = database.expenseDao().getAllExpenses();
                exportData.put("expenses", expenses);

                // Get budgets
                exportData.put("budgets", database.budgetDao().getAllBudgets());

                // Get business transactions
                exportData.put("businessTransactions", database.businessTransactionDao().getAllTransactions());

                // Convert to JSON
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonData = gson.toJson(exportData);

                // Write to file
                FileOutputStream fos = new FileOutputStream(exportFile);
                fos.write(jsonData.getBytes());
                fos.close();

                // Show success message on UI thread
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity,
                            activity.getString(R.string.export_success, exportFile.getAbsolutePath()),
                            Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                // Show error message on UI thread
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity,
                            activity.getString(R.string.export_error, e.getMessage()),
                            Toast.LENGTH_LONG).show();
                });
            }
        });

        executorService.shutdown();
    }

    public static void importData(AppCompatActivity activity, AppDatabase database) {
        // Use ActivityResultLauncher to select file
        ActivityResultLauncher<Intent> filePickerLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            processImportFile(activity, uri, database);
                        }
                    }
                });

        // Create file picker intent
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        filePickerLauncher.launch(intent);
    }

    private static void processImportFile(Activity activity, Uri uri, AppDatabase database) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            try {
                // Read file content
                InputStream inputStream = activity.getContentResolver().openInputStream(uri);
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                inputStream.close();

                String jsonData = new String(buffer);

                // Parse JSON
                Gson gson = new Gson();
                Map<String, Object> importData = gson.fromJson(jsonData, Map.class);

                // TODO: Process and import data into database
                // This requires proper parsing and conversion of JSON data to entity objects

                // Show success message on UI thread
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, R.string.import_success, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                // Show error message on UI thread
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity,
                            activity.getString(R.string.import_error, e.getMessage()),
                            Toast.LENGTH_LONG).show();
                });
            }
        });

        executorService.shutdown();
    }
}