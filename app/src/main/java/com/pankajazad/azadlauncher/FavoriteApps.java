package com.pankajazad.azadlauncher;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
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

    Set<String> snapshot() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(appIds));
    }
}
