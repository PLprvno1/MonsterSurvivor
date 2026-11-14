import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.GL20;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.screen.GameScreen;

/** Actual GameScreen/OpenGL, input, pause/lifecycle, cleanup and Retry integration. No art readback. */
public final class PickupSmoke extends ApplicationAdapter {
    private static boolean failed;
    private GameScreen screen;
    private int checks,home;
    private void check(boolean ok,String label) {if(!ok)throw new AssertionError(label);checks++;}
    private void tick(int frames,float dt) {for(int i=0;i<frames;i++)screen.render(dt);}
    private void collect(PickupType type) {
        var p=screen.getPlayer();check(screen.getSession().pickups.spawn(type,p.x,p.y,2)!=null,"Place functional pickup");screen.render(.1f);
    }
    @Override public void create() {
        try {
            var config=new GameConfig.Builder().fixedSeed(42).mode(GameMode.DEMO).population(0,40)
                .stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).build();
            screen=new GameScreen(config,()->home++);screen.show();screen.resize(960,540);screen.render(0);
            var assets=screen.getAssets();var map=screen.getMap();var session=screen.getSession();var p=screen.getPlayer();
            check(assets.textureCount()==19,"Pickup atlas and one shared weapon texel loaded once");
            for(PickupType type:PickupType.values())check(assets.region(type.region).getRegionWidth()==16,"Kenney item regions available");
            collect(PickupType.DAMAGE_BOX);check(p.stats.damage()>=2 && p.stats.damage()<=4,"Damage effect runs through real screen");
            collect(PickupType.HEALTH_BOX);check(p.hp()==12 && p.maxHp()==12,"Full health gets useful max upgrade");
            collect(PickupType.SPEED_BOX);check(p.stats.speed()==11,"Speed effect runs through screen");
            float x=p.x;screen.getControls().keyDown(Input.Keys.D);tick(10,.05f);screen.getControls().keyUp(Input.Keys.D);
            check(Math.abs(p.x-x-44)<.01f,"Actual input moves 88 units/s at Speed 11");
            session.pickups.spawn(PickupType.SPEED_BOX,p.x+40,p.y,0);screen.render(.1f);var box=session.pickups.get(0);
            float age=box.age,elapsed=screen.getElapsed();int dmg=p.stats.damage();
            screen.togglePause();tick(20,.1f);
            check(box.age==age && screen.getElapsed()==elapsed && p.stats.damage()==dmg,"Pause freezes pickups and preserves stats");
            screen.pause();screen.resume();screen.render(3);check(screen.isPaused() && box.age==age,"Lifecycle requires explicit Resume");
            screen.togglePause();screen.render(3);check(box.age==age,"Resume drops stale delta");
            screen.getControls().keyDown(Input.Keys.D);tick(8,.05f);screen.getControls().keyUp(Input.Keys.D);
            check(p.stats.speed()==12 && session.pickups.collectedItems==4,"Real movement picks up each box once");
            // Place bounded visible and off-camera items over the real generated navigation.
            var nav=session.enemies.navigation;
            for(int cell=0;cell<screen.getMap().width*screen.getMap().height && session.pickups.size()<26;cell++) {
                float px=nav.x(cell),py=nav.y(cell);
                if(Math.hypot(px-p.x,py-p.y)<40 || Math.hypot(px-p.x,py-p.y)>280)continue;
                session.pickups.spawn(session.pickups.boxes()<6?PickupType.DAMAGE_BOX:PickupType.HEALTH_DROP,px,py,2);
            }
            check(session.pickups.boxes()==6 && session.pickups.healthDrops()==20,"Render load holds 6 boxes and 20 drops");
            tick(20,.016f);check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"Items, y-sort, blink, feedback and arrows render without GL errors");
            for(int i=0;i<session.pickups.size();i++)session.pickups.get(i).age=session.pickups.get(i).lifetime-.01f;
            screen.render(.05f);check(session.pickups.size()==0,"Expired items disappear from live/render collections");
            p.damage(Integer.MAX_VALUE);tick(20,.1f);check(screen.gameOver(),"Death animation still reaches Game Over");
            screen.retry();screen.render(5);
            check(screen.getSession()!=session && screen.getMap()!=map && screen.getMap().seed==42,"Retry owns fresh map/session with reproducible seed");
            p=screen.getPlayer();check(p.hp()==10 && p.maxHp()==10 && p.stats.damage()==1 && p.stats.speed()==10,"Retry restores all baseline stats");
            check(screen.getSession().pickups.size()==0 && screen.getSession().pickups.effectCount()==0 && screen.getSession().pickups.noticeTime==0,"Retry clears live items, feedback and effects");
            check(screen.getAssets()==assets && assets.textureCount()==19,"Retry reuses textures and atlas");
            screen.togglePause();screen.home();check(home==1,"Home callback preserved");
            check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"No GL errors after Retry/Home");
            System.out.println("TASK 03A DESKTOP GL PASSED: "+checks+" checks");Gdx.app.exit();
        }catch(Throwable error) {failed=true;error.printStackTrace();Gdx.app.exit();}
    }
    @Override public void dispose() {if(screen!=null)screen.dispose();}
    public static void main(String[] args) {
        var config=new Lwjgl3ApplicationConfiguration();config.setWindowedMode(960,540);config.disableAudio(true);config.setForegroundFPS(60);
        config.setTitle("Monster Survivor Task 03A verification");new Lwjgl3Application(new PickupSmoke(),config);if(failed)System.exit(1);
    }
}
