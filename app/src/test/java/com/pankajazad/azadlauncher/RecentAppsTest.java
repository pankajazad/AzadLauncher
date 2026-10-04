package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class RecentAppsTest {
    @Test
    public void recordingMovesAnAppToTheFrontWithoutDuplicates() {
        RecentApps recentApps = new RecentApps(Arrays.asList("one", "two", "three"));

        assertTrue(recentApps.record("three"));
        assertEquals(Arrays.asList("three", "one", "two"), recentApps.snapshot());
        assertFalse(recentApps.record("three"));
    }

    @Test
    public void removedAppsArePruned() {
        RecentApps recentApps = new RecentApps(Arrays.asList("installed", "removed"));

        assertTrue(recentApps.retainAvailable(Collections.singleton("installed")));
        assertEquals(Collections.singletonList("installed"), recentApps.snapshot());
    }

    @Test
    public void historyIsBounded() {
        RecentApps recentApps = new RecentApps(Collections.emptyList());
        for (int index = 0; index < RecentApps.MAX_ENTRIES + 5; index++) {
            recentApps.record("app-" + index);
        }

        assertEquals(RecentApps.MAX_ENTRIES, recentApps.snapshot().size());
        assertEquals("app-54", recentApps.snapshot().get(0));
    }
}
