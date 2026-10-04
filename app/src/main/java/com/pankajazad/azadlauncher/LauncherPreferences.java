package com.pankajazad.azadlauncher;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

final class LauncherPreferences {
    private static final String PREFERENCES_NAME = "launcher_preferences";
    private static final String FAVORITE_APP_IDS_KEY = "favorite_app_ids";
    private static final String FAVORITE_APP_ORDER_KEY = "favorite_app_order";
    private static final String HIDDEN_APP_IDS_KEY = "hidden_app_ids";
    private static final String GRID_COLUMNS_KEY = "grid_columns";
    private static final String SHOW_LABELS_KEY = "show_app_labels";
    private static final String THEME_MODE_KEY = "theme_mode";
    private static final String ICON_SIZE_KEY = "icon_size";
    private static final String NOTIFICATION_DOT_PACKAGES_KEY = "notification_dot_packages";
    private static final String SWIPE_DOWN_ACTION_KEY = "swipe_down_action";
    private static final String WEB_SEARCH_PROVIDER_KEY = "web_search_provider";
    private static final String SHOW_CONTEXT_CARD_KEY = "show_context_card";
    private static final String DRAWER_GROUP_KEY = "drawer_group";
    private static final String DRAWER_SORT_KEY = "drawer_sort";
    private static final String QUICK_FOLDER_NAME_KEY = "quick_folder_name";
    private static final String QUICK_FOLDER_APP_IDS_KEY = "quick_folder_app_ids";

    private final SharedPreferences preferences;

    LauncherPreferences(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    Set<String> favoriteAppIds() {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        if (preferences.contains(FAVORITE_APP_ORDER_KEY)) {
            String serialized = preferences.getString(FAVORITE_APP_ORDER_KEY, "");
            if (serialized != null && !serialized.isEmpty()) {
                Collections.addAll(ordered, serialized.split("\\n"));
            }
            return ordered;
        }
        ordered.addAll(new TreeSet<>(
                preferences.getStringSet(FAVORITE_APP_IDS_KEY, Collections.emptySet())));
        return ordered;
    }

    void setFavoriteAppIds(Set<String> appIds) {
        preferences.edit()
                .putStringSet(FAVORITE_APP_IDS_KEY, new HashSet<>(appIds))
                .putString(FAVORITE_APP_ORDER_KEY, serializeFavoriteOrder(appIds))
                .apply();
    }

    Set<String> hiddenAppIds() {
        return new HashSet<>(preferences.getStringSet(HIDDEN_APP_IDS_KEY, Collections.emptySet()));
    }

    void setHiddenAppIds(Set<String> appIds) {
        preferences.edit().putStringSet(HIDDEN_APP_IDS_KEY, new HashSet<>(appIds)).apply();
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

    int themeMode() {
        int storedMode = preferences.getInt(THEME_MODE_KEY, ThemeConfiguration.FOLLOW_SYSTEM);
        return ThemeConfiguration.isValidPreference(storedMode)
                ? storedMode
                : ThemeConfiguration.FOLLOW_SYSTEM;
    }

    void setThemeMode(int themeMode) {
        preferences.edit().putInt(THEME_MODE_KEY, themeMode).apply();
    }

    int iconSize() {
        int storedSize = preferences.getInt(ICON_SIZE_KEY, IconSizeConfiguration.STANDARD);
        return IconSizeConfiguration.isValidPreference(storedSize)
                ? storedSize
                : IconSizeConfiguration.STANDARD;
    }

    void setIconSize(int iconSize) {
        preferences.edit().putInt(ICON_SIZE_KEY, iconSize).apply();
    }

    Set<String> notificationDotPackages() {
        return new HashSet<>(preferences.getStringSet(
                NOTIFICATION_DOT_PACKAGES_KEY, Collections.emptySet()));
    }

    void setNotificationDotPackages(Set<String> packageNames) {
        preferences.edit()
                .putStringSet(NOTIFICATION_DOT_PACKAGES_KEY, new HashSet<>(packageNames))
                .apply();
    }

    int swipeDownAction() {
        int storedAction = preferences.getInt(
                SWIPE_DOWN_ACTION_KEY, GestureActionConfiguration.OPEN_SEARCH);
        return GestureActionConfiguration.isValidPreference(storedAction)
                ? storedAction
                : GestureActionConfiguration.OPEN_SEARCH;
    }

    void setSwipeDownAction(int action) {
        preferences.edit().putInt(SWIPE_DOWN_ACTION_KEY, action).apply();
    }

    int webSearchProvider() {
        int storedProvider = preferences.getInt(
                WEB_SEARCH_PROVIDER_KEY, WebSearchProvider.DISABLED);
        return WebSearchProvider.isValidPreference(storedProvider)
                ? storedProvider
                : WebSearchProvider.DISABLED;
    }

    void setWebSearchProvider(int provider) {
        preferences.edit().putInt(WEB_SEARCH_PROVIDER_KEY, provider).apply();
    }

    boolean showContextCard() {
        return preferences.getBoolean(SHOW_CONTEXT_CARD_KEY, true);
    }

    void setShowContextCard(boolean showContextCard) {
        preferences.edit().putBoolean(SHOW_CONTEXT_CARD_KEY, showContextCard).apply();
    }

    int drawerGroup() {
        int storedGroup = preferences.getInt(DRAWER_GROUP_KEY, DrawerGroupConfiguration.ALL);
        return DrawerGroupConfiguration.isValidPreference(storedGroup)
                ? storedGroup
                : DrawerGroupConfiguration.ALL;
    }

    void setDrawerGroup(int drawerGroup) {
        preferences.edit().putInt(DRAWER_GROUP_KEY, drawerGroup).apply();
    }

    int drawerSort() {
        int storedSort = preferences.getInt(
                DRAWER_SORT_KEY, DrawerSortConfiguration.ALPHABETICAL);
        return DrawerSortConfiguration.isValidPreference(storedSort)
                ? storedSort
                : DrawerSortConfiguration.ALPHABETICAL;
    }

    void setDrawerSort(int drawerSort) {
        preferences.edit().putInt(DRAWER_SORT_KEY, drawerSort).apply();
    }

    String quickFolderName() {
        return QuickFolder.normalizeName(preferences.getString(
                QUICK_FOLDER_NAME_KEY, QuickFolder.DEFAULT_NAME));
    }

    void setQuickFolderName(String name) {
        preferences.edit()
                .putString(QUICK_FOLDER_NAME_KEY, QuickFolder.normalizeName(name))
                .apply();
    }

    Set<String> quickFolderAppIds() {
        return new HashSet<>(preferences.getStringSet(
                QUICK_FOLDER_APP_IDS_KEY, Collections.emptySet()));
    }

    void setQuickFolderAppIds(Set<String> appIds) {
        preferences.edit()
                .putStringSet(QUICK_FOLDER_APP_IDS_KEY, new HashSet<>(appIds))
                .apply();
    }

    LauncherBackupData backupData() {
        return new LauncherBackupData(
                gridColumns(),
                showAppLabels(),
                themeMode(),
                iconSize(),
                swipeDownAction(),
                webSearchProvider(),
                showContextCard(),
                drawerGroup(),
                drawerSort(),
                favoriteAppIds(),
                hiddenAppIds(),
                quickFolderName(),
                quickFolderAppIds());
    }

    void restore(LauncherBackupData backupData) {
        preferences.edit()
                .putInt(GRID_COLUMNS_KEY, backupData.gridColumns())
                .putBoolean(SHOW_LABELS_KEY, backupData.showAppLabels())
                .putInt(THEME_MODE_KEY, backupData.themeMode())
                .putInt(ICON_SIZE_KEY, backupData.iconSize())
                .putInt(SWIPE_DOWN_ACTION_KEY, backupData.swipeDownAction())
                .putInt(WEB_SEARCH_PROVIDER_KEY, backupData.webSearchProvider())
                .putBoolean(SHOW_CONTEXT_CARD_KEY, backupData.showContextCard())
                .putInt(DRAWER_GROUP_KEY, backupData.drawerGroup())
                .putInt(DRAWER_SORT_KEY, backupData.drawerSort())
                .putString(QUICK_FOLDER_NAME_KEY, backupData.quickFolderName())
                .putStringSet(
                        QUICK_FOLDER_APP_IDS_KEY,
                        new HashSet<>(backupData.quickFolderAppIds()))
                .putStringSet(FAVORITE_APP_IDS_KEY, new HashSet<>(backupData.favoriteAppIds()))
                .putString(FAVORITE_APP_ORDER_KEY, serializeFavoriteOrder(backupData.favoriteAppIds()))
                .putStringSet(HIDDEN_APP_IDS_KEY, new HashSet<>(backupData.hiddenAppIds()))
                .apply();
    }

    private static String serializeFavoriteOrder(Set<String> appIds) {
        return String.join("\n", appIds);
    }
}
