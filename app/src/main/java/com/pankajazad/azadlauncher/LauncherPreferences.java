package com.pankajazad.azadlauncher;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class LauncherPreferences {
    private static final String PREFERENCES_NAME = "launcher_preferences";
    private static final String FAVORITE_APP_IDS_KEY = "favorite_app_ids";
    private static final String GRID_COLUMNS_KEY = "grid_columns";
    private static final String SHOW_LABELS_KEY = "show_app_labels";

    private final SharedPreferences preferences;

    LauncherPreferences(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    Set<String> favoriteAppIds() {
        return new HashSet<>(preferences.getStringSet(FAVORITE_APP_IDS_KEY, Collections.emptySet()));
    }

    void setFavoriteAppIds(Set<String> appIds) {
        preferences.edit().putStringSet(FAVORITE_APP_IDS_KEY, new HashSet<>(appIds)).apply();
    }

    int gridColumns() {
        return preferences.getInt(GRID_COLUMNS_KEY, GridConfiguration.AUTOMATIC);
    }

    void setGridColumns(int columns) {
        preferences.edit().putInt(GRID_COLUMNS_KEY, columns).apply();
    }

    boolean showAppLabels() {
        return preferences.getBoolean(SHOW_LABELS_KEY, true);
    }

    void setShowAppLabels(boolean showLabels) {
        preferences.edit().putBoolean(SHOW_LABELS_KEY, showLabels).apply();
    }
}
