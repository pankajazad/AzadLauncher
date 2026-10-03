package com.pankajazad.azadlauncher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.pankajazad.azadlauncher.databinding.ActivitySettingsBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public final class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private LauncherPreferences preferences;
    private ActivityResultLauncher<String> createBackupLauncher;
    private ActivityResultLauncher<String[]> openBackupLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferences = new LauncherPreferences(this);
        createBackupLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/octet-stream"),
                this::writeBackup);
        openBackupLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::readBackup);

        ArrayAdapter<CharSequence> gridAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.grid_column_options,
                android.R.layout.simple_spinner_item);
        gridAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.gridColumns.setAdapter(gridAdapter);
        binding.gridColumns.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setGridColumns(GridConfiguration.columnsForSpinnerIndex(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.theme_options,
                android.R.layout.simple_spinner_item);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.themeMode.setAdapter(themeAdapter);
        binding.themeMode.setSelection(ThemeConfiguration.spinnerIndex(preferences.themeMode()));
        binding.themeMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int themeMode = ThemeConfiguration.preferenceForSpinnerIndex(position);
                preferences.setThemeMode(themeMode);
                int nightMode = ThemeConfiguration.appCompatNightMode(themeMode);
                if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
                    AppCompatDelegate.setDefaultNightMode(nightMode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ArrayAdapter<CharSequence> iconSizeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.icon_size_options,
                android.R.layout.simple_spinner_item);
        iconSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.iconSize.setAdapter(iconSizeAdapter);
        binding.iconSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setIconSize(
                        IconSizeConfiguration.preferenceForSpinnerIndex(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ArrayAdapter<CharSequence> swipeActionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gesture_action_options,
                android.R.layout.simple_spinner_item);
        swipeActionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.swipeDownAction.setAdapter(swipeActionAdapter);
        binding.swipeDownAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setSwipeDownAction(
                        GestureActionConfiguration.preferenceForSpinnerIndex(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ArrayAdapter<CharSequence> webSearchAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.web_search_provider_options,
                android.R.layout.simple_spinner_item);
        webSearchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.webSearchProvider.setAdapter(webSearchAdapter);
        binding.webSearchProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setWebSearchProvider(
                        WebSearchProvider.preferenceForSpinnerIndex(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        binding.showLabels.setOnCheckedChangeListener(
                (buttonView, isChecked) -> preferences.setShowAppLabels(isChecked));
        binding.showContextCard.setOnCheckedChangeListener(
                (buttonView, isChecked) -> preferences.setShowContextCard(isChecked));
        binding.manageHiddenApps.setOnClickListener(
                view -> startActivity(new Intent(this, HiddenAppsActivity.class)));
        binding.notificationAccess.setOnClickListener(
                view -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        binding.exportSettings.setOnClickListener(
                view -> createBackupLauncher.launch("azad-launcher-backup.azad"));
        binding.importSettings.setOnClickListener(
                view -> openBackupLauncher.launch(new String[] {"application/octet-stream", "text/plain"}));
        binding.done.setOnClickListener(view -> finish());
        refreshControls();
    }

    private void refreshControls() {
        binding.gridColumns.setSelection(GridConfiguration.spinnerIndex(preferences.gridColumns()));
        binding.showLabels.setChecked(preferences.showAppLabels());
        binding.showContextCard.setChecked(preferences.showContextCard());
        binding.themeMode.setSelection(ThemeConfiguration.spinnerIndex(preferences.themeMode()));
        binding.iconSize.setSelection(IconSizeConfiguration.spinnerIndex(preferences.iconSize()));
        binding.swipeDownAction.setSelection(
                GestureActionConfiguration.spinnerIndex(preferences.swipeDownAction()));
        binding.webSearchProvider.setSelection(
                WebSearchProvider.spinnerIndex(preferences.webSearchProvider()));
    }

    private void writeBackup(Uri uri) {
        if (uri == null) {
            return;
        }
        try (OutputStream output = getContentResolver().openOutputStream(uri);
             OutputStreamWriter writer = output == null
                     ? null
                     : new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            if (writer == null) {
                throw new IOException("Unable to open backup destination");
            }
            writer.write(BackupCodec.encode(preferences.backupData()));
            Toast.makeText(this, R.string.backup_exported, Toast.LENGTH_SHORT).show();
        } catch (IOException exception) {
            Toast.makeText(this, R.string.backup_export_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void readBackup(Uri uri) {
        if (uri == null) {
            return;
        }
        try (InputStream input = getContentResolver().openInputStream(uri);
             Reader reader = input == null
                     ? null
                     : new InputStreamReader(input, StandardCharsets.UTF_8)) {
            if (reader == null) {
                throw new IOException("Unable to open backup");
            }
            StringBuilder contents = new StringBuilder();
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                contents.append(buffer, 0, read);
                if (contents.length() > BackupCodec.MAX_BACKUP_CHARACTERS) {
                    throw new IllegalArgumentException("Backup is too large");
                }
            }
            preferences.restore(BackupCodec.decode(contents.toString()));
            refreshControls();
            Toast.makeText(this, R.string.backup_imported, Toast.LENGTH_SHORT).show();
        } catch (IOException | IllegalArgumentException exception) {
            Toast.makeText(this, R.string.backup_import_failed, Toast.LENGTH_LONG).show();
        }
    }
}
