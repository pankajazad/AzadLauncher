package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AppFilterTest {
    @Test
    public void matchesLabelIgnoringCase() {
        assertTrue(AppFilter.matches("Camera", "com.android.camera", "cam"));
    }

    @Test
    public void matchesPackageName() {
        assertTrue(AppFilter.matches("Files", "com.google.android.documentsui", "documents"));
    }

    @Test
    public void rejectsUnrelatedTerm() {
        assertFalse(AppFilter.matches("Calendar", "com.android.calendar", "photos"));
    }

    @Test
    public void groupAndTextFiltersAreCombined() {
        AppEntry game = new AppEntry(
                "game/Activity", "Chess", "example.chess", DrawerGroupConfiguration.GAMES,
                null, null);
        AppEntry work = new AppEntry(
                "work/Activity", "Chat", "example.chat", DrawerGroupConfiguration.PRODUCTIVITY,
                null, null);

        List<AppEntry> filtered = AppFilter.filter(
                Arrays.asList(game, work), "ch", DrawerGroupConfiguration.GAMES);

        assertEquals(1, filtered.size());
        assertSame(game, filtered.get(0));
    }
}
