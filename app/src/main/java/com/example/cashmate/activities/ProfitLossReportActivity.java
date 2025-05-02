package com.example.cashmate.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cashmate.R;
import com.example.cashmate.database.AppDatabase;
import com.example.cashmate.database.dao.BusinessTransactionDao;
import com.example.cashmate.utils.CurrencyFormatter;
import com.example.cashmate.utils.DateUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfitLossReportActivity extends AppCompatActivity {

    private TextView tvTotalRevenue, tvTotalExpenses, tvNetProfit;
    private AutoCompleteTextView actPeriod;
    private BarChart chartProfitLoss;

    private AppDatabase database;
    private ExecutorService executorService;

    private String[] periods;
    private int selectedPeriodIndex = 0; // Default to daily

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profit_loss_report);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();

        // Initialize database
        database = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        // Setup period dropdown
        setupPeriodDropdown();

        // Setup chart
        setupChart();

        // Load initial data
        loadProfitLossData();
    }

    private void initViews() {
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvTotalExpenses = findViewById(R.id.tv_total_expenses);
        tvNetProfit = findViewById(R.id.tv_net_profit);

        actPeriod = findViewById(R.id.act_period);

        chartProfitLoss = findViewById(R.id.chart_profit_loss);
    }

    private void setupPeriodDropdown() {
        periods = getResources().getStringArray(R.array.report_periods);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, periods);
        actPeriod.setAdapter(adapter);

        // Set default value
        actPeriod.setText(periods[selectedPeriodIndex], false);

        // Set listener
        actPeriod.setOnItemClickListener((parent, view, position, id) -> {
            selectedPeriodIndex = position;
            loadProfitLossData();
        });
    }

    private void setupChart() {
        // Configure chart appearance
        chartProfitLoss.getDescription().setEnabled(false);
        chartProfitLoss.setDrawGridBackground(false);
        chartProfitLoss.getLegend().setEnabled(true);

        // Configure X axis
        XAxis xAxis = chartProfitLoss.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        // Configure Y axis
        chartProfitLoss.getAxisLeft().setAxisMinimum(0f);
        chartProfitLoss.getAxisRight().setEnabled(false);
    }

    private void loadProfitLossData() {
        executorService.execute(() -> {
            BusinessTransactionDao dao = database.businessTransactionDao();

            // Get date range based on selected period
            long[] dateRange = DateUtils.getDateRangeForPeriod(selectedPeriodIndex);
            long startDate = dateRange[0];
            long endDate = dateRange[1];

            // Get total revenue (sales)
            double totalRevenue = dao.getTotalAmountForTypeInDateRange("Penjualan", startDate, endDate);

            // Get total expenses (purchases)
            double totalExpenses = dao.getTotalAmountForTypeInDateRange("Pembelian", startDate, endDate);

            // Calculate net profit
            double netProfit = totalRevenue - totalExpenses;

            // Get data for chart
            List<BarEntry> revenueEntries = new ArrayList<>();
            List<BarEntry> expenseEntries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            // Get data points based on period
            int numPoints = DateUtils.getNumberOfPointsForPeriod(selectedPeriodIndex);
            for (int i = 0; i < numPoints; i++) {
                long[] pointDateRange = DateUtils.getDateRangeForPoint(selectedPeriodIndex, i);

                double pointRevenue = dao.getTotalAmountForTypeInDateRange(
                        "Penjualan", pointDateRange[0], pointDateRange[1]);
                double pointExpense = dao.getTotalAmountForTypeInDateRange(
                        "Pembelian", pointDateRange[0], pointDateRange[1]);

                revenueEntries.add(new BarEntry(i, (float) pointRevenue));
                expenseEntries.add(new BarEntry(i, (float) pointExpense));
                labels.add(DateUtils.getLabelForPoint(selectedPeriodIndex, i));
            }

            runOnUiThread(() -> {
                // Update summary values
                tvTotalRevenue.setText(CurrencyFormatter.format(totalRevenue));
                tvTotalExpenses.setText(CurrencyFormatter.format(totalExpenses));
                tvNetProfit.setText(CurrencyFormatter.format(netProfit));

                // Set text color for net profit (green for profit, red for loss)
                tvNetProfit.setTextColor(netProfit >= 0 ?
                        getResources().getColor(R.color.colorIncome) :
                        getResources().getColor(R.color.colorExpense));

                // Update chart
                updateChart(revenueEntries, expenseEntries, labels);
            });
        });
    }

    private void updateChart(List<BarEntry> revenueEntries, List<BarEntry> expenseEntries, List<String> labels) {
        // Create revenue dataset
        BarDataSet revenueDataSet = new BarDataSet(revenueEntries, getString(R.string.revenue));
        revenueDataSet.setColor(getResources().getColor(R.color.colorIncome));
        revenueDataSet.setValueTextColor(Color.BLACK);
        revenueDataSet.setValueTextSize(10f);

        // Create expense dataset
        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, getString(R.string.expenses));
        expenseDataSet.setColor(getResources().getColor(R.color.colorExpense));
        expenseDataSet.setValueTextColor(Color.BLACK);
        expenseDataSet.setValueTextSize(10f);

        // Create bar data
        BarData barData = new BarData(revenueDataSet, expenseDataSet);
        barData.setBarWidth(0.3f);

        // Set X axis labels
        chartProfitLoss.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        // Set chart data
        chartProfitLoss.setData(barData);

        // Group bars
        float groupSpace = 0.4f;
        float barSpace = 0f;
        chartProfitLoss.getXAxis().setAxisMinimum(0);
        chartProfitLoss.getXAxis().setAxisMaximum(labels.size());
        chartProfitLoss.groupBars(0, groupSpace, barSpace);

        // Refresh chart
        chartProfitLoss.invalidate();
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