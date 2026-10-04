package com.pankajazad.azadlauncher;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

final class LauncherBackupData {
    static final int CURRENT_VERSION = 1;

    private final int gridColumns;
    private final boolean showAppLabels;
    private final int themeMode;
    private final int iconSize;
    private final int swipeDownAction;
    private final int webSearchProvider;
    private final boolean showContextCard;
    private final int drawerGroup;
    private final int drawerSort;
    private final Set<String> favoriteAppIds;
    private final Set<String> hiddenAppIds;
    private final String quickFolderName;
    private final Set<String> quickFolderAppIds;

    LauncherBackupData(
            int gridColumns,
            boolean showAppLabels,
            int themeMode,
            int iconSize,
            int swipeDownAction,
            int webSearchProvider,
            boolean showContextCard,
            int drawerGroup,
            int drawerSort,
            Set<String> favoriteAppIds,
            Set<String> hiddenAppIds,
            String quickFolderName,
            Set<String> quickFolderAppIds) {
        this.gridColumns = gridColumns;
        this.showAppLabels = showAppLabels;
        this.themeMode = themeMode;
        this.iconSize = iconSize;
        this.swipeDownAction = swipeDownAction;
        this.webSearchProvider = webSearchProvider;
        this.showContextCard = showContextCard;
        this.drawerGroup = drawerGroup;
        this.drawerSort = drawerSort;
        this.favoriteAppIds = Collections.unmodifiableSet(new LinkedHashSet<>(favoriteAppIds));
        this.hiddenAppIds = Collections.unmodifiableSet(new LinkedHashSet<>(hiddenAppIds));
        this.quickFolderName = quickFolderName;
        this.quickFolderAppIds = Collections.unmodifiableSet(
                new LinkedHashSet<>(quickFolderAppIds));
    }

    int gridColumns() { return gridColumns; }
    boolean showAppLabels() { return showAppLabels; }
    int themeMode() { return themeMode; }
    int iconSize() { return iconSize; }
    int swipeDownAction() { return swipeDownAction; }
    int webSearchProvider() { return webSearchProvider; }
    boolean showContextCard() { return showContextCard; }
    int drawerGroup() { return drawerGroup; }
    int drawerSort() { return drawerSort; }
    Set<String> favoriteAppIds() { return favoriteAppIds; }
    Set<String> hiddenAppIds() { return hiddenAppIds; }
    String quickFolderName() { return quickFolderName; }
    Set<String> quickFolderAppIds() { return quickFolderAppIds; }
}
