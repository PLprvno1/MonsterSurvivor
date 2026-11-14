import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.assets.GameAssets;
import com.example.monstersurvivor.assets.GameAssets.*;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.input.GameInput;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.world.CollisionGrid;
import java.nio.file.Files;
import java.nio.file.Path;

/** Runs the real GameScreen in an OpenGL desktop window, injects input events, and exits. */
public class SmokeLauncher extends ApplicationAdapter {
    private GameScreen screen;
    private int phase, checks;
    private int performanceFrames, performanceDrawCalls;
    private long performanceStart, renderNanos;
    private static boolean failed;
    private final StringBuilder log=new StringBuilder();
    private static final String OUT=System.getProperty("ms.report","../core/build/reports/task01b2/");
    private void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;log.append("OK ").append(label).append('\n');}
    private void tick(int count,float dt){for(int i=0;i<count;i++)screen.render(dt);}
    private void key(int key,boolean down){if(down)screen.getControls().keyDown(key);else screen.getControls().keyUp(key);}
    private Vector2 pixel(float x,float y){Vector2 p=new Vector2(x,y);screen.getHud().project(p);p.y=Gdx.graphics.getHeight()-p.y;return p;}
    private void capture(String name){Pixmap p=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
        PixmapIO.writePNG(Gdx.files.local(OUT+name),p,-1,true);p.dispose();}
    @Override public void create(){screen=new GameScreen(6);screen.show();}
    @Override public void resize(int w,int h){screen.resize(w,h);}
    @Override public void render(){
        try{
            if(phase==0){runChecks();phase=1;Gdx.graphics.setWindowedMode(640,480);}
            else if(phase==1){screen.render(0);check(Gdx.graphics.getWidth()==640,"Desktop resize to 640x480");capture("desktop-resized.png");
                check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"No OpenGL error");
                screen.dispose();screen=new GameScreen(60);screen.show();screen.resize(640,480);
                screen.cycleAnimation();performanceStart=System.nanoTime();phase=2;}
            else if(phase==2){
                long start=System.nanoTime();screen.render(Gdx.graphics.getDeltaTime());renderNanos+=System.nanoTime()-start;
                performanceDrawCalls+=screen.getWorldDrawCalls();performanceFrames++;
                if(performanceFrames>=180){
                    double seconds=(System.nanoTime()-performanceStart)/1e9;
                    check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"60-exhibit render without OpenGL errors");capture("desktop-60-preview.png");
                    log.append("60 stationary animated exhibits, 180 real frames: ").append(performanceFrames/seconds)
                        .append(" FPS; mean render CPU/driver ms=").append(renderNanos/1e6/performanceFrames)
                        .append("; mean world draw calls=").append((float)performanceDrawCalls/performanceFrames).append('\n');
                    log.append("DESKTOP SMOKE PASSED: ").append(checks).append(" checks\n");
                    Files.writeString(Path.of(OUT,"desktop-smoke.txt"),log);System.out.println(log);Gdx.app.exit();phase=3;
                }
            }
        }catch(Throwable error){failed=true;error.printStackTrace();log.append("FAILED: ").append(error).append('\n');
            try{Files.createDirectories(Path.of(OUT));Files.writeString(Path.of(OUT,"desktop-smoke.txt"),log);}catch(Exception ignored){}
            Gdx.app.exit();phase=2;}
    }
    private void runChecks(){
        com.badlogic.gdx.graphics.g2d.SpriteBatch validationBatch=new com.badlogic.gdx.graphics.g2d.SpriteBatch();
        int actualW=Gdx.graphics.getWidth(),actualH=Gdx.graphics.getHeight();
        screen.resize(3200,100);screen.render(0);
        check(screen.getCamera().viewportWidth<=1600&&screen.getCamera().viewportHeight<=1600,"Extreme window aspect keeps camera inside map");
        screen.resize(actualW,actualH);
        screen.render(0);capture("desktop-start.png");
        check(screen.getAssets().textureCount()==16,"Exactly 16 runtime textures including pickups and shared weapon texel");
        screen.cycleDirection();
        check(screen.getPlayer().direction==Direction.LEFT,"DIR changes stationary Warrior idle facing");
        screen.cycleDirection();screen.cycleDirection();screen.cycleDirection();
        String[] names={"player","tiny_slime","tiny_bat","honey_slime","wild_boar","giant_bee","red_cap","red_crab","giant_frog","rock_golem","living_armor","bone_hound","lich_lord"};
        for(String name:names){GameAssets.SpriteSet set=screen.getAssets().sprite(name);
            for(Direction dir:Direction.values())for(State state:new State[]{State.IDLE,State.WALK,State.ATTACK,State.HURT,State.DEATH,State.VICTORY}){
                if(state==State.VICTORY&&!name.equals("player"))continue;
                check(set.has(state,dir),name+" "+state+" "+dir);
                GameAssets.Clip clip=set.clip(state,dir);
                TextureRegion first=clip.frame(0),last=clip.frame(100);
                if(!name.equals("player")&&!name.startsWith("tiny_"))check(first.getRegionWidth()==set.width&&last.getRegionHeight()==set.height,"Frame cell dimensions");
                else for(TextureRegion f:clip.animation.getKeyFrames()) {
                    check(f instanceof GameAssets.FrameRegion,"Tiny frame carries foot pivot");
                    GameAssets.FrameRegion anchor=(GameAssets.FrameRegion)f;
                    com.example.monstersurvivor.render.WorldSprite actor=new com.example.monstersurvivor.render.WorldSprite(f,808.2f,808.3f,0,0,set.displayScale);
                    check(Math.abs(actor.drawX()+anchor.originX*set.displayScale-808.2f)<.001f && Math.abs(actor.drawY()+anchor.originY*set.displayScale-808.3f)<.001f,"Foot anchor stays fixed across canvases/frames/scales");
                    // Render every actual Tiny frame, not just one timestamp per state.
                    validationBatch.setProjectionMatrix(screen.getCamera().combined);validationBatch.begin();actor.draw(validationBatch);validationBatch.end();
                    check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"Tiny source frame renders on OpenGL");
                }
                if(!clip.loop)check(last==clip.animation.getKeyFrames()[clip.animation.getKeyFrames().length-1],"Non-looping clip holds final frame");
            }
        }
        validationBatch.dispose();
        SpriteSet warrior=screen.getAssets().sprite("player");
        for(Direction dir:Direction.values()) {
            check(warrior.has(State.VICTORY,dir),"Warrior Victory mapping "+dir);
            check(!warrior.has(State.DASH,dir)&&warrior.clip(State.DASH,dir)==warrior.clip(State.WALK,dir),"Dash uses explicit Walk fallback, no invented source clip");
        }
        Player p=screen.getPlayer();GameInput input=screen.getControls();
        float x=p.x,y=p.y;
        Vector2 origin=pixel(GameInput.JOY_X,GameInput.JOY_Y),end=pixel(GameInput.JOY_X+GameInput.JOY_RADIUS,GameInput.JOY_Y);
        input.touchDown((int)origin.x,(int)origin.y,0,0);input.touchDragged((int)end.x,(int)end.y,0);
        tick(60,1f/60);check(p.x-x>78&&p.x-x<81&&Math.abs(p.y-y)<1,"Joystick moves right at configured speed");
        check(Math.abs(screen.getCamera().position.x-p.x)<1 && Math.abs(screen.getCamera().position.y-p.y)<1,"Camera follows actual joystick movement");
        float pixelsPerUnit=Gdx.graphics.getWidth()/screen.getCamera().viewportWidth;
        float projectedEdge=(screen.getCamera().position.x-screen.getCamera().viewportWidth/2)*pixelsPerUnit;
        check(Math.abs(projectedEdge-com.example.monstersurvivor.render.PixelCamera.RASTER_PHASE_PIXELS-Math.round(projectedEdge-com.example.monstersurvivor.render.PixelCamera.RASTER_PHASE_PIXELS))<.01f,"Camera grid uses one fixed physical-pixel sample phase");
        input.touchUp((int)end.x,(int)end.y,0,0);screen.render(0);check(!p.walking,"Joystick release returns to idle");
        x=p.x;y=p.y;key(Input.Keys.W,true);key(Input.Keys.D,true);tick(120,1f/120);key(Input.Keys.W,false);key(Input.Keys.D,false);
        check(Math.abs(Math.hypot(p.x-x,p.y-y)-80)<.1,"Keyboard diagonal normalized");
        check(Math.abs(screen.getCamera().position.x-p.x)<1 && Math.abs(screen.getCamera().position.y-p.y)<1,"Camera follows diagonal movement without drift");
        key(Input.Keys.P,true);float before=screen.getElapsed();x=p.x;screen.render(5);
        check(screen.isPaused()&&screen.getElapsed()==before&&p.x==x,"Pause freezes simulation/timer");capture("desktop-paused.png");
        key(Input.Keys.P,true);screen.render(5);check(screen.getElapsed()==before,"Resume discards large delta");
        screen.pause();screen.resume();check(screen.isPaused(),"Lifecycle resume awaits explicit resume");
        Vector2 resume=pixel(screen.getHud().width()/2,screen.getHud().height()/2-32);
        input.touchDown((int)resume.x,(int)resume.y,1,0);screen.render(5);check(!screen.isPaused()&&screen.getElapsed()==before,"Touch resume and lifecycle delta guard");
        for(int mask:new int[]{CollisionGrid.TREE,CollisionGrid.ROCK,CollisionGrid.WATER}){
            int target=-1;
            for(int row=3;row<97&&target<0;row++)for(int col=3;col<97;col++)
                if(screen.getMap().grid.mask(col,row)==mask&&screen.getMap().grid.isWalkable(col-1,row)){target=row*100+col;break;}
            check(target>=0,"Found real obstacle mask "+mask);int col=target%100,row=target/100;
            p.x=(col-1)*16+8;p.y=row*16+8;key(Input.Keys.D,true);tick(90,1f/60);key(Input.Keys.D,false);
            check(p.x<=col*16-Player.HALF_WIDTH+.01f&&screen.getMap().grid.canOccupy(p.x,p.y,4,3),"Real-map collision mask "+mask);
        }
        for(int side=0;side<4;side++){
            p.x=side==0?24:side==1?1576:808;p.y=side==2?24:side==3?1576:808;
            int key=side==0?Input.Keys.A:side==1?Input.Keys.D:side==2?Input.Keys.S:Input.Keys.W;
            key(key,true);tick(30,1f/60);key(key,false);
            check(screen.getMap().grid.canOccupy(p.x,p.y,4,3),"Map boundary side "+side);
            OrthographicCamera c=screen.getCamera();
            check(c.position.x>=c.viewportWidth/2&&c.position.x<=1600-c.viewportWidth/2&&c.position.y>=c.viewportHeight/2&&c.position.y<=1600-c.viewportHeight/2,"Camera boundary side "+side);
        }
        p.x=screen.getMap().spawnX;p.y=screen.getMap().spawnY;
        for(int i=0;i<6;i++){screen.render(.1f);capture("desktop-animation-"+i+".png");screen.cycleAnimation();screen.cycleDirection();}
        check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"Rendered six animation states and four directions");
    }
    @Override public void dispose(){screen.dispose();}
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config=new Lwjgl3ApplicationConfiguration();config.setTitle("MonsterSurvivor - Task 01B verification");
        config.setWindowedMode(960,540);config.disableAudio(true);config.setForegroundFPS(60);
        new Lwjgl3Application(new SmokeLauncher(),config);
        if(failed)System.exit(1);
    }
}
