package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

public class FavoriteAppsTest {
    @Test
    public void toggleAddsThenRemovesApp() {
        FavoriteApps favorites = new FavoriteApps(Collections.emptySet());

        assertTrue(favorites.toggle("app/component"));
        assertTrue(favorites.contains("app/component"));
        assertFalse(favorites.toggle("app/component"));
        assertFalse(favorites.contains("app/component"));
    }

    @Test
    public void retainAvailableDropsUninstalledApps() {
        FavoriteApps favorites = new FavoriteApps(Arrays.asList("installed", "removed"));

        assertTrue(favorites.retainAvailable(Collections.singleton("installed")));
        assertTrue(favorites.contains("installed"));
        assertFalse(favorites.contains("removed"));
    }

    @Test
    public void snapshotCannotMutateFavorites() {
        FavoriteApps favorites = new FavoriteApps(Collections.singleton("installed"));

        boolean threw = false;
        try {
            favorites.snapshot().add("other");
        } catch (UnsupportedOperationException expected) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    public void reorderPersistsRequestedOrder() {
        FavoriteApps favorites = new FavoriteApps(Arrays.asList("one", "two", "three"));

        assertTrue(favorites.reorder(Arrays.asList("three", "one", "two")));

        assertEquals(
                Arrays.asList("three", "one", "two"),
                new ArrayList<>(favorites.snapshot()));
    }
}
