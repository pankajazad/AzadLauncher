package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class HiddenAppsTest {
    @Test
    public void appCanBeHiddenAndRestored() {
        HiddenApps hiddenApps = new HiddenApps(Collections.emptySet());

        assertTrue(hiddenApps.setHidden("app/component", true));
        assertTrue(hiddenApps.contains("app/component"));
        assertTrue(hiddenApps.setHidden("app/component", false));
        assertFalse(hiddenApps.contains("app/component"));
    }

    @Test
    public void unchangedStateDoesNotReportMutation() {
        HiddenApps hiddenApps = new HiddenApps(Collections.singleton("hidden"));

        assertFalse(hiddenApps.setHidden("hidden", true));
        assertFalse(hiddenApps.setHidden("visible", false));
    }

    @Test
    public void removedAppsArePruned() {
        HiddenApps hiddenApps = new HiddenApps(Arrays.asList("installed", "removed"));

        assertTrue(hiddenApps.retainAvailable(Collections.singleton("installed")));
        assertTrue(hiddenApps.contains("installed"));
        assertFalse(hiddenApps.contains("removed"));
    }
}
