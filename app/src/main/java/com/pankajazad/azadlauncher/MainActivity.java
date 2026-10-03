package com.pankajazad.azadlauncher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pankajazad.azadlauncher.databinding.ActivityMainBinding;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends AppCompatActivity implements AppActionListener {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ActivityMainBinding binding;
    private AppListAdapter appListAdapter;
    private FavoriteListAdapter favoriteListAdapter;
    private FavoriteApps favoriteApps;
    private HiddenApps hiddenApps;
    private LauncherPreferences preferences;
    private List<AppEntry> allApps = Collections.emptyList();
    private boolean appsLoaded;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appListAdapter = new AppListAdapter(this);
        favoriteListAdapter = new FavoriteListAdapter(this);
        preferences = new LauncherPreferences(this);
        favoriteApps = new FavoriteApps(preferences.favoriteAppIds());
        hiddenApps = new HiddenApps(preferences.hiddenAppIds());
        binding.appList.setLayoutManager(new GridLayoutManager(this, spanCount()));
        binding.appList.setAdapter(appListAdapter);
        binding.favoriteList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.favoriteList.setAdapter(favoriteListAdapter);
        binding.openSettings.setOnClickListener(
                view -> startActivity(new Intent(this, SettingsActivity.class)));
        binding.appSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return true; }
            @Override public boolean onQueryTextChange(String newText) {
                displayApps(AppFilter.filter(visibleApps(), newText));
                return true;
            }
        });
        loadApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyDisplayPreferences();
        if (appsLoaded) {
            loadApps();
        }
    }

    private void applyDisplayPreferences() {
        int columns = GridConfiguration.resolveColumns(
                preferences.gridColumns(),
                getResources().getConfiguration().screenWidthDp);
        GridLayoutManager layoutManager = (GridLayoutManager) binding.appList.getLayoutManager();
        if (layoutManager != null && layoutManager.getSpanCount() != columns) {
            layoutManager.setSpanCount(columns);
        }
        boolean showLabels = preferences.showAppLabels();
        appListAdapter.setShowLabels(showLabels);
        favoriteListAdapter.setShowLabels(showLabels);
    }

    private void loadApps() {
        executor.execute(() -> {
            List<AppEntry> loadedApps = new LaunchableAppRepository(this).load();
            runOnUiThread(() -> {
                allApps = loadedApps;
                appsLoaded = true;
                removeUnavailableSavedApps();
                displayFavorites();
                displayApps(AppFilter.filter(visibleApps(), binding.appSearch.getQuery().toString()));
            });
        });
    }

    private void removeUnavailableSavedApps() {
        Set<String> availableAppIds = new HashSet<>();
        for (AppEntry app : allApps) {
            availableAppIds.add(app.getId());
        }
        if (favoriteApps.retainAvailable(availableAppIds)) {
            saveFavorites();
        }
        if (hiddenApps.retainAvailable(availableAppIds)) {
            saveHiddenApps();
        }
    }

    private List<AppEntry> visibleApps() {
        List<AppEntry> visible = new ArrayList<>();
        for (AppEntry app : allApps) {
            if (!hiddenApps.contains(app.getId())) {
                visible.add(app);
            }
        }
        return visible;
    }

    private void displayFavorites() {
        List<AppEntry> favorites = new ArrayList<>();
        for (AppEntry app : allApps) {
            if (favoriteApps.contains(app.getId()) && !hiddenApps.contains(app.getId())) {
                favorites.add(app);
            }
        }
        favoriteListAdapter.submit(favorites);
        boolean hasFavorites = !favorites.isEmpty();
        binding.favoriteList.setVisibility(hasFavorites ? View.VISIBLE : View.GONE);
        binding.favoritesEmpty.setVisibility(hasFavorites ? View.GONE : View.VISIBLE);
    }

    private void saveFavorites() {
        preferences.setFavoriteAppIds(favoriteApps.snapshot());
    }

    private void saveHiddenApps() {
        preferences.setHiddenAppIds(hiddenApps.snapshot());
    }

    private void displayApps(List<AppEntry> apps) {
        appListAdapter.submit(apps);
        binding.emptyState.setVisibility(apps.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private int spanCount() {
        return GridConfiguration.resolveColumns(
                preferences.gridColumns(),
                getResources().getConfiguration().screenWidthDp);
    }

    @Override
    public void onOpenApp(AppEntry app) {
        startActivity(app.getLaunchIntent());
    }

    @Override
    public boolean onLongPressApp(AppEntry app) {
        boolean pinned = favoriteApps.toggle(app.getId());
        saveFavorites();
        displayFavorites();
        Toast.makeText(
                this,
                getString(pinned ? R.string.app_pinned : R.string.app_unpinned, app.getLabel()),
                Toast.LENGTH_SHORT)
                .show();
        return true;
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
