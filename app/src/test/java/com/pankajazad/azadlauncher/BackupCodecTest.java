package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

public class BackupCodecTest {
    @Test
    public void roundTripPreservesAllPreferences() {
        LauncherBackupData source = new LauncherBackupData(
                5,
                false,
                ThemeConfiguration.DARK,
                IconSizeConfiguration.LARGE,
                GestureActionConfiguration.OPEN_SETTINGS,
                WebSearchProvider.DUCKDUCKGO,
                false,
                DrawerGroupConfiguration.PRODUCTIVITY,
                DrawerSortConfiguration.CATEGORY_THEN_NAME,
                new LinkedHashSet<>(Arrays.asList("one/Activity", "two/Activity")),
                Collections.singleton("hidden/Activity"),
                "Travel & tools",
                Collections.singleton("map/Activity"));

        LauncherBackupData decoded = BackupCodec.decode(BackupCodec.encode(source));

        assertEquals(5, decoded.gridColumns());
        assertFalse(decoded.showAppLabels());
        assertEquals(ThemeConfiguration.DARK, decoded.themeMode());
        assertEquals(IconSizeConfiguration.LARGE, decoded.iconSize());
        assertEquals(GestureActionConfiguration.OPEN_SETTINGS, decoded.swipeDownAction());
        assertEquals(WebSearchProvider.DUCKDUCKGO, decoded.webSearchProvider());
        assertFalse(decoded.showContextCard());
        assertEquals(DrawerGroupConfiguration.PRODUCTIVITY, decoded.drawerGroup());
        assertEquals(DrawerSortConfiguration.CATEGORY_THEN_NAME, decoded.drawerSort());
        assertEquals(source.favoriteAppIds(), decoded.favoriteAppIds());
        assertEquals(
                new ArrayList<>(source.favoriteAppIds()),
                new ArrayList<>(decoded.favoriteAppIds()));
        assertEquals(source.hiddenAppIds(), decoded.hiddenAppIds());
        assertEquals("Travel & tools", decoded.quickFolderName());
        assertEquals(source.quickFolderAppIds(), decoded.quickFolderAppIds());
    }

    @Test
    public void automaticGridAndEmptySetsRoundTrip() {
        LauncherBackupData source = new LauncherBackupData(
                GridConfiguration.AUTOMATIC,
                true,
                ThemeConfiguration.FOLLOW_SYSTEM,
                IconSizeConfiguration.STANDARD,
                GestureActionConfiguration.OPEN_SEARCH,
                WebSearchProvider.DISABLED,
                true,
                DrawerGroupConfiguration.ALL,
                DrawerSortConfiguration.ALPHABETICAL,
                Collections.emptySet(),
                Collections.emptySet(),
                QuickFolder.DEFAULT_NAME,
                Collections.emptySet());

        LauncherBackupData decoded = BackupCodec.decode(BackupCodec.encode(source));

        assertEquals(GridConfiguration.AUTOMATIC, decoded.gridColumns());
        assertTrue(decoded.showAppLabels());
        assertEquals(ThemeConfiguration.FOLLOW_SYSTEM, decoded.themeMode());
        assertEquals(IconSizeConfiguration.STANDARD, decoded.iconSize());
        assertEquals(GestureActionConfiguration.OPEN_SEARCH, decoded.swipeDownAction());
        assertEquals(WebSearchProvider.DISABLED, decoded.webSearchProvider());
        assertTrue(decoded.showContextCard());
        assertEquals(DrawerGroupConfiguration.ALL, decoded.drawerGroup());
        assertEquals(DrawerSortConfiguration.ALPHABETICAL, decoded.drawerSort());
        assertTrue(decoded.favoriteAppIds().isEmpty());
        assertTrue(decoded.hiddenAppIds().isEmpty());
        assertEquals(QuickFolder.DEFAULT_NAME, decoded.quickFolderName());
        assertTrue(decoded.quickFolderAppIds().isEmpty());
    }

    @Test
    public void unsupportedVersionIsRejected() {
        try {
            BackupCodec.decode("version=99\ngridColumns=4\nshowAppLabels=true\n");
            fail("Expected unsupported version to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("version"));
        }
    }

    @Test
    public void malformedPreferenceIsRejected() {
        try {
            BackupCodec.decode("version=1\ngridColumns=99\nshowAppLabels=true\n");
            fail("Expected invalid grid to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("grid"));
        }
    }

    @Test
    public void olderBackupDefaultsToStandardIconSize() {
        LauncherBackupData decoded = BackupCodec.decode(
                "version=1\ngridColumns=4\nshowAppLabels=true\n");

        assertEquals(IconSizeConfiguration.STANDARD, decoded.iconSize());
        assertEquals(GestureActionConfiguration.OPEN_SEARCH, decoded.swipeDownAction());
        assertEquals(WebSearchProvider.DISABLED, decoded.webSearchProvider());
        assertTrue(decoded.showContextCard());
        assertEquals(DrawerGroupConfiguration.ALL, decoded.drawerGroup());
        assertEquals(DrawerSortConfiguration.ALPHABETICAL, decoded.drawerSort());
        assertEquals(QuickFolder.DEFAULT_NAME, decoded.quickFolderName());
    }

    @Test
    public void invalidIconSizeIsRejected() {
        try {
            BackupCodec.decode(
                    "version=1\ngridColumns=4\nshowAppLabels=true\niconSize=9\n");
            fail("Expected invalid icon size to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("icon size"));
        }
    }

    @Test
    public void invalidSwipeActionIsRejected() {
        try {
            BackupCodec.decode(
                    "version=1\ngridColumns=4\nshowAppLabels=true\nswipeDownAction=9\n");
            fail("Expected invalid swipe action to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("swipe down"));
        }
    }

    @Test
    public void invalidWebSearchProviderIsRejected() {
        try {
            BackupCodec.decode(
                    "version=1\ngridColumns=4\nshowAppLabels=true\nwebSearchProvider=9\n");
            fail("Expected invalid web provider to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("web search"));
        }
    }

    @Test
    public void invalidContextCardPreferenceIsRejected() {
        try {
            BackupCodec.decode(
                    "version=1\ngridColumns=4\nshowAppLabels=true\nshowContextCard=yes\n");
            fail("Expected invalid context card preference to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("context card"));
        }
    }

    @Test
    public void invalidDrawerGroupIsRejected() {
        try {
            BackupCodec.decode(
                    "version=1\ngridColumns=4\nshowAppLabels=true\ndrawerGroup=9\n");
            fail("Expected invalid drawer group to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("drawer group"));
        }
    }

    @Test
    public void invalidDrawerSortIsRejected() {
        try {
            BackupCodec.decode(
                    "version=1\ngridColumns=4\nshowAppLabels=true\ndrawerSort=9\n");
            fail("Expected invalid drawer sort to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("drawer sort"));
        }
    }

    @Test
    public void invalidQuickFolderNameIsRejected() {
        try {
            BackupCodec.decode(
                    "version=1\ngridColumns=4\nshowAppLabels=true\nquickFolderName=\n");
            fail("Expected invalid quick folder name to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("encoded app id"));
        }
    }
}
