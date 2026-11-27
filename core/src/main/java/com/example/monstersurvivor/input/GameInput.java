package com.example.monstersurvivor.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.skill.SkillType;
import com.example.monstersurvivor.consumable.PotionType;

/** Pointer ownership allows joystick + pause/preview touches without stealing movement. */
public final class GameInput extends InputAdapter {
    /** HUD units, bottom-left origin after unproject; presentation may implement this surface. */
    public interface Surface {
        float width(); float height();
        default float bottomInset(){return 0;}
        void unproject(int x,int y,Vector2 out);
        boolean inPauseButton(float x,float y); boolean inResumeButton(float x,float y); boolean inHomeButton(float x,float y);
        boolean inAnimationButton(float x,float y); boolean inDirectionButton(float x,float y);
        int weaponOfferAt(float x,float y,int count); boolean weaponHomeAt(float x,float y);
        default SkillType skillAt(float x,float y) {return null;}
        default PotionType potionAt(float x,float y){return null;}
    }
    public interface Commands {
        boolean isPaused();
        void togglePause();
        void cycleAnimation();
        void cycleDirection();
        default boolean debugEnabled() { return false; }
        default boolean gameplayStopped() { return false; }
        default boolean gameOver() { return false; }
        default boolean victory() { return false; }
        default void retry() { }
        default void home() { }
        default boolean weaponSelection() {return false;}
        default long weaponToken() {return -1;}
        default int weaponOfferCount() {return 0;}
        default boolean chooseWeapon(int index,long token) {return false;}
        default boolean activateSkill(SkillType type,float axisX,float axisY) {return false;}
        default void usePotion(PotionType type) { }
    }
    public static final float JOY_X = 108, JOY_Y = 108, JOY_RADIUS = 66;
    public static final float TOUCH_WIDTH_FRACTION=.44f, TOP_RESERVED=96, BOTTOM_RESERVED=28, DEADZONE=.12f;
    private final boolean[] keys = new boolean[256];
    private final Vector2 touch = new Vector2();
    private final Surface hud;
    private final Commands commands;
    private int pointer = -1;
    private int modalPointer=-1,selectedOffer=-1;
    private final boolean[] heldPointers=new boolean[32];
    private int dashPointer=-1,ultimatePointer=-1;
    private boolean leftShiftHeld,rightShiftHeld,spaceHeld;
    private long modalToken,selectionBlockedUntil;
    private float joyX, joyY, axisX, axisY;
    private float centerX=JOY_X,centerY=JOY_Y;
    public GameInput(Surface hud, Commands commands) { this.hud = hud; this.commands = commands; }
    private void releaseJoystick() {
        pointer=-1;joyX=joyY=axisX=axisY=0;centerX=JOY_X;centerY=JOY_Y;
    }
    private void resetMovement() {
        java.util.Arrays.fill(keys,false);releaseJoystick();
    }
    public void reset() {
        resetMovement();
        java.util.Arrays.fill(heldPointers,false);dashPointer=ultimatePointer=-1;
        modalPointer=-1;selectedOffer=-1;
    }
    public int selectedOffer() {return selectedOffer;}
    private boolean select(int index,long token) {
        long now=com.badlogic.gdx.utils.TimeUtils.nanoTime();
        if(now<selectionBlockedUntil)return false;
        boolean applied=commands.chooseWeapon(index,token);
        if(applied) {reset();selectionBlockedUntil=now+250_000_000L;}return applied;
    }
    public void sampleAxes() {
        if (commands.isPaused() || commands.gameplayStopped() || commands.weaponSelection()) { resetMovement();dashPointer=ultimatePointer=-1;return; }
        if (pointer >= 0) {
            float len2 = joyX * joyX + joyY * joyY;
            axisX = len2 < DEADZONE*DEADZONE ? 0 : joyX;
            axisY = len2 < DEADZONE*DEADZONE ? 0 : joyY;
        } else {
            axisX = (keys[Input.Keys.D] || keys[Input.Keys.RIGHT] ? 1 : 0) - (keys[Input.Keys.A] || keys[Input.Keys.LEFT] ? 1 : 0);
            axisY = (keys[Input.Keys.W] || keys[Input.Keys.UP] ? 1 : 0) - (keys[Input.Keys.S] || keys[Input.Keys.DOWN] ? 1 : 0);
            if (axisX != 0 && axisY != 0) { axisX *= .70710678f; axisY *= .70710678f; }
        }
    }
    public float axisX() { return axisX; }
    public float axisY() { return axisY; }
    public float knobX() { return joyX * JOY_RADIUS; }
    public float knobY() { return joyY * JOY_RADIUS; }
    public int activePointer() {return pointer;}
    public boolean joystickActive() {return pointer>=0;}
    public float centerX() {return centerX;}
    public float centerY() {return centerY;}
    public boolean inJoystickRegion(float x,float y) {
        return x>=0 && x<=hud.width()*TOUCH_WIDTH_FRACTION && y>=BOTTOM_RESERVED+hud.bottomInset() && y<=hud.height()-TOP_RESERVED;
    }
    @Override public boolean keyDown(int keycode) {
        // Independent latches survive movement reset. OS key-repeat cannot turn a held key into new presses.
        if(keycode==Input.Keys.SHIFT_LEFT || keycode==Input.Keys.SHIFT_RIGHT || keycode==Input.Keys.SPACE) {
            boolean held=keycode==Input.Keys.SPACE?spaceHeld:keycode==Input.Keys.SHIFT_LEFT?leftShiftHeld:rightShiftHeld;
            if(keycode==Input.Keys.SPACE)spaceHeld=true;else if(keycode==Input.Keys.SHIFT_LEFT)leftShiftHeld=true;else rightShiftHeld=true;
            if(!held && !commands.isPaused() && !commands.gameplayStopped() && !commands.weaponSelection()) {
                sampleAxes();commands.activateSkill(keycode==Input.Keys.SPACE?SkillType.ULTIMATE:SkillType.DASH,axisX,axisY);
            }
            return true;
        }
        if(commands.gameOver() || commands.victory()) {
            if(keycode==Input.Keys.ENTER || keycode==Input.Keys.R)commands.retry();
            else if(keycode==Input.Keys.ESCAPE || keycode==Input.Keys.BACK || keycode==Input.Keys.H)commands.home();
            return true;
        }
        if(commands.weaponSelection() && !commands.isPaused()) {
            if(keycode==Input.Keys.BACK || keycode==Input.Keys.ESCAPE || keycode==Input.Keys.P)commands.togglePause();
            else if(keycode==Input.Keys.H)commands.home();
            else if(keycode>=Input.Keys.NUM_1 && keycode<=Input.Keys.NUM_3)select(keycode-Input.Keys.NUM_1,commands.weaponToken());
            return true;
        }
        if(commands.isPaused() && keycode==Input.Keys.H) {commands.home();return true;}
        if (keycode == Input.Keys.P || keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
            reset();commands.togglePause(); return true;
        }
        if(commands.isPaused() || commands.gameplayStopped())return true;
        if (keycode == Input.Keys.F2) { if(commands.debugEnabled())commands.cycleAnimation(); return true; }
        if (keycode == Input.Keys.F3) { if(commands.debugEnabled())commands.cycleDirection(); return true; }
        if (keycode >= 0 && keycode < keys.length) keys[keycode] = true;
        return true;
    }
    @Override public boolean keyUp(int keycode) {
        if(keycode==Input.Keys.SHIFT_LEFT)leftShiftHeld=false;
        else if(keycode==Input.Keys.SHIFT_RIGHT)rightShiftHeld=false;
        else if(keycode==Input.Keys.SPACE)spaceHeld=false;
        if (keycode >= 0 && keycode < keys.length) keys[keycode] = false;
        return true;
    }
    @Override public boolean touchDown(int x, int y, int pointerId, int button) {
        if(pointerId<0 || pointerId>=heldPointers.length)return false;
        if(heldPointers[pointerId])return true;
        heldPointers[pointerId]=true;
        hud.unproject(x, y, touch);
        if(commands.gameOver() || commands.isPaused() || commands.gameplayStopped() || commands.weaponSelection())resetMovement();
        if(commands.gameOver() || commands.victory()) {
            if(hud.inResumeButton(touch.x,touch.y))commands.retry();
            else if(hud.inHomeButton(touch.x,touch.y))commands.home();
            return true;
        }
        if (commands.isPaused()) {
            if (hud.inResumeButton(touch.x, touch.y) || hud.inPauseButton(touch.x,touch.y)) commands.togglePause();
            else if(hud.inHomeButton(touch.x,touch.y))commands.home();
            return true;
        }
        if(commands.weaponSelection()) {
            if(hud.weaponHomeAt(touch.x,touch.y)) {commands.home();return true;}
            if(modalPointer<0 && com.badlogic.gdx.utils.TimeUtils.nanoTime()>=selectionBlockedUntil) {
                modalPointer=pointerId;modalToken=commands.weaponToken();selectedOffer=hud.weaponOfferAt(touch.x,touch.y,commands.weaponOfferCount());
            }
            return true;
        }
        if (hud.inPauseButton(touch.x, touch.y)) { reset();commands.togglePause(); return true; }
        if(commands.gameplayStopped())return true;
        SkillType skill=hud.skillAt(touch.x,touch.y);
        if(skill!=null) {
            if(skill==SkillType.DASH && dashPointer<0)dashPointer=pointerId;
            else if(skill==SkillType.ULTIMATE && ultimatePointer<0)ultimatePointer=pointerId;
            sampleAxes();commands.activateSkill(skill,axisX,axisY);return true;
        }
        PotionType potion=hud.potionAt(touch.x,touch.y);
        if(potion!=null) {commands.usePotion(potion);return true;}
        if (commands.debugEnabled() && hud.inAnimationButton(touch.x, touch.y)) { commands.cycleAnimation(); return true; }
        if (commands.debugEnabled() && hud.inDirectionButton(touch.x, touch.y)) { commands.cycleDirection(); return true; }
        if (pointer < 0 && inJoystickRegion(touch.x,touch.y)) {
            pointer=pointerId;centerX=touch.x;centerY=touch.y;joyX=joyY=axisX=axisY=0;return true;
        }
        return false;
    }
    @Override public boolean touchDragged(int x, int y, int pointerId) {
        if(pointerId==dashPointer || pointerId==ultimatePointer)return true;
        if(commands.weaponSelection() || commands.isPaused() || commands.gameplayStopped()) {resetMovement();return true;}
        if (pointerId != pointer) return false;
        hud.unproject(x, y, touch); updateJoystick(touch.x, touch.y); return true;
    }
    private void updateJoystick(float x, float y) {
        joyX = (x - centerX) / JOY_RADIUS; joyY = (y - centerY) / JOY_RADIUS;
        float len2 = joyX * joyX + joyY * joyY;
        if (len2 > 1) { float inv = 1f / (float) Math.sqrt(len2); joyX *= inv; joyY *= inv; }
    }
    @Override public boolean touchUp(int x, int y, int pointerId, int button) {
        if(pointerId>=0 && pointerId<heldPointers.length)heldPointers[pointerId]=false;
        if(pointerId==dashPointer) {dashPointer=-1;return true;}
        if(pointerId==ultimatePointer) {ultimatePointer=-1;return true;}
        if(pointerId==modalPointer) {
            int chosen=selectedOffer;long token=modalToken;modalPointer=-1;selectedOffer=-1;hud.unproject(x,y,touch);
            if(commands.weaponSelection() && !commands.isPaused() && chosen>=0 && chosen==hud.weaponOfferAt(touch.x,touch.y,commands.weaponOfferCount()))select(chosen,token);
            return true;
        }
        if(commands.weaponSelection())return true;
        if (pointerId != pointer) return false;
        releaseJoystick();return true;
    }
    @Override public boolean touchCancelled(int x, int y, int pointerId, int button) {
        if(pointerId>=0 && pointerId<heldPointers.length)heldPointers[pointerId]=false;
        if(pointerId==dashPointer) {dashPointer=-1;return true;}
        if(pointerId==ultimatePointer) {ultimatePointer=-1;return true;}
        if(pointerId==modalPointer) {modalPointer=-1;selectedOffer=-1;return true;}
        if(pointerId==pointer)releaseJoystick();return true;
    }
}
