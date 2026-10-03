package com.pankajazad.azadlauncher;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class LauncherNotificationListener extends NotificationListenerService {
    static final String ACTION_NOTIFICATION_DOTS_CHANGED =
            "com.pankajazad.azadlauncher.NOTIFICATION_DOTS_CHANGED";

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        refreshPackages();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        refreshPackages();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification notification) {
        refreshPackages();
    }

    @Override
    public void onListenerDisconnected() {
        storeAndBroadcast(Collections.emptySet());
        super.onListenerDisconnected();
    }

    private void refreshPackages() {
        Set<String> packages = new HashSet<>();
        try {
            StatusBarNotification[] notifications = getActiveNotifications();
            if (notifications != null) {
                for (StatusBarNotification notification : notifications) {
                    String packageName = notification.getPackageName();
                    if (packageName != null && !packageName.equals(getPackageName())) {
                        packages.add(packageName);
                    }
                }
            }
        } catch (SecurityException exception) {
            packages.clear();
        }
        storeAndBroadcast(packages);
    }

    private void storeAndBroadcast(Set<String> packages) {
        new LauncherPreferences(this).setNotificationDotPackages(packages);
        sendBroadcast(new Intent(ACTION_NOTIFICATION_DOTS_CHANGED).setPackage(getPackageName()));
    }
}
