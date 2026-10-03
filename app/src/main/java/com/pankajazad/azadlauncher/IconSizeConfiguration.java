package com.pankajazad.azadlauncher;

final class IconSizeConfiguration {
    static final int COMPACT = 0;
    static final int STANDARD = 1;
    static final int LARGE = 2;

    private IconSizeConfiguration() { }

    static boolean isValidPreference(int iconSize) {
        return iconSize == COMPACT || iconSize == STANDARD || iconSize == LARGE;
    }

    static int spinnerIndex(int iconSize) {
        return isValidPreference(iconSize) ? iconSize : STANDARD;
    }

    static int preferenceForSpinnerIndex(int index) {
        return isValidPreference(index) ? index : STANDARD;
    }

    static int iconSizeDp(int iconSize) {
        switch (iconSize) {
            case COMPACT:
                return 40;
            case LARGE:
                return 60;
            default:
                return 48;
        }
    }
}
