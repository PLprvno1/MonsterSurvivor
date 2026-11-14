import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.GL20;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.assets.GameAssets.State;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.screen.*;
import com.example.monstersurvivor.weapon.*;

/** Actual GameScreen integration for every definition. No actor artwork readback. */
public final class EnemyRosterSmoke extends ApplicationAdapter {
    static boolean failed;GameScreen screen;int checks,home;
    void check(boolean b,String s) {if(!b)throw new AssertionError(s);checks++;}
    void tick(int n,float dt) {for(int i=0;i<n;i++)screen.render(dt);}
    public void create() {
        try {
            var b=new GameConfig.Builder().mode(GameMode.DEMO).fixedSeed(42).population(0,60).playerHp(10000)
                .stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).sword(1,1,1,.15f);
            for(var d:EnemyRegistry.all())b.enemy(d,6,1,.001f);
            screen=new GameScreen(b.build(),()->home++);screen.show();screen.resize(960,540);screen.render(0);
            var s=screen.getSession();var p=s.player;var assets=screen.getAssets();long bytes=assets.textureBytes();int kills=0;
            for(var d:EnemyRegistry.all()) {
                s.enemies.clear();s.weapons.clearTransient();s.weapons.loadout.reset();
                var e=s.enemies.spawn(d,p.x+112,p.y+16,d.unlock);check(e!=null && e.sprites==assets.sprite(d.spriteId),"real actor uses correct sprites "+d.id);
                e.damage(1);screen.render(.01f);check(e.state()==State.HURT,"hurt drawn by GameScreen "+d.id);
                for(int i=0;i<3;i++) {s.weapons.loadout.grant(WeaponType.FIREBALL);s.weapons.loadout.grant(WeaponType.LIGHTNING);}
                for(int frame=0;frame<65 && e.alive();frame++)screen.render(.05f);
                check(!e.alive() && e.state()==State.DEATH && s.kills==++kills,"real multiweapon combat and single kill "+d.id);
                check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"no missing texture/render errors "+d.id);
            }
            s.enemies.clear();for(int i=0;i<s.pickups.size();i++)s.pickups.get(i).age=100;screen.render(.1f);
            check(s.pickups.spawn(PickupType.WEAPON_BOX,p.x,p.y,0)!=null,"weapon transaction still available");tick(2,.05f);
            check(s.choosingWeapon(),"modal opens");float clock=s.progress();screen.togglePause();screen.pause();screen.resume();tick(3,2);
            check(screen.isPaused() && s.progress()==clock && s.choosingWeapon(),"pause/lifecycle freezes pending selection");
            screen.togglePause();screen.render(0);check(screen.chooseWeapon(0,s.selection.token()),"selection resumes and commits");
            for(int retry=0;retry<3;retry++) {
                var old=screen.getSession();var e=old.enemies.spawn(EnemyRegistry.get("mc_moss_golem"),old.player.x+112,old.player.y,420);e.path.add(123);
                old.player.damage(Integer.MAX_VALUE);tick(30,.1f);check(screen.gameOver(),"player death flow");screen.retry();screen.render(0);
                check(old.phase()==GameSession.Phase.ENDED && old.enemies.size()==0 && e.path.size==0,"Retry releases old enemies/paths");
                check(screen.getAssets()==assets && assets.textureCount()==19 && assets.textureBytes()==bytes,"Retry never accumulates textures");
                check(screen.getSession().enemies.size()==0 && screen.getSession().weapons.loadout.size()==1 && screen.getSession().night.strength()==0,"fresh match roster/weapon/night state");
            }
            check(screen.getMap().width==200 && screen.getMap().height==200 && Math.abs(screen.getCamera().viewportHeight-240/1.15f)<.01,"map/zoom retained");
            screen.home();check(home==1 && Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"Home and final GL cleanup");
            System.out.println("ROSTER GAME SCREEN PASSED: "+checks+" checks, 82 actual gameplay species, 3 Retry cycles, textures=19 RGBA="+bytes);
        }catch(Throwable t) {failed=true;t.printStackTrace();}Gdx.app.exit();
    }
    public void dispose() {if(screen!=null)screen.dispose();}
    public static void main(String[] args) {var c=new Lwjgl3ApplicationConfiguration();c.setWindowedMode(960,540);c.disableAudio(true);c.setTitle("Roster gameplay verification");new Lwjgl3Application(new EnemyRosterSmoke(),c);if(failed)System.exit(1);}
}
