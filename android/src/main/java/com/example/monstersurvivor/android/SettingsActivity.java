package com.example.monstersurvivor.android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.monstersurvivor.R;
import com.example.monstersurvivor.android.model.GamePreferences;
import com.example.monstersurvivor.android.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Settings Activity managing BGM, SFX, Haptic Vibration, and Theme options.
 * Persists all configurations to SharedPreferences.
 */
public class SettingsActivity extends AppCompatActivity {

    private GamePreferences mPrefs;

    private SwitchMaterial mSwitchMusic;
    private SwitchMaterial mSwitchSfx;
    private SwitchMaterial mSwitchVibration;

    private SeekBar mSeekMusic;
    private SeekBar mSeekSfx;

    private TextView mTvMusicVol;
    private TextView mTvSfxVol;

    private RadioGroup mRgTheme;
    private RadioButton mRbForest;
    private RadioButton mRbDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        UiUtils.applyImmersiveMode(this);
        setContentView(R.layout.activity_settings);

        mPrefs = GamePreferences.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_settings);
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadValues();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiUtils.applyImmersiveMode(this);
    }

    private void initViews() {
        mSwitchMusic = findViewById(R.id.switch_music);
        mSwitchSfx = findViewById(R.id.switch_sfx);
        mSwitchVibration = findViewById(R.id.switch_vibration);

        mSeekMusic = findViewById(R.id.seekbar_music_volume);
        mSeekSfx = findViewById(R.id.seekbar_sfx_volume);

        mTvMusicVol = findViewById(R.id.tv_music_vol_label);
        mTvSfxVol = findViewById(R.id.tv_sfx_vol_label);

        mRgTheme = findViewById(R.id.rg_theme);
        mRbForest = findViewById(R.id.rb_theme_forest);
        mRbDark = findViewById(R.id.rb_theme_dark);
    }

    @SuppressLint("SetTextI18n")
    private void loadValues() {
        mSwitchMusic.setChecked(mPrefs.isMusicEnabled());
        mSwitchSfx.setChecked(mPrefs.isSfxEnabled());
        mSwitchVibration.setChecked(mPrefs.isVibrationEnabled());

        int musicVol = mPrefs.getMusicVolume();
        mSeekMusic.setProgress(musicVol);
        mTvMusicVol.setText(getString(R.string.setting_music_volume, musicVol));

        int sfxVol = mPrefs.getSfxVolume();
        mSeekSfx.setProgress(sfxVol);
        mTvSfxVol.setText(getString(R.string.setting_sfx_volume, sfxVol));

        if (GamePreferences.THEME_DARK.equals(mPrefs.getTheme())) {
            mRbDark.setChecked(true);
        } else {
            mRbForest.setChecked(true);
        }
    }

    private void setupListeners() {
        mSwitchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPrefs.setMusicEnabled(isChecked);
            mSeekMusic.setEnabled(isChecked);
        });

        mSwitchSfx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPrefs.setSfxEnabled(isChecked);
            mSeekSfx.setEnabled(isChecked);
        });

        mSwitchVibration.setOnCheckedChangeListener((buttonView, isChecked) ->
                mPrefs.setVibrationEnabled(isChecked));

        mSeekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvMusicVol.setText(getString(R.string.setting_music_volume, progress));
                if (fromUser) {
                    mPrefs.setMusicVolume(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mSeekSfx.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvSfxVol.setText(getString(R.string.setting_sfx_volume, progress));
                if (fromUser) {
                    mPrefs.setSfxVolume(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mRgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String currentTheme = mPrefs.getTheme();
            String targetTheme = (checkedId == R.id.rb_theme_dark)
                    ? GamePreferences.THEME_DARK
                    : GamePreferences.THEME_FOREST;

            if (!targetTheme.equals(currentTheme)) {
                mPrefs.setTheme(targetTheme);
                Toast.makeText(this, "Đã áp dụng giao diện mới", Toast.LENGTH_SHORT).show();
                recreate();
            }
        });

        findViewById(R.id.btn_reset_defaults).setOnClickListener(v -> UiUtils.animateClick(v, () -> {
            mPrefs.resetDefaults();
            loadValues();
            Toast.makeText(this, "Đã khôi phục cài đặt mặc định", Toast.LENGTH_SHORT).show();
            recreate();
        }));

        findViewById(R.id.btn_about).setOnClickListener(v -> UiUtils.animateClick(v, this::showAboutDialog));
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_about_title)
                .setMessage(R.string.dialog_about_msg)
                .setIcon(R.drawable.ic_info)
                .setPositiveButton(R.string.dialog_close, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
