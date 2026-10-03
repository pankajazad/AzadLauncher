package com.pankajazad.azadlauncher;

final class GridConfiguration {
    static final int AUTOMATIC = 0;
    private static final int MIN_COLUMNS = 3;
    private static final int MAX_COLUMNS = 7;

    private GridConfiguration() { }

    static int resolveColumns(int storedColumns, int screenWidthDp) {
        if (isSelectableColumnCount(storedColumns)) {
            return storedColumns;
        }
        if (screenWidthDp >= 840) {
            return 7;
        }
        if (screenWidthDp >= 600) {
            return 6;
        }
        return 4;
    }

    static boolean isValidPreference(int columns) {
        return columns == AUTOMATIC || isSelectableColumnCount(columns);
    }

    private static boolean isSelectableColumnCount(int columns) {
        return columns >= MIN_COLUMNS && columns <= MAX_COLUMNS;
    }

    static int spinnerIndex(int storedColumns) {
        return storedColumns >= MIN_COLUMNS && storedColumns <= MAX_COLUMNS
                ? storedColumns - 2
                : 0;
    }

    static int columnsForSpinnerIndex(int index) {
        return index >= 1 && index <= 5 ? index + 2 : AUTOMATIC;
    }
}
