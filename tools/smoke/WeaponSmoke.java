import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.pickup.PickupType;
import com.example.monstersurvivor.ui.WeaponHud;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.screen.GameScreen;

/** Actual GL/input/modal/lifecycle integration. Never reads back player or monster artwork. */
public final class WeaponSmoke extends ApplicationAdapter {
    private static boolean failed;
    private GameScreen screen;
    private int checks,home;
    private void check(boolean ok,String text) {if(!ok)throw new AssertionError(text);checks++;}
    private void make() {
        if(screen!=null)screen.dispose();
        var c=new GameConfig.Builder().fixedSeed(42).mode(GameMode.DEMO).population(0,60).enemy(EnemyType.SLIME,100,1,.01f)
            .stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).build();
        screen=new GameScreen(c,()->home++);screen.show();screen.resize(960,540);screen.render(0);
    }
    private void tick(int frames,float dt) {for(int i=0;i<frames;i++)screen.render(dt);}
    private void open() {
        var p=screen.getPlayer();check(screen.getSession().pickups.spawn(PickupType.WEAPON_BOX,p.x,p.y,0)!=null,"Place real Weapon Box");
        tick(2,.05f);check(screen.weaponSelection(),"Real GameScreen enters modal once");
    }
    private int[] coordinates(float x,float y) {
        var point=new Vector2(x,y);screen.getHud().project(point);return new int[]{Math.round(point.x),Gdx.graphics.getHeight()-Math.round(point.y)};
    }
    private void touch(int index,boolean release) {
        int[] at=coordinates(screen.getHud().width()/2,WeaponHud.cardY(screen.getHud().height(),index)+38);
        screen.getControls().touchDown(at[0],at[1],2,0);if(release)screen.getControls().touchUp(at[0],at[1],2,0);
    }
    @Override public void create() {
        try {
            for(int option=0;option<3;option++) {
                make();var s=screen.getSession();var p=screen.getPlayer();var enemy=s.enemies.spawn(EnemyType.SLIME,p.x+112,p.y);
                screen.getControls().keyDown(Input.Keys.D);open();float x=p.x,time=s.elapsed();long token=s.selection.token();
                tick(20,.1f);check(p.x==x && s.elapsed()==time && s.selection.token()==token,"Modal freezes actual screen and held movement");
                touch(option,false);check(screen.getControls().selectedOffer()==option && screen.weaponSelection(),"Choice highlights on press without early commit");
                int[] at=coordinates(screen.getHud().width()/2,WeaponHud.cardY(screen.getHud().height(),option)+38);
                screen.getControls().touchUp(at[0],at[1],2,0);
                WeaponType selected=WeaponType.values()[option+1];
                check(!screen.weaponSelection() && s.weapons.loadout.get(selected)!=null && s.pickups.collectedItems==1,"Touch release equips "+selected);
                tick(20,.05f);check(p.x==x && s.weapons.loadout.get(selected).activations>0,"No held joystick/key leakage; selected weapon fights automatically");
                check(enemy.hp()<100,"Selected weapon causes actual damage: "+selected);
                check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"Modal, slots and weapon VFX render without GL error");
            }
            make();open();var s=screen.getSession();long token=s.selection.token();
            touch(0,false);screen.getControls().touchCancelled(0,0,2,0);
            check(screen.weaponSelection() && s.pickups.collectedItems==0,"Cancelled modal touch never commits a reward");
            screen.getControls().keyDown(Input.Keys.ESCAPE);check(screen.isPaused() && screen.weaponSelection(),"Back pauses over modal; choice remains pending");
            check(!screen.chooseWeapon(0,token),"Paused modal cannot accept a choice");
            screen.pause();screen.resume();tick(2,2);check(screen.isPaused() && screen.weaponSelection(),"Lifecycle preserves pending selection and explicit Resume");
            screen.togglePause();screen.render(3);touch(0,true);check(s.weapons.loadout.get(WeaponType.BOW)!=null,"Resume returns to the same selectable modal");
            open();int count=s.pickups.collectedItems;touch(0,true);
            check(screen.weaponSelection() && s.pickups.collectedItems==count,"Immediate second tap is debounced even when another box opens");
            long oldToken=s.selection.token();screen.home();check(home==1 && s.selection.pendingId()==-1 && !s.chooseWeapon(0,oldToken),"Home clears pending modal and rejects stale session callbacks");
            make();s=screen.getSession();open();long previousMatchToken=s.selection.token();screen.chooseWeapon(0,previousMatchToken);s.weapons.loadout.grant(WeaponType.FIREBALL);
            var p=screen.getPlayer();s.enemies.spawn(EnemyType.SLIME,p.x+120,p.y);tick(4,.05f);
            check(s.weapons.projectiles.size()>0,"Real scene has active projectiles before death");
            p.damage(1000);tick(35,.05f);check(screen.gameOver() && s.weapons.projectiles.size()==0,"Death clears active combat and reaches Game Over");
            var assets=screen.getAssets();screen.retry();screen.render(2);
            check(screen.getSession()!=s && s.phase()==GameSession.Phase.ENDED && screen.getSession().weapons.loadout.size()==1
                && screen.getSession().weapons.loadout.slot(0).level()==1 && screen.getSession().selection.pendingId()==-1,"Retry clears loadout/projectiles/modal and ends old session");
            check(screen.getAssets()==assets && assets.textureCount()==19,"Retry retains all shared textures including two CC0 atlases and one VFX texel");
            open();check(!screen.chooseWeapon(0,previousMatchToken) && screen.weaponSelection(),"Old UI token cannot apply to the first modal after Retry");
            check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"No GL errors after cleanup/Retry");
            System.out.println("TASK 03B DESKTOP GL PASSED: "+checks+" checks");Gdx.app.exit();
        }catch(Throwable error) {failed=true;error.printStackTrace();Gdx.app.exit();}
    }
    @Override public void dispose() {if(screen!=null)screen.dispose();}
    public static void main(String[] args) {
        var config=new Lwjgl3ApplicationConfiguration();config.setWindowedMode(960,540);config.disableAudio(true);config.setForegroundFPS(60);
        config.setTitle("Monster Survivor Task 03B verification");new Lwjgl3Application(new WeaponSmoke(),config);if(failed)System.exit(1);
    }
}
