package com.pankajazad.azadlauncher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

final class RecentApps {
    static final int MAX_ENTRIES = 50;

    private final List<String> appIds = new ArrayList<>();

    RecentApps(Collection<String> storedAppIds) {
        if (storedAppIds == null) {
            return;
        }
        for (String appId : new LinkedHashSet<>(storedAppIds)) {
            if (appIds.size() == MAX_ENTRIES) {
                break;
            }
            appIds.add(appId);
        }
    }

    boolean record(String appId) {
        if (!appIds.isEmpty() && appIds.get(0).equals(appId)) {
            return false;
        }
        appIds.remove(appId);
        appIds.add(0, appId);
        if (appIds.size() > MAX_ENTRIES) {
            appIds.remove(appIds.size() - 1);
        }
        return true;
    }

    boolean retainAvailable(Collection<String> availableAppIds) {
        return appIds.retainAll(new LinkedHashSet<>(availableAppIds));
    }

    List<String> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(appIds));
    }
}
