import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.GL20;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.screen.*;
import com.example.monstersurvivor.weapon.*;
/** Actual GameScreen, no art readbacks. Finite simulation steps, not a performance benchmark. */
public final class WorldNightSmoke extends ApplicationAdapter {
    static boolean failed;GameScreen screen;int checks;
    void check(boolean b,String text) {if(!b)throw new AssertionError(text);checks++;}
    public void create() {
        try {
            screen=new GameScreen(new GameConfig.Builder().mode(GameMode.DEMO).population(0,60)
                .stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build(),()->{});screen.show();screen.resize(960,540);screen.render(0);
            var s=screen.getSession();var p=s.player;long seed=screen.getMap().seed;
            check(screen.getMap().width==200 && p.x==1608 && p.y==1608,"expanded normal match and center");
            for(int i=0;i<450;i++)screen.render(.1f);
            check(s.night.active() && Math.abs(s.elapsed()-45)<.01,"real Demo clock triggers night");
            screen.togglePause();float strength=s.night.strength(),time=s.progress();for(int i=0;i<10;i++)screen.render(1);
            check(s.night.strength()==strength && s.progress()==time,"screen pause freezes fade");screen.togglePause();screen.render(0);
            for(int i=0;i<31;i++)screen.render(.1f);check(s.night.strength()==1,"full night renderer active");
            for(int i=0;i<5;i++) {s.weapons.loadout.grant(WeaponType.FIREBALL);s.weapons.loadout.grant(WeaponType.LIGHTNING);}
            s.enemies.spawn(com.example.monstersurvivor.entity.EnemyType.SLIME,p.x+128,p.y);screen.render(.01f);
            check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"night and emissive weapons GLES-compatible GL20");
            for(float[] at:new float[][]{{24,24},{3176,24},{24,3176},{3176,3176}}) {
                p.x=at[0];p.y=at[1];screen.render(0);var camera=screen.getCamera();
                check(camera.position.x>=camera.viewportWidth/2 && camera.position.x<=3200-camera.viewportWidth/2,"edge camera X");
                check(camera.position.y>=camera.viewportHeight/2 && camera.position.y<=3200-camera.viewportHeight/2,"edge camera Y");
            }
            p.damage(100000);for(int i=0;i<30;i++)screen.render(.1f);check(screen.gameOver(),"death flow at night");
            screen.retry();screen.render(0);var fresh=screen.getSession();
            check(screen.getMap().seed!=seed && fresh.night.strength()==0 && fresh.progress()==0 && fresh.weapons.loadout.size()==1,"Retry changes seed and resets night/loadout/clocks");
            check(fresh.pickups.size()==0 && fresh.selection.pendingId()==-1 && fresh.weapons.projectiles.size()==0,"Retry clears pickups/modal/projectiles");
            check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"expanded world and Retry GL");System.out.println("WORLD NIGHT SCREEN PASSED: "+checks);
        }catch(Throwable t) {failed=true;t.printStackTrace();}Gdx.app.exit();
    }
    public void dispose() {if(screen!=null)screen.dispose();}
    public static void main(String[] args) {var c=new Lwjgl3ApplicationConfiguration();c.setWindowedMode(960,540);c.disableAudio(true);c.setTitle("World night verification");new Lwjgl3Application(new WorldNightSmoke(),c);if(failed)System.exit(1);}
}
