package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.appcompat.app.AppCompatDelegate;

import org.junit.Test;

public class ThemeConfigurationTest {
    @Test
    public void supportedModesRoundTripThroughSpinner() {
        assertEquals(ThemeConfiguration.FOLLOW_SYSTEM, ThemeConfiguration.spinnerIndex(0));
        assertEquals(ThemeConfiguration.LIGHT, ThemeConfiguration.preferenceForSpinnerIndex(1));
        assertEquals(ThemeConfiguration.DARK, ThemeConfiguration.preferenceForSpinnerIndex(2));
    }

    @Test
    public void invalidModeFallsBackToSystem() {
        assertFalse(ThemeConfiguration.isValidPreference(9));
        assertEquals(ThemeConfiguration.FOLLOW_SYSTEM, ThemeConfiguration.spinnerIndex(9));
    }

    @Test
    public void modesMapToAppCompatNightModes() {
        assertTrue(ThemeConfiguration.isValidPreference(ThemeConfiguration.DARK));
        assertEquals(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                ThemeConfiguration.appCompatNightMode(ThemeConfiguration.FOLLOW_SYSTEM));
        assertEquals(
                AppCompatDelegate.MODE_NIGHT_NO,
                ThemeConfiguration.appCompatNightMode(ThemeConfiguration.LIGHT));
        assertEquals(
                AppCompatDelegate.MODE_NIGHT_YES,
                ThemeConfiguration.appCompatNightMode(ThemeConfiguration.DARK));
    }
}
