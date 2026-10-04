package com.pankajazad.azadlauncher;

import android.content.pm.ActivityInfo;

final class HomeRotationConfiguration {
    private HomeRotationConfiguration() { }

    static int requestedOrientation(boolean allowRotation) {
        return allowRotation
                ? ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }
}
