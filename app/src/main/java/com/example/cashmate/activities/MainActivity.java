package com.example.cashmate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cashmate.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Setup Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Setup UI changes based on destination
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Set toolbar title based on destination
            if (destination.getId() == R.id.navigation_dashboard) {
                toolbar.setTitle(R.string.dashboard);
            } else if (destination.getId() == R.id.navigation_transactions) {
                toolbar.setTitle(R.string.transactions);
            } else if (destination.getId() == R.id.navigation_budget) {
                toolbar.setTitle(R.string.budget);
            } else if (destination.getId() == R.id.navigation_reports) {
                toolbar.setTitle(R.string.reports);
            }
        });

        // Hide the middle item for FAB placeholder
        Menu menu = bottomNavigationView.getMenu();
        menu.findItem(R.id.navigation_placeholder).setEnabled(false);

        // Setup FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showTransactionOptions());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_balance_report) {
            startActivity(new Intent(MainActivity.this, BalanceReportActivity.class));
            return true;
        } else if (id == R.id.action_profit_loss_report) {
            startActivity(new Intent(MainActivity.this, ProfitLossReportActivity.class));
            return true;
        } else if (id == R.id.action_budget) {
            startActivity(new Intent(MainActivity.this, BudgetActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            // Jika Anda sudah membuat SettingsActivity
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, (AppBarConfiguration) null);
    }

    private void showTransactionOptions() {
        String[] options = new String[] {
                getString(R.string.add_income),
                getString(R.string.add_expense),
                getString(R.string.business_transaction)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_transaction)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Add Income
                            startActivity(new Intent(MainActivity.this, IncomeActivity.class));
                            break;
                        case 1: // Add Expense
                            startActivity(new Intent(MainActivity.this, ExpenseActivity.class));
                            break;
                        case 2: // Business Transaction
                            startActivity(new Intent(MainActivity.this, BusinessTransactionActivity.class));
                            break;
                    }
                })
                .show();
    }
}