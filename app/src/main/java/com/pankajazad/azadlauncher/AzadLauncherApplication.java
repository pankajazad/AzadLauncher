package com.pankajazad.azadlauncher;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public final class AzadLauncherApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LauncherPreferences preferences = new LauncherPreferences(this);
        AppCompatDelegate.setDefaultNightMode(
                ThemeConfiguration.appCompatNightMode(preferences.themeMode()));
    }
}
