package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DrawerSortConfigurationTest {
    @Test
    public void appsCanBeSortedAlphabeticallyInEitherDirection() {
        List<AppEntry> apps = Arrays.asList(
                app("Zulu", DrawerGroupConfiguration.GAMES),
                app("Alpha", DrawerGroupConfiguration.SOCIAL),
                app("Lima", DrawerGroupConfiguration.PRODUCTIVITY));

        assertEquals(
                Arrays.asList("Alpha", "Lima", "Zulu"),
                labels(DrawerSortConfiguration.sort(
                        apps, DrawerSortConfiguration.ALPHABETICAL)));
        assertEquals(
                Arrays.asList("Zulu", "Lima", "Alpha"),
                labels(DrawerSortConfiguration.sort(
                        apps, DrawerSortConfiguration.REVERSE_ALPHABETICAL)));
    }

    @Test
    public void categorySortUsesNameAsTieBreaker() {
        List<AppEntry> apps = Arrays.asList(
                app("Beta game", DrawerGroupConfiguration.GAMES),
                app("Social", DrawerGroupConfiguration.SOCIAL),
                app("Alpha game", DrawerGroupConfiguration.GAMES));

        assertEquals(
                Arrays.asList("Alpha game", "Beta game", "Social"),
                labels(DrawerSortConfiguration.sort(
                        apps, DrawerSortConfiguration.CATEGORY_THEN_NAME)));
    }

    @Test
    public void invalidPreferenceFallsBackToAlphabetical() {
        assertFalse(DrawerSortConfiguration.isValidPreference(9));
        assertEquals(
                DrawerSortConfiguration.ALPHABETICAL,
                DrawerSortConfiguration.spinnerIndex(9));
    }

    private static AppEntry app(String label, int group) {
        return new AppEntry(label, label, "package", group, null, null);
    }

    private static List<String> labels(List<AppEntry> apps) {
        return apps.stream().map(AppEntry::getLabel).collect(Collectors.toList());
    }
}
