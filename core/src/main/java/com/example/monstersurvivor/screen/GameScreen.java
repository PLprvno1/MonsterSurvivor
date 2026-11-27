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
import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.character.CharacterOwnership;
import com.example.monstersurvivor.character.CharacterProfile;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.consumable.PotionSystem;
import com.example.monstersurvivor.consumable.PotionType;
import com.example.monstersurvivor.consumable.PotionUseBridge;
import com.example.monstersurvivor.gameplay.CoinSettlement;
import com.example.monstersurvivor.debug.CombatProfile;
import com.example.monstersurvivor.debug.DiagnosticRun;
import com.example.monstersurvivor.debug.FrameMetrics;
import static com.example.monstersurvivor.debug.FrameMetrics.Part.*;
import com.example.monstersurvivor.input.GameInput;
import com.example.monstersurvivor.render.WorldSprite;
import com.example.monstersurvivor.render.PixelCamera;
import com.example.monstersurvivor.render.CameraTrace;
import com.example.monstersurvivor.ui.GameHud;
import com.example.monstersurvivor.world.ForestMap;
import java.util.Set;

/** Gameplay orchestration with the established map renderer, foot pivots and pixel camera. */
public final class GameScreen extends ScreenAdapter implements GameInput.Commands {
    private final com.example.monstersurvivor.audio.GameAudioManager audio=new com.example.monstersurvivor.audio.GameAudioManager();
    private com.example.monstersurvivor.audio.AudioEnvironment audioEnvironment=new com.example.monstersurvivor.audio.AudioEnvironment();
    private boolean hadAudioFocus=true;
    private long observedFocusLoss;
    public GameScreen(GameConfig config,Runnable onHome,com.example.monstersurvivor.audio.AudioEnvironment environment) {
        this(config,onHome,environment,DiagnosticRun.NONE,CharacterId.WARRIOR.stableId(),Set.of(CharacterId.WARRIOR.stableId()),settlement->{});
    }
    public GameScreen(GameConfig config,Runnable onHome,com.example.monstersurvivor.audio.AudioEnvironment environment,DiagnosticRun diagnostics) {
        this(config,onHome,environment,diagnostics,CharacterId.WARRIOR.stableId(),Set.of(CharacterId.WARRIOR.stableId()),settlement->{});
    }
    public GameScreen(GameConfig config,Runnable onHome,com.example.monstersurvivor.audio.AudioEnvironment environment,DiagnosticRun diagnostics,String characterId) {
        this(config,onHome,environment,diagnostics,characterId,Set.of(CharacterId.WARRIOR.stableId()),settlement->{});
    }
    public GameScreen(GameConfig config,Runnable onHome,com.example.monstersurvivor.audio.AudioEnvironment environment,DiagnosticRun diagnostics,
                      String characterId,Set<String> ownedCharacterIds,java.util.function.Consumer<CoinSettlement> coinSettlements) {
        this(config,onHome,environment,diagnostics,characterId,ownedCharacterIds,coinSettlements,new int[3],PotionUseBridge.NONE);
    }
    public GameScreen(GameConfig config,Runnable onHome,com.example.monstersurvivor.audio.AudioEnvironment environment,DiagnosticRun diagnostics,
                      String characterId,Set<String> ownedCharacterIds,java.util.function.Consumer<CoinSettlement> coinSettlements,
                      int[] potionCounts,PotionUseBridge potionBridge) {
        this(config,onHome,0,diagnostics,characterId,ownedCharacterIds,coinSettlements,potionCounts,potionBridge);
        audioEnvironment=environment;hadAudioFocus=environment.focused;observedFocusLoss=environment.focusLossSerial;syncAudio();
    }
    private void syncAudio() {
        boolean focused=audioEnvironment.focused;
        if(hadAudioFocus && !focused || observedFocusLoss!=audioEnvironment.focusLossSerial) {userPaused=true;input.reset();dropNextDelta=true;}
        observedFocusLoss=audioEnvironment.focusLossSerial;
        hadAudioFocus=focused;
        audio.settings(audioEnvironment.settings);audio.blocked(applicationPaused || !focused);
        audio.musicPaused((isPaused() && !gameOver() && !victory()) || session.choosingReward());
        audio.listener(player.x,player.y);audio.music(com.example.monstersurvivor.audio.GameAudioManager.musicFor(session));
    }
    private static final State[] PREVIEW_STATES = {State.IDLE, State.WALK, State.ATTACK, State.HURT, State.DEATH, State.VICTORY};
    private static final Direction[] DIRECTIONS = Direction.values();
    private final DiagnosticRun diagnostics;
    private final CombatProfile profile;
    private final com.example.monstersurvivor.debug.BossDiagnostics bossDiagnostics;
    private final com.example.monstersurvivor.debug.SkillDiagnostics skillDiagnostics;
    private final GameAssets assets = new GameAssets();
    private ForestMap map;
    private Player player;
    private GameSession session;
    private final GameConfig config;
    private final Runnable onHome;
    private final String characterId;
    private final Set<String> ownedCharacterIds;
    private final java.util.function.Consumer<CoinSettlement> coinSettlements;
    private final int[] potionCounts;
    private final PotionUseBridge potionBridge;
    private final boolean exhibitMode;
    private final Long fixedMapSeed;
    private final SpriteBatch batch = new SpriteBatch();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ExtendViewport viewport;
    private final GameHud hud;
    private final com.example.monstersurvivor.pickup.PickupRenderer pickupRenderer=new com.example.monstersurvivor.pickup.PickupRenderer(assets);
    private final com.example.monstersurvivor.weapon.WeaponRenderer weaponRenderer=new com.example.monstersurvivor.weapon.WeaponRenderer(assets);
    private final com.example.monstersurvivor.render.NightRenderer nightRenderer=new com.example.monstersurvivor.render.NightRenderer(assets.region("weapon_pixel"));
    private final com.example.monstersurvivor.boss.BossRenderer bossRenderer=new com.example.monstersurvivor.boss.BossRenderer(assets.region("weapon_pixel"));
    private final com.example.monstersurvivor.skill.SkillRenderer skillRenderer=new com.example.monstersurvivor.skill.SkillRenderer(assets.region("weapon_pixel"));
    private final com.example.monstersurvivor.skill.ClassSkillRenderer classSkillRenderer=new com.example.monstersurvivor.skill.ClassSkillRenderer(assets);
    private final com.example.monstersurvivor.enemy.skill.EnemySkillRenderer enemySkillRenderer=new com.example.monstersurvivor.enemy.skill.EnemySkillRenderer(assets);
    private final GameInput input;
    private final CameraTrace trace;
    private final PixelCamera view;
    private final Array<WorldSprite> visible = new Array<>(512);
    private final Array<Exhibit> exhibits = new Array<>(10);
    private final SpriteSet playerSet;
    private final WorldSprite playerSprite;
    private boolean userPaused, applicationPaused, dropNextDelta = true, disposed,confirmingHome;
    private boolean potionRequestPending;
    private long potionRequestSerial;
    private float elapsed, exhibitTime;
    private int stateIndex, directionIndex, worldDrawCalls;
    private int loggedNotice,loggedBoxes;
    private int loggedBossNotice;
    private boolean loggedVictory;
    private int loggedSkillImpacts;
    private long loggedSelectionToken=-1;
    private java.util.function.Consumer<com.example.monstersurvivor.gameplay.MatchResult> completed=result->{};
    public void onMatchCompleted(java.util.function.Consumer<com.example.monstersurvivor.gameplay.MatchResult> observer) {
        completed=observer;attachMatchObserver();
    }
    private void attachMatchObserver() {
        session.onCompleted(completed,diagnostics.enabled() || exhibitMode || debugEnabled());
    }
    private long loggedSkillSelectionToken;
    private int loggedSkillBoxes;
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
        this(config,onHome,exhibitCount,DiagnosticRun.NONE);
    }
    private GameScreen(GameConfig config,Runnable onHome,int exhibitCount,DiagnosticRun diagnostics) {
        this(config,onHome,exhibitCount,diagnostics,CharacterId.WARRIOR.stableId(),Set.of(CharacterId.WARRIOR.stableId()),settlement->{});
    }
    private GameScreen(GameConfig config,Runnable onHome,int exhibitCount,DiagnosticRun diagnostics,String requestedCharacterId,
                       Set<String> ownedCharacters,java.util.function.Consumer<CoinSettlement> coinSettlements) {
        this(config,onHome,exhibitCount,diagnostics,requestedCharacterId,ownedCharacters,coinSettlements,new int[3],PotionUseBridge.NONE);
    }
    private GameScreen(GameConfig config,Runnable onHome,int exhibitCount,DiagnosticRun diagnostics,String requestedCharacterId,
                       Set<String> ownedCharacters,java.util.function.Consumer<CoinSettlement> coinSettlements,int[] initialPotionCounts,
                       PotionUseBridge potionBridge) {
        this.diagnostics=diagnostics;
        // Exactly one tuning fixture per explicit run. Legacy files at the local root are never read.
        skillDiagnostics=com.example.monstersurvivor.debug.SkillDiagnostics.load(diagnostics);
        bossDiagnostics=skillDiagnostics==null?com.example.monstersurvivor.debug.BossDiagnostics.load(diagnostics):null;
        profile=skillDiagnostics==null && bossDiagnostics==null?CombatProfile.load(diagnostics):null;
        trace=skillDiagnostics==null && bossDiagnostics==null && profile==null?CameraTrace.load(diagnostics):null;
        view=new PixelCamera(trace==null?PixelCamera.Mode.SCREEN:trace.mode);
        this.config=skillDiagnostics!=null?skillDiagnostics.tuning(config):bossDiagnostics!=null?bossDiagnostics.tuning(config):profile==null?config:profile.tuning(config);this.onHome=onHome;
        ownedCharacterIds=ownedCharacters==null?Set.of(CharacterId.WARRIOR.stableId()):Set.copyOf(ownedCharacters);
        this.coinSettlements=coinSettlements==null?settlement->{}:coinSettlements;
        potionCounts=initialPotionCounts==null?new int[3]:initialPotionCounts.clone();
        this.potionBridge=potionBridge==null?PotionUseBridge.NONE:potionBridge;
        characterId=CharacterOwnership.selectableOrWarrior(requestedCharacterId,ownedCharacterIds).stableId();
        playerSet=assets.sprite(CharacterProfile.forId(characterId).spriteId);
        hud=new GameHud(this.config.strings,assets);input=new GameInput(hud,this);
        // Existing local camera measurement explicitly opts into the isolated preview scene.
        exhibitMode=exhibitCount>0 || trace!=null;
        fixedMapSeed=debugSeed(this.config.fixedMapSeed,diagnostics);
        map=exhibitMode || (profile!=null && !profile.worldProbe)?new ForestMap(assets):new ForestMap(assets,nextSeed());
        viewport=new ExtendViewport(320/this.config.visuals.zoom,240/this.config.visuals.zoom,map.pixelWidth(),map.pixelHeight(),camera);
        playerSprite=new WorldSprite(playerSet.frame(State.IDLE,Direction.DOWN,0),map.spawnX,map.spawnY,
            playerSet.feetX,playerSet.height-playerSet.feetY,playerSet.displayScale);
        session=new GameSession(this.config,map.grid,map.spawnX,map.spawnY,assets,skillDiagnostics!=null?skillDiagnostics.metrics:bossDiagnostics!=null?bossDiagnostics.metrics:profile==null?null:profile.metrics,map.seed,characterId,potionCounts);player=session.player;
        session.audio(audio);audio.listener(player.x,player.y);
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
        logBaseline("Start");
    }
    private void logBaseline(String reason) {
        Gdx.app.log("MatchBaseline",reason+" diagnostics="+diagnostics.id()+" debug="+debugEnabled()+" hp="+player.hp()+"/"+player.maxHp()
            +" damage="+player.stats.damage()+" speed="+player.stats.speed()+" weapons="+session.weapons.loadout.describe()
            +" seed="+map.seed+" fixedSeed="+(fixedMapSeed!=null)+" progress="+session.progress()+" elapsed="+session.elapsed()
            +" dashLevel="+session.skills.view(com.example.monstersurvivor.skill.SkillType.DASH).level()
            +" ultLevel="+session.skills.view(com.example.monstersurvivor.skill.SkillType.ULTIMATE).level()
            +" dashCd="+session.skills.view(com.example.monstersurvivor.skill.SkillType.DASH).cooldownRemaining()
            +" ultCd="+session.skills.view(com.example.monstersurvivor.skill.SkillType.ULTIMATE).cooldownRemaining());
    }
    private static Long debugSeed(Long configured,DiagnosticRun diagnostics) {
        if(configured!=null)return configured;
        if(!diagnostics.enabled())return null;
        var file=diagnostics.file("map-seed.txt");if(!file.exists())return null;
        try {return Long.valueOf(file.readString("UTF-8").trim());}
        catch(NumberFormatException error) {Gdx.app.log("MonsterSurvivor","Invalid map-seed.txt; using random seed");return null;}
    }
    private long nextSeed() {
        if(fixedMapSeed!=null)return fixedMapSeed;
        long seed=java.util.concurrent.ThreadLocalRandom.current().nextLong();return map!=null && seed==map.seed?seed+1:seed;
    }
    private void exhibit(String id, float dx, float dy) { exhibits.add(new Exhibit(assets.sprite(id),player.x+dx,player.y+dy)); }
    @Override public void show() {
        syncAudio();
        AssetVisualCheck.runIfRequested(assets,diagnostics);
        Gdx.input.setInputProcessor(input); Gdx.input.setCatchKey(Input.Keys.BACK,true);
    }
    @Override public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width,height); hud.resize(width,height); followCamera(); input.reset();
    }
    @Override public void render(float delta) {
        syncAudio();
        if(profile!=null)profile.beforeFrame(this);
        if(bossDiagnostics!=null)bossDiagnostics.before(this);
        if(skillDiagnostics!=null)skillDiagnostics.before(this);
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
            session.update(step,input.axisX(),input.axisY(),isPaused() || potionRequestPending,
                camera.position.x-camera.viewportWidth/2,camera.position.y-camera.viewportHeight/2,
                camera.position.x+camera.viewportWidth/2,camera.position.y+camera.viewportHeight/2);
            elapsed=session.elapsed();syncAudio();
        }
        if(session.choosingChest() && session.chestSelection.token()!=loggedSkillSelectionToken) {
            loggedSkillSelectionToken=session.chestSelection.token();input.reset();dropNextDelta=true;
            StringBuilder choices=new StringBuilder();for(int i=0;i<session.chestSelection.size();i++) {
                if(i>0)choices.append('|');var offer=session.chestSelection.get(i);choices.append(offer.stableId).append(':').append(offer.fromLevel).append("->").append(offer.toLevel);
            }
            Gdx.app.log("TreasureChest","branch="+session.chestSelection.branch()+" id="+session.chestSelection.pendingId()+" token="+session.chestSelection.token()+" offers="+choices+" elapsed="+session.elapsed()+" progress="+session.progress());
        }
        if(loggedSkillBoxes!=session.pickups.spawnedSkillBoxes) {
            loggedSkillBoxes=session.pickups.spawnedSkillBoxes;
            for(int i=0;i<session.pickups.size();i++) {var p=session.pickups.get(i);if(p.type==com.example.monstersurvivor.pickup.PickupType.TREASURE_CHEST)
                Gdx.app.log("TreasureChest","Chest id="+p.id+" player="+player.x+","+player.y+" item="+p.x+","+p.y+" elapsed="+session.elapsed()+" progress="+session.progress()+" count="+session.pickups.treasureChests());}
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
            if(e.definition.index>=0 && (config.debug || fixedMapSeed!=null) && !loggedSpecies[e.definition.index]) {
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
            bossRenderer.draw(batch,session.bosses,left,bottom,right,top);
            weaponRenderer.draw(batch,session.weapons,left,bottom,right,top);
            skillRenderer.draw(batch,session.skills,player.x,player.y,left,bottom,right,top);
            classSkillRenderer.draw(batch,session.classSkills,session.player,left,bottom,right,top);batch.end();worldDrawCalls+=batch.renderCalls;
            if(metrics!=null)metrics.finish(WEAPON_RENDER,start);
            start=metrics==null?0:metrics.start();batch.begin();
            enemySkillRenderer.draw(batch,session.enemies,player,left,bottom,right,top);batch.end();worldDrawCalls+=batch.renderCalls;
            if(metrics!=null)metrics.finish(ENEMY_FX,start);
        }
        long hudStart=metrics==null?0:metrics.start();
        hud.render(batch,input,isPaused(),elapsed,Math.min(delta,.5f),PREVIEW_STATES[stateIndex],DIRECTIONS[directionIndex],
            player,session.kills,config.mode,gameOver(),session.pickups,camera,session);
        if(loggedBossNotice!=session.bosses.noticeSerial) {
            loggedBossNotice=session.bosses.noticeSerial;var b=session.bosses.primary();
            Gdx.app.log("MonsterSurvivor","Boss event="+session.bosses.noticeKey()+" progress="+session.progress()+" elapsed="+session.elapsed()
                +" mini="+session.bosses.miniState()+" final="+session.bosses.finalState()+" actor="+(b==null?"pending":b.definition.body().id+" hp="+b.hp()+"/"+b.maxHp()+" phase="+b.phase()+" xy="+b.actor.x+","+b.actor.y));
        }
        if(victory() && !loggedVictory) {loggedVictory=true;Gdx.app.log("MonsterSurvivor","Victory result="+session.result());}
        if(loggedSkillImpacts!=session.skills.ultimateImpacts) {
            loggedSkillImpacts=session.skills.ultimateImpacts;
            Gdx.app.log("Skills","Impact token="+session.skills.view(com.example.monstersurvivor.skill.SkillType.ULTIMATE).activationId()
                +" impacts="+loggedSkillImpacts+" hits="+session.skills.ultimateHits+" kills="+session.skills.ultimateKills+" knockbacks="+session.skills.knockbacks
                +" bossHits="+session.skills.ultimateBossHits
                +" elapsed="+session.elapsed()+" radius="+session.skills.ultimateRadius()+" phase="+session.phase());
        }
        if(metrics!=null)metrics.finish(HUD,hudStart);
        if(trace!=null)trace.afterFrame(this,view,delta,step);
        if(profile!=null)profile.afterFrame(this,delta);
        if(bossDiagnostics!=null)bossDiagnostics.after(this);
        if(skillDiagnostics!=null)skillDiagnostics.after(this);
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
    @Override public boolean activateSkill(com.example.monstersurvivor.skill.SkillType type,float x,float y) {
        if(disposed || exhibitMode || !audioEnvironment.focused || !session.activateSkill(type,x,y,isPaused()))return false;
        var state=session.skills.view(type);
        Gdx.app.log("Skills","Activate type="+type+" token="+state.activationId()+" count="+state.activations()+" elapsed="+session.elapsed()
            +" axis="+x+","+y+" dash="+session.skills.dashX()+","+session.skills.dashY()+" player="+player.x+","+player.y
            +" joystick="+input.activePointer()+" cooldown="+state.cooldownRemaining());return true;
    }
    @Override public void usePotion(PotionType type) {
        if(disposed || exhibitMode || potionRequestPending || !audioEnvironment.focused)return;
        if(isPaused() || session.choosingReward()) {hud.potionFeedback(type,PotionSystem.Availability.NOT_RUNNING);return;}
        PotionSystem.Availability availability=session.potionAvailability(type);
        if(availability!=PotionSystem.Availability.USABLE) {hud.potionFeedback(type,availability);return;}
        potionRequestPending=true;long request=++potionRequestSerial;input.reset();dropNextDelta=true;
        hud.potionPending(true);hud.potionFeedback(type,availability);
        try {
            potionBridge.consume(type,success->Gdx.app.postRunnable(()->{
                if(disposed || request!=potionRequestSerial)return;
                potionRequestPending=false;hud.potionPending(false);
                boolean applied=Boolean.TRUE.equals(success) && session.applyConsumedPotion(type);
                if(applied)potionCounts[type.ordinal()]=Math.max(0,potionCounts[type.ordinal()]-1);
                PotionSystem.Availability feedback=applied?PotionSystem.Availability.APPLIED
                    :Boolean.TRUE.equals(success)?PotionSystem.Availability.NOT_RUNNING:PotionSystem.Availability.SAVE_FAILED;
                hud.potionFeedback(type,feedback);input.reset();dropNextDelta=true;
            }));
        } catch(RuntimeException failure) {
            potionRequestPending=false;hud.potionPending(false);hud.potionFeedback(type,PotionSystem.Availability.SAVE_FAILED);input.reset();
        }
    }
    @Override public void togglePause() {
        if (applicationPaused || !audioEnvironment.focused || gameOver() || victory()) return;
        if(confirmingHome) {confirmingHome=false;hud.homeConfirmation(false);}
        userPaused=!userPaused; input.reset(); dropNextDelta=true;
        if(userPaused)audio.stopVoices();
        syncAudio();audio.play(userPaused?com.example.monstersurvivor.audio.AudioEvent.UI_PAUSE:com.example.monstersurvivor.audio.AudioEvent.UI_RESUME);
        Gdx.app.log("MonsterSurvivor", "User pause="+userPaused+" time="+elapsed+" player="+player.x+","+player.y+" fps="+Gdx.graphics.getFramesPerSecond()
            +" hp="+player.hp()+"/"+player.maxHp()+" dmg="+player.stats.damage()+" speed="+player.stats.speed()
            +" boxes="+session.pickups.boxes()+" drops="+session.pickups.healthDrops()+" collected="+session.pickups.collectedItems
            +" kills="+session.kills+" seed="+map.seed+" move="+player.movementDirection+" attack="+player.attackDirection
            +" phase="+session.phase()+" loadout="+session.weapons.loadout.describe()+" projectiles="+session.weapons.projectiles.size()
            +" effects="+session.weapons.projectiles.effectCount()+" swordSwings="+session.combat.swings
            +" rangedHits="+session.weapons.projectiles.hits+" lightningCasts="+session.weapons.lightningCasts
            +" lightningHits="+session.weapons.lightningHits+" explosions="+session.weapons.projectiles.explosions+" casts="+session.weapons.loadout.activity()
            +" progress="+session.progress()+" night="+session.night.strength()+" zoom="+config.visuals.zoom
            +" dashCd="+session.skills.view(com.example.monstersurvivor.skill.SkillType.DASH).cooldownRemaining()
            +" ultCd="+session.skills.view(com.example.monstersurvivor.skill.SkillType.ULTIMATE).cooldownRemaining());
    }
    @Override public void cycleAnimation() { if (debugEnabled() && exhibitMode && !isPaused()) { stateIndex=(stateIndex+1)%PREVIEW_STATES.length; exhibitTime=0; } }
    @Override public void cycleDirection() {
        if (debugEnabled() && exhibitMode && !isPaused()) {
            directionIndex=(directionIndex+1)%DIRECTIONS.length; exhibitTime=0;
            if (!player.walking) { player.direction=DIRECTIONS[directionIndex]; player.animationTime=0; }
        }
    }
    @Override public boolean debugEnabled() { return config.debug || exhibitMode; }
    @Override public boolean gameplayStopped() { return !exhibitMode && (session.phase()!=GameSession.Phase.RUNNING || potionRequestPending); }
    @Override public boolean gameOver() { return !exhibitMode && session.phase()==GameSession.Phase.GAME_OVER; }
    @Override public boolean victory() {return !exhibitMode && session.victory();}
    // Existing GameInput modal protocol routes both reward kinds; their token domains are disjoint.
    @Override public boolean weaponSelection() {return !exhibitMode && session.choosingReward();}
    @Override public long weaponToken() {return session.chestSelection.token();}
    @Override public int weaponOfferCount() {return session.chestSelection.size();}
    @Override public boolean chooseWeapon(int index,long token) {
        if(isPaused() || !weaponSelection())return false;
        if(!session.choosingChest() || index<0 || index>=session.chestSelection.size())return false;
        var offer=session.chestSelection.get(index);
        if(!session.chooseTreasureChest(index,token))return false;
        input.reset();dropNextDelta=true;
        Gdx.app.log("TreasureChest","Selected type="+offer.stableId+" from="+offer.fromLevel+" to="+offer.toLevel+" token="+token
            +" dashCd="+session.skills.view(com.example.monstersurvivor.skill.SkillType.DASH).cooldownRemaining()
            +" ultCd="+session.skills.view(com.example.monstersurvivor.skill.SkillType.ULTIMATE).cooldownRemaining()
            +" passives="+session.weapons.loadout.describe()+" elapsed="+session.elapsed());return true;
    }
    @Override public void retry() {
        if(!gameOver() && !victory())return;
        confirmingHome=false;hud.homeConfirmation(false);
        audio.reset();session.end();
        if(!exhibitMode && (profile==null || profile.worldProbe))map=new ForestMap(assets,nextSeed());
        potionRequestPending=false;hud.potionPending(false);
        session=new GameSession(config,map.grid,map.spawnX,map.spawnY,assets,skillDiagnostics!=null?skillDiagnostics.metrics:bossDiagnostics!=null?bossDiagnostics.metrics:profile==null?null:profile.metrics,map.seed,characterId,potionCounts);player=session.player;
        session.audio(audio);
        attachMatchObserver();
        loggedNotice=loggedBoxes=0;loggedSelectionToken=-1;
        loggedBossNotice=0;loggedVictory=false;
        loggedSkillImpacts=0;
        loggedSkillBoxes=0;loggedSkillSelectionToken=0;
        java.util.Arrays.fill(loggedSpecies,false);
        elapsed=0;userPaused=applicationPaused=false;input.reset();dropNextDelta=true;followCamera();
        syncAudio();audio.play(com.example.monstersurvivor.audio.AudioEvent.UI_CONFIRM);
        Gdx.app.log("MonsterSurvivor","Retry seed="+map.seed+" walkable="+map.walkableTiles+" fallback="+map.fallback);
        logBaseline("Retry");
    }
    @Override public void home() {
        if(potionRequestPending)return;
        if(session.phase()==GameSession.Phase.ENDED)return;
        if(!gameOver() && !victory() && !confirmingHome) {
            confirmingHome=true;userPaused=true;input.reset();dropNextDelta=true;hud.homeConfirmation(true);
            audio.stopVoices();syncAudio();return;
        }
        coinSettlements.accept(session.coinSettlement());
        confirmingHome=false;hud.homeConfirmation(false);userPaused=true;input.reset();session.end();audio.reset();audio.blocked(true);
        if(onHome!=null)onHome.run();
    }
    @Override public void pause() {
        applicationPaused=true; userPaused=true; input.reset();
        syncAudio();
        Gdx.app.log("MonsterSurvivor", "Lifecycle pause time="+elapsed+" player="+player.x+","+player.y);
    }
    @Override public void resume() {
        applicationPaused=false; dropNextDelta=true;
        syncAudio();
        Gdx.app.log("MonsterSurvivor", "Lifecycle resume: waiting for explicit resume");
    }
    @Override public void hide() { audio.blocked(true);input.reset(); if (Gdx.input.getInputProcessor()==input) Gdx.input.setInputProcessor(null); }
    @Override public void dispose() {
        if (disposed) return;
        disposed=true; hide();session.end();audio.dispose();hud.dispose();batch.dispose();assets.dispose();
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
