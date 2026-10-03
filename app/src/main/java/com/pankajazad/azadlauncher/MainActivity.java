package com.pankajazad.azadlauncher;

import android.content.BroadcastReceiver;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pankajazad.azadlauncher.databinding.ActivityMainBinding;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private float gestureStartX;
    private float gestureStartY;
    private long gestureStartTime;
    private boolean trackingTouchGesture;
    private final BroadcastReceiver notificationDotsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            applyNotificationDots();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appListAdapter = new AppListAdapter(this);
        favoriteListAdapter = new FavoriteListAdapter(this, this::reorderFavorites);
        preferences = new LauncherPreferences(this);
        favoriteApps = new FavoriteApps(preferences.favoriteAppIds());
        hiddenApps = new HiddenApps(preferences.hiddenAppIds());
        ViewCompat.addAccessibilityAction(
                binding.getRoot(),
                getString(R.string.accessibility_open_search),
                (view, arguments) -> {
                    focusAppSearch();
                    return true;
                });
        binding.appList.setLayoutManager(new GridLayoutManager(this, spanCount()));
        binding.appList.setAdapter(appListAdapter);
        binding.favoriteList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.favoriteList.setAdapter(favoriteListAdapter);
        ItemTouchHelper favoriteTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
                    @Override
                    public boolean isLongPressDragEnabled() {
                        return false;
                    }

                    @Override
                    public boolean onMove(
                            RecyclerView recyclerView,
                            RecyclerView.ViewHolder source,
                            RecyclerView.ViewHolder target) {
                        return favoriteListAdapter.move(
                                source.getBindingAdapterPosition(),
                                target.getBindingAdapterPosition());
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) { }
                });
        favoriteTouchHelper.attachToRecyclerView(binding.favoriteList);
        favoriteListAdapter.setDragStarter(favoriteTouchHelper::startDrag);
        binding.openSettings.setOnClickListener(
                view -> startActivity(new Intent(this, SettingsActivity.class)));
        binding.webSearch.setOnClickListener(
                view -> openWebSearch(binding.appSearch.getQuery().toString()));
        binding.appSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return true; }
            @Override public boolean onQueryTextChange(String newText) {
                displayApps(AppFilter.filter(visibleApps(), newText));
                updateWebSearchButton(newText);
                return true;
            }
        });
        loadApps();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_TOUCHSCREEN)
                == InputDevice.SOURCE_TOUCHSCREEN) {
            trackHomeGesture(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private void trackHomeGesture(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                gestureStartX = event.getX();
                gestureStartY = event.getY();
                gestureStartTime = event.getEventTime();
                trackingTouchGesture = true;
                break;
            case MotionEvent.ACTION_UP:
                if (trackingTouchGesture) {
                    float horizontalDistance = event.getX() - gestureStartX;
                    float verticalDistance = event.getY() - gestureStartY;
                    long elapsedMillis = Math.max(1L, event.getEventTime() - gestureStartTime);
                    float verticalVelocity = verticalDistance * 1000f / elapsedMillis;
                    float density = getResources().getDisplayMetrics().density;
                    if (GestureClassifier.isDownwardSwipe(
                            horizontalDistance,
                            verticalDistance,
                            verticalVelocity,
                            72f * density,
                            250f * density,
                            binding.appList.canScrollVertically(-1))) {
                        performGestureAction(preferences.swipeDownAction());
                    }
                }
                trackingTouchGesture = false;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_DOWN:
                trackingTouchGesture = false;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyDisplayPreferences();
        if (appsLoaded) {
            loadApps();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ContextCompat.registerReceiver(
                this,
                notificationDotsReceiver,
                new IntentFilter(LauncherNotificationListener.ACTION_NOTIFICATION_DOTS_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED);
        applyNotificationDots();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(notificationDotsReceiver);
        super.onStop();
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
        int iconSizeDp = IconSizeConfiguration.iconSizeDp(preferences.iconSize());
        appListAdapter.setIconSizeDp(iconSizeDp);
        favoriteListAdapter.setIconSizeDp(iconSizeDp);
        applyNotificationDots();
        updateWebSearchButton(binding.appSearch.getQuery().toString());
    }

    private void applyNotificationDots() {
        if (preferences == null || appListAdapter == null || favoriteListAdapter == null) {
            return;
        }
        boolean accessGranted = NotificationManagerCompat.getEnabledListenerPackages(this)
                .contains(getPackageName());
        Set<String> packages = NotificationDotState.visiblePackages(
                accessGranted, preferences.notificationDotPackages());
        appListAdapter.setNotificationPackages(packages);
        favoriteListAdapter.setNotificationPackages(packages);
    }

    private void performGestureAction(int action) {
        switch (action) {
            case GestureActionConfiguration.OPEN_SEARCH:
                focusAppSearch();
                break;
            case GestureActionConfiguration.OPEN_SETTINGS:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                break;
        }
    }

    private void focusAppSearch() {
        binding.appSearch.setIconified(false);
        View searchInput = binding.appSearch.findViewById(androidx.appcompat.R.id.search_src_text);
        searchInput.requestFocus();
        searchInput.post(() -> {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(searchInput, 0);
        });
    }

    private void updateWebSearchButton(String query) {
        String url = WebSearchProvider.searchUrl(preferences.webSearchProvider(), query);
        boolean visible = url != null;
        binding.webSearch.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            binding.webSearch.setText(getString(R.string.search_web_for, query.trim()));
        }
    }

    private void openWebSearch(String query) {
        String url = WebSearchProvider.searchUrl(preferences.webSearchProvider(), query);
        if (url == null) {
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.web_search_unavailable, Toast.LENGTH_SHORT).show();
        }
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
        Map<String, AppEntry> availableApps = new HashMap<>();
        for (AppEntry app : allApps) {
            availableApps.put(app.getId(), app);
        }
        List<AppEntry> favorites = new ArrayList<>();
        for (String appId : favoriteApps.snapshot()) {
            AppEntry app = availableApps.get(appId);
            if (app != null && !hiddenApps.contains(appId)) {
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

    private void reorderFavorites(List<String> orderedAppIds) {
        if (favoriteApps.reorder(orderedAppIds)) {
            saveFavorites();
        }
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
