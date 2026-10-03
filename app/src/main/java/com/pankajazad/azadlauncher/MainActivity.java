package com.pankajazad.azadlauncher;

import android.os.Bundle;
import android.content.SharedPreferences;
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
    private static final String PREFERENCES_NAME = "launcher_preferences";
    private static final String FAVORITE_APP_IDS_KEY = "favorite_app_ids";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ActivityMainBinding binding;
    private AppListAdapter appListAdapter;
    private FavoriteListAdapter favoriteListAdapter;
    private FavoriteApps favoriteApps;
    private List<AppEntry> allApps = Collections.emptyList();
    private boolean appsLoaded;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appListAdapter = new AppListAdapter(this);
        favoriteListAdapter = new FavoriteListAdapter(this);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        favoriteApps = new FavoriteApps(preferences.getStringSet(FAVORITE_APP_IDS_KEY, Collections.emptySet()));
        binding.appList.setLayoutManager(new GridLayoutManager(this, spanCount()));
        binding.appList.setAdapter(appListAdapter);
        binding.favoriteList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.favoriteList.setAdapter(favoriteListAdapter);
        binding.appSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return true; }
            @Override public boolean onQueryTextChange(String newText) {
                displayApps(AppFilter.filter(allApps, newText));
                return true;
            }
        });
        loadApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appsLoaded) {
            loadApps();
        }
    }

    private void loadApps() {
        executor.execute(() -> {
            List<AppEntry> loadedApps = new LaunchableAppRepository(this).load();
            runOnUiThread(() -> {
                allApps = loadedApps;
                appsLoaded = true;
                removeUnavailableFavorites();
                displayFavorites();
                displayApps(AppFilter.filter(allApps, binding.appSearch.getQuery().toString()));
            });
        });
    }

    private void removeUnavailableFavorites() {
        Set<String> availableAppIds = new HashSet<>();
        for (AppEntry app : allApps) {
            availableAppIds.add(app.getId());
        }
        if (favoriteApps.retainAvailable(availableAppIds)) {
            saveFavorites();
        }
    }

    private void displayFavorites() {
        List<AppEntry> favorites = new ArrayList<>();
        for (AppEntry app : allApps) {
            if (favoriteApps.contains(app.getId())) {
                favorites.add(app);
            }
        }
        favoriteListAdapter.submit(favorites);
        boolean hasFavorites = !favorites.isEmpty();
        binding.favoriteList.setVisibility(hasFavorites ? View.VISIBLE : View.GONE);
        binding.favoritesEmpty.setVisibility(hasFavorites ? View.GONE : View.VISIBLE);
    }

    private void saveFavorites() {
        getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
                .edit()
                .putStringSet(FAVORITE_APP_IDS_KEY, new HashSet<>(favoriteApps.snapshot()))
                .apply();
    }

    private void displayApps(List<AppEntry> apps) {
        appListAdapter.submit(apps);
        binding.emptyState.setVisibility(apps.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private int spanCount() {
        int widthDp = getResources().getConfiguration().screenWidthDp;
        if (widthDp >= 840) return 7;
        if (widthDp >= 600) return 6;
        return 4;
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
