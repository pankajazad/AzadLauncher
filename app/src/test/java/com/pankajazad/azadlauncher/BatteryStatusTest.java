package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.BatteryManager;

import org.junit.Test;

public class BatteryStatusTest {
    @Test
    public void percentageHandlesNormalAndInvalidValues() {
        assertEquals(50, BatteryStatus.percentage(25, 50));
        assertEquals(100, BatteryStatus.percentage(55, 50));
        assertEquals(-1, BatteryStatus.percentage(-1, 100));
        assertEquals(-1, BatteryStatus.percentage(50, 0));
    }

    @Test
    public void chargingIncludesFullBattery() {
        assertTrue(BatteryStatus.isCharging(BatteryManager.BATTERY_STATUS_CHARGING));
        assertTrue(BatteryStatus.isCharging(BatteryManager.BATTERY_STATUS_FULL));
        assertFalse(BatteryStatus.isCharging(BatteryManager.BATTERY_STATUS_DISCHARGING));
    }
}
