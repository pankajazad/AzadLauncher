package com.pankajazad.azadlauncher;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class DrawerSortConfiguration {
    static final int ALPHABETICAL = 0;
    static final int REVERSE_ALPHABETICAL = 1;
    static final int CATEGORY_THEN_NAME = 2;
    static final int RECENTLY_USED = 3;

    private DrawerSortConfiguration() { }

    static boolean isValidPreference(int sortMode) {
        return sortMode >= ALPHABETICAL && sortMode <= RECENTLY_USED;
    }

    static int spinnerIndex(int sortMode) {
        return isValidPreference(sortMode) ? sortMode : ALPHABETICAL;
    }

    static int preferenceForSpinnerIndex(int index) {
        return isValidPreference(index) ? index : ALPHABETICAL;
    }

    static List<AppEntry> sort(List<AppEntry> apps, int sortMode) {
        return sort(apps, sortMode, new ArrayList<>());
    }

    static List<AppEntry> sort(
            List<AppEntry> apps, int sortMode, List<String> recentAppIds) {
        List<AppEntry> sorted = new ArrayList<>(apps);
        Collator collator = Collator.getInstance();
        Comparator<AppEntry> byName = (left, right) ->
                collator.compare(left.getLabel(), right.getLabel());
        switch (sortMode) {
            case REVERSE_ALPHABETICAL:
                sorted.sort(byName.reversed());
                break;
            case CATEGORY_THEN_NAME:
                sorted.sort(Comparator.comparingInt(AppEntry::getDrawerGroup).thenComparing(byName));
                break;
            case RECENTLY_USED:
                Map<String, Integer> recentPositions = new HashMap<>();
                for (int index = 0; index < recentAppIds.size(); index++) {
                    recentPositions.putIfAbsent(recentAppIds.get(index), index);
                }
                sorted.sort(Comparator
                        .comparingInt((AppEntry app) -> recentPositions.getOrDefault(
                                app.getId(), Integer.MAX_VALUE))
                        .thenComparing(byName));
                break;
            default:
                sorted.sort(byName);
                break;
        }
        return sorted;
    }
}
