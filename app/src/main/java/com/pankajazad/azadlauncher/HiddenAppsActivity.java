package com.pankajazad.azadlauncher;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pankajazad.azadlauncher.databinding.ActivityHiddenAppsBinding;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class HiddenAppsActivity extends AppCompatActivity implements HiddenAppListAdapter.Listener {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ActivityHiddenAppsBinding binding;
    private HiddenAppListAdapter adapter;
    private LauncherPreferences preferences;
    private HiddenApps hiddenApps;
    private List<AppEntry> allApps = Collections.emptyList();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHiddenAppsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferences = new LauncherPreferences(this);
        hiddenApps = new HiddenApps(preferences.hiddenAppIds());
        adapter = new HiddenAppListAdapter(this);
        binding.hiddenAppList.setLayoutManager(new LinearLayoutManager(this));
        binding.hiddenAppList.setAdapter(adapter);
        binding.hiddenAppSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return true; }

            @Override
            public boolean onQueryTextChange(String newText) {
                displayApps(AppFilter.filter(allApps, newText));
                return true;
            }
        });
        binding.done.setOnClickListener(view -> finish());
        loadApps();
    }

    private void loadApps() {
        executor.execute(() -> {
            List<AppEntry> loadedApps = new LaunchableAppRepository(this).load();
            runOnUiThread(() -> {
                allApps = loadedApps;
                binding.loading.setVisibility(View.GONE);
                displayApps(AppFilter.filter(allApps, binding.hiddenAppSearch.getQuery().toString()));
            });
        });
    }

    private void displayApps(List<AppEntry> apps) {
        adapter.submit(apps, hiddenApps.snapshot());
        binding.hiddenAppList.setVisibility(apps.isEmpty() ? View.GONE : View.VISIBLE);
        binding.emptyState.setVisibility(apps.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onHiddenChanged(AppEntry app, boolean hidden) {
        if (hiddenApps.setHidden(app.getId(), hidden)) {
            preferences.setHiddenAppIds(hiddenApps.snapshot());
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
