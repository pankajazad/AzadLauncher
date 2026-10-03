package com.pankajazad.azadlauncher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pankajazad.azadlauncher.databinding.ActivitySettingsBinding;

public final class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private LauncherPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferences = new LauncherPreferences(this);

        ArrayAdapter<CharSequence> gridAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.grid_column_options,
                android.R.layout.simple_spinner_item);
        gridAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.gridColumns.setAdapter(gridAdapter);
        binding.gridColumns.setSelection(GridConfiguration.spinnerIndex(preferences.gridColumns()));
        binding.gridColumns.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setGridColumns(GridConfiguration.columnsForSpinnerIndex(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        binding.showLabels.setChecked(preferences.showAppLabels());
        binding.showLabels.setOnCheckedChangeListener(
                (buttonView, isChecked) -> preferences.setShowAppLabels(isChecked));
        binding.manageHiddenApps.setOnClickListener(
                view -> startActivity(new Intent(this, HiddenAppsActivity.class)));
        binding.done.setOnClickListener(view -> finish());
    }
}
