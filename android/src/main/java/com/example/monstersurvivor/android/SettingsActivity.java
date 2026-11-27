package com.example.monstersurvivor.android;

import android.annotation.SuppressLint;
import android.content.Context;
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
 * Settings Activity managing BGM, SFX, Haptic Vibration, Language, and Theme options.
 * Persists all configurations to SharedPreferences.
 */
public class SettingsActivity extends NativeAudioActivity {

    private GamePreferences mPrefs;

    private SwitchMaterial mSwitchMusic;
    private SwitchMaterial mSwitchSfx;
    private SwitchMaterial mSwitchVibration;

    private SeekBar mSeekMusic;
    private SeekBar mSeekSfx;

    private TextView mTvMusicVol;
    private TextView mTvSfxVol;

    private RadioGroup mRgLanguage;
    private RadioButton mRbLangVi;
    private RadioButton mRbLangEn;

    private RadioGroup mRgTheme;
    private RadioButton mRbForest;
    private RadioButton mRbDark;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(UiUtils.wrapLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        UiUtils.applyImmersiveMode(this);
        setContentView(R.layout.activity_settings);

        mPrefs = GamePreferences.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_settings);
        toolbar.setNavigationOnClickListener(v -> UiUtils.navigateBack(this));

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                UiUtils.navigateBack(SettingsActivity.this);
            }
        });

        initViews();
        loadValues();
        setupListeners();
        mSeekMusic.setEnabled(mPrefs.isMusicEnabled());mSeekSfx.setEnabled(mPrefs.isSfxEnabled());
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

        mRgLanguage = findViewById(R.id.rg_language);
        mRbLangVi = findViewById(R.id.rb_lang_vi);
        mRbLangEn = findViewById(R.id.rb_lang_en);

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

        if ("en".equalsIgnoreCase(mPrefs.getGameLanguage())) {
            mRbLangEn.setChecked(true);
        } else {
            mRbLangVi.setChecked(true);
        }

        if (GamePreferences.THEME_DARK.equals(mPrefs.getTheme())) {
            mRbDark.setChecked(true);
        } else {
            mRbForest.setChecked(true);
        }
    }

    private void setupListeners() {
        mSwitchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPrefs.setMusicEnabled(isChecked);
            com.example.monstersurvivor.android.audio.NativeAudio.refresh(this);
            mSeekMusic.setEnabled(isChecked);
        });

        mSwitchSfx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPrefs.setSfxEnabled(isChecked);
            com.example.monstersurvivor.android.audio.NativeAudio.refresh(this);
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
                    com.example.monstersurvivor.android.audio.NativeAudio.refresh(SettingsActivity.this);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {com.example.monstersurvivor.android.audio.NativeAudio.play(SettingsActivity.this,com.example.monstersurvivor.audio.AudioEvent.UI_CONFIRM);}
        });

        mSeekSfx.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvSfxVol.setText(getString(R.string.setting_sfx_volume, progress));
                if (fromUser) {
                    mPrefs.setSfxVolume(progress);
                    com.example.monstersurvivor.android.audio.NativeAudio.refresh(SettingsActivity.this);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {com.example.monstersurvivor.android.audio.NativeAudio.play(SettingsActivity.this,com.example.monstersurvivor.audio.AudioEvent.UI_CONFIRM);}
        });

        mRgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String currentLang = mPrefs.getGameLanguage();
            String targetLang = (checkedId == R.id.rb_lang_en) ? "en" : "vi";

            if (!targetLang.equalsIgnoreCase(currentLang)) {
                mPrefs.setGameLanguage(targetLang);
                Toast.makeText(this, R.string.toast_language_changed, Toast.LENGTH_SHORT).show();
                recreate();
            }
        });

        mRgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String currentTheme = mPrefs.getTheme();
            String targetTheme = (checkedId == R.id.rb_theme_dark)
                    ? GamePreferences.THEME_DARK
                    : GamePreferences.THEME_FOREST;

            if (!targetTheme.equals(currentTheme)) {
                mPrefs.setTheme(targetTheme);
                Toast.makeText(this, R.string.toast_theme_changed, Toast.LENGTH_SHORT).show();
                recreate();
            }
        });

        findViewById(R.id.btn_reset_defaults).setOnClickListener(v -> UiUtils.animateClick(v, () -> {
            mPrefs.resetDefaults();
            com.example.monstersurvivor.android.audio.NativeAudio.refresh(this);
            loadValues();
            Toast.makeText(this, R.string.toast_defaults_reset, Toast.LENGTH_SHORT).show();
            recreate();
        }));

        findViewById(R.id.btn_about).setOnClickListener(v -> UiUtils.animateClick(v, this::showAboutDialog));
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
}
