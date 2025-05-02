package com.example.cashmate.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashmate.R;
import com.example.cashmate.adapters.TransactionAdapter;
import com.example.cashmate.database.AppDatabase;
import com.example.cashmate.database.dao.ExpenseDao;
import com.example.cashmate.database.dao.IncomeDao;
import com.example.cashmate.models.Transaction;
import com.example.cashmate.utils.CurrencyFormatter;
import com.example.cashmate.utils.DateUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BalanceReportActivity extends AppCompatActivity {

    private TextView tvTotalIncome, tvTotalExpenses, tvBalance;
    private LineChart chartBalance;
    private RecyclerView rvTransactions;

    private AppDatabase database;
    private ExecutorService executorService;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_report);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();

        // Initialize database
        database = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        // Setup recycler view
        setupRecyclerView();

        // Setup chart
        setupChart();

        // Load data
        loadBalanceData();
        loadTransactions();
    }

    private void initViews() {
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpenses = findViewById(R.id.tv_total_expenses);
        tvBalance = findViewById(R.id.tv_balance);

        chartBalance = findViewById(R.id.chart_balance);

        rvTransactions = findViewById(R.id.rv_transactions);
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void setupChart() {
        // Configure chart appearance
        chartBalance.getDescription().setEnabled(false);
        chartBalance.setDrawGridBackground(false);
        chartBalance.getLegend().setEnabled(false);

        // Configure X axis
        XAxis xAxis = chartBalance.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        // Configure Y axis
        chartBalance.getAxisLeft().setAxisMinimum(0f);
        chartBalance.getAxisRight().setEnabled(false);
    }

    private void loadBalanceData() {
        executorService.execute(() -> {
            IncomeDao incomeDao = database.incomeDao();
            ExpenseDao expenseDao = database.expenseDao();

            // Get date range (last 30 days)
            long endDate = System.currentTimeMillis();
            long startDate = endDate - (30 * 24 * 60 * 60 * 1000L); // 30 days in milliseconds

            // Get total income
            double totalIncome = incomeDao.getTotalIncomeInDateRange(startDate, endDate);

            // Get total expenses
            double totalExpenses = expenseDao.getTotalExpenseInDateRange(startDate, endDate);

            // Calculate balance
            double balance = totalIncome - totalExpenses;

            // Get data for chart
            List<Entry> balanceEntries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            // Calculate daily balance for the last 30 days
            double runningBalance = 0;
            for (int i = 0; i < 30; i++) {
                long dayStart = startDate + (i * 24 * 60 * 60 * 1000L);
                long dayEnd = dayStart + (24 * 60 * 60 * 1000L);

                double dayIncome = incomeDao.getTotalIncomeInDateRange(dayStart, dayEnd);
                double dayExpense = expenseDao.getTotalExpenseInDateRange(dayStart, dayEnd);

                runningBalance += (dayIncome - dayExpense);

                balanceEntries.add(new Entry(i, (float) runningBalance));
                labels.add(DateUtils.formatDate(new Date(dayStart), "dd/MM"));
            }

            runOnUiThread(() -> {
                // Update summary values
                tvTotalIncome.setText(CurrencyFormatter.format(totalIncome));
                tvTotalExpenses.setText(CurrencyFormatter.format(totalExpenses));
                tvBalance.setText(CurrencyFormatter.format(balance));

                // Update chart
                updateChart(balanceEntries, labels);
            });
        });
    }

    private void loadTransactions() {
        executorService.execute(() -> {
            IncomeDao incomeDao = database.incomeDao();
            ExpenseDao expenseDao = database.expenseDao();

            // Get date range (last 30 days)
            long endDate = System.currentTimeMillis();
            long startDate = endDate - (30 * 24 * 60 * 60 * 1000L); // 30 days in milliseconds

            // Get combined transactions
            List<Transaction> combinedTransactions = new ArrayList<>();

            // Add incomes
            combinedTransactions.addAll(incomeDao.getIncomesAsTransactions(startDate, endDate));

            // Add expenses
            combinedTransactions.addAll(expenseDao.getExpensesAsTransactions(startDate, endDate));

            // Sort by date (newest first)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                combinedTransactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
            }

            runOnUiThread(() -> {
                transactions.clear();
                transactions.addAll(combinedTransactions);
                transactionAdapter.notifyDataSetChanged();
            });
        });
    }

    private void updateChart(List<Entry> balanceEntries, List<String> labels) {
        // Create dataset
        LineDataSet dataSet = new LineDataSet(balanceEntries, getString(R.string.balance_report));
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setFillAlpha(50);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Create line data
        LineData lineData = new LineData(dataSet);

        // Set X axis labels
        chartBalance.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        // Set chart data
        chartBalance.setData(lineData);

        // Refresh chart
        chartBalance.invalidate();
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