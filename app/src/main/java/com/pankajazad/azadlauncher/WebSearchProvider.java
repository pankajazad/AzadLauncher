package com.pankajazad.azadlauncher;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

final class WebSearchProvider {
    static final int DISABLED = 0;
    static final int DUCKDUCKGO = 1;
    static final int GOOGLE = 2;

    private WebSearchProvider() { }

    static boolean isValidPreference(int provider) {
        return provider == DISABLED || provider == DUCKDUCKGO || provider == GOOGLE;
    }

    static int spinnerIndex(int provider) {
        return isValidPreference(provider) ? provider : DISABLED;
    }

    static int preferenceForSpinnerIndex(int index) {
        return isValidPreference(index) ? index : DISABLED;
    }

    static String searchUrl(int provider, String query) {
        String trimmedQuery = query == null ? "" : query.trim();
        if (provider == DISABLED || trimmedQuery.isEmpty()) {
            return null;
        }
        String baseUrl;
        switch (provider) {
            case DUCKDUCKGO:
                baseUrl = "https://duckduckgo.com/?q=";
                break;
            case GOOGLE:
                baseUrl = "https://www.google.com/search?q=";
                break;
            default:
                return null;
        }
        try {
            return baseUrl + URLEncoder.encode(trimmedQuery, "UTF-8");
        } catch (UnsupportedEncodingException exception) {
            throw new AssertionError("UTF-8 must be supported", exception);
        }
    }
}
