package com.example.cashmate.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    static {
        currencyFormatter.setMaximumFractionDigits(0);
    }

    public static String format(double amount) {
        return currencyFormatter.format(amount);
    }
}