package com.example.monstersurvivor.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.weapon.WeaponType;

/** Local opt-in encounter capture. Absent boss-test.json: no instance, files, tuning, clocks or telemetry. */
public final class BossDiagnostics {
    private static final FrameMetrics.Part[] PARTS=FrameMetrics.Part.values();
    public final FrameMetrics metrics=new FrameMetrics();
    private final float startProgress;
    private final int weaponLevel;
    private final boolean holdHp;
    private final DiagnosticRun run;
    private long session=-1,previous;
    private int frame,commandSerial=-1;
    private final float[] frames=new float[120];
    private final long[] parts=new long[FrameMetrics.Part.values().length];
    private float aiMaximum;
    private boolean miniVisible,finalVisible,pendingDeath;
    private BossDiagnostics(JsonValue value,DiagnosticRun run) {this.run=run;startProgress=Math.max(0,Math.min(600,value.getFloat("progress",0)));weaponLevel=Math.max(1,Math.min(5,value.getInt("weaponLevel",1)));holdHp=value.getBoolean("holdHp",true);}
    public static BossDiagnostics load(DiagnosticRun run) {var v=run.object("boss-test.json");return v==null?null:new BossDiagnostics(v,run);}
    public GameConfig tuning(GameConfig source) {
        return new GameConfig.Builder().difficulty(source.difficulty).mode(source.mode).skills(source.skills).language(source.language).visuals(source.visuals).weapons(source.weapons).pickups(source.pickups).bosses(source.bosses)
            .playerHp(holdHp?100000:source.playerHp).debug(true).build();
    }
    public void before(GameScreen screen) {
        var s=screen.getSession();
        if(session!=s.id) {session=s.id;s.diagnosticProgress(startProgress);frame=0;previous=0;miniVisible=finalVisible=pendingDeath=false;
            for(int i=0;i<weaponLevel;i++){if(i>0)s.weapons.loadout.grant(WeaponType.SWORD);s.weapons.loadout.grant(WeaponType.FIREBALL);s.weapons.loadout.grant(WeaponType.LIGHTNING);}
            Gdx.app.log("BossDiagnostics","LOCAL FIXTURE progress="+startProgress+" holdHp="+holdHp+" level="+weaponLevel+" actual event/combat/placement/render; real elapsed not offset");
        }
        metrics.reset();
        // Poll at 2Hz only in this explicitly enabled diagnostic; no normal-play filesystem polling.
        if((frame%30)==0) {
            var cmd=run.object("boss-command.json");
            if(cmd!=null) {int serial=cmd.getInt("serial",0);
                if(serial!=commandSerial) {commandSerial=serial;String action=cmd.getString("action","");var b=s.bosses.lich();
                    if(action.equals("progress"))s.diagnosticProgress(cmd.getFloat("value"));
                    else if(action.equals("phase2") && b!=null) {b.actor.damage(b.maxHp());}
                    else if(action.equals("defeat") && b!=null)b.actor.damage(Integer.MAX_VALUE);
                    else if(action.equals("playerDeath"))pendingDeath=true;
                    Gdx.app.log("BossDiagnostics","command="+action+" serial="+serial+" finalHp="+(b==null?-1:b.hp()));
                }
            }
        }
        if(pendingDeath && !s.player.invulnerable()) {pendingDeath=false;s.player.damage(Integer.MAX_VALUE);}
    }
    public void after(GameScreen screen) {
        var encounter=screen.getSession().bosses;var camera=screen.getCamera();
        for(int i=0;i<2;i++) {var b=i==0?encounter.mini():encounter.lich();
            if(b!=null && !(i==0?miniVisible:finalVisible) && b.actor.sprite.visible(camera.position.x-camera.viewportWidth/2,camera.position.y-camera.viewportHeight/2,camera.position.x+camera.viewportWidth/2,camera.position.y+camera.viewportHeight/2)) {
                if(i==0)miniVisible=true;else finalVisible=true;
                Gdx.app.log("BossDiagnostics","VISIBLE actor="+b.definition.body().id+" progress="+screen.getSession().progress()+" elapsed="+screen.getSession().elapsed()+" phase="+b.phase());
            }
        }
        long now=System.nanoTime();if(previous!=0)frames[frame%frames.length]=(now-previous)/1_000_000f;previous=now;
        for(int i=0;i<parts.length;i++)parts[i]+=metrics.value(PARTS[i]);
        aiMaximum=Math.max(aiMaximum,metrics.value(FrameMetrics.Part.AI)/1_000_000f);frame++;
        if(frame%120==0) {
            float total=0,max=0;for(float ms:frames) {total+=ms;max=Math.max(ms,max);}var sorted=frames.clone();java.util.Arrays.sort(sorted);
            var s=screen.getSession();StringBuilder out=new StringBuilder(256);out.append("window frames=120 fps=").append(120000/total).append(" meanMs=").append(total/120).append(" p95Ms=").append(sorted[113]).append(" maxMs=").append(max)
                .append(" progress=").append(s.progress()).append(" elapsed=").append(s.elapsed()).append(" phase=").append(s.phase()).append(" normal=").append(s.enemies.normalSize()).append(" bosses=").append(s.enemies.bossSize()).append(" shots=").append(s.bosses.projectiles.size());
            var b=s.bosses.lich();out.append(" lichPhase=").append(b==null?0:b.phase()).append(" aiMaxMs=").append(aiMaximum);
            for(int i=0;i<parts.length;i++) {out.append(' ').append(PARTS[i]).append("Ms=").append(parts[i]/120_000_000f);parts[i]=0;}
            Gdx.app.log("BossDiagnostics",out.toString());aiMaximum=0;
        }
    }
}
