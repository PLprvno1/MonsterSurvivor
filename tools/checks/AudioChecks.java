import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.audio.*;
import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.boss.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.world.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import static com.example.monstersurvivor.audio.AudioEvent.*;

/** Focused audio contracts: fake backend calls, real simulation events, no timing/damage changes. */
public final class AudioChecks {
    static int checks;static long now=2_000_000_000L;
    static void check(boolean b,String label){if(!b)throw new AssertionError(label);checks++;}
    static final class Fake implements InvocationHandler {
        int plays,stops,pauses,disposes;float volume;boolean playing,loop,fail,reject;long next=1;
        Music.OnCompletionListener completion;
        Object proxy(Class<?> type){return Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},this);}
        public Object invoke(Object p,Method method,Object[] args) {
            String n=method.getName();
            if(n.equals("play")) {if(fail)throw new IllegalStateException("fixture failure");plays++;playing=true;if(args!=null)volume=(float)args[0];return method.getReturnType()==long.class?(reject?-1L:next++):null;}
            if(n.equals("pause")){pauses++;playing=false;}
            if(n.equals("stop")){stops++;playing=false;}
            if(n.equals("dispose")){disposes++;playing=false;}
            if(n.equals("isPlaying"))return playing;
            if(n.equals("setVolume"))volume=(float)args[args.length-1];
            if(n.equals("setLooping"))loop=(boolean)args[0];
            if(n.equals("setOnCompletionListener"))completion=(Music.OnCompletionListener)args[0];
            if(method.getReturnType()==float.class)return 0f;
            if(method.getReturnType()==boolean.class)return false;
            return null;
        }
    }
    static final class Backend implements GameAudioManager.Loader {
        Map<String,Fake> sounds=new HashMap<>(),music=new HashMap<>();boolean failLoad;
        public Sound sound(String path){if(failLoad)throw new IllegalStateException("fixture load");Fake f=new Fake();check(!sounds.containsKey(path),"single Sound allocation/path");sounds.put(path,f);return (Sound)f.proxy(Sound.class);}
        public Music music(String path){Fake f=new Fake();check(!music.containsKey(path),"single Music allocation/path");music.put(path,f);return (Music)f.proxy(Music.class);}
    }
    static JsonValue manifest()throws Exception{return new JsonReader().parse(Files.readString(Path.of("audio/manifest.json")));}
    static String path(JsonValue m,AudioEvent event){for(JsonValue row:m.get("events"))if(row.getString("event_id").equals(event.name()))return row.getString("file");throw new AssertionError(event);}
    static void mixing()throws Exception {
        JsonValue m=manifest();Backend b=new Backend();var a=new GameAudioManager(m,b,()->now);
        check(b.sounds.size()==33 && b.music.size()==3,"33 Sound + 3 streaming Music, shared paths deduplicated");
        a.music(BGM_MINI);Fake boss=b.music.get(path(m,BGM_MINI));check(boss.plays==1 && boss.loop,"Mini starts looping");
        for(int i=0;i<1000;i++)a.music(BGM_MINI);
        a.music(BGM_FINAL);check(boss.plays==1 && boss.stops==0,"shared Mini/Final never restarts");
        a.music(BGM_VICTORY);Fake victory=b.music.get(path(m,BGM_VICTORY));check(boss.stops==1 && victory.plays==1 && !victory.loop,"Victory stops combat and plays one shot");
        victory.playing=false;victory.completion.onCompletion(null);a.musicPaused(true);a.musicPaused(false);
        a.settings(new AudioSettings(true,true,50,50));check(victory.plays==1,"completed result track not replayed on pause/settings");
        a.music(BGM_GAME_OVER);Fake night=b.music.get(path(m,BGM_NIGHT));check(!night.loop,"GameOver one-shot policy");
        a.music(BGM_NIGHT);check(night.loop && night.plays==1,"same-file policy updates without restart");
        a.settings(new AudioSettings(true,true,0,100));check(!night.playing,"music volume zero really pauses");
        a.settings(new AudioSettings(true,true,50,50));check(night.playing && Math.abs(night.volume-.13f)<.0001,"50 percent Music gain");
        a.settings(new AudioSettings(false,true,100,100));check(!night.playing,"Music switch off");
        a.settings(new AudioSettings(true,true,100,100));check(night.playing && Math.abs(night.volume-.26f)<.0001,"Music on, 100 percent");
        Fake hit=b.sounds.get(path(m,WEAPON_SWORD_HIT));a.play(WEAPON_SWORD_HIT);a.play(WEAPON_SWORD_HIT);check(hit.plays==1,"event cooldown coalesces same activation hit batch");
        now+=100_000_000L;a.settings(new AudioSettings(true,false,100,100));a.play(WEAPON_SWORD_HIT);check(hit.plays==1 && a.activeVoices()==0,"SFX off stops voices");
        a.settings(new AudioSettings(true,true,100,0));a.play(WEAPON_SWORD_HIT);check(hit.plays==1,"zero SFX mute");
        a.settings(new AudioSettings(true,true,100,50));a.play(WEAPON_SWORD_HIT);check(hit.plays==2 && hit.volume==.25f,"SFX 50 percent");
        a.blocked(true);a.play(PLAYER_HURT);check(!night.playing && a.activeVoices()==0,"focus/background pauses Music and stops SFX");a.blocked(false);
        a.musicPaused(true);check(!night.playing,"modal music suspended");a.play(PICKUP_WEAPON_SELECTED);check(a.activeVoices()==1,"successful modal reward cue allowed while music paused");a.musicPaused(false);
        int plays=a.played;now+=2_000_000_000L;a.listener(10,10);a.at(ENEMY_HIT,1000,1000,1);check(a.played==plays,"distant enemies filtered");
        a.at(ENEMY_HIT,10,10,1);now+=200_000_000L;a.at(ENEMY_BEAST_REACTION,10,10,1);check(a.played==plays+1,"one actor reaction budget");
        a.at(ENEMY_BEAST_REACTION,10,10,2);check(a.played==plays+2,"another actor after category cooldown");
        // Extend durations only in test data to pressure the 16-voice budget deterministically.
        for(JsonValue row:m.get("events"))if(!row.getString("category").equals("MUSIC"))row.get("duration").set(30f,"30");
        Backend capBackend=new Backend();var cap=new GameAudioManager(m,capBackend,()->now);
        for(int i=0;i<40;i++){now+=200_000_000L;cap.play(ENEMY_HIT);check(cap.activeVoices()<=16,"bounded voice count under crowd");}
        check(cap.activeVoices()==16,"voice cap filled");int old=cap.played;cap.play(MINI_SLAM_WARNING);check(cap.played==old+1 && cap.activeVoices()==16,"critical warning evicts enemy voice");
        for(int i=0;i<20;i++){now+=100_000_000L;cap.play(PLAYER_HURT);}old=cap.played;now+=200_000_000L;cap.play(ENEMY_HIT);check(cap.played==old,"common enemy cannot evict critical voices");
        Fake broken=capBackend.sounds.get(path(m,PLAYER_DEATH));broken.fail=true;cap.play(PLAYER_DEATH);check(cap.failures>0,"backend exception contained");
        broken.fail=false;broken.reject=true;cap.play(PLAYER_DEATH);check(cap.failures>1,"backend negative ID contained");
        a.reset();check(a.activeVoices()==0 && !night.playing,"Retry/Home clears previous audio");a.dispose();a.dispose();
        check(b.sounds.values().stream().allMatch(f->f.disposes==1) && b.music.values().stream().allMatch(f->f.disposes==1),"every deduplicated resource disposed exactly once");
        int after=a.played;a.play(PLAYER_HURT);a.music(BGM_DAY);check(a.played==after,"disposed manager cannot play");cap.dispose();
        Backend bad=new Backend();bad.failLoad=true;var unloaded=new GameAudioManager(manifest(),bad,()->now);unloaded.play(WEAPON_SWORD_HIT);check(unloaded.failures==33,"failed cue loads do not crash rest of audio/gameplay");unloaded.dispose();
    }
    static final class Events implements AudioSink {
        final EnumMap<AudioEvent,Integer> count=new EnumMap<>(AudioEvent.class);
        public void play(AudioEvent e){count.merge(e,1,Integer::sum);}
        int n(AudioEvent e){return count.getOrDefault(e,0);}
    }
    static CollisionGrid open(){return new CollisionGrid(100,100,16,new byte[10000]);}
    static GameConfig config(){var b=new GameConfig.Builder().playerHp(100000).population(0,40).stages(new GameConfig.Stage(0,0,1000000,1,0,0,0));for(var d:EnemyRegistry.all())b.enemy(d,10000,1,1);return b.build();}
    static GameSession session(Events e){var s=new GameSession(config(),open(),600,600,null,null,42);s.audio(e);return s;}
    static void tick(GameSession s,int n){for(int i=0;i<n;i++)s.update(.05f,0,0,false,460,496,740,704);}
    static void damage() {
        Events e=new Events();var s=session(e);check(s.player.damage(1),"accepted hurt");s.player.damage(1);check(e.n(PLAYER_HURT)==1,"immunity blocks Hurt cue");
        s.player.update(.71f,0,0,open());s.player.damage(Integer.MAX_VALUE);s.player.damage(1);check(e.n(PLAYER_DEATH)==1 && e.n(PLAYER_HURT)==1,"one death, no subsequent hurt");
        s=session(e);Enemy foe=s.enemies.spawn(EnemyType.SLIME,620,600);foe.damage(1);check(e.n(ENEMY_HIT)==1,"real generic nonlethal hit");foe.damage(Integer.MAX_VALUE);foe.damage(1);check(e.n(ENEMY_DEATH)==1,"nonboss death once");
        for(var event:new AudioEvent[]{ENEMY_FLYING_REACTION,ENEMY_TANK_REACTION,ENEMY_UNDEAD_REACTION,ENEMY_BEAST_REACTION}) {
            EnemyDefinition type=EnemyRegistry.get(event==ENEMY_FLYING_REACTION?"tiny_bat":event==ENEMY_TANK_REACTION?"red_crab":event==ENEMY_UNDEAD_REACTION?"bone_hound":"wild_boar");
            Enemy enemy=s.enemies.spawn(type,680,600);enemy.damage(1);check(e.n(event)>0,"real species reaction "+event);
        }
        Enemy charge=s.enemies.spawn(EnemyType.WILD_BOAR,700,600);s.enemies.update(.05f,s.player);s.enemies.update(.05f,s.player);check(e.n(ENEMY_LEGACY_CHARGE)==1,"legacy charge only on actual burst activation");s.end();
    }
    static void pickups() {
        Events e=new Events();var s=session(e);
        for(PickupType type:new PickupType[]{PickupType.DAMAGE_BOX,PickupType.HEALTH_BOX,PickupType.SPEED_BOX}) {
            Pickup p=s.pickups.spawn(type,600,600,1);check(s.pickups.collect(p.id),"pickup commit "+type);check(!s.pickups.collect(p.id),"already collected rejected");
        }
        check(e.n(PICKUP_DAMAGE)==1 && e.n(PICKUP_HEALTH)==1 && e.n(PICKUP_SPEED)==1,"reward-specific cues");
        Pickup drop=s.pickups.spawn(PickupType.HEALTH_DROP,600,600,2);check(!s.pickups.collect(drop.id) && e.n(PICKUP_HEALTH_DROP)==0,"full health drop no false cue");
        s.player.damage(3);check(s.pickups.collect(drop.id) && e.n(PICKUP_HEALTH_DROP)==1,"healing drop cue on success");
        for(int i=0;i<20;i++)s.player.stats.addSpeed();Pickup capped=s.pickups.spawn(PickupType.SPEED_BOX,600,600,1);s.pickups.collect(capped.id);check(e.n(PICKUP_DAMAGE)==2 && e.n(PICKUP_SPEED)==1,"capped Speed uses real Damage reward cue");
        Pickup box=s.pickups.spawn(PickupType.WEAPON_BOX,600,600,1);tick(s,1);long token=s.selection.token();tick(s,20);check(e.n(PICKUP_WEAPON_OPEN)==1 && s.choosingWeapon(),"one valid modal open");
        check(!s.chooseWeapon(0,token+1) && e.n(PICKUP_WEAPON_SELECTED)==0,"stale choice silent");check(s.chooseWeapon(0,token),"valid choice");check(!s.chooseWeapon(0,token) && e.n(PICKUP_WEAPON_SELECTED)==1,"double tap commits once");
        s.pickups.spawn(PickupType.WEAPON_BOX,600,600,1);tick(s,1);int upgrade=-1;for(int i=0;i<s.selection.size();i++)if(s.selection.get(i).fromLevel>0)upgrade=i;
        check(upgrade>=0 && s.chooseWeapon(upgrade,s.selection.token()) && e.n(PICKUP_WEAPON_UPGRADED)==1,"upgrade cue after successful commit");s.end();
    }
    static void weapons() {
        for(WeaponType type:WeaponType.values()) {
            Events e=new Events();var s=session(e);if(type!=WeaponType.SWORD)s.weapons.loadout.grant(type);
            for(int i=0;i<5;i++)s.enemies.spawn(EnemyType.SLIME,625+i*3,600);
            tick(s,35);
            AudioEvent cast=type==WeaponType.SWORD?WEAPON_SWORD_SWING:type==WeaponType.BOW?WEAPON_BOW_RELEASE:type==WeaponType.FIREBALL?WEAPON_FIRE_CAST:WEAPON_LIGHTNING_ACTIVATE;
            AudioEvent hit=type==WeaponType.SWORD?WEAPON_SWORD_HIT:type==WeaponType.BOW?WEAPON_ARROW_HIT:type==WeaponType.FIREBALL?WEAPON_FIRE_EXPLOSION:WEAPON_LIGHTNING_IMPACT;
            check(e.n(cast)>0 && e.n(hit)>0,"actual weapon cast and impact "+type);
            if(type==WeaponType.LIGHTNING)check(e.n(hit)==e.n(cast),"one impact cue per lightning chain");
            if(type==WeaponType.FIREBALL)check(e.n(cast)==s.weapons.loadout.get(type).activations,"one fire cue per volley");s.end();
        }
    }
    static void bosses()throws Exception {
        Events e=new Events();var s=session(e);check(GameAudioManager.musicFor(s)==BGM_DAY,"initial Day");
        s.night.update(1,300);check(GameAudioManager.musicFor(s)==BGM_NIGHT,"Night priority");
        s.bosses.prepare(.05f,300,s.player,460,496,740,704);s.bosses.prepare(.05f,300,s.player,460,496,740,704);
        check(e.n(MINI_APPEARS)==1 && GameAudioManager.musicFor(s)==BGM_MINI,"Mini once + BGM priority");
        s.bosses.prepare(.05f,600,s.player,460,496,740,704);check(e.n(LICH_APPEARS)==1 && GameAudioManager.musicFor(s)==BGM_FINAL,"Final once over living Mini");
        var mini=s.bosses.mini();var lich=s.bosses.lich();mini.actor.x=640;mini.actor.y=600;lich.actor.x=700;lich.actor.y=600;
        for(int i=0;i<700;i++) {s.bosses.prepare(.05f,600,s.player,460,496,740,704);s.bosses.fight(.05f,600,s.player);s.enemies.update(.05f,s.player);s.player.update(.05f,0,0,open());}
        check(e.n(MINI_SLAM_WARNING)>0 && e.n(MINI_SLAM_IMPACT)==mini.areaCasts,"slam cues on cast start and real impact");
        check(e.n(LICH_AOE_WARNING)>0 && e.n(LICH_AOE_IMPACT)==lich.areaCasts && e.n(LICH_PROJECTILE)==lich.primaryCasts && lich.primaryCasts>0,"AoE cues and one projectile cue per fan");
        s.bosses.projectiles.clear();for(int i=0;i<s.bosses.projectiles.capacity();i++)s.bosses.projectiles.fire(999,1200,1200,1,0,1,1);
        int releases=e.n(LICH_PROJECTILE);
        for(Object[] setting:new Object[][]{{"cast",BossInstance.Cast.BOLT},{"castRemaining",0f},{"aimX",600f},{"aimY",600f}}) {
            var fixture=BossInstance.class.getDeclaredField((String)setting[0]);fixture.setAccessible(true);fixture.set(lich,setting[1]);
        }
        s.bosses.fight(0,600,s.player);check(e.n(LICH_PROJECTILE)==releases,"full hostile projectile pool produces no phantom release cue");s.bosses.projectiles.clear();
        for(int i=0;i<3;i++){lich.actor.advance(.21f);lich.actor.damage(Integer.MAX_VALUE);}
        for(int i=0;i<30;i++){s.bosses.prepare(.05f,600,s.player,460,496,740,704);s.bosses.fight(.05f,600,s.player);}
        check(e.n(LICH_PHASE2)==1 && e.n(LICH_APPEARS)==1,"Phase2 once, no replay intro");
        for(int i=0;i<10 && mini.actor.alive();i++){mini.actor.advance(.21f);mini.actor.damage(Integer.MAX_VALUE);}check(e.n(MINI_DEFEATED)==1,"Mini defeat once");
        int deaths=e.n(ENEMY_DEATH);for(int i=0;i<10 && lich.actor.alive();i++){lich.actor.advance(.21f);lich.actor.damage(Integer.MAX_VALUE);}lich.actor.damage(1);
        check(e.n(LICH_DEFEATED)==1 && e.n(ENEMY_DEATH)==deaths,"Boss defeat excludes generic death");
        var f=GameSession.class.getDeclaredField("progress");f.setAccessible(true);f.setDouble(s,600);tick(s,40);
        check(s.victory() && e.n(UI_VICTORY)==1 && e.n(UI_GAME_OVER)==0 && GameAudioManager.musicFor(s)==BGM_VICTORY,"true Victory only");
        tick(s,40);check(e.n(UI_VICTORY)==1,"terminal cue does not replay");s.end();check(GameAudioManager.musicFor(s)==null,"Home has no combat music");
        e=new Events();s=session(e);s.player.damage(Integer.MAX_VALUE);tick(s,40);check(s.phase()==GameSession.Phase.GAME_OVER && e.n(UI_GAME_OVER)==1 && GameAudioManager.musicFor(s)==BGM_GAME_OVER,"true GameOver state cue");s.end();
    }
    public static void main(String[] args)throws Exception {mixing();damage();pickups();weapons();bosses();System.out.println("AudioChecks PASS "+checks+" assertions; fake backend API + real simulation triggers (no listening claim)");}
}
