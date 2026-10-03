package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NotificationDotStateTest {
    @Test
    public void packagesAreHiddenWithoutNotificationAccess() {
        Set<String> visible = NotificationDotState.visiblePackages(
                false, new HashSet<>(Arrays.asList("mail.app", "chat.app")));

        assertTrue(visible.isEmpty());
    }

    @Test
    public void grantedAccessReturnsSanitizedCopy() {
        Set<String> stored = new HashSet<>(Arrays.asList("mail.app", "", "chat.app"));

        Set<String> visible = NotificationDotState.visiblePackages(true, stored);
        stored.clear();

        assertEquals(2, visible.size());
        assertTrue(visible.contains("mail.app"));
        assertTrue(visible.contains("chat.app"));
        assertFalse(visible.contains(""));
    }
}
