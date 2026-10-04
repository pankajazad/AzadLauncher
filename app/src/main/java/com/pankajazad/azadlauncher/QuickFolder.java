package com.pankajazad.azadlauncher;

import java.util.HashSet;
import java.util.Set;

final class QuickFolder {
    static final String DEFAULT_NAME = "Quick folder";
    static final int MAX_NAME_LENGTH = 50;

    private final Set<String> appIds;

    QuickFolder(Set<String> appIds) {
        this.appIds = new HashSet<>(appIds);
    }

    boolean contains(String appId) {
        return appIds.contains(appId);
    }

    boolean setIncluded(String appId, boolean included) {
        return included ? appIds.add(appId) : appIds.remove(appId);
    }

    boolean retainAvailable(Set<String> availableAppIds) {
        return appIds.retainAll(availableAppIds);
    }

    Set<String> snapshot() {
        return new HashSet<>(appIds);
    }

    static boolean isValidName(String name) {
        return name != null
                && !name.trim().isEmpty()
                && name.trim().length() <= MAX_NAME_LENGTH;
    }

    static String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return DEFAULT_NAME;
        }
        String trimmed = name.trim();
        return trimmed.length() <= MAX_NAME_LENGTH
                ? trimmed
                : trimmed.substring(0, MAX_NAME_LENGTH);
    }
}
