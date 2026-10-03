package com.pankajazad.azadlauncher;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class FavoriteApps {
    private final LinkedHashSet<String> appIds = new LinkedHashSet<>();

    FavoriteApps(Collection<String> storedAppIds) {
        if (storedAppIds != null) {
            appIds.addAll(storedAppIds);
        }
    }

    boolean contains(String appId) {
        return appIds.contains(appId);
    }

    boolean toggle(String appId) {
        if (appIds.remove(appId)) {
            return false;
        }
        appIds.add(appId);
        return true;
    }

    boolean retainAvailable(Collection<String> availableAppIds) {
        return appIds.retainAll(new LinkedHashSet<>(availableAppIds));
    }

    boolean reorder(List<String> orderedAppIds) {
        LinkedHashSet<String> reordered = new LinkedHashSet<>();
        for (String appId : orderedAppIds) {
            if (appIds.contains(appId)) {
                reordered.add(appId);
            }
        }
        reordered.addAll(appIds);
        if (new java.util.ArrayList<>(reordered).equals(new java.util.ArrayList<>(appIds))) {
            return false;
        }
        appIds.clear();
        appIds.addAll(reordered);
        return true;
    }

    Set<String> snapshot() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(appIds));
    }
}
