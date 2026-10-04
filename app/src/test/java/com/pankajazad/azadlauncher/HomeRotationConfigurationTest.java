package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;

import android.content.pm.ActivityInfo;

import org.junit.Test;

public class HomeRotationConfigurationTest {
    @Test
    public void disabledRotationLocksHomeToPortrait() {
        assertEquals(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                HomeRotationConfiguration.requestedOrientation(false));
    }

    @Test
    public void enabledRotationLetsAndroidChooseOrientation() {
        assertEquals(
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
                HomeRotationConfiguration.requestedOrientation(true));
    }
}
