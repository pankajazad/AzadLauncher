package com.pankajazad.azadlauncher;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

final class HiddenApps {
    private final LinkedHashSet<String> appIds = new LinkedHashSet<>();

    HiddenApps(Collection<String> storedAppIds) {
        if (storedAppIds != null) {
            appIds.addAll(storedAppIds);
        }
    }

    boolean contains(String appId) {
        return appIds.contains(appId);
    }

    boolean setHidden(String appId, boolean hidden) {
        return hidden ? appIds.add(appId) : appIds.remove(appId);
    }

    boolean retainAvailable(Collection<String> availableAppIds) {
        return appIds.retainAll(new LinkedHashSet<>(availableAppIds));
    }

    Set<String> snapshot() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(appIds));
    }
}
