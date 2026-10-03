package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GestureClassifierTest {
    @Test
    public void downwardFlingAtTopIsRecognized() {
        assertTrue(GestureClassifier.isDownwardSwipe(
                20f, 180f, 900f, 100f, 400f, false));
    }

    @Test
    public void scrollingContentDoesNotTriggerAction() {
        assertFalse(GestureClassifier.isDownwardSwipe(
                20f, 180f, 900f, 100f, 400f, true));
    }

    @Test
    public void horizontalShortAndSlowGesturesAreRejected() {
        assertFalse(GestureClassifier.isDownwardSwipe(
                180f, 120f, 900f, 100f, 400f, false));
        assertFalse(GestureClassifier.isDownwardSwipe(
                10f, 80f, 900f, 100f, 400f, false));
        assertFalse(GestureClassifier.isDownwardSwipe(
                10f, 180f, 300f, 100f, 400f, false));
    }
}
