package com.pankajazad.azadlauncher;

import androidx.appcompat.app.AppCompatDelegate;

final class ThemeConfiguration {
    static final int FOLLOW_SYSTEM = 0;
    static final int LIGHT = 1;
    static final int DARK = 2;

    private ThemeConfiguration() { }

    static boolean isValidPreference(int themeMode) {
        return themeMode == FOLLOW_SYSTEM || themeMode == LIGHT || themeMode == DARK;
    }

    static int spinnerIndex(int themeMode) {
        return isValidPreference(themeMode) ? themeMode : FOLLOW_SYSTEM;
    }

    static int preferenceForSpinnerIndex(int index) {
        return isValidPreference(index) ? index : FOLLOW_SYSTEM;
    }

    static int appCompatNightMode(int themeMode) {
        switch (themeMode) {
            case LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}
