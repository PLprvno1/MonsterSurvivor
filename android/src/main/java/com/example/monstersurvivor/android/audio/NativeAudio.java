package com.example.monstersurvivor.android.audio;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.audio.*;
import com.example.monstersurvivor.android.model.GamePreferences;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** One native menu player across screens; no GL context, service, or background playback. UI thread only. */
public final class NativeAudio {
    private static NativeAudio instance;
    private final Context context;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final AudioFocus focus;
    private final Map<AudioEvent,JsonValue> cues=new EnumMap<>(AudioEvent.class);
    private final Map<AudioEvent,Integer> sounds=new EnumMap<>(AudioEvent.class);
    private final Set<Integer> ready=new HashSet<>();
    private final Map<AudioEvent,Long> last=new EnumMap<>(AudioEvent.class);
    private final Set<AudioEvent> logged=EnumSet.noneOf(AudioEvent.class);
    private Activity owner;
    private MediaPlayer music;
    private SoundPool pool;
    private boolean focused;
    private final Runnable cleanup=()->{if(owner==null)disposePlayers();};
    private NativeAudio(Context context) {
        this.context=context.getApplicationContext();
        focus=new AudioFocus(context,value->{focused=value;if(!value && pool!=null)pool.autoPause();applyMusic();});
        try(InputStream stream=this.context.getAssets().open("audio/manifest.json")) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] chunk=new byte[4096];int size;
            while((size=stream.read(chunk))!=-1)bytes.write(chunk,0,size);
            JsonValue manifest=new JsonReader().parse(new String(bytes.toByteArray(),StandardCharsets.UTF_8));
            for(JsonValue row:manifest.get("events"))cues.put(AudioEvent.valueOf(row.getString("event_id")),row);
        }catch(Exception error){Log.e("NativeAudio","Manifest unavailable",error);}
    }
    private static NativeAudio get(Context context) {if(instance==null)instance=new NativeAudio(context);return instance;}
    public static void resume(Activity activity) {
        NativeAudio audio=get(activity);audio.handler.removeCallbacks(audio.cleanup);audio.owner=activity;
        audio.focused=audio.focus.acquire();audio.load();audio.applyMusic();
    }
    public static void pause(Activity activity) {
        if(instance==null || instance.owner!=activity)return;
        instance.owner=null;instance.focused=false;instance.applyMusic();
        // Do not replay in-flight UI cues on the next screen/focus gain.
        if(instance.pool!=null)instance.pool.autoPause();
        instance.focus.release();instance.handler.postDelayed(instance.cleanup,300);
    }
    /** Called before libGDX initialization; native MediaPlayer/SoundPool cannot overlap combat. */
    public static void enterGame() {if(instance!=null) {instance.owner=null;instance.focused=false;instance.focus.release();instance.handler.removeCallbacks(instance.cleanup);instance.disposePlayers();}}
    public static void refresh(Context context) {get(context).applyMusic();if(instance.pool!=null && (!GamePreferences.getInstance(context).isSfxEnabled() || GamePreferences.getInstance(context).getSfxVolume()==0))instance.pool.autoPause();}
    private void load() {
        if(music==null && cues.containsKey(AudioEvent.BGM_MENU)) {
            MediaPlayer next=new MediaPlayer();
            try(AssetFileDescriptor fd=context.getAssets().openFd(cues.get(AudioEvent.BGM_MENU).getString("file"))) {
                next.setAudioAttributes(AudioFocus.attributes());next.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());next.setLooping(true);
                next.setOnErrorListener((player,what,extra)->{Log.e("NativeAudio","Menu backend error="+what+"/"+extra);if(music==player)music=null;player.release();return true;});
                next.prepare();music=next;Log.i("NativeAudio","Menu prepared "+cues.get(AudioEvent.BGM_MENU).getString("file"));
            }catch(Exception error){next.release();Log.e("NativeAudio","Menu load failed",error);}
        }
        if(pool==null) {
            try {pool=new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(AudioFocus.attributes()).build();}
            catch(RuntimeException error){Log.e("NativeAudio","SoundPool unavailable",error);return;}
            pool.setOnLoadCompleteListener((loaded,id,status)->{if(pool==loaded && status==0)ready.add(id);else if(status!=0)Log.e("NativeAudio","SFX load failed id="+id+" status="+status);});
            for(AudioEvent event:new AudioEvent[]{AudioEvent.UI_TAP,AudioEvent.UI_NAVIGATION,AudioEvent.UI_CONFIRM,AudioEvent.UI_CANCEL}) {
                JsonValue cue=cues.get(event);if(cue==null)continue;
                try(AssetFileDescriptor fd=context.getAssets().openFd(cue.getString("file"))) {sounds.put(event,pool.load(fd,1));}
                catch(Exception error){Log.e("NativeAudio","SFX load failed "+event,error);}
            }
        }
    }
    private void applyMusic() {
        if(music==null)return;GamePreferences prefs=GamePreferences.getInstance(context);
        try {
            float volume=prefs.isMusicEnabled()?prefs.getMusicVolume()/100f*cues.get(AudioEvent.BGM_MENU).getFloat("gain"):0;
            music.setVolume(volume,volume);
            if(owner!=null && focused && volume>0) {if(!music.isPlaying())music.start();}
            else if(music.isPlaying())music.pause();
        }catch(RuntimeException error){Log.e("NativeAudio","Menu playback failed",error);}
    }
    public static void play(Context context,AudioEvent event) {
        NativeAudio audio=get(context);if(audio.owner==null || !audio.focused || audio.pool==null)return;
        GamePreferences prefs=GamePreferences.getInstance(context);if(!prefs.isSfxEnabled() || prefs.getSfxVolume()==0)return;
        Integer sound=audio.sounds.get(event);if(sound==null || !audio.ready.contains(sound))return;
        long now=android.os.SystemClock.uptimeMillis();Long last=audio.last.get(event);
        if(last!=null && now-last<100)return;
        float gain=prefs.getSfxVolume()/100f*audio.cues.get(event).getFloat("gain");
        try {
            int voice=audio.pool.play(sound,gain,gain,1,0,1);
            if(voice!=0) {audio.last.put(event,now);if(audio.logged.add(event))Log.i("NativeAudio","SFX event="+event+" stream="+voice);}
            else Log.w("NativeAudio","SFX rejected "+event);
        }catch(RuntimeException error){Log.e("NativeAudio","SFX playback failed "+event,error);}
    }
    private void disposePlayers() {
        if(music!=null) {try {music.release();}catch(RuntimeException error){Log.e("NativeAudio","Menu release",error);}music=null;}
        if(pool!=null) {try {pool.release();}catch(RuntimeException error){Log.e("NativeAudio","SoundPool release",error);}pool=null;}sounds.clear();ready.clear();last.clear();Log.i("NativeAudio","Players released");
    }
}
