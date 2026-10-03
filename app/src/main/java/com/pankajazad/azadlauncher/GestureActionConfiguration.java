package com.pankajazad.azadlauncher;

final class GestureActionConfiguration {
    static final int NONE = 0;
    static final int OPEN_SEARCH = 1;
    static final int OPEN_SETTINGS = 2;

    private GestureActionConfiguration() { }

    static boolean isValidPreference(int action) {
        return action == NONE || action == OPEN_SEARCH || action == OPEN_SETTINGS;
    }

    static int spinnerIndex(int action) {
        return isValidPreference(action) ? action : OPEN_SEARCH;
    }

    static int preferenceForSpinnerIndex(int index) {
        return isValidPreference(index) ? index : OPEN_SEARCH;
    }
}
