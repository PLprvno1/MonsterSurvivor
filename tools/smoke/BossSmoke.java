import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.assets.GameAssets.*;
import com.example.monstersurvivor.boss.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.localization.*;
import com.example.monstersurvivor.pickup.PickupType;
import com.example.monstersurvivor.screen.GameScreen;

/** Actual OpenGL, loaded animation clips, UI02/input lifecycle. No readback of game artwork. */
public final class BossSmoke extends ApplicationAdapter {
    static boolean failed;int checks;GameScreen screen;
    void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;}
    void tick(int n){for(int i=0;i<n;i++)screen.render(.05f);}
    void tap(float x,float y){var at=new Vector2(x,y);screen.getHud().project(at);screen.getControls().touchDown(Math.round(at.x),Gdx.graphics.getHeight()-Math.round(at.y),2,0);}
    void hudOnly(String name) {
        // This readback contains only code-drawn UI on a solid background, no licensed game artwork.
        com.badlogic.gdx.utils.ScreenUtils.clear(.07f,.10f,.08f,1);var s=screen.getSession();var batch=new SpriteBatch();
        screen.getHud().render(batch,screen.getControls(),false,s.elapsed(),.05f,State.IDLE,Direction.DOWN,s.player,s.kills,s.config.mode,screen.gameOver(),s.pickups,screen.getCamera(),s);
        var image=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());PixmapIO.writePNG(Gdx.files.local("../core/build/reports/task05/"+name+".png"),image,-1,true);image.dispose();batch.dispose();
    }
    void clips() {
        var assets=screen.getAssets();var s=screen.getSession();
        for(var dir:Direction.values())for(var state:new State[]{State.IDLE,State.WALK,State.ATTACK,State.HURT,State.DEATH,State.SPECIAL,State.SPAWN,State.ENTRANCE,State.SLAM}) {
            var sprite=assets.sprite("lich_lord");check(sprite.has(state,dir),"Lich actual clip "+state+" "+dir);var clip=sprite.clip(state,dir);
            for(var frame:clip.animation.getKeyFrames())check(frame!=null,"non-null animation region");
        }
        for(var b:s.config.bosses.miniPool)for(var dir:Direction.values())for(var state:new State[]{State.WALK,State.ATTACK,State.HURT,State.DEATH})check(assets.sprite(b.body().spriteId).has(state,dir),"Mini required clip");
        var b=s.bosses.mini();check(Math.abs(b.actor.sprite.scale-b.definition.body().scale)<.001,"uniform configured Mini scale");
        check(assets.textureCount()==19,"no boss texture load");
        var font=new BitmapFont(Gdx.files.internal("game/fonts/game-ui.fnt"));
        for(var language:GameLanguage.values())for(var key:GameStrings.Key.values())for(char c:GameStrings.of(language).get(key).toCharArray())check(font.getData().getGlyph(c)!=null,"actual localized glyph "+key+" U+"+Integer.toHexString(c));font.dispose();
    }
    void run(GameLanguage language) {
        var config=new GameConfig.Builder().language(language).debug(true).playerHp(100000).fixedSeed(42).build();int[] homes={0};screen=new GameScreen(config,()->homes[0]++);screen.show();screen.resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());screen.render(0);
        var s=screen.getSession();s.diagnosticProgress(299.99f);tick(1);check(s.bosses.mini()!=null && s.night.strength()>0,"GL Mini and Night event");clips();
        var b=s.bosses.mini();b.actor.x=s.player.x+80;b.actor.y=s.player.y;tick(4);var camera=screen.getCamera();check(b.actor.sprite.visible(camera.position.x-camera.viewportWidth/2,camera.position.y-camera.viewportHeight/2,camera.position.x+camera.viewportWidth/2,camera.position.y+camera.viewportHeight/2),"real Mini visible in gameplay viewport");
        screen.getControls().keyDown(Input.Keys.P);float time=s.elapsed(),anim=b.actor.animationTime;tick(8);check(time==s.elapsed() && anim==b.actor.animationTime,"Pause actual Boss freeze");screen.getControls().keyDown(Input.Keys.P);tick(2);
        screen.pause();screen.resume();check(screen.isPaused(),"lifecycle no auto resume or duplicate");screen.togglePause();tick(2);check(s.bosses.mini()==b,"same boss after lifecycle");
        s.diagnosticProgress(599.99f);tick(1);var lich=s.bosses.lich();check(lich!=null && s.enemies.bossSize()==2,"two real actors primary HUD");lich.actor.x=s.player.x+88;lich.actor.y=s.player.y;tick(3);check(s.bosses.primary()==lich,"Final primary");
        for(int i=0;i<3;i++){lich.actor.advance(.21f);lich.actor.damage(10000);}tick(20);check(lich.phase()==2 && lich.phaseChanges==1,"real clip Entrance and phase 2 render");hudOnly("hud-boss-"+language.code);tick(120);
        s.pickups.spawn(PickupType.WEAPON_BOX,s.player.x,s.player.y,1);tick(1);check(s.choosingWeapon(),"UI02 modal retained");time=s.progress();tick(8);check(time==s.progress(),"modal Boss progress frozen");screen.chooseWeapon(0,s.selection.token());tick(2);
        for(int i=0;i<10 && lich.actor.alive();i++){lich.actor.advance(.21f);lich.actor.damage(10000);}tick(1);check(s.phase()==GameSession.Phase.WINNING,"GL winning waits Death");tick(30);check(screen.victory() && s.result().finalBossDefeated(),"actual Victory HUD");hudOnly("hud-victory-"+language.code);
        var oldId=s.id;tap(screen.getHud().width()/2,screen.getHud().height()/2-30);tick(2);check(screen.getSession().id!=oldId && screen.getSession().bosses.mini()==null && screen.getSession().config.language==language,"Victory touch Retry resets events keeps language");
        check(screen.getAssets().textureCount()==19,"Retry no texture reload");
        screen.getPlayer().damage(Integer.MAX_VALUE);tick(40);check(screen.gameOver() && !screen.victory(),"old GameOver remains");screen.getControls().keyDown(Input.Keys.H);screen.home();check(homes[0]==1 && screen.getSession().phase()==GameSession.Phase.ENDED,"terminal Home delivered once even for repeated callback");
        check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"no GL errors");screen.dispose();screen=null;
    }
    void randomRetry() {
        screen=new GameScreen(new GameConfig.Builder().difficulty(Difficulty.HARD).mode(GameMode.DEMO).language(GameLanguage.EN).playerHp(100000).debug(true).build(),()->{});
        screen.show();screen.resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());screen.render(0);long seed=screen.getMap().seed;var s=screen.getSession();s.diagnosticProgress(600);tick(1);
        var b=s.bosses.lich();for(int i=0;i<8 && b.actor.alive();i++){b.actor.advance(.21f);b.actor.damage(10000);}tick(30);check(screen.victory(),"random-map Victory");screen.retry();
        check(screen.getMap().seed!=seed,"ordinary Retry generates fresh seed");check(screen.getSession().config.difficulty==Difficulty.HARD && screen.getSession().config.mode==GameMode.DEMO && screen.getSession().config.language==GameLanguage.EN,"Retry retains difficulty mode language");
        check(s.phase()==GameSession.Phase.ENDED && s.enemies.size()==0 && s.bosses.projectiles.size()==0 && screen.getSession().bosses.lich()==null,"old session fully retired, new events clean");screen.dispose();screen=null;
    }
    public void create(){try{run(GameLanguage.VI);run(GameLanguage.EN);randomRetry();System.out.println("TASK05 BOSS GL PASSED: "+checks+" assertions");}catch(Throwable t){failed=true;t.printStackTrace();}finally{if(screen!=null)screen.dispose();Gdx.app.exit();}}
    public static void main(String[] args){var config=new Lwjgl3ApplicationConfiguration();config.setTitle("Task05 focused Boss GL");config.setWindowedMode(960,540);config.useVsync(false);new Lwjgl3Application(new BossSmoke(),config);if(failed)System.exit(1);}
}
