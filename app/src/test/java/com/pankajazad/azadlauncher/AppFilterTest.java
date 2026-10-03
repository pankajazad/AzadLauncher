package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
}
