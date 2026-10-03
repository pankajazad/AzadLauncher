package com.pankajazad.azadlauncher;

import android.content.Intent;
import android.graphics.drawable.Drawable;

final class AppEntry {
    private final String id;
    private final String label;
    private final String packageName;
    private final int drawerGroup;
    private final Drawable icon;
    private final Intent launchIntent;

    AppEntry(
            String id,
            String label,
            String packageName,
            int drawerGroup,
            Drawable icon,
            Intent launchIntent) {
        this.id = id;
        this.label = label;
        this.packageName = packageName;
        this.drawerGroup = drawerGroup;
        this.icon = icon;
        this.launchIntent = launchIntent;
    }

    String getId() { return id; }
    String getLabel() { return label; }
    String getPackageName() { return packageName; }
    int getDrawerGroup() { return drawerGroup; }
    Drawable getIcon() { return icon; }
    Intent getLaunchIntent() { return launchIntent; }
}
