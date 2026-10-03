package com.pankajazad.azadlauncher;

import android.content.pm.ApplicationInfo;

final class DrawerGroupConfiguration {
    static final int ALL = 0;
    static final int GAMES = 1;
    static final int PRODUCTIVITY = 2;
    static final int SOCIAL = 3;
    static final int MEDIA = 4;
    static final int OTHER = 5;

    private DrawerGroupConfiguration() { }

    static boolean isValidPreference(int group) {
        return group >= ALL && group <= OTHER;
    }

    static int spinnerIndex(int group) {
        return isValidPreference(group) ? group : ALL;
    }

    static int preferenceForSpinnerIndex(int index) {
        return isValidPreference(index) ? index : ALL;
    }

    static int fromApplicationCategory(int category) {
        switch (category) {
            case ApplicationInfo.CATEGORY_GAME:
                return GAMES;
            case ApplicationInfo.CATEGORY_PRODUCTIVITY:
                return PRODUCTIVITY;
            case ApplicationInfo.CATEGORY_SOCIAL:
                return SOCIAL;
            case ApplicationInfo.CATEGORY_AUDIO:
            case ApplicationInfo.CATEGORY_VIDEO:
            case ApplicationInfo.CATEGORY_IMAGE:
                return MEDIA;
            default:
                return OTHER;
        }
    }

    static boolean includes(int selectedGroup, int appGroup) {
        return selectedGroup == ALL || selectedGroup == appGroup;
    }
}
