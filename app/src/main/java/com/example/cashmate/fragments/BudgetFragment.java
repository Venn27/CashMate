package com.example.cashmate.fragments;

import android.content.Intent;
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
import com.example.cashmate.activities.BudgetActivity;
import com.example.cashmate.adapters.BudgetAdapter;
import com.example.cashmate.database.AppDatabase;
import com.example.cashmate.database.dao.BudgetDao;
import com.example.cashmate.database.dao.ExpenseDao;
import com.example.cashmate.database.dao.BudgetDao;
import com.example.cashmate.database.dao.ExpenseDao;
import com.example.cashmate.models.BudgetWithSpent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetFragment extends Fragment {

    private Spinner spinnerMonth, spinnerYear;
    private RecyclerView rvBudgets;
    private FloatingActionButton fabAddBudget;

    private AppDatabase database;
    private ExecutorService executorService;

    private BudgetAdapter budgetAdapter;
    private List<BudgetWithSpent> budgets = new ArrayList<>();

    private String[] months;
    private String[] years;

    private int selectedMonth;
    private int selectedYear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        // Initialize views
        initViews(view);

        // Initialize database
        database = AppDatabase.getDatabase(requireContext());
        executorService = Executors.newSingleThreadExecutor();

        // Setup spinners
        setupSpinners();

        // Setup recycler view
        setupRecyclerView();

        // Setup fab
        fabAddBudget.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BudgetActivity.class);
            startActivity(intent);
        });

        // Load initial data
        loadBudgets();

        return view;
    }

    private void initViews(View view) {
        spinnerMonth = view.findViewById(R.id.spinner_month);
        spinnerYear = view.findViewById(R.id.spinner_year);
        rvBudgets = view.findViewById(R.id.rv_budgets);
        fabAddBudget = view.findViewById(R.id.fab_add_budget);
    }

    private void setupSpinners() {
        // Setup month spinner
        months = getResources().getStringArray(R.array.months);
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Setup year spinner
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        years = new String[] {
                String.valueOf(currentYear - 1),
                String.valueOf(currentYear),
                String.valueOf(currentYear + 1)
        };
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Set initial selection to current month and year
        Calendar calendar = Calendar.getInstance();
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedYear = calendar.get(Calendar.YEAR);

        spinnerMonth.setSelection(selectedMonth);
        spinnerYear.setSelection(1); // Current year is in the middle

        // Set listeners
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = position;
                loadBudgets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = Integer.parseInt(years[position]);
                loadBudgets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupRecyclerView() {
        budgetAdapter = new BudgetAdapter(budgets);
        rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBudgets.setAdapter(budgetAdapter);
    }

    private void loadBudgets() {
        executorService.execute(() -> {
            BudgetDao budgetDao = database.budgetDao();
            ExpenseDao expenseDao = database.expenseDao();

            // Get budgets for selected month and year
            List<com.example.cashmate.database.Budget> rawBudgets =
                    budgetDao.getBudgetsForMonthYear(selectedMonth, selectedYear);

            // Convert to BudgetWithSpent
            List<BudgetWithSpent> budgetList = new ArrayList<>();
            for (com.example.cashmate.database.Budget budget : rawBudgets) {
                double spent = expenseDao.getTotalExpenseForCategoryInMonthYear(
                        budget.getCategory(), selectedMonth, selectedYear);

                BudgetWithSpent budgetWithSpent = new BudgetWithSpent(
                        budget.getId(),
                        budget.getCategory(),
                        budget.getAmount(),
                        spent,
                        budget.getMonth(),
                        budget.getYear()
                );

                budgetList.add(budgetWithSpent);
            }

            // Update UI
            getActivity().runOnUiThread(() -> {
                budgets.clear();
                budgets.addAll(budgetList);
                budgetAdapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data when coming back from budget activity
        loadBudgets();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}