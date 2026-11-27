package com.example.monstersurvivor.android;

import androidx.appcompat.app.AppCompatActivity;
import com.example.monstersurvivor.android.audio.NativeAudio;
import com.example.monstersurvivor.audio.AudioEvent;

/** Shared native-screen audio lifecycle; subclasses keep their existing layouts/localization. */
public abstract class NativeAudioActivity extends AppCompatActivity {
    @Override protected void onCreate(android.os.Bundle saved) {
        super.onCreate(saved);
        getOnBackPressedDispatcher().addCallback(this,new androidx.activity.OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                NativeAudio.play(NativeAudioActivity.this,AudioEvent.UI_CANCEL);setEnabled(false);
                try {getOnBackPressedDispatcher().onBackPressed();}finally {setEnabled(true);}
            }
        });
    }
    @Override protected void onResume() {super.onResume();NativeAudio.resume(this);}
    @Override protected void onPause() {NativeAudio.pause(this);super.onPause();}
}
