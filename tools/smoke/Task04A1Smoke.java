import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.localization.*;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.pickup.PickupType;
import com.example.monstersurvivor.ui.WeaponHud;

/** Focused real GL/font/viewport/input/lifecycle checks. Readback is text only, never game artwork. */
public final class Task04A1Smoke extends ApplicationAdapter {
    static boolean failed;int checks;GameScreen screen;
    void check(boolean b,String text){if(!b)throw new AssertionError(text);checks++;}
    void tick(int frames){for(int i=0;i<frames;i++)screen.render(.05f);}
    int[] pixel(float x,float y){var v=new Vector2(x,y);screen.getHud().project(v);return new int[]{Math.round(v.x),Gdx.graphics.getHeight()-Math.round(v.y)};}
    void down(float x,float y,int id){var p=pixel(x,y);screen.getControls().touchDown(p[0],p[1],id,0);}
    void drag(float x,float y,int id){var p=pixel(x,y);screen.getControls().touchDragged(p[0],p[1],id);}
    void font() {
        var font=new BitmapFont(Gdx.files.internal("game/fonts/game-ui.fnt"));var old=new BitmapFont();var batch=new SpriteBatch();
        int oldBytes=old.getRegion().getTexture().getWidth()*old.getRegion().getTexture().getHeight()*4;
        check(old.getData().getGlyph('\u1ec7')==null,"old default font lacks actual Vietnamese glyph");old.dispose();
        for(var language:GameLanguage.values()) {
            var strings=GameStrings.of(language);
            for(var key:GameStrings.Key.values())for(char c:strings.get(key).toCharArray())check(font.getData().getGlyph(c)!=null,"actual glyph U+"+Integer.toHexString(c)+" "+key);
            Gdx.gl.glViewport(0,0,960,540);Gdx.gl.glClearColor(.02f,.04f,.04f,1);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.getProjectionMatrix().setToOrtho2D(0,0,960,540);batch.begin();
            var demo=new GameStrings.Key[]{GameStrings.Key.CHOOSE_WEAPON,GameStrings.Key.PAUSE,GameStrings.Key.RESUME,GameStrings.Key.RETRY,GameStrings.Key.HOME,GameStrings.Key.SWORD,GameStrings.Key.BOW,GameStrings.Key.FIREBALL,GameStrings.Key.LIGHTNING,GameStrings.Key.LIGHTNING_UPGRADE,GameStrings.Key.FALLBACK_HP_DESC,GameStrings.Key.CONTROLS};
            for(int i=0;i<demo.length;i++)font.draw(batch,strings.get(demo[i]),40,500-i*30);
            for(var key:new GameStrings.Key[]{GameStrings.Key.SWORD_NEW,GameStrings.Key.BOW_NEW,GameStrings.Key.FIREBALL_NEW,GameStrings.Key.LIGHTNING_NEW,GameStrings.Key.BOW_UPGRADE,GameStrings.Key.FIREBALL_UPGRADE})check(new GlyphLayout(font,strings.get(key)).width<532,"modal description fits existing card");
            batch.end();var image=Pixmap.createFromFrameBuffer(0,0,960,540);
            PixmapIO.writePNG(Gdx.files.local("../core/build/reports/task04a_1/localization-"+language.code+".png"),image, -1,true);image.dispose();
        }
        Gdx.files.local("../core/build/reports/task04a_1/font-memory.json").writeString("{\"oldUiRgbaBytes\":"+oldBytes+",\"newUiRgbaBytes\":524288,\"pages\":1,\"glyphs\":261}",false);
        check(font.getRegion().getTexture().getMinFilter()==Texture.TextureFilter.Nearest,"font nearest");font.dispose();batch.dispose();
    }
    void gameplay(GameLanguage language) {
        var c=new GameConfig.Builder().language(language).fixedSeed(42).build();screen=new GameScreen(c,()->{});screen.show();screen.resize(960,540);screen.render(0);
        var in=screen.getControls();float startX=screen.getPlayer().x,startY=screen.getPlayer().y;
        down(320,220,0);tick(4);check(in.joystickActive() && screen.getPlayer().x==startX && screen.getPlayer().y==startY,"floating tap no motion "+language);
        drag(386,220,0);tick(6);check(screen.getPlayer().x>startX+15,"real viewport floating movement");
        in.touchUp(0,0,0,0);float released=screen.getPlayer().x;tick(4);check(screen.getPlayer().x==released && !in.joystickActive(),"release stops actual player");
        down(200,200,0);drag(266,200,0);in.keyDown(Input.Keys.BACK);check(screen.isPaused() && in.activePointer()==-1 && in.axisX()==0,"Back resets thumb");
        screen.togglePause();screen.pause();screen.resume();check(screen.isPaused() && !in.joystickActive(),"lifecycle requires explicit resume, no stale thumb");screen.togglePause();
        var p=screen.getPlayer();check(screen.getSession().pickups.spawn(PickupType.WEAPON_BOX,p.x,p.y,1)!=null,"actual modal pickup");tick(3);check(screen.weaponSelection(),"modal opened");
        float cardX=screen.getHud().width()/2-200,cardY=WeaponHud.cardY(screen.getHud().height(),0)+38;
        check(in.inJoystickRegion(cardX,cardY),"card overlaps left activation for priority test");down(cardX,cardY,1);check(!in.joystickActive() && in.selectedOffer()==0,"card wins over joystick");var at=pixel(cardX,cardY);in.touchUp(at[0],at[1],1,0);
        check(!screen.weaponSelection() && screen.getSession().weapons.loadout.size()==2,"one reward commit");
        screen.getPlayer().damage(10000);for(int i=0;i<40 && !screen.gameOver();i++)screen.render(.1f);check(screen.gameOver() && in.activePointer()==-1,"GameOver resets input");
        int textures=screen.getAssets().textureCount();in.keyDown(Input.Keys.R);tick(2);check(screen.getSession().config.language==language && in.activePointer()==-1,"Retry retains language and resets pointer");
        check(textures==19 && screen.getAssets().textureCount()==19,"game texture count stable");check(screen.getMap().grid.width==150 && screen.getSession().config.visuals.zoom==1.15f,"map/zoom preserved");
        in.keyDown(Input.Keys.W);in.keyDown(Input.Keys.D);in.sampleAxes();check(Math.abs(in.axisX()-.7071f)<.001,"desktop controls intact");screen.home();check(in.axisX()==0 && in.activePointer()==-1,"Home resets");
        check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"no GL errors");screen.dispose();screen=null;
    }
    @Override public void create(){try{font();gameplay(GameLanguage.VI);gameplay(GameLanguage.EN);System.out.println("TASK 04A.1 GL PASSED: "+checks+" checks; both languages, floating input, modal/lifecycle/GameOver/Retry");}catch(Throwable t){failed=true;t.printStackTrace();}finally{if(screen!=null)screen.dispose();Gdx.app.exit();}}
    public static void main(String[] args){var config=new Lwjgl3ApplicationConfiguration();config.setTitle("Task 04A.1 focused checks");config.setWindowedMode(960,540);config.useVsync(false);new Lwjgl3Application(new Task04A1Smoke(),config);if(failed)System.exit(1);}
}
