package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.pm.ApplicationInfo;

import org.junit.Test;

public class DrawerGroupConfigurationTest {
    @Test
    public void publicApplicationCategoriesMapToDrawerGroups() {
        assertEquals(DrawerGroupConfiguration.GAMES,
                DrawerGroupConfiguration.fromApplicationCategory(ApplicationInfo.CATEGORY_GAME));
        assertEquals(DrawerGroupConfiguration.PRODUCTIVITY,
                DrawerGroupConfiguration.fromApplicationCategory(
                        ApplicationInfo.CATEGORY_PRODUCTIVITY));
        assertEquals(DrawerGroupConfiguration.SOCIAL,
                DrawerGroupConfiguration.fromApplicationCategory(ApplicationInfo.CATEGORY_SOCIAL));
        assertEquals(DrawerGroupConfiguration.MEDIA,
                DrawerGroupConfiguration.fromApplicationCategory(ApplicationInfo.CATEGORY_VIDEO));
        assertEquals(DrawerGroupConfiguration.OTHER,
                DrawerGroupConfiguration.fromApplicationCategory(ApplicationInfo.CATEGORY_UNDEFINED));
    }

    @Test
    public void allGroupIncludesEveryCategory() {
        assertTrue(DrawerGroupConfiguration.includes(
                DrawerGroupConfiguration.ALL, DrawerGroupConfiguration.SOCIAL));
        assertTrue(DrawerGroupConfiguration.includes(
                DrawerGroupConfiguration.MEDIA, DrawerGroupConfiguration.MEDIA));
        assertFalse(DrawerGroupConfiguration.includes(
                DrawerGroupConfiguration.GAMES, DrawerGroupConfiguration.PRODUCTIVITY));
    }

    @Test
    public void invalidPreferenceFallsBackToAll() {
        assertFalse(DrawerGroupConfiguration.isValidPreference(9));
        assertEquals(DrawerGroupConfiguration.ALL,
                DrawerGroupConfiguration.spinnerIndex(9));
    }
}
