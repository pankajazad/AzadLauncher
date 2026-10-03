package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WebSearchProviderTest {
    @Test
    public void disabledAndBlankSearchesHaveNoUrl() {
        assertNull(WebSearchProvider.searchUrl(WebSearchProvider.DISABLED, "launcher"));
        assertNull(WebSearchProvider.searchUrl(WebSearchProvider.GOOGLE, "   "));
        assertNull(WebSearchProvider.searchUrl(WebSearchProvider.GOOGLE, null));
    }

    @Test
    public void providersEncodeQueriesSafely() {
        assertEquals(
                "https://duckduckgo.com/?q=android+launcher%2Fsearch",
                WebSearchProvider.searchUrl(
                        WebSearchProvider.DUCKDUCKGO, " android launcher/search "));
        assertEquals(
                "https://www.google.com/search?q=azad+launcher",
                WebSearchProvider.searchUrl(WebSearchProvider.GOOGLE, "azad launcher"));
    }

    @Test
    public void invalidProviderFallsBackToDisabled() {
        assertFalse(WebSearchProvider.isValidPreference(7));
        assertEquals(WebSearchProvider.DISABLED, WebSearchProvider.spinnerIndex(7));
        assertTrue(WebSearchProvider.isValidPreference(WebSearchProvider.DUCKDUCKGO));
    }
}
