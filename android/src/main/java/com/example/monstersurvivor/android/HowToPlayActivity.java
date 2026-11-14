package com.example.monstersurvivor.android;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.monstersurvivor.R;
import com.example.monstersurvivor.android.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * How to Play guide detailing joystick control, weapons, skills, boxes, and victory conditions.
 */
public class HowToPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        UiUtils.applyImmersiveMode(this);
        setContentView(R.layout.activity_how_to_play);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_htp);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiUtils.applyImmersiveMode(this);
    }
}
