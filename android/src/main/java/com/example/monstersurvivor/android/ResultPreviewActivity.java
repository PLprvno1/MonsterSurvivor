package com.example.monstersurvivor.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.monstersurvivor.R;
import com.example.monstersurvivor.android.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Interactive preview activity showcasing Victory, Game Over, and Pause layouts.
 * Used for evaluation and testing without having to finish a full 10-minute gameplay session.
 */
public class ResultPreviewActivity extends NativeAudioActivity {

    private ViewGroup mContainer;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(UiUtils.wrapLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        UiUtils.applyImmersiveMode(this);
        setContentView(R.layout.activity_result_preview);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_preview);
        toolbar.setNavigationOnClickListener(v -> {com.example.monstersurvivor.android.audio.NativeAudio.play(this,com.example.monstersurvivor.audio.AudioEvent.UI_CANCEL);finish();});

        mContainer = findViewById(R.id.frame_preview_container);

        findViewById(R.id.btn_show_victory).setOnClickListener(v -> showVictoryLayout());
        findViewById(R.id.btn_show_gameover).setOnClickListener(v -> showGameOverLayout());
        findViewById(R.id.btn_show_pause).setOnClickListener(v -> showPauseLayout());

        // Default to Victory layout
        showVictoryLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiUtils.applyImmersiveMode(this);
    }

    private void showVictoryLayout() {
        mContainer.removeAllViews();
        View view = LayoutInflater.from(this).inflate(R.layout.layout_match_result_victory, mContainer, false);
        mContainer.addView(view);

        view.findViewById(R.id.btn_victory_retry).setOnClickListener(v -> {
            Toast.makeText(this, R.string.toast_preview_retry, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AndroidLauncher.class);
            startActivity(intent);
            finish();
        });

        view.findViewById(R.id.btn_victory_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void showGameOverLayout() {
        mContainer.removeAllViews();
        View view = LayoutInflater.from(this).inflate(R.layout.layout_match_result_gameover, mContainer, false);
        mContainer.addView(view);

        view.findViewById(R.id.btn_gameover_retry).setOnClickListener(v -> {
            Toast.makeText(this, R.string.toast_preview_retry, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AndroidLauncher.class);
            startActivity(intent);
            finish();
        });

        view.findViewById(R.id.btn_gameover_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void showPauseLayout() {
        mContainer.removeAllViews();
        View view = LayoutInflater.from(this).inflate(R.layout.layout_game_pause, mContainer, false);
        mContainer.addView(view);

        view.findViewById(R.id.btn_pause_resume).setOnClickListener(v ->
                Toast.makeText(this, R.string.toast_preview_resume, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_pause_settings).setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.btn_pause_give_up).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
