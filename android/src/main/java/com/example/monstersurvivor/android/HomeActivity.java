package com.example.monstersurvivor.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.monstersurvivor.R;
import com.example.monstersurvivor.android.model.GamePreferences;
import com.example.monstersurvivor.android.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main Home Activity for Monster Survivor.
 * Acts as the primary LAUNCHER entry point. Supports bilingual localization.
 */
public class HomeActivity extends NativeAudioActivity {

    private BottomNavigationView mBottomNav;
    private View mPanelTitle;
    private View mLayoutButtons;
    private String mCurrentLanguage;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(UiUtils.wrapLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        UiUtils.applyImmersiveMode(this);
        setContentView(R.layout.activity_home);

        mCurrentLanguage = GamePreferences.getInstance(this).getGameLanguage();

        MaterialToolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.game_title);
            getSupportActionBar().setSubtitle(null);
        }

        mPanelTitle = findViewById(R.id.panel_title);
        mLayoutButtons = findViewById(R.id.layout_action_buttons);

        setupButtons();
        setupBottomNavigation();
        setupBackNavigation();
        playEntranceAnimations();
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiUtils.applyImmersiveMode(this);
        String savedLang = GamePreferences.getInstance(this).getGameLanguage();
        if (mCurrentLanguage != null && !mCurrentLanguage.equals(savedLang)) {
            mCurrentLanguage = savedLang;
            recreate();
            return;
        }
        if (mBottomNav != null) {
            mBottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void playEntranceAnimations() {
        Animation slideLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        Animation slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        if (mPanelTitle != null) {
            mPanelTitle.startAnimation(slideLeft);
        }
        if (mLayoutButtons != null) {
            mLayoutButtons.startAnimation(slideRight);
        }
    }

    private void setupButtons() {
        View btnPlay = findViewById(R.id.btn_play);
        View btnAchievements = findViewById(R.id.btn_achievements);
        View btnSettings = findViewById(R.id.btn_settings);
        View btnHowToPlay = findViewById(R.id.btn_how_to_play);
        View btnExit = findViewById(R.id.btn_exit);

        btnPlay.setOnClickListener(v -> UiUtils.animateClick(v, () -> {
            Intent intent = new Intent(HomeActivity.this, GameModeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }));

        btnAchievements.setOnClickListener(v -> UiUtils.animateClick(v, () -> {
            Intent intent = new Intent(HomeActivity.this, AchievementsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }));

        btnSettings.setOnClickListener(v -> UiUtils.animateClick(v, () -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }));

        btnHowToPlay.setOnClickListener(v -> UiUtils.animateClick(v, () -> {
            Intent intent = new Intent(HomeActivity.this, HowToPlayActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }));

        btnExit.setOnClickListener(v -> UiUtils.animateClick(v, this::showExitConfirmationDialog));
    }

    private void setupBottomNavigation() {
        mBottomNav = findViewById(R.id.bottom_navigation);
        mBottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_achievements) {
                Intent intent = new Intent(HomeActivity.this, AchievementsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }
            return false;
        });
    }

    private void showExitConfirmationDialog() {
        com.example.monstersurvivor.android.audio.NativeAudio.play(this, com.example.monstersurvivor.audio.AudioEvent.UI_CANCEL);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_exit_title)
                .setMessage(R.string.dialog_exit_msg)
                .setIcon(R.drawable.ic_exit)
                .setPositiveButton(R.string.dialog_yes, (d, which) -> {
                    d.dismiss();
                    com.example.monstersurvivor.android.audio.NativeAudio.pause(this);
                    finishAffinity();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask();
                    }
                })
                .setNegativeButton(R.string.dialog_no, (d, which) -> {
                    com.example.monstersurvivor.android.audio.NativeAudio.play(this, com.example.monstersurvivor.audio.AudioEvent.UI_CANCEL);
                    d.dismiss();
                })
                .setOnCancelListener(d -> {
                    com.example.monstersurvivor.android.audio.NativeAudio.play(this, com.example.monstersurvivor.audio.AudioEvent.UI_CANCEL);
                })
                .create();
        dialog.show();
        UiUtils.styleDialog(dialog);
    }

    private void showAboutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_about_title)
                .setMessage(R.string.dialog_about_msg)
                .setIcon(R.drawable.ic_info)
                .setPositiveButton(R.string.dialog_close, (d, which) -> d.dismiss())
                .create();
        dialog.show();
        UiUtils.styleDialog(dialog);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_item_shop) {
            startActivity(new Intent(this, ItemShopActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        } else if (id == R.id.action_how_to_play) {
            startActivity(new Intent(this, HowToPlayActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_theme_forest) {
            GamePreferences.getInstance(this).setTheme(GamePreferences.THEME_FOREST);
            Toast.makeText(this, R.string.toast_theme_forest, Toast.LENGTH_SHORT).show();
            recreate();
            return true;
        } else if (id == R.id.action_theme_dark) {
            GamePreferences.getInstance(this).setTheme(GamePreferences.THEME_DARK);
            Toast.makeText(this, R.string.toast_theme_dark, Toast.LENGTH_SHORT).show();
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
