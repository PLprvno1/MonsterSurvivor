package com.example.monstersurvivor.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.enemy.*;

/** Finite, local opt-in diagnostic: files/combat-profile.json. No art readback or normal-play changes. */
public final class CombatProfile {
    public final FrameMetrics metrics=new FrameMetrics();
    private static final EnemyType[] TYPES=EnemyType.values();
    private final int[] populations;
    private final boolean pickupProbe,weaponProbe;
    public final boolean worldProbe;
    private final boolean rosterProbe;
    private final boolean[] seenSpecies=new boolean[EnemyRegistry.all().size()];
    public float nightStrength() {return (stage&1)==0?0:1;}
    private final int warmup,samples;
    private final float[] rows;
    private final String label;
    private final DiagnosticRun run;
    private final boolean[] used=new boolean[40000];
    private int stage,frame,rowCount;
    private long previous;
    private boolean setup=true,done;
    private final int columns=4+FrameMetrics.Part.values().length+12;
    private CombatProfile(JsonValue value,DiagnosticRun run) {
        this.run=run;
        pickupProbe=value.getBoolean("pickupProbe",false);weaponProbe=value.getBoolean("weaponProbe",false);
        rosterProbe=value.getBoolean("rosterProbe",false);worldProbe=value.getBoolean("worldProbe",false) || rosterProbe;
        populations=worldProbe?new int[]{10,10,30,30,60,60}:weaponProbe?new int[]{8,8,8}:pickupProbe?new int[]{8,8}:new int[]{8,20,40,60};
        label=value.getString("label","profile").replaceAll("[^a-zA-Z0-9_-]","_");
        warmup=Math.max(15,Math.min(120,value.getInt("warmupFrames",30)));
        samples=Math.max(30,Math.min(180,value.getInt("sampleFrames",60)));
        rows=new float[populations.length*samples*columns];
    }
    public static CombatProfile load(DiagnosticRun run) {
        var value=run.object("combat-profile.json");return value==null?null:new CombatProfile(value,run);
    }
    public GameConfig tuning(GameConfig source) {
        if(rosterProbe) {
            var builder=new GameConfig.Builder().difficulty(source.difficulty).mode(source.mode).skills(source.skills).language(source.language).visuals(source.visuals).population(0,60).playerHp(100000)
                .stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).sword(1,1,1,.15f);
            for(var d:EnemyRegistry.all())builder.enemy(d,100000,1,d.speed);return builder.build();
        }
        if(weaponProbe || worldProbe)return new GameConfig.Builder().difficulty(source.difficulty).mode(source.mode).skills(source.skills).language(source.language).population(0,60).playerHp(100000)
            .enemy(EnemyType.SLIME,100000,1,28).enemy(EnemyType.BAT,100000,1,52).enemy(EnemyType.WILD_BOAR,100000,1,38)
            .stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).sword(1,1,1,.15f).build();
        return new GameConfig.Builder().difficulty(source.difficulty).mode(source.mode).skills(source.skills).language(source.language).population(0,60).playerHp(100000)
            .sword(1,1,1,.15f).build();
    }
    public void beforeFrame(GameScreen screen) {
        if(done)return;
        if(setup) {
            setup=false;java.util.Arrays.fill(used,false);
            var enemies=screen.getSession().enemies;enemies.clear();
            var nav=enemies.navigation;int spawned=0;
            if(worldProbe) {
                var weapons=screen.getSession().weapons;weapons.clearTransient();weapons.loadout.reset();
                for(int level=0;level<5;level++) {weapons.loadout.grant(com.example.monstersurvivor.weapon.WeaponType.FIREBALL);weapons.loadout.grant(com.example.monstersurvivor.weapon.WeaponType.LIGHTNING);}
                int center=nav.cell(screen.getPlayer().x,screen.getPlayer().y);
                for(int cell=0;cell<nav.collision.width*nav.collision.height && spawned<populations[stage];cell++) {
                    float x=nav.x(cell),y=nav.y(cell),dx=x-screen.getPlayer().x,dy=y-screen.getPlayer().y;
                    if(dx*dx+dy*dy<60*60 || dx*dx+dy*dy>145*145 || Math.abs(dy)>88 || !nav.connected(cell,center,false)
                        || !nav.collision.clearLine(x,y,screen.getPlayer().x,screen.getPlayer().y,2,2,true))continue;
                    Enemy resident=rosterProbe?enemies.spawn(EnemyRegistry.all().get((spawned+11+(stage/2)*10)%EnemyRegistry.all().size()),x,y,540)
                        :enemies.spawn(TYPES[spawned%TYPES.length],x,y);
                    if(resident!=null)spawned++;
                }
            }
            if(weaponProbe) {
                var weapons=screen.getSession().weapons;weapons.clearTransient();weapons.loadout.reset();
                if(stage>0) {
                    var first=stage==1?com.example.monstersurvivor.weapon.WeaponType.BOW:com.example.monstersurvivor.weapon.WeaponType.FIREBALL;
                    var second=stage==1?com.example.monstersurvivor.weapon.WeaponType.FIREBALL:com.example.monstersurvivor.weapon.WeaponType.LIGHTNING;
                    for(int level=0;level<3;level++) {weapons.loadout.grant(first);weapons.loadout.grant(second);}
                    if(stage==2)weapons.loadout.get(second).cooldownRemaining=1.2f;
                }
                for(int cell=0;cell<10000 && spawned<8;cell++) {
                    float x=nav.x(cell),y=nav.y(cell),dx=x-screen.getPlayer().x,dy=y-screen.getPlayer().y;
                    if(dx*dx+dy*dy<80*80 || dx*dx+dy*dy>145*145 || Math.abs(dy)>72 || !nav.connected(cell,5050,false)
                        || !nav.collision.clearLine(x,y,screen.getPlayer().x,screen.getPlayer().y,2,2,true))continue;
                    if(enemies.spawn(TYPES[spawned%TYPES.length],x,y)!=null)spawned++;
                }
            }
            if(!weaponProbe && !worldProbe)
            for(int actor=0;actor<populations[stage];actor++) {
                EnemyType type=TYPES[actor%TYPES.length];boolean blocked=(actor&1)==1,placed=false;
                for(int row=39;row<=61 && !placed;row++)for(int col=34;col<=66 && !placed;col++) {
                    int cell=row*100+col;if(used[cell] || !nav.connected(cell,5050,type.flying))continue;
                    boolean clear=nav.collision.clearLine(nav.x(cell),nav.y(cell),screen.getPlayer().x,screen.getPlayer().y,5,3,type.flying);
                    if(clear==blocked)continue;
                    if(enemies.spawn(type,nav.x(cell),nav.y(cell))!=null) {used[cell]=true;placed=true;spawned++;}
                }
            }
            if(spawned!=populations[stage])throw new IllegalStateException("Profile population unavailable");
            if(pickupProbe && stage==1) {
                var items=screen.getSession().pickups;
                for(int cell=0;cell<10000 && items.size()<26;cell++) {
                    float x=nav.x(cell),y=nav.y(cell),dx=x-screen.getPlayer().x,dy=y-screen.getPlayer().y;
                    if(Math.abs(dx)>180 || Math.abs(dy)>88 || dx*dx+dy*dy<40*40)continue;
                    items.spawn(items.boxes()<6?com.example.monstersurvivor.pickup.PickupType.DAMAGE_BOX
                        :com.example.monstersurvivor.pickup.PickupType.HEALTH_DROP,x,y,2);
                }
                if(items.size()!=26)throw new IllegalStateException("Pickup profile placement unavailable");
            }
            frame=0;previous=0;
        }
        metrics.reset();
    }
    public void afterFrame(GameScreen screen,float delta) {
        if(done)return;
        long now=System.nanoTime();
        if(frame>=warmup) {
            int i=rowCount++*columns;
            rows[i++]=populations[stage];rows[i++]=delta*1000;rows[i++]=previous==0?0:(now-previous)*1e-6f;rows[i++]=screen.getSession().enemies.livingCount();
            for(FrameMetrics.Part part:PARTS)rows[i++]=metrics.value(part)*1e-6f;
            rows[i++]=metrics.separationPairs;rows[i++]=metrics.occupancyChecks;
            rows[i++]=screen.getSession().enemies.searchesThisFrame;rows[i++]=screen.getWorldDrawCalls();rows[i++]=screen.getVisibleCount();rows[i++]=screen.getSession().pickups.size();
            rows[i++]=stage;rows[i++]=screen.getSession().weapons.projectiles.size();rows[i++]=screen.getSession().weapons.projectiles.effectCount();
            java.util.Arrays.fill(seenSpecies,false);int distinct=0;
            for(int n=0;n<screen.getSession().enemies.size();n++) {int id=screen.getSession().enemies.get(n).definition.index;if(id>=0 && !seenSpecies[id]) {seenSpecies[id]=true;distinct++;}}
            rows[i++]=distinct;rows[i++]=screen.getAssets().textureCount();rows[i]=screen.getAssets().textureBytes();
        }
        previous=now;
        if(++frame>=warmup+samples) {
            Gdx.app.log("MonsterSurvivor","Profile stage "+populations[stage]+" complete");
            if(++stage<populations.length)setup=true;else finish(screen);
        }
    }
    private static final FrameMetrics.Part[] PARTS=FrameMetrics.Part.values();
    private void finish(GameScreen screen) {
        done=true;metrics.active=false;
        StringBuilder csv=new StringBuilder(rowCount*200);
        csv.append("population,deltaMs,callbackMs,living");for(var part:PARTS)csv.append(',').append(part).append("Ms");
        csv.append(",separationPairs,occupancyChecks,aStarSearches,worldDrawCalls,visibleSprites,pickupCount,probeStage,projectiles,weaponEffects,distinctSpecies,gameTextureCount,gameTextureBytes\n");
        for(int row=0;row<rowCount;row++)for(int col=0;col<columns;col++)csv.append(rows[row*columns+col]).append(col==columns-1?'\n':',');
        run.file("combat-profile-"+label+".csv").writeString(csv.toString(),false,"UTF-8");
        run.file("combat-profile-"+label+"-environment.txt").writeString("renderer="+Gdx.gl.glGetString(GL20.GL_RENDERER)
            +"\nplatform="+Gdx.app.getType()+"\nresolution="+Gdx.graphics.getWidth()+"x"+Gdx.graphics.getHeight()
            +"\nwarmupFrames="+warmup+"\nsampleFrames="+samples+"\ntextures="+screen.getAssets().textureCount()+"\nrgbaBytes="+screen.getAssets().textureBytes()+"\nrosterProbe="+rosterProbe+"\n",false,"UTF-8");
        Gdx.app.log("MonsterSurvivor","Profile complete "+label+" rows="+rowCount);
    }
}
