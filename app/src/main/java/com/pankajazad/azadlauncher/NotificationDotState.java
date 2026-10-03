package com.pankajazad.azadlauncher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class NotificationDotState {
    private NotificationDotState() { }

    static Set<String> visiblePackages(boolean accessGranted, Set<String> storedPackages) {
        if (!accessGranted) {
            return Collections.emptySet();
        }
        Set<String> packages = new HashSet<>();
        for (String packageName : storedPackages) {
            if (packageName != null && !packageName.isEmpty()) {
                packages.add(packageName);
            }
        }
        return packages;
    }
}
