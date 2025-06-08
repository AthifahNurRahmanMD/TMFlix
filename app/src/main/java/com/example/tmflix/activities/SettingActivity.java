package com.example.tmflix.activities;

package com.example.tmflix.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.example.tmflix.R;
//import com.example.tmflix2.notification.NotificationDailyReceiver;
//import com.example.tmflix2.notification.NotificationReleaseReceiver;
import com.example.tmflix.preference.SettingPreference;

public class SettingActivity extends AppCompatActivity {

    private SettingPreference settingPreference;
    private Button button;
    private Toolbar toolbar;

    // Theme components
    private RadioGroup rgThemeSelector;
    private RadioButton rbLightTheme, rbDarkTheme;
    private SharedPreferences themePreferences;
    private static final String THEME_PREF = "theme_pref";
    private static final String THEME_KEY = "selected_theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        button = findViewById(R.id.btnChangeLanguage);
        toolbar = findViewById(R.id.toolbar);

        // Initialize theme components
        rgThemeSelector = findViewById(R.id.rgThemeSelector);
        rbLightTheme = findViewById(R.id.rbLightTheme);
        rbDarkTheme = findViewById(R.id.rbDarkTheme);
        themePreferences = getSharedPreferences(THEME_PREF, MODE_PRIVATE);

        // Setup Toolbar
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settingPreference = new SettingPreference(this);


        // Setup theme selector, penting iniiiii
        setupThemeSelector();
        loadThemePreference();

        // Tombol ganti bahasa
        button.setOnClickListener(v -> {
            Intent mIntent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
            startActivity(mIntent);
        });
    }



    private void setupThemeSelector() {
        if (rgThemeSelector != null) {
            rgThemeSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    String selectedTheme;

                    if (checkedId == R.id.rbDarkTheme) {
                        selectedTheme = "dark";
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        Toast.makeText(getApplicationContext(), "Tema gelap diaktifkan", Toast.LENGTH_SHORT).show();
                    } else {
                        selectedTheme = "light";
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        Toast.makeText(getApplicationContext(), "Tema terang diaktifkan", Toast.LENGTH_SHORT).show();
                    }

                    // Simpan pilihan tema
                    SharedPreferences.Editor editor = themePreferences.edit();
                    editor.putString(THEME_KEY, selectedTheme);
                    editor.apply();
                }
            });
        }
    }

    private void loadThemePreference() {
        if (themePreferences != null && rbLightTheme != null && rbDarkTheme != null) {
            String savedTheme = themePreferences.getString(THEME_KEY, "light");

            if (savedTheme.equals("dark")) {
                rbDarkTheme.setChecked(true);
            } else {
                rbLightTheme.setChecked(true);
            }
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