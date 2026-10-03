package com.pankajazad.azadlauncher;

import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.pankajazad.azadlauncher.databinding.ActivityMainBinding;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ActivityMainBinding binding;
    private final AppListAdapter appListAdapter = new AppListAdapter();
    private List<AppEntry> allApps = Collections.emptyList();
    private boolean appsLoaded;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.appList.setLayoutManager(new GridLayoutManager(this, spanCount()));
        binding.appList.setAdapter(appListAdapter);
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
                displayApps(AppFilter.filter(allApps, binding.appSearch.getQuery().toString()));
            });
        });
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
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
