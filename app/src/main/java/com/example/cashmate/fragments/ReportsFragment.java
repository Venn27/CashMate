package com.example.cashmate.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cashmate.R;
import com.example.cashmate.activities.BalanceReportActivity;
import com.example.cashmate.activities.ProfitLossReportActivity;
import com.google.android.material.card.MaterialCardView;

public class ReportsFragment extends Fragment {

    private MaterialCardView cardBalanceReport, cardProfitLossReport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        // Initialize views
        initViews(view);

        // Setup click listeners
        cardBalanceReport.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BalanceReportActivity.class);
            startActivity(intent);
        });

        cardProfitLossReport.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfitLossReportActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void initViews(View view) {
        cardBalanceReport = view.findViewById(R.id.card_balance_report);
        cardProfitLossReport = view.findViewById(R.id.card_profit_loss_report);
    }
}