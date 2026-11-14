package com.example.monstersurvivor.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.example.monstersurvivor.assets.GameAssets;
import com.example.monstersurvivor.assets.AssetVisualCheck;
import com.example.monstersurvivor.assets.GameAssets.*;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.entity.Enemy;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.debug.CombatProfile;
import com.example.monstersurvivor.debug.FrameMetrics;
import static com.example.monstersurvivor.debug.FrameMetrics.Part.*;
import com.example.monstersurvivor.input.GameInput;
import com.example.monstersurvivor.render.WorldSprite;
import com.example.monstersurvivor.render.PixelCamera;
import com.example.monstersurvivor.render.CameraTrace;
import com.example.monstersurvivor.ui.GameHud;
import com.example.monstersurvivor.world.ForestMap;

/** Gameplay orchestration with the established map renderer, foot pivots and pixel camera. */
public final class GameScreen extends ScreenAdapter implements GameInput.Commands {
    private static final State[] PREVIEW_STATES = {State.IDLE, State.WALK, State.ATTACK, State.HURT, State.DEATH, State.VICTORY};
    private static final Direction[] DIRECTIONS = Direction.values();
    private final CombatProfile profile=CombatProfile.load();
    private final GameAssets assets = new GameAssets();
    private ForestMap map;
    private Player player;
    private GameSession session;
    private final GameConfig config;
    private final Runnable onHome;
    private final boolean exhibitMode;
    private final Long fixedMapSeed;
    private final SpriteBatch batch = new SpriteBatch();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ExtendViewport viewport;
    private final GameHud hud;
    private final com.example.monstersurvivor.pickup.PickupRenderer pickupRenderer=new com.example.monstersurvivor.pickup.PickupRenderer(assets);
    private final com.example.monstersurvivor.weapon.WeaponRenderer weaponRenderer=new com.example.monstersurvivor.weapon.WeaponRenderer(assets);
    private final com.example.monstersurvivor.render.NightRenderer nightRenderer=new com.example.monstersurvivor.render.NightRenderer(assets.region("weapon_pixel"));
    private final GameInput input;
    private final CameraTrace trace = CameraTrace.load();
    private final PixelCamera view = new PixelCamera(trace==null ? PixelCamera.Mode.SCREEN : trace.mode);
    private final Array<WorldSprite> visible = new Array<>(512);
    private final Array<Exhibit> exhibits = new Array<>(10);
    private final SpriteSet playerSet = assets.sprite("player");
    private final WorldSprite playerSprite;
    private boolean userPaused, applicationPaused, dropNextDelta = true, disposed;
    private float elapsed, exhibitTime;
    private int stateIndex, directionIndex, worldDrawCalls;
    private int loggedNotice,loggedBoxes;
    private long loggedSelectionToken=-1;
    private final boolean[] loggedSpecies=new boolean[com.example.monstersurvivor.enemy.EnemyRegistry.all().size()];

    private static final class Exhibit {
        final SpriteSet set;
        final WorldSprite sprite;
        Exhibit(SpriteSet set, float x, float y) {
            this.set = set;
            sprite = new WorldSprite(set.frame(State.IDLE,Direction.DOWN,0),x,y,set.feetX,set.height-set.feetY,set.displayScale);
        }
    }
    public GameScreen() { this(GameConfig.defaults(), () -> Gdx.app.exit()); }
    public GameScreen(GameConfig config,Runnable onHome) { this(config,onHome,0); }
    /** Explicit exhibit count for render verification, not a gameplay spawn system. */
    public GameScreen(int exhibitCount) { this(new GameConfig.Builder().debug(true).build(),()->Gdx.app.exit(),checkedExhibits(exhibitCount)); }
    private static int checkedExhibits(int count) { if(count<6 || count>60)throw new IllegalArgumentException("Exhibits must be 6..60");return count; }
    private GameScreen(GameConfig config,Runnable onHome,int exhibitCount) {
        this.config=profile==null?config:profile.tuning(config);this.onHome=onHome;
        hud=new GameHud(this.config.strings);input=new GameInput(hud,this);
        // Existing local camera measurement explicitly opts into the isolated preview scene.
        exhibitMode=exhibitCount>0 || trace!=null;
        fixedMapSeed=debugSeed(this.config.fixedMapSeed);
        map=exhibitMode || (profile!=null && !profile.worldProbe)?new ForestMap(assets):new ForestMap(assets,nextSeed());
        viewport=new ExtendViewport(320/this.config.visuals.zoom,240/this.config.visuals.zoom,map.pixelWidth(),map.pixelHeight(),camera);
        playerSprite=new WorldSprite(playerSet.frame(State.IDLE,Direction.DOWN,0),map.spawnX,map.spawnY,
            playerSet.feetX,playerSet.height-playerSet.feetY,playerSet.displayScale);
        session=new GameSession(this.config,map.grid,map.spawnX,map.spawnY,assets,profile==null?null:profile.metrics,map.seed);player=session.player;
        hud.setDebug(debugEnabled());
        if(exhibitMode) {
            exhibit("tiny_slime",-78,38); exhibit("tiny_bat",-26,52); exhibit("giant_bee",30,52);
            exhibit("red_cap",77,38); exhibit("living_armor",-65,-42); exhibit("lich_lord",65,-50);
            String[] ids={"honey_slime","wild_boar","giant_bee","red_cap","red_crab","giant_frog","rock_golem","living_armor","bone_hound"};
            for (int i=6;i<exhibitCount;i++) exhibit(ids[i%ids.length],(i%10-4.5f)*26,(i/10-2.5f)*26);
        }
        Gdx.app.log("MonsterSurvivor", "Ready: "+this.config.difficulty+" "+this.config.mode+" debug="+debugEnabled()+" preview="+exhibitMode
            +" seed="+map.seed+" walkable="+map.walkableTiles+" fallback="+map.fallback+" sword="+this.config.swordRange+"/"+this.config.swordCooldown
            +" roster="+com.example.monstersurvivor.enemy.EnemyRegistry.all().size()+" textures="+assets.textureCount()+" rgbaBytes="+assets.textureBytes()+" language="+this.config.language.code
            +" initialEnemies="+this.config.initialEnemies+" residentCap="+this.config.maxEnemies+" enemySpeedMultiplier="+this.config.enemySpeedMultiplier);
    }
    private static Long debugSeed(Long configured) {
        if(configured!=null)return configured;
        var file=Gdx.files.local("map-seed.txt");if(!file.exists())return null;
        try {return Long.valueOf(file.readString("UTF-8").trim());}
        catch(NumberFormatException error) {Gdx.app.log("MonsterSurvivor","Invalid map-seed.txt; using random seed");return null;}
    }
    private long nextSeed() {
        if(fixedMapSeed!=null)return fixedMapSeed;
        long seed=java.util.concurrent.ThreadLocalRandom.current().nextLong();return map!=null && seed==map.seed?seed+1:seed;
    }
    private void exhibit(String id, float dx, float dy) { exhibits.add(new Exhibit(assets.sprite(id),player.x+dx,player.y+dy)); }
    @Override public void show() {
        AssetVisualCheck.runIfRequested(assets);
        Gdx.input.setInputProcessor(input); Gdx.input.setCatchKey(Input.Keys.BACK,true);
    }
    @Override public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width,height); hud.resize(width,height); followCamera(); input.reset();
    }
    @Override public void render(float delta) {
        if(profile!=null)profile.beforeFrame(this);
        boolean wasGameOver=gameOver();
        if(trace!=null)trace.beforeFrame(this);
        float step = dropNextDelta ? 0 : Math.min(Math.max(delta,0),.1f);
        dropNextDelta = false;
        input.sampleAxes();
        if (exhibitMode && !isPaused()) {
            elapsed += step; exhibitTime += step;
            player.update(step,input.axisX(),input.axisY(),map.grid);
        } else if(!exhibitMode) {
            // Follow before spawn sampling so the exclusion rectangle matches the current camera.
            followCamera();
            session.update(step,input.axisX(),input.axisY(),isPaused(),
                camera.position.x-camera.viewportWidth/2,camera.position.y-camera.viewportHeight/2,
                camera.position.x+camera.viewportWidth/2,camera.position.y+camera.viewportHeight/2);
            elapsed=session.elapsed();
        }
        if(session.choosingWeapon() && session.selection.token()!=loggedSelectionToken) {
            loggedSelectionToken=session.selection.token();input.reset();dropNextDelta=true;
            StringBuilder choices=new StringBuilder();for(int i=0;i<session.selection.size();i++) {
                if(i>0)choices.append('|');choices.append(session.selection.get(i).title);
            }
            Gdx.app.log("MonsterSurvivor","Weapon choice id="+session.selection.pendingId()+" token="+session.selection.token()
                +" time="+elapsed+" player="+player.x+","+player.y+" offers="+choices+" loadout="+session.weapons.loadout.describe());
        }
        if(!wasGameOver && gameOver())Gdx.app.log("MonsterSurvivor","Game Over seed="+map.seed+" survived="+elapsed+" kills="+session.kills);
        if(session.pickups.noticeSerial!=loggedNotice) {
            loggedNotice=session.pickups.noticeSerial;
            Gdx.app.log("MonsterSurvivor","Pickup "+session.pickups.notice+" hp="+player.hp()+"/"+player.maxHp()
                +" dmg="+player.stats.damage()+" speed="+player.stats.speed()+" collected="+session.pickups.collectedItems);
        }
        if((config.debug || fixedMapSeed!=null) && session.pickups.spawnedBoxes!=loggedBoxes) {
            loggedBoxes=session.pickups.spawnedBoxes;
            for(int i=0;i<session.pickups.size();i++) {
                var item=session.pickups.get(i);
                if(item.type.box && item.age==0)Gdx.app.log("MonsterSurvivor","Box "+item.type+" at="+item.x+","+item.y+" time="+elapsed+" progress="+session.progress());
            }
        }
        followCamera();
        FrameMetrics metrics=session.metrics;
        long worldStart=metrics==null?0:metrics.start(),animationBefore=metrics==null?0:metrics.value(ANIMATION);
        ScreenUtils.clear(.17f,.23f,.13f,1);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        map.renderGround(batch,camera);
        visible.clear();
        float left=camera.position.x-camera.viewportWidth/2, bottom=camera.position.y-camera.viewportHeight/2;
        float right=left+camera.viewportWidth, top=bottom+camera.viewportHeight;
        map.enqueueProps(visible,left,bottom,right,top);
        if(!exhibitMode)pickupRenderer.enqueue(session.pickups,visible,left,bottom,right,top);
        playerSprite.x=view.playerX; playerSprite.y=view.playerY;
        // Preview buttons affect the stationary Warrior too. Movement always keeps its original Walk logic.
        boolean previewPlayer=exhibitMode && !player.walking && stateIndex>0;
        long animationStart=metrics==null?0:metrics.start();
        State playerState=exhibitMode?(player.walking?State.WALK:PREVIEW_STATES[previewPlayer?stateIndex:0]):player.state();
        playerSprite.frame=playerSet.frame(playerState,
            previewPlayer?DIRECTIONS[directionIndex]:player.direction,previewPlayer?exhibitTime:player.renderAnimationTime());
        if(metrics!=null)metrics.finish(ANIMATION,animationStart);
        enqueue(playerSprite,left,bottom,right,top);
        for (int i=0;i<exhibits.size;i++) {
            Exhibit e=exhibits.get(i);
            e.sprite.frame=e.set.frame(PREVIEW_STATES[stateIndex],DIRECTIONS[directionIndex],exhibitTime);
            enqueue(e.sprite,left,bottom,right,top);
        }
        for(int i=0;i<session.enemies.size();i++) {
            Enemy e=session.enemies.get(i);
            if((config.debug || fixedMapSeed!=null) && !loggedSpecies[e.definition.index]) {
                loggedSpecies[e.definition.index]=true;
                Gdx.app.log("MonsterSurvivor","Enemy species="+e.definition.id+" progress="+session.progress()+" hp="+e.stats.hp+" flying="+e.definition.flying);
            }
            if(e.x<left-80 || e.x>right+80 || e.y<bottom-80 || e.y>top+80)continue;
            animationStart=metrics==null?0:metrics.start();e.prepareSprite();
            if(metrics!=null)metrics.finish(ANIMATION,animationStart);
            enqueue(e.sprite,left,bottom,right,top);
        }
        visible.sort(WorldSprite.BACK_TO_FRONT);
        for (int i=0;i<visible.size;i++) {
            WorldSprite sprite=visible.get(i);
            if(sprite==playerSprite && player.alive() && player.immunity()>0) {
                float alpha=((int)(player.immunity()*16)&1)==0?.4f:1;
                batch.setColor(1,.55f,.55f,alpha);sprite.draw(batch);batch.setColor(1,1,1,1);
            } else sprite.draw(batch);
        }
        batch.end();
        worldDrawCalls=batch.renderCalls;
        if(worldStart!=0)metrics.add(WORLD_RENDER,System.nanoTime()-worldStart-(metrics.value(ANIMATION)-animationBefore));
        if(!exhibitMode) {
            long start=metrics==null?0:metrics.start();
            float nightStrength=profile!=null && profile.worldProbe?profile.nightStrength():session.night.strength();
            if(nightStrength>0) {
                nightRenderer.draw(batch,config.visuals,nightStrength,camera.viewportWidth,camera.viewportHeight,view.playerX-left,view.playerY-bottom);
                worldDrawCalls+=batch.renderCalls;
            }
            if(metrics!=null)metrics.finish(NIGHT_RENDER,start);
            start=metrics==null?0:metrics.start();batch.setProjectionMatrix(camera.combined);batch.begin();
            weaponRenderer.draw(batch,session.weapons,left,bottom,right,top);batch.end();worldDrawCalls+=batch.renderCalls;
            if(metrics!=null)metrics.finish(WEAPON_RENDER,start);
        }
        long hudStart=metrics==null?0:metrics.start();
        hud.render(batch,input,isPaused(),elapsed,Math.min(delta,.5f),PREVIEW_STATES[stateIndex],DIRECTIONS[directionIndex],
            player,session.kills,config.mode,gameOver(),session.pickups,camera,session);
        if(metrics!=null)metrics.finish(HUD,hudStart);
        if(trace!=null)trace.afterFrame(this,view,delta,step);
        if(profile!=null)profile.afterFrame(this,delta);
    }
    private void enqueue(WorldSprite sprite,float left,float bottom,float right,float top) {
        if (sprite.visible(left,bottom,right,top)) visible.add(sprite);
    }
    private void followCamera() {
        view.update(player.x,player.y,camera.viewportWidth,camera.viewportHeight,
            viewport.getScreenWidth(),viewport.getScreenHeight(),map.pixelWidth(),map.pixelHeight());
        camera.position.set(view.cameraX,view.cameraY,0);
        camera.update();
    }
    public static float clampCamera(float target,float halfExtent,float mapExtent) {
        return PixelCamera.clamp(target,halfExtent,mapExtent);
    }
    @Override public boolean isPaused() { return userPaused || applicationPaused; }
    @Override public void togglePause() {
        if (applicationPaused || gameOver()) return;
        userPaused=!userPaused; input.reset(); dropNextDelta=true;
        Gdx.app.log("MonsterSurvivor", "User pause="+userPaused+" time="+elapsed+" player="+player.x+","+player.y+" fps="+Gdx.graphics.getFramesPerSecond()
            +" hp="+player.hp()+"/"+player.maxHp()+" dmg="+player.stats.damage()+" speed="+player.stats.speed()
            +" boxes="+session.pickups.boxes()+" drops="+session.pickups.healthDrops()+" collected="+session.pickups.collectedItems
            +" kills="+session.kills+" seed="+map.seed+" move="+player.movementDirection+" attack="+player.attackDirection
            +" phase="+session.phase()+" loadout="+session.weapons.loadout.describe()+" projectiles="+session.weapons.projectiles.size()
            +" effects="+session.weapons.projectiles.effectCount()+" swordSwings="+session.combat.swings
            +" rangedHits="+session.weapons.projectiles.hits+" lightningCasts="+session.weapons.lightningCasts
            +" lightningHits="+session.weapons.lightningHits+" explosions="+session.weapons.projectiles.explosions+" casts="+session.weapons.loadout.activity()
            +" progress="+session.progress()+" night="+session.night.strength()+" zoom="+config.visuals.zoom);
    }
    @Override public void cycleAnimation() { if (debugEnabled() && exhibitMode && !isPaused()) { stateIndex=(stateIndex+1)%PREVIEW_STATES.length; exhibitTime=0; } }
    @Override public void cycleDirection() {
        if (debugEnabled() && exhibitMode && !isPaused()) {
            directionIndex=(directionIndex+1)%DIRECTIONS.length; exhibitTime=0;
            if (!player.walking) { player.direction=DIRECTIONS[directionIndex]; player.animationTime=0; }
        }
    }
    @Override public boolean debugEnabled() { return config.debug || exhibitMode; }
    @Override public boolean gameplayStopped() { return !exhibitMode && session.phase()!=GameSession.Phase.RUNNING; }
    @Override public boolean gameOver() { return !exhibitMode && session.phase()==GameSession.Phase.GAME_OVER; }
    @Override public boolean weaponSelection() {return !exhibitMode && session.choosingWeapon();}
    @Override public long weaponToken() {return session.selection.token();}
    @Override public int weaponOfferCount() {return session.selection.size();}
    @Override public boolean chooseWeapon(int index,long token) {
        if(isPaused() || !weaponSelection())return false;
        String title=index>=0 && index<session.selection.size()?session.selection.get(index).title:"invalid";
        if(!session.chooseWeapon(index,token))return false;
        input.reset();dropNextDelta=true;
        Gdx.app.log("MonsterSurvivor","Weapon selected "+title+" time="+elapsed+" loadout="+session.weapons.loadout.describe());return true;
    }
    @Override public void retry() {
        if(!gameOver())return;
        session.end();
        if(!exhibitMode && (profile==null || profile.worldProbe))map=new ForestMap(assets,nextSeed());
        session=new GameSession(config,map.grid,map.spawnX,map.spawnY,assets,profile==null?null:profile.metrics,map.seed);player=session.player;
        loggedNotice=loggedBoxes=0;loggedSelectionToken=-1;
        java.util.Arrays.fill(loggedSpecies,false);
        elapsed=0;userPaused=applicationPaused=false;input.reset();dropNextDelta=true;followCamera();
        Gdx.app.log("MonsterSurvivor","Retry seed="+map.seed+" walkable="+map.walkableTiles+" fallback="+map.fallback);
    }
    @Override public void home() { userPaused=true;input.reset();session.end();if(onHome!=null)onHome.run(); }
    @Override public void pause() {
        applicationPaused=true; userPaused=true; input.reset();
        Gdx.app.log("MonsterSurvivor", "Lifecycle pause time="+elapsed+" player="+player.x+","+player.y);
    }
    @Override public void resume() {
        applicationPaused=false; dropNextDelta=true;
        Gdx.app.log("MonsterSurvivor", "Lifecycle resume: waiting for explicit resume");
    }
    @Override public void hide() { input.reset(); if (Gdx.input.getInputProcessor()==input) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() {
        if (disposed) return;
        disposed=true; hide();session.end();hud.dispose();batch.dispose();assets.dispose();
    }
    public Player getPlayer() { return player; }
    public ForestMap getMap() { return map; }
    public GameAssets getAssets() { return assets; }
    public GameInput getControls() { return input; }
    public GameHud getHud() { return hud; }
    public OrthographicCamera getCamera() { return camera; }
    public float getElapsed() { return elapsed; }
    public int getWorldDrawCalls() { return worldDrawCalls; }
    public GameSession getSession() { return session; }
    public int getVisibleCount() {return visible.size;}
}
