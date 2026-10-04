package com.pankajazad.azadlauncher;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pankajazad.azadlauncher.databinding.ActivityQuickFolderBinding;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class QuickFolderActivity extends AppCompatActivity
        implements FolderAppListAdapter.Listener {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ActivityQuickFolderBinding binding;
    private FolderAppListAdapter adapter;
    private LauncherPreferences preferences;
    private QuickFolder quickFolder;
    private List<AppEntry> allApps = Collections.emptyList();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuickFolderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferences = new LauncherPreferences(this);
        quickFolder = new QuickFolder(preferences.quickFolderAppIds());
        binding.folderName.setText(preferences.quickFolderName());
        adapter = new FolderAppListAdapter(this);
        binding.folderAppList.setLayoutManager(new LinearLayoutManager(this));
        binding.folderAppList.setAdapter(adapter);
        binding.folderAppSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                displayApps(AppFilter.filter(
                        allApps, binding.folderAppSearch.getQuery().toString()));
            });
        });
    }

    private void displayApps(List<AppEntry> apps) {
        adapter.submit(apps, quickFolder.snapshot());
        binding.folderAppList.setVisibility(apps.isEmpty() ? View.GONE : View.VISIBLE);
        binding.emptyState.setVisibility(apps.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onIncludedChanged(AppEntry app, boolean included) {
        if (quickFolder.setIncluded(app.getId(), included)) {
            preferences.setQuickFolderAppIds(quickFolder.snapshot());
        }
    }

    @Override
    protected void onPause() {
        preferences.setQuickFolderName(binding.folderName.getText().toString());
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
