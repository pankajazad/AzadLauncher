package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IconSizeConfigurationTest {
    @Test
    public void supportedSizesMapToExpectedDp() {
        assertEquals(40, IconSizeConfiguration.iconSizeDp(IconSizeConfiguration.COMPACT));
        assertEquals(48, IconSizeConfiguration.iconSizeDp(IconSizeConfiguration.STANDARD));
        assertEquals(60, IconSizeConfiguration.iconSizeDp(IconSizeConfiguration.LARGE));
    }

    @Test
    public void spinnerRoundTripPreservesSupportedSizes() {
        assertEquals(IconSizeConfiguration.COMPACT, IconSizeConfiguration.spinnerIndex(0));
        assertEquals(IconSizeConfiguration.STANDARD,
                IconSizeConfiguration.preferenceForSpinnerIndex(1));
        assertEquals(IconSizeConfiguration.LARGE,
                IconSizeConfiguration.preferenceForSpinnerIndex(2));
    }

    @Test
    public void invalidSizeFallsBackToStandard() {
        assertFalse(IconSizeConfiguration.isValidPreference(7));
        assertEquals(IconSizeConfiguration.STANDARD, IconSizeConfiguration.spinnerIndex(7));
        assertEquals(48, IconSizeConfiguration.iconSizeDp(7));
        assertTrue(IconSizeConfiguration.isValidPreference(IconSizeConfiguration.LARGE));
    }
}
