package com.example.cashmate.utils;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtil {

    public static void applyTheme(boolean isDarkTheme) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void applyThemeFromPreferences(boolean isDarkTheme) {
        applyTheme(isDarkTheme);
    }
}