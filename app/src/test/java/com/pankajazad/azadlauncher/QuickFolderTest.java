package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class QuickFolderTest {
    @Test
    public void membershipCanBeChangedAndCleaned() {
        QuickFolder folder = new QuickFolder(
                new HashSet<>(Arrays.asList("one/Activity", "missing/Activity")));

        assertTrue(folder.setIncluded("two/Activity", true));
        assertFalse(folder.setIncluded("two/Activity", true));
        assertTrue(folder.retainAvailable(new HashSet<>(
                Arrays.asList("one/Activity", "two/Activity"))));

        assertEquals(
                new HashSet<>(Arrays.asList("one/Activity", "two/Activity")),
                folder.snapshot());
    }

    @Test
    public void namesAreTrimmedBoundedAndDefaulted() {
        assertEquals("Travel", QuickFolder.normalizeName("  Travel  "));
        assertEquals(QuickFolder.DEFAULT_NAME, QuickFolder.normalizeName("   "));
        assertEquals(50, QuickFolder.normalizeName(String.join("", Collections.nCopies(60, "x"))).length());
    }
}
