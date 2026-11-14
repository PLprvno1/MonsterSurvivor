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
import com.example.monstersurvivor.config.GameMode;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.localization.*;
import static com.example.monstersurvivor.localization.GameStrings.Key.*;

/** Temporary libGDX gameplay HUD. Native Android menus remain owned by the native UI layer. */
public final class GameHud implements Disposable,GameInput.Surface {
    private final ExtendViewport viewport = new ExtendViewport(800, 480);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont(Gdx.files.internal("game/fonts/game-ui.fnt"));
    private final GameStrings strings;
    private final GlyphLayout hp, help, title, resume, pauseLabel;
    private final GlyphLayout timer = new GlyphLayout(), fps = new GlyphLayout();
    private final GlyphLayout animation = new GlyphLayout(), direction = new GlyphLayout();
    private final StringBuilder text = new StringBuilder(64);
    private int lastSecond = -1;
    private float fpsCountdown;
    private State lastState;
    private Direction lastDirection;
    private boolean debug;
    private int lastHp=-1,lastMaxHp=-1,lastKills=-1;
    private int lastDamage=-1,lastSpeed=-1,lastNotice=-1;
    private final GlyphLayout stats=new GlyphLayout(),notice=new GlyphLayout();
    private final GlyphLayout[] itemLabels=new GlyphLayout[PickupType.values().length];
    private final PickupIndicator indicator=new PickupIndicator();
    private final WeaponHud weaponHud=new WeaponHud();
    private final GlyphLayout kills=new GlyphLayout(), home, gameOverTitle, retry, result=new GlyphLayout(), demo;
    public GameHud() {
        this(GameStrings.of(GameLanguage.VI));
    }
    public GameHud(GameStrings strings) {
        this.strings=strings;
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        hp = new GlyphLayout(font, strings.get(HP)+" 10/10");
        help = new GlyphLayout(font, strings.get(CONTROLS));
        title = new GlyphLayout(font, strings.get(PAUSED)); resume = new GlyphLayout(font, strings.get(RESUME));
        pauseLabel = new GlyphLayout(font, strings.get(PAUSE));
        home=new GlyphLayout(font,strings.get(HOME));gameOverTitle=new GlyphLayout(font,strings.get(GAME_OVER));retry=new GlyphLayout(font,strings.get(RETRY));
        demo=new GlyphLayout(font,strings.get(DEMO));
        for(PickupType type:PickupType.values()) {
            font.setColor(type.r,type.g,type.b,1);itemLabels[type.ordinal()]=new GlyphLayout(font,strings.pickupLabel(type));
        }
        font.setColor(1,1,1,1);
    }
    public void setDebug(boolean debug) {
        this.debug=debug;
        help.setText(font,strings.get(debug?DEBUG_CONTROLS:CONTROLS));
    }
    public void resize(int width, int height) { viewport.update(width, height, true); }
    public float width() { return viewport.getWorldWidth(); }
    public float height() { return viewport.getWorldHeight(); }
    public void unproject(int x, int y, Vector2 out) { out.set(x,y); viewport.unproject(out); }
    public void project(Vector2 point) { viewport.project(point); }
    public boolean inPauseButton(float x, float y) { return x >= width()-112 && x <= width()-16 && y >= height()-58 && y <= height()-12; }
    public boolean inResumeButton(float x, float y) { return Math.abs(x-width()/2) <= 120 && y >= height()/2-64 && y <= height()/2; }
    public boolean inHomeButton(float x,float y) { return Math.abs(x-width()/2)<=120 && y>=height()/2-140 && y<=height()/2-76; }
    public boolean inAnimationButton(float x, float y) { return x >= width()-208 && x <= width()-16 && y >= 20 && y <= 62; }
    public boolean inDirectionButton(float x, float y) { return x >= width()-208 && x <= width()-16 && y >= 70 && y <= 112; }
    public int weaponOfferAt(float x,float y,int count) {return WeaponHud.offerAt(x,y,width(),height(),count);}
    public boolean weaponHomeAt(float x,float y) {return WeaponHud.homeAt(x,y,width(),height());}
    public void render(SpriteBatch batch, GameInput input, boolean paused, float elapsed, float realDelta, State state, Direction facing,
                       Player player,int killCount,GameMode mode,boolean gameOver,PickupSystem pickups,OrthographicCamera camera,
                       com.example.monstersurvivor.gameplay.GameSession session) {
        viewport.apply();
        weaponHud.prepare(font,session);
        if(lastHp!=player.hp() || lastMaxHp!=player.maxHp()) {
            lastHp=player.hp();lastMaxHp=player.maxHp();text.setLength(0);text.append(strings.get(HP)).append(' ').append(lastHp).append('/').append(lastMaxHp);hp.setText(font,text);
        }
        if(lastKills!=killCount) {lastKills=killCount;text.setLength(0);text.append(strings.get(KILLS)).append(' ').append(killCount);kills.setText(font,text);}
        if(lastDamage!=player.stats.damage() || lastSpeed!=player.stats.speed()) {
            lastDamage=player.stats.damage();lastSpeed=player.stats.speed();text.setLength(0);
            text.append(strings.get(DAMAGE_SHORT)).append(' ').append(lastDamage).append("    ").append(strings.get(SPEED_SHORT)).append(' ').append(lastSpeed);stats.setText(font,text);
        }
        // Retry creates a fresh system whose notice serial may equal the previous match's.
        if(pickups.noticeTime>0 && (lastNotice!=pickups.noticeSerial || !pickups.notice.contentEquals(noticeText))) {
            lastNotice=pickups.noticeSerial;noticeText=pickups.notice;
            font.setColor(pickups.noticeType.r,pickups.noticeType.g,pickups.noticeType.b,1);
            notice.setText(font,noticeText);font.setColor(1,1,1,1);
        }
        int second = (int)(elapsed*mode.progressScale);
        if (second != lastSecond) {
            lastSecond = second; text.setLength(0);
            int minutes = second / 60, seconds = second % 60;
            if (minutes < 10) text.append('0'); text.append(minutes).append(':');
            if (seconds < 10) text.append('0'); text.append(seconds);
            timer.setText(font, text);
        }
        fpsCountdown -= realDelta;
        if (fpsCountdown <= 0) { fpsCountdown = .5f; text.setLength(0); text.append(strings.get(FPS)).append(Gdx.graphics.getFramesPerSecond()); fps.setText(font, text); }
        if (state != lastState) { lastState = state; text.setLength(0); text.append(strings.get(ANIMATION)).append(state.name()); animation.setText(font, text); }
        if (facing != lastDirection) { lastDirection = facing; text.setLength(0); text.append(strings.get(DIRECTION)).append(facing.name()); direction.setText(font, text); }
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(.035f,.07f,.075f,.88f); shapes.rect(12,height()-84,265,72);
        shapes.rect(width()/2-50,height()-58,100,46);
        shapes.rect(width()-112,height()-58,96,46);
        if(input.joystickActive()) {
            shapes.setColor(.08f,.13f,.12f,.55f); shapes.circle(input.centerX(),input.centerY(),GameInput.JOY_RADIUS+6,48);
            shapes.setColor(.72f,.85f,.67f,.5f); shapes.circle(input.centerX(),input.centerY(),GameInput.JOY_RADIUS,48);
            shapes.setColor(.12f,.24f,.20f,.85f); shapes.circle(input.centerX()+input.knobX(),input.centerY()+input.knobY(),27,32);
        } else {
            shapes.setColor(.72f,.85f,.67f,.12f);shapes.circle(GameInput.JOY_X,GameInput.JOY_Y,GameInput.JOY_RADIUS,48);
        }
        if(debug) {shapes.setColor(.035f,.07f,.075f,.85f); shapes.rect(width()-208,20,192,42); shapes.rect(width()-208,70,192,42);}
        if(!paused && !gameOver) {
            for(int i=0;i<pickups.size();i++) {
                Pickup item=pickups.get(i);
                if(!item.type.box || !locate(item,camera))continue;
                shapes.setColor(.035f,.07f,.075f,.9f);shapes.circle(indicator.x,indicator.y,13,12);
                shapes.setColor(item.type.r,item.type.g,item.type.b,1);
                float ax=indicator.x+indicator.dx*21,ay=indicator.y+indicator.dy*21;
                float bx=indicator.x+indicator.dx*12,by=indicator.y+indicator.dy*12;
                shapes.triangle(ax,ay,bx-indicator.dy*5,by+indicator.dx*5,bx+indicator.dy*5,by-indicator.dx*5);
            }
            if(pickups.noticeTime>0) {
                shapes.setColor(.035f,.07f,.075f,.85f);shapes.rect(width()/2-notice.width/2-12,height()-123,notice.width+24,30);
            }
        }
        if (paused || gameOver) {
            shapes.setColor(0,0,0,.65f); shapes.rect(0,0,width(),height());
            shapes.setColor(.12f,.28f,.22f,1); shapes.rect(width()/2-120,height()/2-64,240,64);
            shapes.rect(width()/2-120,height()/2-140,240,64);
        }
        // Slots sit above the footer, between joystick and debug controls.
        if(!paused && !gameOver)weaponHud.slotShapes(shapes,session,width());
        shapes.end();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.draw(batch,hp,26,height()-28);font.draw(batch,kills,142,height()-28);
        font.draw(batch,stats,26,height()-57);
        if(debug)font.draw(batch,fps,26,height()-102);
        if(!paused && !gameOver) {
            for(int i=0;i<pickups.size();i++) {
                Pickup item=pickups.get(i);if(!item.type.box || !locate(item,camera))continue;
                GlyphLayout label=itemLabels[item.type.ordinal()];
                font.draw(batch,label,indicator.x-label.width/2,indicator.y+label.height/2);
            }
            if(pickups.noticeTime>0) {
                font.draw(batch,notice,width()/2-notice.width/2,height()-102);
            }
            font.setColor(1,1,1,1);
        }
        if(mode==GameMode.DEMO)font.draw(batch,demo,width()/2-demo.width/2,height()-76);
        font.draw(batch,timer,width()/2-timer.width/2,height()-28);
        font.draw(batch,pauseLabel,width()-64-pauseLabel.width/2,height()-28);
        font.draw(batch,help,16,18);
        if(!paused && !gameOver)weaponHud.slotText(batch,font,width());
        if(debug) {font.draw(batch,animation,width()-194,47); font.draw(batch,direction,width()-194,97);}
        if (paused || gameOver) {
            GlyphLayout heading=gameOver?gameOverTitle:title, action=gameOver?retry:resume;
            font.draw(batch,heading,width()/2-heading.width/2,height()/2+70);
            if(gameOver) {
                text.setLength(0);text.append(strings.get(SURVIVED)).append(' ').append((int)elapsed/60).append(':');
                if((int)elapsed%60<10)text.append('0');text.append((int)elapsed%60).append("    ").append(strings.get(KILLS)).append(' ').append(killCount);
                result.setText(font,text);font.draw(batch,result,width()/2-result.width/2,height()/2+35);
            }
            font.draw(batch,action,width()/2-action.width/2,height()/2-25);
            font.draw(batch,home,width()/2-home.width/2,height()/2-101);
        }
        batch.end();
        if(session.choosingWeapon() && !paused && !gameOver)weaponHud.modal(batch,shapes,font,session,input,width(),height());
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
    private String noticeText="";
    private boolean locate(Pickup item,OrthographicCamera camera) {
        return indicator.locate(item.x,item.y,camera.position.x,camera.position.y,camera.viewportWidth,camera.viewportHeight,width(),height(),debug);
    }
    @Override public void dispose() { shapes.dispose(); font.dispose(); }
}
