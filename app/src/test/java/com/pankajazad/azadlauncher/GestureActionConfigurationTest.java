package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GestureActionConfigurationTest {
    @Test
    public void supportedActionsRoundTripThroughSpinner() {
        assertEquals(GestureActionConfiguration.NONE,
                GestureActionConfiguration.spinnerIndex(0));
        assertEquals(GestureActionConfiguration.OPEN_SEARCH,
                GestureActionConfiguration.preferenceForSpinnerIndex(1));
        assertEquals(GestureActionConfiguration.OPEN_SETTINGS,
                GestureActionConfiguration.preferenceForSpinnerIndex(2));
    }

    @Test
    public void invalidActionFallsBackToSearch() {
        assertFalse(GestureActionConfiguration.isValidPreference(8));
        assertEquals(GestureActionConfiguration.OPEN_SEARCH,
                GestureActionConfiguration.spinnerIndex(8));
        assertTrue(GestureActionConfiguration.isValidPreference(
                GestureActionConfiguration.OPEN_SETTINGS));
    }
}
