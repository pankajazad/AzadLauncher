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
                new LinkedHashSet<>(Arrays.asList("one/Activity", "two/Activity")),
                Collections.singleton("hidden/Activity"));

        LauncherBackupData decoded = BackupCodec.decode(BackupCodec.encode(source));

        assertEquals(5, decoded.gridColumns());
        assertFalse(decoded.showAppLabels());
        assertEquals(ThemeConfiguration.DARK, decoded.themeMode());
        assertEquals(IconSizeConfiguration.LARGE, decoded.iconSize());
        assertEquals(source.favoriteAppIds(), decoded.favoriteAppIds());
        assertEquals(
                new ArrayList<>(source.favoriteAppIds()),
                new ArrayList<>(decoded.favoriteAppIds()));
        assertEquals(source.hiddenAppIds(), decoded.hiddenAppIds());
    }

    @Test
    public void automaticGridAndEmptySetsRoundTrip() {
        LauncherBackupData source = new LauncherBackupData(
                GridConfiguration.AUTOMATIC,
                true,
                ThemeConfiguration.FOLLOW_SYSTEM,
                IconSizeConfiguration.STANDARD,
                Collections.emptySet(),
                Collections.emptySet());

        LauncherBackupData decoded = BackupCodec.decode(BackupCodec.encode(source));

        assertEquals(GridConfiguration.AUTOMATIC, decoded.gridColumns());
        assertTrue(decoded.showAppLabels());
        assertEquals(ThemeConfiguration.FOLLOW_SYSTEM, decoded.themeMode());
        assertEquals(IconSizeConfiguration.STANDARD, decoded.iconSize());
        assertTrue(decoded.favoriteAppIds().isEmpty());
        assertTrue(decoded.hiddenAppIds().isEmpty());
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
}
