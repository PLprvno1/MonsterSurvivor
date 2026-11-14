import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.assets.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.world.*;

/** Geometric VFX gallery only. No actor artwork or source asset readback. */
public final class WeaponVfxProbe extends ApplicationAdapter {
    static boolean failed;
    public void create() {
        try {
            var assets=new GameAssets();var batch=new SpriteBatch();var font=new BitmapFont();var renderer=new WeaponRenderer(assets);
            var camera=new OrthographicCamera();camera.setToOrtho(false,400,300);batch.setProjectionMatrix(camera.combined);
            var c=new GameConfig.Builder().population(0,60).enemy(EnemyType.SLIME,100,1,.01f).build();
            var grid=new CollisionGrid(200,200,16,new byte[40000]);int checks=0;
            com.badlogic.gdx.utils.ScreenUtils.clear(.025f,.035f,.10f,1);batch.begin();
            font.getData().setScale(.6f);font.draw(batch,"FIRE: 1 / 2 / 2 / 3 / 3 projectiles",8,289);
            font.draw(batch,"LIGHTNING: Lv 5 has two direct roots",8,127);
            for(var type:new WeaponType[]{WeaponType.FIREBALL,WeaponType.LIGHTNING})for(int level=1;level<=5;level++) {
                float x=40+(level-1)*80,y=type==WeaponType.FIREBALL?183:30;
                var s=new GameSession(c,grid,x,y,null,null,level);
                for(int i=0;i<level;i++)s.weapons.loadout.grant(type);
                s.enemies.spawn(EnemyType.SLIME,x+24,y+35);s.enemies.spawn(EnemyType.SLIME,x-24,y+35);
                s.weapons.update(.01f,s.player,s.enemies,grid);
                if(type==WeaponType.FIREBALL) {
                    s.weapons.projectiles.update(.12f,s.enemies,grid);
                    s.weapons.projectiles.effect(ProjectileSystem.EffectType.EXPLOSION,x,y+71,0,0,25,s.config.weapons.level(type,level));
                    s.weapons.projectiles.effect(s.weapons.projectiles.effectCount()-1).age=.16f;
                }
                renderer.draw(batch,s.weapons,0,0,400,300);font.draw(batch,"L"+level,x-6,y-10);
                if(Gdx.gl.glGetError()!=GL20.GL_NO_ERROR)throw new AssertionError("Level VFX GL error");checks++;
            }
            batch.end();Pixmap pixels=Pixmap.createFromFrameBuffer(0,0,1000,750);
            PixmapIO.writePNG(Gdx.files.local("../core/build/reports/task03b_1/weapon-level-vfx.png"),pixels,-1,true);pixels.dispose();
            font.dispose();batch.dispose();assets.dispose();System.out.println("GEOMETRIC WEAPON VFX PASSED: "+checks+" level renders");
        }catch(Throwable t) {failed=true;t.printStackTrace();}Gdx.app.exit();
    }
    public static void main(String[] args) {var c=new Lwjgl3ApplicationConfiguration();c.setWindowedMode(1000,750);c.disableAudio(true);c.setTitle("Geometric weapon VFX");new Lwjgl3Application(new WeaponVfxProbe(),c);if(failed)System.exit(1);}
}
