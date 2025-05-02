package com.example.cashmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cashmate.R;
import com.example.cashmate.models.Transaction;
import com.example.cashmate.utils.CurrencyFormatter;
import com.example.cashmate.utils.DateUtils;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivCategoryIcon;
        private TextView tvCategory, tvDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }

        public void bind(Transaction transaction) {
            // Set category
            tvCategory.setText(transaction.getCategory());

            // Set date
            tvDate.setText(DateUtils.formatDate(transaction.getDate(), "dd MMMM yyyy"));

            // Set amount with + or - prefix
            String amountText = transaction.isIncome() ?
                    "+ " + CurrencyFormatter.format(transaction.getAmount()) :
                    "- " + CurrencyFormatter.format(transaction.getAmount());
            tvAmount.setText(amountText);

            // Set text color
            tvAmount.setTextColor(itemView.getContext().getResources().getColor(
                    transaction.isIncome() ? R.color.colorIncome : R.color.colorExpense));

            // Set icon
            ivCategoryIcon.setImageResource(
                    transaction.isIncome() ? R.drawable.ic_income : R.drawable.ic_profit_loss);

            // Set icon background color
            ivCategoryIcon.setBackgroundTintList(itemView.getContext().getResources().getColorStateList(
                    transaction.isIncome() ? R.color.colorIncome : R.color.colorExpense));
        }
    }
}