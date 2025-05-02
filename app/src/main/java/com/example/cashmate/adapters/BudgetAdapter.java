package com.example.cashmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashmate.R;
import com.example.cashmate.models.BudgetWithSpent;
import com.example.cashmate.utils.CurrencyFormatter;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<BudgetWithSpent> budgets;

    public BudgetAdapter(List<BudgetWithSpent> budgets) {
        this.budgets = budgets;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetWithSpent budget = budgets.get(position);
        holder.bind(budget);
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public class BudgetViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCategory, tvBudgetAmount, tvSpentAmount, tvRemaining;
        private ProgressBar progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvBudgetAmount = itemView.findViewById(R.id.tv_budget_amount);
            tvSpentAmount = itemView.findViewById(R.id.tv_spent_amount);
            tvRemaining = itemView.findViewById(R.id.tv_remaining);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }

        public void bind(BudgetWithSpent budget) {
            // Set category
            tvCategory.setText(budget.getCategory());

            // Set budget amount
            tvBudgetAmount.setText(CurrencyFormatter.format(budget.getBudgetAmount()));

            // Set spent amount
            tvSpentAmount.setText(CurrencyFormatter.format(budget.getSpentAmount()));

            // Set remaining amount
            tvRemaining.setText(CurrencyFormatter.format(budget.getRemainingAmount()));

            // Set progress bar
            int progress = budget.getProgress();
            progressBar.setProgress(progress);

            // Set progress bar color based on usage percentage
            int colorResId;
            if (progress < 70) {
                colorResId = R.color.colorBudgetLow;
            } else if (progress < 90) {
                colorResId = R.color.colorBudgetMedium;
            } else {
                colorResId = R.color.colorBudgetHigh;
            }

            progressBar.setProgressTintList(
                    ContextCompat.getColorStateList(itemView.getContext(), colorResId));
        }
    }
}