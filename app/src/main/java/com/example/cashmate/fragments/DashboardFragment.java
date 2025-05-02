package com.example.cashmate.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashmate.R;
import com.example.cashmate.adapters.BudgetAdapter;
import com.example.cashmate.adapters.TransactionAdapter;
import com.example.cashmate.database.AppDatabase;
import com.example.cashmate.database.dao.BudgetDao;
import com.example.cashmate.database.dao.ExpenseDao;
import com.example.cashmate.database.dao.IncomeDao;
import com.example.cashmate.models.BudgetWithSpent;
import com.example.cashmate.models.Transaction;
import com.example.cashmate.utils.CurrencyFormatter;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {

    private TextView tvBalance, tvIncome, tvExpenses;
    private RecyclerView rvRecentTransactions, rvBudgetStatus;
    private MaterialCardView cardBalance;

    private AppDatabase database;
    private ExecutorService executorService;

    private TransactionAdapter transactionAdapter;
    private BudgetAdapter budgetAdapter;

    private List<Transaction> recentTransactions = new ArrayList<>();
    private List<BudgetWithSpent> budgets = new ArrayList<>();

    private int currentMonth, currentYear;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize views
        initViews(view);

        // Initialize database
        database = AppDatabase.getDatabase(requireContext());
        executorService = Executors.newFixedThreadPool(2);

        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);

        // Setup recycler views
        setupRecyclerViews();

        // Load data
        loadFinancialSummary();
        loadRecentTransactions();
        loadBudgets();

        return view;
    }

    private void initViews(View view) {
        tvBalance = view.findViewById(R.id.tv_balance);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpenses = view.findViewById(R.id.tv_expenses);

        rvRecentTransactions = view.findViewById(R.id.rv_recent_transactions);
        rvBudgetStatus = view.findViewById(R.id.rv_budget_status);

        cardBalance = view.findViewById(R.id.card_balance);
    }

    private void setupRecyclerViews() {
        // Setup recent transactions recycler view
        transactionAdapter = new TransactionAdapter(recentTransactions);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentTransactions.setAdapter(transactionAdapter);

        // Setup budget status recycler view
        budgetAdapter = new BudgetAdapter(budgets);
        rvBudgetStatus.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBudgetStatus.setAdapter(budgetAdapter);
    }

    private void loadFinancialSummary() {
        executorService.execute(() -> {
            IncomeDao incomeDao = database.incomeDao();
            ExpenseDao expenseDao = database.expenseDao();

            // Get total income (all time)
            double totalIncome = incomeDao.getTotalIncome();

            // Get total expenses (all time)
            double totalExpenses = expenseDao.getTotalExpense();

            // Calculate balance
            double balance = totalIncome - totalExpenses;

            // Get monthly income
            double monthlyIncome = incomeDao.getTotalIncomeForMonthYear(currentMonth, currentYear);

            // Get monthly expenses
            double monthlyExpenses = expenseDao.getTotalExpenseForMonthYear(currentMonth, currentYear);

            // Update UI
            getActivity().runOnUiThread(() -> {
                tvBalance.setText(CurrencyFormatter.format(balance));
                tvIncome.setText(CurrencyFormatter.format(monthlyIncome));
                tvExpenses.setText(CurrencyFormatter.format(monthlyExpenses));
            });
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadRecentTransactions() {
        executorService.execute(() -> {
            IncomeDao incomeDao = database.incomeDao();
            ExpenseDao expenseDao = database.expenseDao();

            // Get combined recent transactions
            List<Transaction> combinedTransactions = new ArrayList<>();

            // Add incomes
            combinedTransactions.addAll(incomeDao.getRecentIncomesAsTransactions(5));

            // Add expenses
            combinedTransactions.addAll(expenseDao.getRecentExpensesAsTransactions(5));

            // Sort by date (newest first)
            combinedTransactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));

            // Limit to 5 most recent
            if (combinedTransactions.size() > 5) {
                combinedTransactions = combinedTransactions.subList(0, 5);
            }

            // Update UI
            List<Transaction> finalTransactions = combinedTransactions;
            getActivity().runOnUiThread(() -> {
                recentTransactions.clear();
                recentTransactions.addAll(finalTransactions);
                transactionAdapter.notifyDataSetChanged();
            });
        });
    }

    private void loadBudgets() {
        executorService.execute(() -> {
            BudgetDao budgetDao = database.budgetDao();
            ExpenseDao expenseDao = database.expenseDao();

            // Get budgets for current month and year
            List<BudgetWithSpent> budgetList = new ArrayList<>();

            // Get all budgets
            List<com.example.cashmate.database.Budget> rawBudgets = budgetDao.getBudgetsForMonthYear(currentMonth, currentYear);

            // Calculate spent amount for each budget
            for (com.example.cashmate.database.Budget budget : rawBudgets) {
                double spent = expenseDao.getTotalExpenseForCategoryInMonthYear(
                        budget.getCategory(), currentMonth, currentYear);

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
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}