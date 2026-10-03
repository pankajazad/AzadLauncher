package com.pankajazad.azadlauncher;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GridConfigurationTest {
    @Test
    public void automaticModeAdaptsToScreenWidth() {
        assertEquals(4, GridConfiguration.resolveColumns(GridConfiguration.AUTOMATIC, 412));
        assertEquals(6, GridConfiguration.resolveColumns(GridConfiguration.AUTOMATIC, 700));
        assertEquals(7, GridConfiguration.resolveColumns(GridConfiguration.AUTOMATIC, 900));
    }

    @Test
    public void validUserSelectionOverridesAutomaticMode() {
        assertEquals(3, GridConfiguration.resolveColumns(3, 900));
        assertEquals(7, GridConfiguration.resolveColumns(7, 412));
    }

    @Test
    public void invalidStoredSelectionFallsBackToAutomaticMode() {
        assertEquals(4, GridConfiguration.resolveColumns(9, 412));
        assertEquals(0, GridConfiguration.spinnerIndex(9));
    }

    @Test
    public void spinnerValuesRoundTrip() {
        assertEquals(GridConfiguration.AUTOMATIC, GridConfiguration.columnsForSpinnerIndex(0));
        assertEquals(3, GridConfiguration.columnsForSpinnerIndex(1));
        assertEquals(5, GridConfiguration.columnsForSpinnerIndex(3));
        assertEquals(7, GridConfiguration.columnsForSpinnerIndex(5));
        assertEquals(3, GridConfiguration.spinnerIndex(5));
    }
}
