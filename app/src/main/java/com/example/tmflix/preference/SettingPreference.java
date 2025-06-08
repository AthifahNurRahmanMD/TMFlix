package com.example.tmflix.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingPreference {

    private static final String PREFS_NAME = "setting_pref";
    private final SharedPreferences mSharedPreferences;

    public SettingPreference(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
