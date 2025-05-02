package com.example.cashmate;

import android.app.Application;

import com.example.cashmate.database.AppDatabase;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize database
        AppDatabase.getInstance(this);
    }
}