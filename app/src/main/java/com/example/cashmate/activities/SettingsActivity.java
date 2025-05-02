package com.example.cashmate.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.example.cashmate.R;
import com.example.cashmate.database.AppDatabase;
import com.example.cashmate.utils.DataExportImportUtil;
import com.example.cashmate.utils.ThemeUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout layoutLanguage, layoutTheme, layoutCurrency;
    private LinearLayout layoutExportData, layoutImportData, layoutClearData;
    private TextView tvSelectedLanguage, tvSelectedTheme, tvSelectedCurrency, tvAppVersion;

    private SharedPreferences sharedPreferences;
    private ExecutorService executorService;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize database
        database = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        initViews();

        // Set current values
        setCurrentValues();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        layoutLanguage = findViewById(R.id.layout_language);
        layoutTheme = findViewById(R.id.layout_theme);
        layoutCurrency = findViewById(R.id.layout_currency);

        layoutExportData = findViewById(R.id.layout_export_data);
        layoutImportData = findViewById(R.id.layout_import_data);
        layoutClearData = findViewById(R.id.layout_clear_data);

        tvSelectedLanguage = findViewById(R.id.tv_selected_language);
        tvSelectedTheme = findViewById(R.id.tv_selected_theme);
        tvSelectedCurrency = findViewById(R.id.tv_selected_currency);
        tvAppVersion = findViewById(R.id.tv_app_version);

        // Set app version
        tvAppVersion.setText(getAppVersion());
    }

    private void setCurrentValues() {
        // Set current language
        String selectedLanguage = sharedPreferences.getString("language", "id");
        if (selectedLanguage.equals("id")) {
            tvSelectedLanguage.setText(R.string.language_indonesian);
        } else {
            tvSelectedLanguage.setText(R.string.language_english);
        }

        // Set current theme
        boolean isDarkTheme = sharedPreferences.getBoolean("dark_theme", false);
        tvSelectedTheme.setText(isDarkTheme ? R.string.theme_dark : R.string.theme_light);

        // Set current currency
        String selectedCurrency = sharedPreferences.getString("currency", "IDR");
        if (selectedCurrency.equals("IDR")) {
            tvSelectedCurrency.setText(R.string.currency_idr);
        } else {
            tvSelectedCurrency.setText(R.string.currency_usd);
        }
    }

    private void setupListeners() {
        // Language selection
        layoutLanguage.setOnClickListener(v -> {
            String[] languages = {getString(R.string.language_indonesian), getString(R.string.language_english)};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.select_language)
                    .setItems(languages, (dialog, which) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        if (which == 0) {
                            // Indonesian
                            editor.putString("language", "id");
                            tvSelectedLanguage.setText(R.string.language_indonesian);
                        } else {
                            // English
                            editor.putString("language", "en");
                            tvSelectedLanguage.setText(R.string.language_english);
                        }

                        editor.apply();

                        // Show restart needed message
                        Toast.makeText(SettingsActivity.this, R.string.restart_needed, Toast.LENGTH_SHORT).show();
                    });

            builder.create().show();
        });

        // Theme selection
        layoutTheme.setOnClickListener(v -> {
            String[] themes = {getString(R.string.theme_light), getString(R.string.theme_dark)};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.select_theme)
                    .setItems(themes, (dialog, which) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        if (which == 0) {
                            // Light theme
                            editor.putBoolean("dark_theme", false);
                            tvSelectedTheme.setText(R.string.theme_light);
                            ThemeUtil.applyTheme(false);
                        } else {
                            // Dark theme
                            editor.putBoolean("dark_theme", true);
                            tvSelectedTheme.setText(R.string.theme_dark);
                            ThemeUtil.applyTheme(true);
                        }

                        editor.apply();
                    });

            builder.create().show();
        });

        // Currency selection
        layoutCurrency.setOnClickListener(v -> {
            String[] currencies = {getString(R.string.currency_idr), getString(R.string.currency_usd)};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.select_currency)
                    .setItems(currencies, (dialog, which) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        if (which == 0) {
                            // IDR
                            editor.putString("currency", "IDR");
                            tvSelectedCurrency.setText(R.string.currency_idr);
                        } else {
                            // USD
                            editor.putString("currency", "USD");
                            tvSelectedCurrency.setText(R.string.currency_usd);
                        }

                        editor.apply();
                    });

            builder.create().show();
        });

        // Export data
        layoutExportData.setOnClickListener(v -> {
            DataExportImportUtil.exportData(this, database);
        });

        // Import data
        layoutImportData.setOnClickListener(v -> {
            DataExportImportUtil.importData(this, database);
        });

        // Clear data
        layoutClearData.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.clear_data_title)
                    .setMessage(R.string.clear_data_confirmation)
                    .setPositiveButton(R.string.clear, (dialog, which) -> {
                        // Clear all data in database
                        executorService.execute(() -> {
                            database.clearAllTables();

                            runOnUiThread(() -> {
                                Toast.makeText(SettingsActivity.this, R.string.data_cleared, Toast.LENGTH_SHORT).show();
                            });
                        });
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    private String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}