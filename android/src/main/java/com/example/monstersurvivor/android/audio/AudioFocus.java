package com.example.monstersurvivor.android.audio;

import android.content.Context;
import android.media.*;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import java.util.function.Consumer;

/** Foreground-only game focus, with pause (including duck requests) on every loss. */
public final class AudioFocus {
    private final AudioManager manager;
    private final AudioManager.OnAudioFocusChangeListener listener;
    private AudioFocusRequest request;
    private boolean requested;
    public AudioFocus(Context context,Consumer<Boolean> change) {
        manager=(AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        listener=value -> change.accept(value==AudioManager.AUDIOFOCUS_GAIN);
        if(Build.VERSION.SDK_INT>=26)request=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes()).setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener(listener,new Handler(Looper.getMainLooper())).build();
    }
    public static AudioAttributes attributes() {return new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();}
    @SuppressWarnings("deprecation") public boolean acquire() {
        try {
            int result=Build.VERSION.SDK_INT>=26?manager.requestAudioFocus(request):manager.requestAudioFocus(listener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
            requested=result==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;return requested;
        }catch(RuntimeException error){android.util.Log.e("GameAudioFocus","Focus request failed",error);return false;}
    }
    @SuppressWarnings("deprecation") public void release() {
        if(!requested)return;requested=false;
        try {if(Build.VERSION.SDK_INT>=26)manager.abandonAudioFocusRequest(request);else manager.abandonAudioFocus(listener);}
        catch(RuntimeException error){android.util.Log.e("GameAudioFocus","Focus abandon failed",error);}
    }
}
