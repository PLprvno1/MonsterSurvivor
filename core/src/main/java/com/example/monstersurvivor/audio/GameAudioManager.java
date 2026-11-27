package com.example.monstersurvivor.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.boss.BossSystem.EventState;
import java.util.function.LongSupplier;
import static com.example.monstersurvivor.audio.AudioEvent.*;

/** Preloaded, path-deduplicated libGDX audio. All methods run on the game thread. */
public final class GameAudioManager implements AudioSink,Disposable {
    public static final int VOICE_CAP=16;
    public interface Loader {Sound sound(String path);Music music(String path);}
    private static final class Asset {
        final String path;
        Sound sound;Music music;
        long lastPlay=Long.MIN_VALUE;
        volatile boolean completed;
        Asset(String path) {this.path=path;}
    }
    private static final class Cue {
        Asset asset;float gain,duration;int priority,cooldown;boolean loop,enemy;
        long last=Long.MIN_VALUE;boolean logged,failed;
        int plays;
    }
    private static final class Voice {Sound sound;long id,end;int priority;float gain;}
    private final Cue[] cues=new Cue[AudioEvent.values().length];
    private final ObjectMap<String,Asset> assets=new ObjectMap<>();
    private final Voice[] voices=new Voice[VOICE_CAP];
    // Bounded per-actor reaction history, replacing oldest only after the slot is reused.
    private final int[] actors=new int[128];
    private final long[] actorTimes=new long[128];
    private final LongSupplier clock;
    private AudioSettings settings=AudioSettings.DEFAULT;
    private AudioEvent currentEvent;
    private Asset current;
    private boolean blocked,musicPaused,disposed;
    private float listenerX,listenerY;
    private long enemyLast=Long.MIN_VALUE;
    public int played,dropped,failures,musicStarts;

    public GameAudioManager() {
        this(new JsonReader().parse(Gdx.files.internal("audio/manifest.json")),new Loader() {
            public Sound sound(String path) {return Gdx.audio.newSound(Gdx.files.internal(path));}
            public Music music(String path) {return Gdx.audio.newMusic(Gdx.files.internal(path));}
        },System::nanoTime);
    }
    /** Backend/clock injection lets focused tests assert actual calls and voice eviction. */
    public GameAudioManager(JsonValue manifest,Loader loader,LongSupplier clock) {
        this.clock=clock;long start=clock.getAsLong();
        for(int i=0;i<voices.length;i++)voices[i]=new Voice();
        for(JsonValue row:manifest.get("events")) {
            AudioEvent event=AudioEvent.valueOf(row.getString("event_id"));
            Cue cue=new Cue();String path=row.getString("file");boolean music=row.getString("category").equals("MUSIC");
            Asset asset=assets.get(path);
            if(asset==null) {
                asset=new Asset(path);assets.put(path,asset);
                try {
                    if(music) {asset.music=loader.music(path);final Asset loaded=asset;asset.music.setOnCompletionListener(m -> loaded.completed=true);}
                    else asset.sound=loader.sound(path);
                } catch(RuntimeException error) {failures++;log("Load failed "+path+": "+error);}
            }
            cue.asset=asset;cue.gain=row.getFloat("gain");cue.duration=row.getFloat("duration");cue.priority=row.getInt("priority");
            cue.cooldown=row.getInt("cooldown_ms");cue.loop=row.getBoolean("loop");cue.enemy=row.getString("category").equals("ENEMY");
            if(cues[event.ordinal()]!=null)throw new IllegalArgumentException("Duplicate cue "+event);
            cues[event.ordinal()]=cue;
        }
        for(AudioEvent e:AudioEvent.values())if(cues[e.ordinal()]==null && !e.optional())throw new IllegalArgumentException("Missing required audio cue "+e);
        log("Loaded unique="+assets.size+" failures="+failures+" ms="+(clock.getAsLong()-start)/1_000_000f+" voiceCap="+VOICE_CAP);
    }
    private static void log(String message) {if(Gdx.app!=null)Gdx.app.log("GameAudio",message);}
    private void failed(Cue cue,RuntimeException error) {failures++;if(!cue.failed) {cue.failed=true;log("Playback failed "+cue.asset.path+": "+error);}}
    public void listener(float x,float y) {listenerX=x;listenerY=y;}
    public void settings(AudioSettings value) {
        if(value==null || settings==value || disposed)return;settings=value;
        if(!settings.sfxEnabled || settings.sfxVolume==0)stopVoices();
        else for(Voice v:voices)if(v.sound!=null)try {v.sound.setVolume(v.id,v.gain*settings.sfxVolume);}catch(RuntimeException error){failures++;}
        applyMusic();
    }
    @Override public void at(AudioEvent event,float x,float y,int actor) {
        Cue cue=cues[event.ordinal()];
        if(cue!=null && cue.enemy) {
            float dx=x-listenerX,dy=y-listenerY;if(dx*dx+dy*dy>220*220) {dropped++;return;}
            long now=clock.getAsLong();int slot=(actor&Integer.MAX_VALUE)%actors.length;
            // Death is allowed after a reaction; reaction/charge share a one-second actor budget.
            if(event!=ENEMY_DEATH && actors[slot]==actor && now-actorTimes[slot]<1_000_000_000L) {dropped++;return;}
            if(emit(event,now) && event!=ENEMY_DEATH) {actors[slot]=actor;actorTimes[slot]=now;}
        } else play(event);
    }
    @Override public void play(AudioEvent event) {emit(event,clock.getAsLong());}
    private boolean emit(AudioEvent event,long now) {
        if(disposed)return false;Cue cue=cues[event.ordinal()];if(cue==null) {dropped++;return false;}Asset asset=cue.asset;
        if(asset.sound==null || blocked || !settings.sfxEnabled || settings.sfxVolume==0) {dropped++;return false;}
        if(cue.last!=Long.MIN_VALUE && now-cue.last<cue.cooldown*1_000_000L
            || asset.lastPlay!=Long.MIN_VALUE && now-asset.lastPlay<35_000_000L
            || cue.enemy && enemyLast!=Long.MIN_VALUE && now-enemyLast<120_000_000L) {dropped++;return false;}
        Voice slot=null,lowest=null;
        for(Voice v:voices) {
            if(v.sound!=null && now>=v.end)stop(v);
            if(v.sound==null && slot==null)slot=v;
            else if(v.sound!=null && (lowest==null || v.priority<lowest.priority || v.priority==lowest.priority && v.end<lowest.end))lowest=v;
        }
        if(slot==null) {if(lowest==null || lowest.priority>cue.priority) {dropped++;return false;}slot=lowest;stop(slot);}
        try {
            long id=asset.sound.play(cue.gain*settings.sfxVolume);
            if(id<0) {failed(cue,new IllegalStateException("backend rejected Sound.play"));return false;}
            slot.sound=asset.sound;slot.id=id;slot.end=now+(long)((cue.duration+.04f)*1_000_000_000L);slot.priority=cue.priority;slot.gain=cue.gain;
            cue.last=asset.lastPlay=now;if(cue.enemy)enemyLast=now;played++;
            cue.plays++;
            if(!cue.logged || cue.priority>=100) {cue.logged=true;log("SFX event="+event+" id="+id+" count="+cue.plays+" path="+asset.path);}
            return true;
        } catch(RuntimeException error) {failed(cue,error);return false;}
    }
    private void stop(Voice v) {if(v.sound!=null)try {v.sound.stop(v.id);}catch(RuntimeException error){failures++;}finally {v.sound=null;}}
    public void stopVoices() {for(Voice v:voices)stop(v);}
    /** User pause/modal/background never auto-resumes simulation. The screen owns that decision. */
    public void blocked(boolean value) {
        if(disposed || blocked==value)return;blocked=value;if(value)stopVoices();applyMusic();
    }
    public void musicPaused(boolean value) {
        if(disposed || musicPaused==value)return;musicPaused=value;
        applyMusic();
    }
    public static AudioEvent musicFor(GameSession session) {
        if(session.phase()==GameSession.Phase.ENDED)return null;
        if(session.victory())return BGM_VICTORY;
        if(session.phase()==GameSession.Phase.GAME_OVER)return BGM_GAME_OVER;
        if(session.bosses.finalState()==EventState.ACTIVE || session.phase()==GameSession.Phase.WINNING)return BGM_FINAL;
        if(session.bosses.miniState()==EventState.ACTIVE)return BGM_MINI;
        return session.night.active()?BGM_NIGHT:BGM_DAY;
    }
    public void music(AudioEvent event) {
        if(disposed || event==currentEvent)return;
        Asset next=event==null || cues[event.ordinal()]==null?null:cues[event.ordinal()].asset;
        if(current!=next) {
            if(current!=null && current.music!=null)try {current.music.stop();}catch(RuntimeException error){failures++;}
            current=next;if(next!=null)next.completed=false;
        }
        currentEvent=event;
        // Same-file MINI -> FINAL updates loop/gain without stop/seek/play.
        applyMusic();log("Music state="+event+" path="+(next==null?"none":next.path));
    }
    private void applyMusic() {
        if(current==null || current.music==null || currentEvent==null || disposed)return;
        Cue cue=cues[currentEvent.ordinal()];
        try {
            Music m=current.music;m.setLooping(cue.loop);m.setVolume(settings.musicVolume*cue.gain);
            if(blocked || musicPaused || !settings.musicEnabled || settings.musicVolume==0)m.pause();
            else if(!m.isPlaying() && !current.completed) {m.play();musicStarts++;}
        }catch(RuntimeException error) {failed(cue,error);}
    }
    /** Session boundary; no resource reload and no inherited cooldown/voice/music state. */
    public void reset() {
        music(null);stopVoices();enemyLast=Long.MIN_VALUE;
        java.util.Arrays.fill(actors,0);java.util.Arrays.fill(actorTimes,0);
        for(Cue c:cues)if(c!=null)c.last=Long.MIN_VALUE;
        for(Asset a:assets.values()) {a.lastPlay=Long.MIN_VALUE;a.completed=false;}
    }
    public int activeVoices() {int count=0;long now=clock.getAsLong();for(Voice v:voices)if(v.sound!=null && v.end>now)count++;return count;}
    @Override public void dispose() {
        if(disposed)return;music(null);stopVoices();disposed=true;
        for(Asset a:assets.values()) {
            if(a.sound!=null)try {a.sound.dispose();}catch(RuntimeException error){failures++;}
            if(a.music!=null)try {a.music.dispose();}catch(RuntimeException error){failures++;}
        }
        log("Disposed played="+played+" dropped="+dropped+" failures="+failures+" musicStarts="+musicStarts);
    }
}
