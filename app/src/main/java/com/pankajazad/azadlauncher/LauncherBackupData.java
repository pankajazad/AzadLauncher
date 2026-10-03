package com.pankajazad.azadlauncher;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

final class LauncherBackupData {
    static final int CURRENT_VERSION = 1;

    private final int gridColumns;
    private final boolean showAppLabels;
    private final Set<String> favoriteAppIds;
    private final Set<String> hiddenAppIds;

    LauncherBackupData(
            int gridColumns,
            boolean showAppLabels,
            Set<String> favoriteAppIds,
            Set<String> hiddenAppIds) {
        this.gridColumns = gridColumns;
        this.showAppLabels = showAppLabels;
        this.favoriteAppIds = Collections.unmodifiableSet(new LinkedHashSet<>(favoriteAppIds));
        this.hiddenAppIds = Collections.unmodifiableSet(new LinkedHashSet<>(hiddenAppIds));
    }

    int gridColumns() { return gridColumns; }
    boolean showAppLabels() { return showAppLabels; }
    Set<String> favoriteAppIds() { return favoriteAppIds; }
    Set<String> hiddenAppIds() { return hiddenAppIds; }
}
