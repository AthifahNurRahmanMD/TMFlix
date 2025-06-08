package com.example.tmflix.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingPreference {

    private static final String PREFS_NAME = "setting_pref";
    private static final String THEME_KEY = "selected_theme";
    private final SharedPreferences mSharedPreferences;

    public SettingPreference(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setSelectedTheme(String theme) {
        mSharedPreferences.edit().putString(THEME_KEY, theme).apply();
    }

    public String getSelectedTheme() {
        return mSharedPreferences.getString(THEME_KEY, "light");
    }
}
