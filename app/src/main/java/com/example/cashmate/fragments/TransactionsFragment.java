package com.example.cashmate.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashmate.R;
import com.example.cashmate.adapters.TransactionAdapter;
import com.example.cashmate.database.AppDatabase;
import com.example.cashmate.database.dao.ExpenseDao;
import com.example.cashmate.database.dao.IncomeDao;
import com.example.cashmate.models.Transaction;
import com.example.cashmate.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionsFragment extends Fragment {

    private Spinner spinnerFilter;
    private RecyclerView rvTransactions;

    private AppDatabase database;
    private ExecutorService executorService;

    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactions = new ArrayList<>();

    private static final int FILTER_ALL = 0;
    private static final int FILTER_INCOME = 1;
    private static final int FILTER_EXPENSE = 2;
    private static final int FILTER_TODAY = 3;
    private static final int FILTER_THIS_WEEK = 4;
    private static final int FILTER_THIS_MONTH = 5;

    private int currentFilter = FILTER_ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        // Initialize views
        initViews(view);

        // Initialize database
        database = AppDatabase.getDatabase(requireContext());
        executorService = Executors.newSingleThreadExecutor();

        // Setup spinner
        setupFilterSpinner();

        // Setup recycler view
        setupRecyclerView();

        // Load initial data
        loadTransactions();

        return view;
    }

    private void initViews(View view) {
        spinnerFilter = view.findViewById(R.id.spinner_filter);
        rvTransactions = view.findViewById(R.id.rv_transactions);
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.transaction_filters,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = position;
                loadTransactions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void loadTransactions() {
        executorService.execute(() -> {
            IncomeDao incomeDao = database.incomeDao();
            ExpenseDao expenseDao = database.expenseDao();

            // Get date ranges based on filter
            long[] dateRange = getDateRangeForFilter(currentFilter);
            long startDate = dateRange[0];
            long endDate = dateRange[1];

            // Get combined transactions
            List<Transaction> combinedTransactions = new ArrayList<>();

            // Add incomes (if filter allows)
            if (currentFilter == FILTER_ALL || currentFilter == FILTER_INCOME ||
                    currentFilter == FILTER_TODAY || currentFilter == FILTER_THIS_WEEK ||
                    currentFilter == FILTER_THIS_MONTH) {
                combinedTransactions.addAll(incomeDao.getIncomesAsTransactions(startDate, endDate));
            }

            // Add expenses (if filter allows)
            if (currentFilter == FILTER_ALL || currentFilter == FILTER_EXPENSE ||
                    currentFilter == FILTER_TODAY || currentFilter == FILTER_THIS_WEEK ||
                    currentFilter == FILTER_THIS_MONTH) {
                combinedTransactions.addAll(expenseDao.getExpensesAsTransactions(startDate, endDate));
            }

            // Sort by date (newest first)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                combinedTransactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
            }

            // Update UI
            getActivity().runOnUiThread(() -> {
                transactions.clear();
                transactions.addAll(combinedTransactions);
                transactionAdapter.notifyDataSetChanged();
            });
        });
    }

    private long[] getDateRangeForFilter(int filter) {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();

        switch (filter) {
            case FILTER_TODAY:
                // Start of today
                calendar.setTime(new Date(now));
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startOfDay = calendar.getTimeInMillis();

                return new long[]{startOfDay, now};

            case FILTER_THIS_WEEK:
                // Start of week
                calendar.setTime(new Date(now));
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startOfWeek = calendar.getTimeInMillis();

                return new long[]{startOfWeek, now};

            case FILTER_THIS_MONTH:
                // Start of month
                calendar.setTime(new Date(now));
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startOfMonth = calendar.getTimeInMillis();

                return new long[]{startOfMonth, now};

            case FILTER_ALL:
            case FILTER_INCOME:
            case FILTER_EXPENSE:
            default:
                // All time
                return new long[]{0, now};
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}