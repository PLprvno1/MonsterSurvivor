import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.assets.GameAssets.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.input.GameInput;
import com.example.monstersurvivor.render.WorldSprite;
import com.example.monstersurvivor.screen.GameScreen;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Real OpenGL screen/input/lifecycle verification. Screenshots remain local, never sent to an AI model. */
public final class CombatSmokeLauncher extends ApplicationAdapter {
    private static boolean failed;
    private static final Path OUT=Path.of("../core/build/reports/task02");
    private final StringBuilder log=new StringBuilder();
    private GameScreen screen;
    private int phase,checks,homeCalls,stage,frames;
    private long renderNanos,lastFrame;
    private final double[] frameTimes=new double[240];
    private final List<String> measurements=new ArrayList<>();
    private final int[] populations={10,30,60};
    private void check(boolean ok,String label) {if(!ok)throw new AssertionError(label);checks++;}
    private void tick(int count,float dt) {for(int i=0;i<count;i++)screen.render(dt);}
    private void screen(GameConfig config) {
        if(screen!=null)screen.dispose();screen=new GameScreen(config,()->homeCalls++);screen.show();screen.resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());screen.render(0);
    }
    private void touch(float x,float y) {
        Vector2 p=new Vector2(x,y);screen.getHud().project(p);
        screen.getControls().touchDown(Math.round(p.x),Gdx.graphics.getHeight()-Math.round(p.y),7,0);
        screen.getControls().touchUp(Math.round(p.x),Gdx.graphics.getHeight()-Math.round(p.y),7,0);
    }
    private void behavioralChecks() {
        screen(new GameConfig.Builder().population(0,60).enemy(EnemyType.SLIME,2,1,.01f).build());
        check(!screen.debugEnabled(),"Main match disables animation preview");
        Player p=screen.getPlayer();Direction facing=p.direction;
        screen.getControls().keyDown(Input.Keys.F2);screen.getControls().keyDown(Input.Keys.F3);screen.render(.01f);
        check(p.state()==State.IDLE && p.direction==facing,"F2/F3 do not change gameplay");
        for(String id:new String[]{"player","tiny_slime","tiny_bat","wild_boar"}) {
            SpriteSet set=screen.getAssets().sprite(id);
            for(Direction d:Direction.values())for(State state:new State[]{State.IDLE,State.WALK,State.ATTACK,State.HURT,State.DEATH}) {
                check(set.has(state,d),"Required gameplay clip exists "+id+state+d);
                Clip clip=set.clip(state,d);
                for(var frame:clip.animation.getKeyFrames()) {
                    WorldSprite sprite=new WorldSprite(frame,808,808,set.feetX,set.height-set.feetY,set.displayScale);
                    check(Float.isFinite(sprite.drawX()) && Float.isFinite(sprite.drawY()),"Frame uses valid unchanged pivot");
                }
                if(state!=State.IDLE && state!=State.WALK)check(!clip.loop,"Action clip is one-shot");
            }
        }
        check(screen.getAssets().textureCount()==16,"Selected textures including pickups and shared weapon texel loaded once");
        Enemy e=screen.getSession().enemies.spawn(EnemyType.SLIME,p.x+22,p.y);
        float x=p.x,y=p.y;tick(36,1f/60);
        check(e.hp()==1 && e.state()==State.HURT,"Real screen auto sword damages and plays enemy Hurt");
        check(p.state()==State.ATTACK && p.direction==Direction.RIGHT && p.x==x && p.y==y,"Warrior uses stationary Attack and target facing");
        float time=screen.getElapsed(),enemyX=e.x,cooldown=p.cooldown(),anim=p.animationTime;
        touch(screen.getHud().width()-64,screen.getHud().height()-32);tick(20,.1f);
        check(screen.isPaused() && screen.getElapsed()==time && e.x==enemyX && p.cooldown()==cooldown && p.animationTime==anim,"Touch Pause freezes all gameplay");
        touch(screen.getHud().width()/2,screen.getHud().height()/2-32);screen.render(5);
        check(!screen.isPaused() && screen.getElapsed()==time,"Touch Resume discards stalled delta");
        screen.pause();screen.resume();screen.render(3);check(screen.isPaused() && screen.getElapsed()==time,"Lifecycle return waits for Resume");
        touch(screen.getHud().width()/2,screen.getHud().height()/2-108);check(homeCalls==1,"Pause Home invokes platform bridge");
        screen.togglePause();screen.render(3);screen.render(.1f);check(screen.getElapsed()>time,"Lifecycle Resume continues normally");
        check(p.damage(1),"Player accepts damage");screen.render(.1f);
        check(p.state()==State.HURT && !p.damage(1),"Player Hurt and invulnerability in real renderer");
        tick(50,1f/60);p.damage(100);
        screen.render(.1f);check(!screen.gameOver() && screen.getSession().phase()==GameSession.Phase.DYING,"Death animation precedes result screen");
        screen.pause();screen.resume();screen.togglePause();screen.render(8); // Death must remain resumable after Android Home.
        check(!screen.isPaused(),"Lifecycle can resume during player Death");
        tick(16,.1f);check(screen.gameOver() && p.hp()==0,"Full Death reaches Game Over");
        time=screen.getElapsed();screen.render(.1f);check(screen.getElapsed()==time,"Result freezes survived time");
        GameConfig config=screen.getSession().config;var assets=screen.getAssets();
        touch(screen.getHud().width()/2,screen.getHud().height()/2-32);screen.render(5);
        check(!screen.gameOver() && screen.getPlayer().hp()==10 && screen.getElapsed()==0 && screen.getSession().enemies.size()==0,"Retry creates a clean session");
        check(screen.getAssets()==assets && screen.getSession().config==config,"Retry preserves assets and mode tuning");
        // Joystick and camera retain the foundation's movement/pixel behavior.
        p=screen.getPlayer();x=p.x;screen.getControls().keyDown(Input.Keys.D);tick(60,1f/60);screen.getControls().keyUp(Input.Keys.D);
        check(Math.abs(p.x-x-80)<.1f,"Gameplay keyboard speed remains 80 units/s");
        check(Math.abs(screen.getCamera().position.x-p.x)<1,"Established pixel camera follows movement");
        screen.resize(3200,100);screen.render(0);check(screen.getCamera().viewportWidth<=screen.getMap().pixelWidth() && screen.getCamera().viewportHeight<=screen.getMap().pixelHeight(),"Extreme viewport respects map extent");
        screen.resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        // Default spawning runs through the actual screen, not only the model.
        screen(new GameConfig.Builder().playerHp(10000).build());tick(120,.1f);
        check(screen.getSession().spawner.successfulSpawns>=7,"Actual screen spawns the initial wave");
        check(screen.getSession().enemies.size()>=5 && screen.getSession().enemies.size()<=10,"Initial wave remains within target density");
        check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"Combat rendering has no GL error");
        log.append("OPENGL BEHAVIOR PASSED: ").append(checks).append(" checks\n");
    }
    private void beginStress() {
        int population=populations[stage];
        screen(new GameConfig.Builder().population(0,population).playerHp(100000).sword(1,1,3.1f,.45f).build());
        var nav=screen.getSession().enemies.navigation;
        int spawned=0;boolean[] used=new boolean[10000];
        // Alternate open and obstructed routes close to the visible forest, with all three sets.
        for(int actor=0;actor<population;actor++) {
            EnemyType type=EnemyType.values()[actor%3];boolean blocked=(actor&1)==1;boolean placed=false;
            for(int row=39;row<=61 && !placed;row++)for(int col=34;col<=66 && !placed;col++) {
                int cell=row*100+col;
                if(used[cell] || !nav.connected(cell,5050,type.flying))continue;
                boolean clear=nav.collision.clearLine(nav.x(cell),nav.y(cell),808,808,5,3,type.flying);
                if(clear==blocked)continue;
                if(screen.getSession().enemies.spawn(type,nav.x(cell),nav.y(cell))!=null) {used[cell]=true;placed=true;spawned++;}
            }
        }
        check(spawned==population,"Stress population created "+population);
        frames=0;renderNanos=0;lastFrame=0;
    }
    private void stressFrame() {
        long start=System.nanoTime();screen.render(Gdx.graphics.getDeltaTime());long end=System.nanoTime();
        if(frames>=60) {renderNanos+=end-start;frameTimes[frames-60]=lastFrame==0?0:(start-lastFrame)/1e6;}
        lastFrame=start;frames++;
        check(screen.getSession().enemies.searchesThisFrame<=2,"Stress A* budget");
        if(frames==300) {
            double[] sorted=frameTimes.clone();Arrays.sort(sorted);double sum=0;for(double t:frameTimes)sum+=t;
            int count=populations[stage];
            String json=String.format(Locale.ROOT,"{\"enemies\":%d,\"frames\":240,\"fps\":%.3f,\"p95CallbackMs\":%.3f,\"p99CallbackMs\":%.3f,\"meanRenderCpuDriverMs\":%.3f,\"aStarSearches\":%d}",
                count,240000/sum,sorted[227],sorted[237],renderNanos/1e6/240,screen.getSession().enemies.totalSearches);
            measurements.add(json);log.append("DESKTOP STRESS ").append(json).append('\n');
            check(screen.getSession().enemies.size()==count && Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"Stable active population and GL "+count);
            check(screen.getSession().enemies.totalSearches>0,"Desktop stress includes actual A* work "+count);
            stage++;
            if(stage<populations.length)beginStress();else finish();
        }
    }
    private void finish() {
        try {
            Files.createDirectories(OUT);Files.writeString(OUT.resolve("desktop-combat.txt"),log+"TOTAL CHECKS "+checks+"\n");
            Files.writeString(OUT.resolve("desktop-performance.json"),"{\"platform\":\"Desktop LWJGL3 960x540, VSync, 60 FPS cap\",\"renderer\":\""+Gdx.gl.glGetString(GL20.GL_RENDERER)+"\",\"warmupFramesPerStage\":60,\"measurements\":["+String.join(",",measurements)+"]}\n");
        }catch(Exception error){throw new RuntimeException(error);}
        System.out.print(log);Gdx.app.exit();phase=2;
    }
    @Override public void render() {
        try {
            if(phase==0) {behavioralChecks();phase=1;beginStress();}
            else if(phase==1)stressFrame();
        } catch(Throwable error) {failed=true;error.printStackTrace();Gdx.app.exit();phase=2;}
    }
    @Override public void resize(int w,int h) {if(screen!=null)screen.resize(w,h);}
    @Override public void dispose() {if(screen!=null)screen.dispose();}
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();c.setTitle("Monster Survivor Task 02 checks");c.setWindowedMode(960,540);
        c.disableAudio(true);c.useVsync(true);c.setForegroundFPS(60);new Lwjgl3Application(new CombatSmokeLauncher(),c);if(failed)System.exit(1);
    }
}
