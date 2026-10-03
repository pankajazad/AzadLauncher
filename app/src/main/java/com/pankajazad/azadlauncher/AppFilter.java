package com.pankajazad.azadlauncher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class AppFilter {
    private AppFilter() { }

    static boolean matches(CharSequence label, String packageName, String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isEmpty()) {
            return true;
        }
        String normalizedLabel = label == null ? "" : label.toString().toLowerCase(Locale.ROOT);
        String normalizedPackage = packageName == null ? "" : packageName.toLowerCase(Locale.ROOT);
        return normalizedLabel.contains(normalizedQuery) || normalizedPackage.contains(normalizedQuery);
    }

    static List<AppEntry> filter(List<AppEntry> apps, String query) {
        return filter(apps, query, DrawerGroupConfiguration.ALL);
    }

    static List<AppEntry> filter(List<AppEntry> apps, String query, int drawerGroup) {
        List<AppEntry> filtered = new ArrayList<>();
        for (AppEntry app : apps) {
            if (DrawerGroupConfiguration.includes(drawerGroup, app.getDrawerGroup())
                    && matches(app.getLabel(), app.getPackageName(), query)) {
                filtered.add(app);
            }
        }
        return filtered;
    }
}
