package com.pankajazad.azadlauncher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.BatteryManager;
import android.provider.Settings;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
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
import java.text.DateFormat;
import java.util.Date;

public final class MainActivity extends AppCompatActivity implements AppActionListener {
    private static final int APPWIDGET_PICK_REQUEST = 4101;
    private static final int APPWIDGET_CONFIGURE_REQUEST = 4102;
    private static final int APPWIDGET_HOST_ID = 4100;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private AppWidgetHost appWidgetHost;
    private int pendingWidgetId = AppWidgetHost.INVALID_APPWIDGET_ID;
    private ComponentName pendingWidgetProvider;
    private ActivityMainBinding binding;
    private AppListAdapter appListAdapter;
    private FavoriteListAdapter favoriteListAdapter;
    private FavoriteApps favoriteApps;
    private HiddenApps hiddenApps;
    private RecentApps recentApps;
    private QuickFolder quickFolder;
    private LauncherPreferences preferences;
    private List<AppEntry> allApps = Collections.emptyList();
    private boolean appsLoaded;
    private boolean widgetsRestored;
    private float gestureStartX;
    private float gestureStartY;
    private long gestureStartTime;
    private boolean trackingTouchGesture;
    private int batteryPercentage = -1;
    private boolean batteryCharging;
    private final BroadcastReceiver notificationDotsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            applyNotificationDots();
        }
    };
    private final BroadcastReceiver contextCardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateContextCard(intent);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appWidgetHost = new AppWidgetHost(this, APPWIDGET_HOST_ID);
        appWidgetHost.startListening();
        binding.workspace.setGrid(5, 8);
        binding.openDrawer.setOnClickListener(view -> showDrawer(true));
        binding.closeDrawer.setOnClickListener(view -> showDrawer(false));
        binding.addWidget.setOnClickListener(view -> pickWidget());
        appListAdapter = new AppListAdapter(this);
        favoriteListAdapter = new FavoriteListAdapter(this, this::reorderFavorites);
        preferences = new LauncherPreferences(this);
        favoriteApps = new FavoriteApps(preferences.favoriteAppIds());
        hiddenApps = new HiddenApps(preferences.hiddenAppIds());
        recentApps = new RecentApps(preferences.recentAppIds());
        quickFolder = new QuickFolder(preferences.quickFolderAppIds());
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
        binding.contextCard.setOnClickListener(view -> openCalendar());
        binding.quickFolder.setOnClickListener(view -> showQuickFolder());
        binding.webSearch.setOnClickListener(
                view -> openWebSearch(binding.appSearch.getQuery().toString()));
        ArrayAdapter<CharSequence> drawerGroupAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.drawer_group_options,
                android.R.layout.simple_spinner_item);
        drawerGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.drawerGroup.setAdapter(drawerGroupAdapter);
        binding.drawerGroup.setSelection(
                DrawerGroupConfiguration.spinnerIndex(preferences.drawerGroup()));
        binding.drawerGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setDrawerGroup(
                        DrawerGroupConfiguration.preferenceForSpinnerIndex(position));
                refreshAppResults();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        ArrayAdapter<CharSequence> drawerSortAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.drawer_sort_options,
                android.R.layout.simple_spinner_item);
        drawerSortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.drawerSort.setAdapter(drawerSortAdapter);
        binding.drawerSort.setSelection(
                DrawerSortConfiguration.spinnerIndex(preferences.drawerSort()));
        binding.drawerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setDrawerSort(
                        DrawerSortConfiguration.preferenceForSpinnerIndex(position));
                refreshAppResults();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        binding.appSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return true; }
            @Override public boolean onQueryTextChange(String newText) {
                refreshAppResults();
                updateWebSearchButton(newText);
                return true;
            }
        });
        loadApps();
        binding.workspace.post(this::restoreWidgets);
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
        quickFolder = new QuickFolder(preferences.quickFolderAppIds());
        applyDisplayPreferences();
        binding.workspace.setGrid(spanCount(), 8);
        if (appsLoaded) {
            loadApps();
        }
        if (!widgetsRestored) restoreWidgets();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ContextCompat.registerReceiver(
                this,
                notificationDotsReceiver,
                new IntentFilter(LauncherNotificationListener.ACTION_NOTIFICATION_DOTS_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED);
        IntentFilter contextFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        contextFilter.addAction(Intent.ACTION_DATE_CHANGED);
        contextFilter.addAction(Intent.ACTION_TIME_CHANGED);
        contextFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        Intent batteryIntent = ContextCompat.registerReceiver(
                this,
                contextCardReceiver,
                contextFilter,
                ContextCompat.RECEIVER_EXPORTED);
        applyNotificationDots();
        updateContextCard(batteryIntent);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(notificationDotsReceiver);
        unregisterReceiver(contextCardReceiver);
        super.onStop();
    }

    private void applyDisplayPreferences() {
        int requestedOrientation = HomeRotationConfiguration.requestedOrientation(
                preferences.allowHomeRotation());
        if (getRequestedOrientation() != requestedOrientation) {
            setRequestedOrientation(requestedOrientation);
        }
        int columns = GridConfiguration.resolveColumns(
                preferences.gridColumns(),
                getResources().getConfiguration().screenWidthDp);
        binding.workspace.setGrid(columns, 8);
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
        updateContextCard(null);
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
        showDrawer(true);
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

    private void updateContextCard(@Nullable Intent intent) {
        boolean visible = preferences.showContextCard();
        binding.contextCard.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) {
            return;
        }
        if (intent != null && Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            batteryPercentage = BatteryStatus.percentage(
                    intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1));
            batteryCharging = BatteryStatus.isCharging(
                    intent.getIntExtra(
                            BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN));
        }
        String formattedDate = DateFormat.getDateInstance(DateFormat.FULL).format(new Date());
        binding.contextDate.setText(formattedDate);
        String batteryText;
        if (batteryPercentage < 0) {
            batteryText = getString(R.string.battery_status_unavailable);
        } else {
            batteryText = getString(
                    batteryCharging ? R.string.battery_charging : R.string.battery_remaining,
                    batteryPercentage);
        }
        binding.contextBattery.setText(batteryText);
        binding.contextCard.setContentDescription(getString(
                R.string.context_card_description, formattedDate, batteryText));
    }

    private void openCalendar() {
        try {
            startActivity(Intent.makeMainSelectorActivity(
                    Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALENDAR));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.calendar_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadApps() {
        executor.execute(() -> {
            List<AppEntry> loadedApps = new LaunchableAppRepository(this).load();
            runOnUiThread(() -> {
                allApps = loadedApps;
                appsLoaded = true;
                ensureInitialHomeApps();
                displayHomeApps();
                removeUnavailableSavedApps();
                displayFavorites();
                displayQuickFolder();
                refreshAppResults();
            });
        });
    }

    private void refreshAppResults() {
        List<AppEntry> filteredApps = AppFilter.filter(
                visibleApps(),
                binding.appSearch.getQuery().toString(),
                preferences.drawerGroup());
        displayApps(DrawerSortConfiguration.sort(
                filteredApps, preferences.drawerSort(), recentApps.snapshot()));
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
        if (quickFolder.retainAvailable(availableAppIds)) {
            preferences.setQuickFolderAppIds(quickFolder.snapshot());
        }
        if (recentApps.retainAvailable(availableAppIds)) {
            preferences.setRecentAppIds(recentApps.snapshot());
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

    private List<AppEntry> quickFolderApps() {
        List<AppEntry> apps = new ArrayList<>();
        Set<String> includedIds = quickFolder.snapshot();
        for (AppEntry app : allApps) {
            if (includedIds.contains(app.getId()) && !hiddenApps.contains(app.getId())) {
                apps.add(app);
            }
        }
        return apps;
    }

    private void displayQuickFolder() {
        List<AppEntry> apps = quickFolderApps();
        boolean hasApps = !apps.isEmpty();
        binding.quickFolder.setVisibility(hasApps ? View.VISIBLE : View.GONE);
        if (hasApps) {
            binding.quickFolder.setText(getString(
                    R.string.quick_folder_button,
                    preferences.quickFolderName(),
                    apps.size()));
        }
    }

    private void showQuickFolder() {
        List<AppEntry> apps = quickFolderApps();
        if (apps.isEmpty()) {
            return;
        }
        String[] labels = new String[apps.size()];
        for (int index = 0; index < apps.size(); index++) {
            labels[index] = apps.get(index).getLabel();
        }
        new AlertDialog.Builder(this)
                .setTitle(preferences.quickFolderName())
                .setItems(labels, (dialog, index) -> onOpenApp(apps.get(index)))
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void showDrawer(boolean visible) {
        binding.drawerPanel.setVisibility(visible ? View.VISIBLE : View.GONE);
        binding.homeControls.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    private void ensureInitialHomeApps() {
        if (!preferences.homeAppPositions().isEmpty()) return;
        Map<String, int[]> positions = new HashMap<>();
        int index = 0;
        for (String id : favoriteApps.snapshot()) {
            if (index >= 5) break;
            positions.put(id, new int[] { index, 6 });
            index++;
        }
        for (AppEntry app : allApps) {
            if (index >= 5) break;
            if (!positions.containsKey(app.getId()) && !hiddenApps.contains(app.getId())) {
                positions.put(app.getId(), new int[] { index, 6 });
                index++;
            }
        }
        preferences.setHomeAppPositions(positions);
    }

    private void displayHomeApps() {
        Map<String, int[]> positions = preferences.homeAppPositions();
        Map<String, AppEntry> apps = new HashMap<>();
        for (AppEntry app : allApps) apps.put(app.getId(), app);
        for (Map.Entry<String, int[]> entry : positions.entrySet()) {
            AppEntry app = apps.get(entry.getKey());
            if (app != null && !hiddenApps.contains(app.getId())) {
                binding.workspace.addApp(app, entry.getValue()[0], entry.getValue()[1]);
            }
        }
    }

    private void addAppToHome(AppEntry app) {
        Map<String, int[]> positions = preferences.homeAppPositions();
        if (positions.containsKey(app.getId())) {
            Toast.makeText(this, R.string.app_already_on_home, Toast.LENGTH_SHORT).show();
            return;
        }
        for (int y = 0; y < 8; y++) {
            boolean placed = false;
            for (int x = 0; x < spanCount(); x++) {
                boolean occupied = false;
                for (int[] p : positions.values()) {
                    if (p[0] == x && p[1] == y) { occupied = true; break; }
                }
                if (!occupied) { positions.put(app.getId(), new int[] { x, y }); placed = true; break; }
            }
            if (placed) break;
        }
        preferences.setHomeAppPositions(positions);
        displayHomeApps();
        showDrawer(false);
    }

    private void pickWidget() {
        int id = appWidgetHost.allocateAppWidgetId();
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        pendingWidgetId = id;
        startActivityForResult(intent, APPWIDGET_PICK_REQUEST);
    }

    private void restoreWidgets() {
        if (appWidgetHost == null || widgetsRestored) return;
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        widgetsRestored = true;
        for (String entry : preferences.homeWidgetEntries()) {
            String[] p = entry.split("\\\\|");
            if (p.length < 6) continue;
            try {
                int id = Integer.parseInt(p[0]);
                AppWidgetProviderInfo info = manager.getAppWidgetInfo(id);
                if (info == null) continue;
                AppWidgetHostView view = appWidgetHost.createView(this, id, info);
                addWidgetView(view, Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4]), Integer.parseInt(p[5]));
            } catch (NumberFormatException ignored) { }
        }
    }

    private void addWidgetView(AppWidgetHostView view, int cellX, int cellY, int spanX, int spanY) {
        int width = Math.max(1, binding.workspace.getWidth() / spanCount());
        int height = Math.max(1, binding.workspace.getHeight() / 8);
        android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams(width * spanX, height * spanY);
        lp.leftMargin = cellX * width;
        lp.topMargin = cellY * height;
        binding.workspace.addView(view, lp);
    }

    private void finishAddingWidget() {
        if (pendingWidgetId == AppWidgetHost.INVALID_APPWIDGET_ID) return;
        AppWidgetProviderInfo info = AppWidgetManager.getInstance(this).getAppWidgetInfo(pendingWidgetId);
        if (info == null) return;
        AppWidgetHostView view = appWidgetHost.createView(this, pendingWidgetId, info);
        addWidgetView(view, 0, 0, Math.min(2, spanCount()), 2);
        Set<String> entries = preferences.homeWidgetEntries();
        entries.add(pendingWidgetId + "|" + info.provider.flattenToString() + "|0|0|2|2");
        preferences.setHomeWidgetEntries(entries);
        pendingWidgetId = AppWidgetHost.INVALID_APPWIDGET_ID;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            if (pendingWidgetId != AppWidgetHost.INVALID_APPWIDGET_ID) appWidgetHost.deleteAppWidgetId(pendingWidgetId);
            pendingWidgetId = AppWidgetHost.INVALID_APPWIDGET_ID;
            return;
        }
        if (requestCode == APPWIDGET_PICK_REQUEST) {
            AppWidgetProviderInfo info = AppWidgetManager.getInstance(this).getAppWidgetInfo(pendingWidgetId);
            if (info != null && info.configure != null) {
                Intent configure = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).setComponent(info.configure);
                configure.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingWidgetId);
                startActivityForResult(configure, APPWIDGET_CONFIGURE_REQUEST);
            } else {
                finishAddingWidget();
            }
        } else if (requestCode == APPWIDGET_CONFIGURE_REQUEST) {
            finishAddingWidget();
        }
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
        if (recentApps.record(app.getId())) {
            preferences.setRecentAppIds(recentApps.snapshot());
        }
    }

    @Override
    public boolean onLongPressApp(AppEntry app) {
        showAppActions(app);
        return true;
    }

    private void showAppActions(AppEntry app) {
        boolean favorite = favoriteApps.contains(app.getId());
        boolean inQuickFolder = quickFolder.contains(app.getId());
        String folderName = preferences.quickFolderName();
        String[] actions = {
                getString(favorite ? R.string.unpin_from_favorites : R.string.pin_to_favorites),
                getString(R.string.add_to_home),
                getString(
                        inQuickFolder ? R.string.remove_from_folder : R.string.add_to_folder,
                        folderName),
                getString(R.string.hide_app_action),
                getString(R.string.open_app_info),
                getString(R.string.uninstall_app)
        };
        new AlertDialog.Builder(this)
                .setTitle(app.getLabel())
                .setItems(actions, (dialog, index) -> performAppAction(app, index))
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void performAppAction(AppEntry app, int actionIndex) {
        switch (actionIndex) {
            case 0:
                toggleFavorite(app);
                break;
            case 1:
                addAppToHome(app);
                break;
            case 2:
                toggleQuickFolderMembership(app);
                break;
            case 3:
                hideApp(app);
                break;
            case 4:
                openAppInfo(app);
                break;
            case 5:
                requestUninstall(app);
                break;
            default:
                break;
        }
    }

    private void toggleFavorite(AppEntry app) {
        boolean pinned = favoriteApps.toggle(app.getId());
        saveFavorites();
        displayFavorites();
        Toast.makeText(
                this,
                getString(pinned ? R.string.app_pinned : R.string.app_unpinned, app.getLabel()),
                Toast.LENGTH_SHORT)
                .show();
    }

    private void toggleQuickFolderMembership(AppEntry app) {
        boolean included = !quickFolder.contains(app.getId());
        if (quickFolder.setIncluded(app.getId(), included)) {
            preferences.setQuickFolderAppIds(quickFolder.snapshot());
            displayQuickFolder();
        }
        Toast.makeText(
                this,
                getString(
                        included ? R.string.app_added_to_folder : R.string.app_removed_from_folder,
                        app.getLabel(),
                        preferences.quickFolderName()),
                Toast.LENGTH_SHORT)
                .show();
    }

    private void hideApp(AppEntry app) {
        if (hiddenApps.setHidden(app.getId(), true)) {
            saveHiddenApps();
            displayFavorites();
            displayQuickFolder();
            refreshAppResults();
        }
        Toast.makeText(
                this,
                getString(R.string.app_hidden, app.getLabel()),
                Toast.LENGTH_SHORT)
                .show();
    }

    private void openAppInfo(AppEntry app) {
        openPackageAction(
                new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", app.getPackageName(), null)));
    }

    private void requestUninstall(AppEntry app) {
        openPackageAction(new Intent(
                Intent.ACTION_DELETE,
                Uri.fromParts("package", app.getPackageName(), null)));
    }

    private void openPackageAction(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.app_management_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (appWidgetHost != null) appWidgetHost.stopListening();
        executor.shutdownNow();
        super.onDestroy();
    }
}
