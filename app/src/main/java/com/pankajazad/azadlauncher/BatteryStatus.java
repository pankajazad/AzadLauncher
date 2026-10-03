package com.pankajazad.azadlauncher;

import android.os.BatteryManager;

final class BatteryStatus {
    private BatteryStatus() { }

    static int percentage(int level, int scale) {
        if (level < 0 || scale <= 0) {
            return -1;
        }
        return Math.min(100, Math.round(level * 100f / scale));
    }

    static boolean isCharging(int status) {
        return status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
    }
}
