import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.example.monstersurvivor.assets.GameAssets.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.render.WorldSprite;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.world.ForestMap;
import java.nio.file.Path;

/** Actual GL/game screen regression, no repeated desktop performance benchmark. */
public final class Task021Smoke extends ApplicationAdapter {
    private static boolean failed;
    private GameScreen screen;
    private int checks,homeCalls;
    private void check(boolean ok,String label) {if(!ok)throw new AssertionError(label);checks++;}
    private void tick(int frames,float dt) {for(int i=0;i<frames;i++)screen.render(dt);}
    private void make(GameConfig config) {
        if(screen!=null)screen.dispose();screen=new GameScreen(config,()->homeCalls++);screen.show();screen.resize(960,540);screen.render(0);
    }
    private void checkProps(ForestMap map) {
        for(float[] rect:new float[][]{{0,0,426,240},{1174,0,1600,240},{0,1360,426,1600},{1174,1360,1600,1600},{600,680,1026,920},{0,0,1600,1600}}) {
            Array<WorldSprite> indexed=new Array<>();map.enqueueProps(indexed,rect[0],rect[1],rect[2],rect[3]);int fullCount=0;
            for(int i=0;i<map.props.size;i++) {WorldSprite prop=map.props.get(i);if(prop.visible(rect[0],rect[1],rect[2],rect[3])) {
                fullCount++;check(indexed.contains(prop,true),"Prop chunk lookup never drops a visible prop");
            }}
            check(fullCount==indexed.size,"Chunk culling matches full culling exactly");
        }
    }
    /** Only CC0 Kenney terrain/props are exported; no player/monster pixels or HUD. */
    private void overview() {
        FrameBuffer buffer=new FrameBuffer(Pixmap.Format.RGBA8888,1600,1600,false);SpriteBatch batch=new SpriteBatch();
        OrthographicCamera camera=new OrthographicCamera();camera.setToOrtho(false,1600,1600);
        Array<WorldSprite> props=new Array<>(screen.getMap().props);props.sort(WorldSprite.BACK_TO_FRONT);
        try {buffer.begin();Gdx.gl.glClearColor(0,0,0,1);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.setProjectionMatrix(camera.combined);batch.begin();screen.getMap().renderGround(batch,camera);
            for(int i=0;i<props.size;i++)props.get(i).draw(batch);batch.end();
            Pixmap image=Pixmap.createFromFrameBuffer(0,0,1600,1600);
            com.badlogic.gdx.graphics.PixmapIO.writePNG(Gdx.files.local("../core/build/reports/task02_1/map-42.png"),image,-1,true);image.dispose();
        } finally {buffer.end();batch.dispose();buffer.dispose();Gdx.gl.glViewport(0,0,960,540);}
    }
    @Override public void create() {
        try {
            make(new GameConfig.Builder().fixedSeed(42).population(0,40).enemy(EnemyType.SLIME,1,1,.01f).build());
            check(screen.getMap().seed==42 && screen.getMap().walkableTiles>=8500,"Fixed seed controls real generated map");
            checkProps(screen.getMap());overview();
            Player p=screen.getPlayer();Enemy rear=screen.getSession().enemies.spawn(EnemyType.SLIME,p.x-28,p.y);
            screen.getControls().keyDown(Input.Keys.D);tick(12,1f/60);screen.getControls().keyUp(Input.Keys.D);
            check(p.movementDirection==Direction.RIGHT && p.attackDirection==Direction.LEFT && p.x>823,"Real screen runs and attacks behind");
            check(!rear.alive() && screen.getSession().combat.kills==1,"Easy slime dies in one actual auto slash");
            var assets=screen.getAssets();var map=screen.getMap();var session=screen.getSession();float elapsed=screen.getElapsed(),clock=p.animationTime;
            screen.togglePause();tick(10,.1f);
            check(screen.getMap()==map && screen.getElapsed()==elapsed && p.animationTime==clock,"Pause freezes map/combat/time");
            screen.pause();screen.resume();screen.render(2);check(screen.isPaused(),"Lifecycle remains paused");
            screen.togglePause();screen.render(5);check(screen.getElapsed()==elapsed,"Resume rejects stale delta");
            p.damage(100);tick(5,.1f);check(!screen.gameOver(),"Death clip is not shortened with sword playback speed");
            tick(12,.1f);check(screen.gameOver(),"Full Death reaches Game Over");
            screen.retry();screen.render(5);
            check(screen.getMap()!=map && screen.getMap().grid!=map.grid && screen.getMap().seed==42,"Fixed-seed Retry creates fresh map/navigation reproducibly");
            check(screen.getSession()!=session && screen.getSession().enemies.size()==0 && screen.getElapsed()==0 && screen.getPlayer().hp()==10,"Retry clears paths, HP, timer and entities");
            check(screen.getAssets()==assets && assets.textureCount()==16,"Retry never reloads textures (including pickups and shared weapon texel)");
            screen.togglePause();screen.home();check(homeCalls==1,"Home bridge remains available");
            make(new GameConfig.Builder().population(0,40).build());long seed=screen.getMap().seed;map=screen.getMap();
            screen.getPlayer().damage(100);tick(18,.1f);screen.retry();screen.render(0);
            check(screen.getMap().seed!=seed && screen.getMap()!=map,"Normal Retry chooses a new seed/layout");
            checkProps(screen.getMap());
            for(int side=0;side<4;side++) {
                p=screen.getPlayer();p.x=side==0?24:side==1?1576:808;p.y=side==2?24:side==3?1576:808;
                int key=side==0?Input.Keys.A:side==1?Input.Keys.D:side==2?Input.Keys.S:Input.Keys.W;
                screen.getControls().keyDown(key);tick(12,1f/30);screen.getControls().keyUp(key);
                check(screen.getMap().grid.canOccupy(p.x,p.y,4,3),"Generated-map edge collision");
                var camera=screen.getCamera();check(camera.position.x>=camera.viewportWidth/2 && camera.position.x<=1600-camera.viewportWidth/2
                    && camera.position.y>=camera.viewportHeight/2 && camera.position.y<=1600-camera.viewportHeight/2,"SCREEN camera stays within generated map");
            }
            check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"Generated map and new attack timing render without GL errors");
            String message="TASK 02.1 DESKTOP GL PASSED: "+checks+" checks\n";
            java.nio.file.Files.writeString(Path.of("../core/build/reports/task02_1/desktop-smoke.txt"),message);
            System.out.print(message);Gdx.app.exit();
        }catch(Throwable error) {failed=true;error.printStackTrace();Gdx.app.exit();}
    }
    @Override public void dispose() {if(screen!=null)screen.dispose();}
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config=new Lwjgl3ApplicationConfiguration();config.setWindowedMode(960,540);config.disableAudio(true);config.setForegroundFPS(60);
        config.setTitle("Monster Survivor Task 02.1 verification");new Lwjgl3Application(new Task021Smoke(),config);if(failed)System.exit(1);
    }
}
