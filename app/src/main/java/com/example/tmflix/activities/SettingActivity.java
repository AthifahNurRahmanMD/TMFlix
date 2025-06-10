package com.example.tmflix.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.tmflix.R;
import com.example.tmflix.preference.SettingPreference;

public class SettingActivity extends AppCompatActivity {

    private SettingPreference settingPreference;
    private Toolbar toolbar;

    // Theme components
    private RadioGroup rgThemeSelector;
    private RadioButton rbLightTheme, rbDarkTheme;

    // Flag untuk menghindari double toast
    private boolean isThemeInitializing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        toolbar = findViewById(R.id.toolbar);

        // Initialize theme components
        rgThemeSelector = findViewById(R.id.rgThemeSelector);
        rbLightTheme = findViewById(R.id.rbLightTheme);
        rbDarkTheme = findViewById(R.id.rbDarkTheme);

        // Setup Toolbar
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settingPreference = new SettingPreference(this);

        // Setup theme selector dan load theme
        setupThemeSelector();
        loadThemePreference();
    }

    private void setupThemeSelector() {
        if (rgThemeSelector != null) {
            rgThemeSelector.setOnCheckedChangeListener((group, checkedId) -> {
                if (isThemeInitializing) return; // Mencegah pemanggilan dobel

                String selectedTheme;
                if (checkedId == R.id.rbDarkTheme) {
                    selectedTheme = "dark";
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Toast.makeText(getApplicationContext(), "Dark theme enabled", Toast.LENGTH_SHORT).show();
                } else {
                    selectedTheme = "light";
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Toast.makeText(getApplicationContext(), "light theme enabled", Toast.LENGTH_SHORT).show();
                }
                settingPreference.setSelectedTheme(selectedTheme);
            });
        }
    }

    private void loadThemePreference() {
        if (rbLightTheme != null && rbDarkTheme != null) {
            String savedTheme = settingPreference.getSelectedTheme();
            isThemeInitializing = true; // Set flag agar saat setChecked tidak trigger listener
            if ("dark".equals(savedTheme)) {
                rbDarkTheme.setChecked(true);
            } else {
                rbLightTheme.setChecked(true);
            }
            isThemeInitializing = false; // Reset flag
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}