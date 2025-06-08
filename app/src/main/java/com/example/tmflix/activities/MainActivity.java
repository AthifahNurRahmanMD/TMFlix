package com.example.tmflix.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tmflix.R;
import com.example.tmflix.utils.BottomBarBehavior;

public class MainActivity extends AppCompatActivity {

    ImageView imgNotification;
    LinearLayout tabFilm, tabTV, tabFav;
    View indicatorFilm, indicatorTV, indicatorFav;

    // Theme constants
    private static final String THEME_PREF = "theme_pref";
    private static final String THEME_KEY = "selected_theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setContentView
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        // Tombol notifikasi
        imgNotification = findViewById(R.id.imgNotification);
        imgNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        // Inisialisasi tab
        tabFilm = findViewById(R.id.tabFilm);
        tabTV = findViewById(R.id.tabTV);
        tabFav = findViewById(R.id.tabFav);

        // Inisialisasi indicator
        indicatorFilm = findViewById(R.id.indicatorMovie);
        indicatorTV = findViewById(R.id.indicatorTv);
        indicatorFav = findViewById(R.id.indicatorFavorite);

        // Bottom bar behavior
        LinearLayout navigationBar = findViewById(R.id.navigationBar);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navigationBar.getLayoutParams();
        layoutParams.setBehavior(new BottomBarBehavior());

        // Setup NavController dari NavHostFragment
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // Sinkronkan indicator dengan fragment yang tampil
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            indicatorFilm.setVisibility(destination.getId() == R.id.fragmentMovie ? View.VISIBLE : View.GONE);
            indicatorTV.setVisibility(destination.getId() == R.id.fragmentTv ? View.VISIBLE : View.GONE);
            indicatorFav.setVisibility(destination.getId() == R.id.fragmentFavorite ? View.VISIBLE : View.GONE);
        });

        // Event klik tab navigasi
        tabFilm.setOnClickListener(v -> navController.navigate(R.id.fragmentMovie));
        tabTV.setOnClickListener(v -> navController.navigate(R.id.fragmentTv));
        tabFav.setOnClickListener(v -> navController.navigate(R.id.fragmentFavorite));
    }

    private void applyTheme() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(THEME_PREF, MODE_PRIVATE);
            String savedTheme = sharedPreferences.getString(THEME_KEY, "light");

            if (savedTheme.equals("dark")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        } catch (Exception e) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkThemeChange();
    }

    private void checkThemeChange() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(THEME_PREF, MODE_PRIVATE);
            String savedTheme = sharedPreferences.getString(THEME_KEY, "light");

            boolean currentlyDark = (getResources().getConfiguration().uiMode &
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES;

            boolean shouldBeDark = savedTheme.equals("dark");

            if (currentlyDark != shouldBeDark) {
                recreate();
            }
        } catch (Exception e) {
            // Ignore error
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        window.setAttributes(winParams);
    }
}