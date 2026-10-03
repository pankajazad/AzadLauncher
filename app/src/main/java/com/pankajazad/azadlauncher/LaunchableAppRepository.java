package com.pankajazad.azadlauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class LaunchableAppRepository {
    private final Context context;

    LaunchableAppRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    List<AppEntry> load() {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        List<AppEntry> apps = new ArrayList<>();
        Set<ComponentName> seen = new HashSet<>();
        for (ResolveInfo info : activities) {
            ComponentName component = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
            if (context.getPackageName().equals(component.getPackageName()) || !seen.add(component)) {
                continue;
            }
            Intent launchIntent = new Intent(intent).setComponent(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            apps.add(new AppEntry(
                    component.flattenToString(),
                    info.loadLabel(packageManager).toString(),
                    component.getPackageName(),
                    info.loadIcon(packageManager),
                    launchIntent));
        }
        Collator collator = Collator.getInstance();
        apps.sort(Comparator.comparing(AppEntry::getLabel, (left, right) -> collator.compare(left, right)));
        return apps;
    }
}
