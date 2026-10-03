package com.pankajazad.azadlauncher;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

final class BackupCodec {
    static final int MAX_BACKUP_CHARACTERS = 100_000;

    private BackupCodec() { }

    static String encode(LauncherBackupData data) {
        StringBuilder backup = new StringBuilder();
        backup.append("version=").append(LauncherBackupData.CURRENT_VERSION).append('\n');
        backup.append("gridColumns=").append(data.gridColumns()).append('\n');
        backup.append("showAppLabels=").append(data.showAppLabels()).append('\n');
        backup.append("themeMode=").append(data.themeMode()).append('\n');
        for (String appId : data.favoriteAppIds()) {
            backup.append("favorite=").append(encodeAppId(appId)).append('\n');
        }
        for (String appId : new TreeSet<>(data.hiddenAppIds())) {
            backup.append("hidden=").append(encodeAppId(appId)).append('\n');
        }
        return backup.toString();
    }

    static LauncherBackupData decode(String backup) {
        if (backup == null || backup.length() > MAX_BACKUP_CHARACTERS) {
            throw new IllegalArgumentException("Backup is missing or too large");
        }
        Integer version = null;
        Integer gridColumns = null;
        Boolean showAppLabels = null;
        int themeMode = ThemeConfiguration.FOLLOW_SYSTEM;
        Set<String> favorites = new LinkedHashSet<>();
        Set<String> hidden = new LinkedHashSet<>();
        for (String line : backup.split("\\r?\\n")) {
            if (line.isEmpty()) {
                continue;
            }
            int separator = line.indexOf('=');
            if (separator <= 0) {
                throw new IllegalArgumentException("Invalid backup line");
            }
            String key = line.substring(0, separator);
            String value = line.substring(separator + 1);
            switch (key) {
                case "version":
                    version = parseInteger(value, "version");
                    break;
                case "gridColumns":
                    gridColumns = parseInteger(value, "grid columns");
                    break;
                case "showAppLabels":
                    if (!"true".equals(value) && !"false".equals(value)) {
                        throw new IllegalArgumentException("Invalid label preference");
                    }
                    showAppLabels = Boolean.parseBoolean(value);
                    break;
                case "themeMode":
                    themeMode = parseInteger(value, "theme mode");
                    break;
                case "favorite":
                    favorites.add(decodeAppId(value));
                    break;
                case "hidden":
                    hidden.add(decodeAppId(value));
                    break;
                default:
                    // Ignore future optional fields while retaining strict validation of known fields.
                    break;
            }
        }
        if (version == null || version != LauncherBackupData.CURRENT_VERSION) {
            throw new IllegalArgumentException("Unsupported backup version");
        }
        if (gridColumns == null || !GridConfiguration.isValidPreference(gridColumns)) {
            throw new IllegalArgumentException("Invalid grid preference");
        }
        if (showAppLabels == null) {
            throw new IllegalArgumentException("Missing label preference");
        }
        if (!ThemeConfiguration.isValidPreference(themeMode)) {
            throw new IllegalArgumentException("Invalid theme preference");
        }
        return new LauncherBackupData(gridColumns, showAppLabels, themeMode, favorites, hidden);
    }

    private static int parseInteger(String value, String field) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid " + field, exception);
        }
    }

    private static String encodeAppId(String appId) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(appId.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeAppId(String encoded) {
        try {
            String appId = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            if (appId.isEmpty() || appId.indexOf('\n') >= 0 || appId.indexOf('\r') >= 0) {
                throw new IllegalArgumentException("Invalid app id");
            }
            return appId;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid encoded app id", exception);
        }
    }
}
