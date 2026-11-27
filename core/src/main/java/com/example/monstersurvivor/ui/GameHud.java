package com.example.monstersurvivor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.example.monstersurvivor.assets.GameAssets.Direction;
import com.example.monstersurvivor.assets.GameAssets.State;
import com.example.monstersurvivor.input.GameInput;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.config.Difficulty;
import com.example.monstersurvivor.config.GameMode;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.localization.*;
import static com.example.monstersurvivor.localization.GameStrings.Key.*;

/**
 * Redesigned Fantasy Pixel Art Gameplay HUD for Monster Survivor.
 * Displays player stats with a visual health bar, match timer & difficulty,
 * pause button, weapon loadout, floating joystick feedback, and pickup indicators.
 */
public final class GameHud implements Disposable, GameInput.Surface {
    private static final com.example.monstersurvivor.skill.SkillType[] SKILL_TYPES=com.example.monstersurvivor.skill.SkillType.values();
    private final ExtendViewport viewport = new ExtendViewport(800, 480);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont(Gdx.files.internal("game/fonts/game-ui.fnt"));
    private final GameStrings strings;
    private final GlyphLayout hp, help, title, resume, pauseLabel,homeConfirmTitle,cancel,confirm,coins;
    private final GlyphLayout timer = new GlyphLayout(), fps = new GlyphLayout();
    private final GlyphLayout animation = new GlyphLayout(), direction = new GlyphLayout();
    private final StringBuilder text = new StringBuilder(64);
    private int lastSecond = -1;
    private float fpsCountdown;
    private State lastState;
    private Direction lastDirection;
    private boolean debug;
    private int lastHp = -1, lastMaxHp = -1, lastKills = -1;
    private int lastDamage = -1, lastSpeed = -1, lastNotice = -1;
    private final GlyphLayout stats = new GlyphLayout(), notice = new GlyphLayout();
    private final GlyphLayout kills = new GlyphLayout(), home, gameOverTitle, retry, result = new GlyphLayout();
    private final GlyphLayout demo, diffLabel = new GlyphLayout();
    private final GlyphLayout endlessInfo=new GlyphLayout();
    private long endlessSession=-1;
    private int endlessBosses=-1,endlessStage=-1;
    private float endlessScale=.65f;
    private final GlyphLayout[] itemLabels = new GlyphLayout[PickupType.values().length];
    private final PickupIndicator indicator = new PickupIndicator();
    private final WeaponHud weaponHud = new WeaponHud();
    private final SkillRewardHud skillRewardHud = new SkillRewardHud();
    private final BossHud bossHud = new BossHud();
    private final SkillHud skillHud = new SkillHud();
    private final PotionHud potionHud=new PotionHud();
    private final GlyphLayout potionNotice=new GlyphLayout();
    private float potionNoticeTime;
    private boolean potionPending;
    private float safeRight,safeBottom,safeLeft,safeTop;
    private float hpScale=1,statsScale=1,killsScale=1,resultScale=1,coinsScale=.75f;
    private float helpScale=.62f;
    private int lastRunCoins=-1;
    private boolean confirmingHome;
    private long resultSession=-1;
    private boolean bossVisible;
    private final TextureRegion[] statusIcons=new TextureRegion[3];
    private final TextureRegion coinIcon;
    private final GlyphLayout[] statusLabels={new GlyphLayout(),new GlyphLayout(),new GlyphLayout()};
    private String noticeText = "";

    public GameHud() {
        this(GameStrings.of(GameLanguage.VI));
    }

    public GameHud(GameStrings strings) {
        this(strings,null);
    }
    public GameHud(GameStrings strings,com.example.monstersurvivor.assets.GameAssets assets) {
        this.strings = strings;
        coinIcon=assets==null?null:assets.enemyFrame("coin_shot_right",0);
        var statusKeys=new GameStrings.Key[]{STATUS_POISON,STATUS_BURN,STATUS_SLOW};
        String[] iconNames={"status_poison","status_burn","status_slow"};
        for(int i=0;i<3;i++){
            HudStyle.fit(font,statusLabels[i],strings.get(statusKeys[i]),.65f,52);
            if(assets!=null)statusIcons[i]=assets.enemyFrame(iconNames[i],0);
        }
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        hp = new GlyphLayout(font, strings.get(HP) + " 10/10");
        help = new GlyphLayout(font, strings.get(CONTROLS));
        title = new GlyphLayout(font, strings.get(PAUSED));
        resume = new GlyphLayout(font, strings.get(RESUME));
        pauseLabel = new GlyphLayout(font, strings.get(PAUSE));
        home = new GlyphLayout(font, strings.get(HOME));
        homeConfirmTitle=new GlyphLayout(font,strings.get(HOME_CONFIRM_TITLE));
        cancel=new GlyphLayout(font,strings.get(CANCEL));confirm=new GlyphLayout(font,strings.get(HOME_CONFIRM));coins=new GlyphLayout();
        gameOverTitle = new GlyphLayout(font, strings.get(GAME_OVER));
        retry = new GlyphLayout(font, strings.get(RETRY));
        demo = new GlyphLayout(font, strings.get(DEMO));
        HudStyle.fit(font,pauseLabel,strings.get(PAUSE),.85f,70);
        HudStyle.wrap(font,help,strings.get(CONTROLS),helpScale,200);

        for (PickupType type : PickupType.values()) {
            font.setColor(type.r, type.g, type.b, 1);
            itemLabels[type.ordinal()] = new GlyphLayout(font, strings.pickupLabel(type));
        }
        font.setColor(1, 1, 1, 1);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        HudStyle.wrap(font,help,strings.get(debug ? DEBUG_CONTROLS : CONTROLS),helpScale,200);
    }
    public void homeConfirmation(boolean active) {confirmingHome=active;}

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        safeLeft=Gdx.graphics.getSafeInsetLeft()*width()/(float)width;
        safeTop=Gdx.graphics.getSafeInsetTop()*height()/(float)height;
        safeRight=Gdx.graphics.getSafeInsetRight()*width()/(float)width;
        safeBottom=Gdx.graphics.getSafeInsetBottom()*height()/(float)height;
    }

    public float width() { return viewport.getWorldWidth(); }
    public float height() { return viewport.getWorldHeight(); }
    @Override public float bottomInset(){return safeBottom;}
    public void unproject(int x, int y, Vector2 out) { out.set(x, y); viewport.unproject(out); }
    public void project(Vector2 point) { viewport.project(point); }

    public boolean inPauseButton(float x, float y) {
        return x >= width() - safeRight - 112 && x <= width() - safeRight - 16 && y >= height() - safeTop - 58 && y <= height() - safeTop - 12;
    }

    public boolean inResumeButton(float x, float y) {
        return Math.abs(x - width() / 2) <= 120 && y >= height() / 2 - 64 && y <= height() / 2;
    }

    public boolean inHomeButton(float x, float y) {
        return Math.abs(x - width() / 2) <= 120 && y >= height() / 2 - 140 && y <= height() / 2 - 76;
    }

    public boolean inAnimationButton(float x, float y) {
        return x >= width() - 208 && x <= width() - 16 && y >= 20 && y <= 62;
    }

    public boolean inDirectionButton(float x, float y) {
        return x >= width() - 208 && x <= width() - 16 && y >= 70 && y <= 112;
    }

    public int weaponOfferAt(float x, float y, int count) {
        return WeaponHud.offerAt(x, y, width(), height(), count);
    }

    public boolean weaponHomeAt(float x, float y) {
        return WeaponHud.homeAt(x, y, width(), height());
    }
    @Override public com.example.monstersurvivor.skill.SkillType skillAt(float x,float y) {return SkillHud.at(x,y,width(),height(),safeRight,safeBottom);}
    @Override public com.example.monstersurvivor.consumable.PotionType potionAt(float x,float y) {return PotionHud.at(x,y,width(),safeBottom);}
    public void potionPending(boolean value){potionPending=value;}
    public void potionFeedback(com.example.monstersurvivor.consumable.PotionType type,com.example.monstersurvivor.consumable.PotionSystem.Availability result){
        if(type==null)return;
        GameStrings.Key name=switch(type){
            case HEALTH->POTION_HEALTH;case SPEED->POTION_SPEED;case COOLDOWN->POTION_COOLDOWN;
        };
        GameStrings.Key description=switch(type){
            case HEALTH->POTION_HEALTH_DESC;case SPEED->POTION_SPEED_DESC;case COOLDOWN->POTION_COOLDOWN_DESC;
        };
        GameStrings.Key message=switch(result){
            case USABLE->POTION_SAVING;case APPLIED->POTION_APPLIED;case SAVE_FAILED->POTION_SAVE_FAILED;
            case DISABLED->POTION_MODE_DISABLED;case EMPTY->POTION_NO_INVENTORY;case HEALTH_FULL->POTION_HEALTH_FULL;
            case ALREADY_ACTIVE->POTION_ALREADY_ACTIVE;case NOT_RUNNING->POTION_NOT_RUNNING;
        };
        text.setLength(0);text.append(strings.get(name)).append(" — ");
        text.append(result==com.example.monstersurvivor.consumable.PotionSystem.Availability.USABLE
            || result==com.example.monstersurvivor.consumable.PotionSystem.Availability.APPLIED?strings.get(description):strings.get(message));
        HudStyle.wrap(font,potionNotice,text,.68f,390);potionNoticeTime=2.5f;
    }
    public float skillX(com.example.monstersurvivor.skill.SkillType type) {return SkillHud.centerX(type,width(),safeRight);}
    public float skillY(com.example.monstersurvivor.skill.SkillType type) {return SkillHud.centerY(type,height(),safeBottom);}

    public void render(SpriteBatch batch, GameInput input, boolean paused, float elapsed, float realDelta,
                       State state, Direction facing, Player player, int killCount, GameMode mode,
                       boolean gameOver, PickupSystem pickups, OrthographicCamera camera,
                       com.example.monstersurvivor.gameplay.GameSession session) {
        viewport.apply();
        weaponHud.prepare(font, session);
        potionHud.prepare(font,session);
        potionNoticeTime=Math.max(0,potionNoticeTime-Math.min(Math.max(0,realDelta),.5f));
        skillRewardHud.prepare(font,session);
        boolean skillsEnabled=!paused && session.phase()==com.example.monstersurvivor.gameplay.GameSession.Phase.RUNNING && player.alive();
        skillHud.prepare(font,session,skillsEnabled);
        boolean won=session.victory();
        bossVisible=session.bosses.primary()!=null;
        gameOver|=won;
        bossHud.prepare(font,session,camera,width(),height()-safeTop,safeRight);
        if(session.endless() && (endlessSession!=session.id || endlessBosses!=session.bosses.endlessKills() || endlessStage!=session.endlessStage())) {
            endlessSession=session.id;endlessBosses=session.bosses.endlessKills();endlessStage=session.endlessStage();text.setLength(0);
            text.append(strings.get(ENDLESS)).append(" / ").append(strings.get(session.config.difficulty==Difficulty.HARD?HARD:EASY))
                .append(" / ").append(strings.get(ENDLESS_STAGE)).append(' ').append(endlessStage)
                .append(" / ").append(strings.get(BOSS_KILLS)).append(' ').append(endlessBosses);
            endlessScale=HudStyle.fit(font,endlessInfo,text,.65f,220);
        }

        // Update HP text
        if (lastHp != player.hp() || lastMaxHp != player.maxHp()) {
            lastHp = player.hp();
            lastMaxHp = player.maxHp();
            text.setLength(0);
            text.append(strings.get(HP)).append(' ').append(lastHp).append('/').append(lastMaxHp);
            hpScale=HudStyle.fit(font,hp,text,1,230);
        }

        // Update Kills text
        if (lastKills != killCount) {
            lastKills = killCount;
            text.setLength(0);
            text.append(strings.get(KILLS)).append(' ').append(killCount);
            killsScale=HudStyle.fit(font,kills,text,.8f,76);
        }

        // Update Damage & Speed stats text
        if (lastDamage != player.stats.damage() || lastSpeed != player.stats.speed()) {
            lastDamage = player.stats.damage();
            lastSpeed = player.stats.speed();
            text.setLength(0);
            text.append(strings.get(DAMAGE_SHORT)).append(' ').append(lastDamage)
                .append("    ").append(strings.get(SPEED_SHORT)).append(' ').append(lastSpeed);
            statsScale=HudStyle.fit(font,stats,text,.85f,145);
        }
        if(lastRunCoins!=session.runCoins()) {
            lastRunCoins=session.runCoins();text.setLength(0);text.append(strings.format(COINS_TOTAL,lastRunCoins));
            coinsScale=HudStyle.fit(font,coins,text,.75f,190);
        }

        // Update Pickup Notice text
        if (pickups.noticeTime > 0 && (lastNotice != pickups.noticeSerial || !pickups.notice.contentEquals(noticeText))) {
            lastNotice = pickups.noticeSerial;
            noticeText = pickups.notice;
            font.setColor(pickups.noticeType.r, pickups.noticeType.g, pickups.noticeType.b, 1);
            notice.setText(font, noticeText);
            font.setColor(1, 1, 1, 1);
        }

        // Update Game Clock
        int second = (int)session.progress();
        if (second != lastSecond) {
            lastSecond = second;
            text.setLength(0);
            int minutes = second / 60, seconds = second % 60;
            if (minutes < 10) text.append('0');
            text.append(minutes).append(':');
            if (seconds < 10) text.append('0');
            text.append(seconds);
            timer.setText(font, text);

            // Sub-difficulty label
            text.setLength(0);
            text.append(strings.get(session.config.difficulty == Difficulty.HARD ? HARD : EASY));
            diffLabel.setText(font, text);
        }

        // Debug FPS & animation telemetry
        if(debug) {
        fpsCountdown -= realDelta;
        if (fpsCountdown <= 0) {
            fpsCountdown = 0.5f;
            text.setLength(0);
            text.append(strings.get(FPS)).append(Gdx.graphics.getFramesPerSecond());
            fps.setText(font, text);
        }
        if (state != lastState) {
            lastState = state;
            text.setLength(0);
            text.append(strings.get(ANIMATION)).append(state.name());
            animation.setText(font, text);
        }
        if (facing != lastDirection) {
            lastDirection = facing;
            text.setLength(0);
            text.append(strings.get(DIRECTION)).append(facing.name());
            direction.setText(font, text);
        }

        }

        // ==========================================
        // 1. SHAPES PASS (BACKGROUNDS, BARS, JOYSTICK)
        // ==========================================
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        float top=height()-safeTop,left=safeLeft;
        HudStyle.panel(shapes,left+12,top-98,256,86,.35f,.62f,.57f);
        float hpTroughX=left+22,hpTroughY=top-39,hpTroughW=234,hpTroughH=18;
        shapes.setColor(.19f,.045f,.07f,1);shapes.rect(hpTroughX,hpTroughY,hpTroughW,hpTroughH);
        float hpRatio=Math.max(0,Math.min(1,(float)player.hp()/Math.max(1,player.maxHp())));
        shapes.setColor(hpRatio>.35f?.65f:.9f,.14f,.22f,1);shapes.rect(hpTroughX,hpTroughY,hpTroughW*hpRatio,hpTroughH);
        shapes.setColor(1,.5f,.48f,.55f);shapes.rect(hpTroughX,hpTroughY+hpTroughH-2,hpTroughW*hpRatio,2);
        HudStyle.panel(shapes,width()/2-58,top-58,116,46,.86f,.68f,.3f);
        HudStyle.panel(shapes,width()-safeRight-112,top-58,96,46,paused?1:.86f,paused?.9f:.68f,.3f);
        shapes.setColor(.95f,.81f,.42f,1);shapes.rect(width()-safeRight-103,top-42,3,16);shapes.rect(width()-safeRight-97,top-42,3,16);
        int statusRow=0;
        for(var type:com.example.monstersurvivor.enemy.skill.PlayerStatuses.TYPES)if(player.statuses.active(type)){
            HudStyle.panel(shapes,left+12,top-110-18*statusRow++,Math.max(64,Math.min(96,(width()-480)/2-28-safeLeft)),16,.5f,.55f,.75f);
        }

        // --- Floating Joystick Visuals ---
        if (input.joystickActive()) {
            float cx = input.centerX(), cy = input.centerY();
            float kx = cx + input.knobX(), ky = cy + input.knobY();

            // Outer soft glow aura
            shapes.setColor(0.10f, 0.25f, 0.18f, 0.35f);
            shapes.circle(cx, cy, GameInput.JOY_RADIUS + 6, 48);

            // Translucent inner base
            shapes.setColor(0.04f, 0.10f, 0.08f, 0.50f);
            shapes.circle(cx, cy, GameInput.JOY_RADIUS, 48);

            // Boundary guide rim
            shapes.setColor(0.45f, 0.85f, 0.65f, 0.65f);
            HudStyle.ring(shapes,cx,cy,GameInput.JOY_RADIUS,1.5f);

            // 4 cardinal notches
            shapes.setColor(0.65f, 0.95f, 0.75f, 0.70f);
            shapes.rect(cx - 2, cy + GameInput.JOY_RADIUS - 8, 4, 8);
            shapes.rect(cx - 2, cy - GameInput.JOY_RADIUS, 4, 8);
            shapes.rect(cx + GameInput.JOY_RADIUS - 8, cy - 2, 8, 4);
            shapes.rect(cx - GameInput.JOY_RADIUS, cy - 2, 8, 4);

            // Directional drag vector guide line
            if (input.knobX() != 0 || input.knobY() != 0) {
                shapes.setColor(0.55f, 0.90f, 0.70f, 0.40f);
                shapes.rectLine(cx, cy, kx, ky, 2.5f);
            }

            // Crystal Thumb Knob
            // Outer rim
            shapes.setColor(0.85f, 0.95f, 0.80f, 0.92f);
            shapes.circle(kx, ky, 27, 36);
            // Main knob body
            shapes.setColor(0.14f, 0.32f, 0.24f, 0.96f);
            shapes.circle(kx, ky, 25, 36);
            // Inner core glow
            shapes.setColor(0.24f, 0.50f, 0.38f, 0.96f);
            shapes.circle(kx, ky, 14, 24);
            // Specular shine pip
            shapes.setColor(1.0f, 1.0f, 1.0f, 0.85f);
            shapes.circle(kx - 6, ky + 6, 3.5f, 12);
        } else {
            // Idle state: subtle, low-opacity hint ring at bottom-left
            shapes.setColor(0.65f, 0.82f, 0.65f, 0.16f);
            HudStyle.ring(shapes,GameInput.JOY_X,GameInput.JOY_Y,GameInput.JOY_RADIUS,1.5f);
            shapes.setColor(0.65f, 0.82f, 0.65f, 0.08f);
            shapes.circle(GameInput.JOY_X, GameInput.JOY_Y, GameInput.JOY_RADIUS * 0.5f, 32);
            shapes.circle(GameInput.JOY_X, GameInput.JOY_Y, 4, 16);

            // Subtle cardinal ticks
            shapes.setColor(0.65f, 0.82f, 0.65f, 0.18f);
            shapes.rect(GameInput.JOY_X - 1.5f, GameInput.JOY_Y + GameInput.JOY_RADIUS - 6, 3, 6);
            shapes.rect(GameInput.JOY_X - 1.5f, GameInput.JOY_Y - GameInput.JOY_RADIUS, 3, 6);
            shapes.rect(GameInput.JOY_X + GameInput.JOY_RADIUS - 6, GameInput.JOY_Y - 1.5f, 6, 3);
            shapes.rect(GameInput.JOY_X - GameInput.JOY_RADIUS, GameInput.JOY_Y - 1.5f, 6, 3);
        }

        // Preview buttons in debug mode
        if (debug) {
            shapes.setColor(0.035f, 0.07f, 0.075f, 0.85f);
            shapes.rect(width() - 208, 20, 192, 42);
            shapes.rect(width() - 208, 70, 192, 42);
        }

        // Pickup indicators (off-screen mystery boxes)
        if (!paused && !gameOver) {
            for (int i = 0; i < pickups.size(); i++) {
                Pickup item = pickups.get(i);
                if (!item.type.box || !locate(item, camera)) continue;
                shapes.setColor(0.035f, 0.07f, 0.075f, 0.92f);
                shapes.circle(indicator.x, indicator.y, 14, 16);
                shapes.setColor(item.type.r, item.type.g, item.type.b, 1.0f);
                float ax = indicator.x + indicator.dx * 21, ay = indicator.y + indicator.dy * 21;
                float bx = indicator.x + indicator.dx * 12, by = indicator.y + indicator.dy * 12;
                shapes.triangle(ax, ay, bx - indicator.dy * 5, by + indicator.dx * 5,
                                bx + indicator.dy * 5, by - indicator.dx * 5);
            }
            if (pickups.noticeTime > 0) {
                shapes.setColor(0.035f, 0.07f, 0.075f, 0.90f);
                shapes.rect(width() / 2 - notice.width / 2 - 14, 104+safeBottom, notice.width + 28, 30);
                shapes.setColor(pickups.noticeType.r, pickups.noticeType.g, pickups.noticeType.b, 0.90f);
                shapes.rect(width() / 2 - notice.width / 2 - 14, 132+safeBottom, notice.width + 28, 2);
                shapes.rect(width() / 2 - notice.width / 2 - 14, 104+safeBottom, notice.width + 28, 2);
            }
        }

        if(!paused && !gameOver && !session.choosingReward())bossHud.shapes(shapes,width(),height()-safeTop);
        skillHud.shapes(shapes,session,skillsEnabled,width(),height(),safeRight,safeBottom);
        // Pause & Game Over dialog shapes
        if (paused || gameOver) {
            shapes.setColor(0, 0, 0, 0.70f);
            shapes.rect(0, 0, width(), height());

            HudStyle.panel(shapes,width()/2-180,height()/2-160,360,won?310:260,
                    gameOver&&!won?.85f:.9f,gameOver&&!won?.25f:.72f,gameOver&&!won?.32f:.3f);
            if(won){
                shapes.setColor(1,.82f,.3f,1);
                for(int i=0;i<5;i++){float x=width()/2-50+i*25,y=height()/2+134;
                    shapes.triangle(x,y+4,x-3,y,x+3,y);shapes.triangle(x,y-4,x-3,y,x+3,y);}
            }

            // Action Button 1 (Resume / Retry)
            HudStyle.panel(shapes,width()/2-120,height()/2-64,240,64,.9f,.72f,.3f);

            // Action Button 2 (Home)
            HudStyle.panel(shapes,width()/2-120,height()/2-140,240,64,.4f,.6f,.65f);
        }

        // Weapon slots shapes (3 slots near bottom)
        if (!paused && !gameOver) {
            weaponHud.slotShapes(shapes, session, width(),safeBottom);
            if(session.phase()==com.example.monstersurvivor.gameplay.GameSession.Phase.RUNNING)
                potionHud.shapes(shapes,session,skillsEnabled,width(),safeBottom,potionPending);
        }
        if(potionNoticeTime>0 && !paused && !gameOver){
            HudStyle.panel(shapes,width()/2-204,top-152,408,54,.72f,.58f,.25f);
            shapes.setColor(.94f,.76f,.35f,.9f);shapes.rect(width()/2-198,top-103,396,2);
        }

        shapes.end();

        // ==========================================
        // 2. TEXT & SPRITE BATCH PASS
        // ==========================================
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // HP & Kills
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        HudStyle.draw(batch,font,hp,hpScale,left+26,top-25);

        font.setColor(1.0f, 0.88f, 0.35f, 1.0f);
        HudStyle.draw(batch,font,kills,killsScale,left+180,top-58);

        // Stats (Damage & Speed)
        font.setColor(0.85f, 0.95f, 0.90f, 1.0f);
        HudStyle.draw(batch,font,stats,statsScale,left+26,top-58);
        font.setColor(1,.84f,.38f,1);
        if(coinIcon!=null)batch.draw(coinIcon,left+25,top-94,12,12);
        HudStyle.draw(batch,font,coins,coinsScale,left+42,top-81);
        font.setColor(1,1,1,1);
        statusRow=0;
        for(var type:com.example.monstersurvivor.enemy.skill.PlayerStatuses.TYPES)if(player.statuses.active(type)){
            float y=top-110-18*statusRow++;
            if(statusIcons[type.ordinal()]!=null)batch.draw(statusIcons[type.ordinal()],left+15,y+2,12,12);
            HudStyle.draw(batch,font,statusLabels[type.ordinal()],.65f,left+31,y+12);
        }

        if (debug) {
            font.setColor(0.9f, 0.9f, 0.5f, 1.0f);
            font.draw(batch, fps, 26, height() - safeTop - (statusRow>0?166:102));
        }

        // Pickup Indicators text
        if (!paused && !gameOver) {
            for (int i = 0; i < pickups.size(); i++) {
                Pickup item = pickups.get(i);
                if (!item.type.box || !locate(item, camera)) continue;
                GlyphLayout label = itemLabels[item.type.ordinal()];
                font.draw(batch, label, indicator.x - label.width / 2, indicator.y + label.height / 2);
            }
            if (pickups.noticeTime > 0) {
                font.draw(batch, notice, width() / 2 - notice.width / 2, 126+safeBottom);
            }
            font.setColor(1, 1, 1, 1);
        }

        // Top-Center Timer & Mode/Difficulty
        font.setColor(1.0f, 0.92f, 0.55f, 1.0f);
        font.draw(batch, timer, width() / 2 - timer.width / 2, top - 25);

        if(mode==GameMode.ENDLESS){
            font.setColor(1,.72f,.3f,1);
            HudStyle.draw(batch,font,endlessInfo,endlessScale,width()/2-endlessInfo.width/2,top-44);
        } else if (mode == GameMode.DEMO) {
            font.setColor(1.0f, 0.70f, 0.20f, 1.0f);
            font.draw(batch, demo, width() / 2 - demo.width / 2, top - 44);
        } else {
            font.setColor(0.65f, 0.85f, 0.70f, 1.0f);
            font.draw(batch, diffLabel, width() / 2 - diffLabel.width / 2, top - 44);
        }

        // Pause Button text
        font.setColor(1.0f, 0.90f, 0.50f, 1.0f);
        HudStyle.draw(batch,font,pauseLabel,.85f,width()-safeRight-58-pauseLabel.width/2,top-29);

        // Controls hint (Bottom-Left)
        font.setColor(0.60f, 0.75f, 0.68f, 0.80f);
        HudStyle.draw(batch,font,help,helpScale,safeLeft+16,safeBottom+18);

        // Weapon slots text
        if (!paused && !gameOver) {
            weaponHud.slotText(batch, font, width(),safeBottom);
            if(session.phase()==com.example.monstersurvivor.gameplay.GameSession.Phase.RUNNING)
                potionHud.text(batch,font,session,width(),safeBottom,skillsEnabled,potionPending);
        }

        if(potionNoticeTime>0 && !paused && !gameOver){
            font.setColor(1,.91f,.68f,1);
            HudStyle.draw(batch,font,potionNotice,.68f,width()/2-potionNotice.width*.34f,top-115);
        }

        if (debug) {
            font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            font.draw(batch, animation, width() - 194, 47);
            font.draw(batch, direction, width() - 194, 97);
        }

        skillHud.text(batch,font,skillsEnabled,width(),height(),safeRight,safeBottom);
        if(!paused && !gameOver && !session.choosingReward())bossHud.text(batch,font,width(),height()-safeTop);
        // Pause & Game Over dialog text
        if (paused || gameOver) {
            GlyphLayout heading = gameOver ? gameOverTitle : confirmingHome?homeConfirmTitle:title;
            GlyphLayout action = gameOver ? retry : confirmingHome?cancel:resume;
            GlyphLayout homeAction=confirmingHome?confirm:home;

            font.setColor(gameOver ? 0.95f : 1.0f, gameOver ? 0.25f : 0.88f, gameOver ? 0.25f : 0.35f, 1.0f);
            if(won)bossHud.victoryText(batch,font,width(),height());
            else font.draw(batch, heading, width() / 2 - heading.width / 2, height() / 2 + 70);

            if (gameOver) {
                if(resultSession!=session.id) {
                resultSession=session.id;
                text.setLength(0);
                text.append(strings.get(SURVIVED)).append(' ').append((int) elapsed / 60).append(':');
                if ((int) elapsed % 60 < 10) text.append('0');
                text.append((int) elapsed % 60).append("    ").append(strings.get(KILLS)).append(' ').append(killCount);
                if(session.endless())text.append("    ").append(strings.get(BOSS_KILLS)).append(' ').append(session.result().endlessBossKills());
                resultScale=HudStyle.fit(font,result,text,.85f,328);
                }

                font.setColor(1.0f, 0.92f, 0.65f, 1.0f);
                HudStyle.draw(batch,font,result,resultScale,width()/2-result.width/2,height()/2+(won?90:35));
            }

            font.setColor(1.0f, 0.92f, 0.60f, 1.0f);
            font.draw(batch, action, width() / 2 - action.width / 2, height() / 2 - 25);

            font.setColor(0.85f, 0.92f, 0.88f, 1.0f);
            font.draw(batch, homeAction, width() / 2 - homeAction.width / 2, height() / 2 - 101);
        }

        font.setColor(Color.WHITE);
        batch.end();

        // Render modal if choosing weapon
        if(session.choosingChest() && !paused && !gameOver)skillRewardHud.modal(batch,shapes,font,session,input,width(),height());

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private boolean locate(Pickup item, OrthographicCamera camera) {
        if(!indicator.locate(item.x, item.y, camera.position.x, camera.position.y,
                camera.viewportWidth, camera.viewportHeight, width(), height(), debug))return false;
        indicator.x=Math.max(safeLeft+28,Math.min(width()-safeRight-28,indicator.x));
        indicator.y=Math.min(height()-safeTop-108,indicator.y);
        if(bossVisible && indicator.y>height()-safeTop-198 && Math.abs(indicator.x-width()/2)<268)
            indicator.x=width()/2+(indicator.dx>=0?278:-278);
        if(indicator.y<156+safeBottom && Math.abs(indicator.x-width()/2)<Math.max(240,notice.width/2+30))
            indicator.y=166+safeBottom;
        for(var type:SKILL_TYPES){
            float dx=indicator.x-skillX(type),dy=indicator.y-skillY(type),radius=SkillHud.radius(type)+28;
            if(dx*dx+dy*dy<radius*radius)indicator.x=skillX(com.example.monstersurvivor.skill.SkillType.DASH)-SkillHud.radius(com.example.monstersurvivor.skill.SkillType.DASH)-30;
        }
        return true;
    }

    @Override
    public void dispose() {
        shapes.dispose();
        font.dispose();
    }
}
