import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.assets.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.render.*;
import com.example.monstersurvivor.world.*;

/** Readbacks contain only Kenney CC0 and geometric overlays. No player/monster art is drawn. */
public final class PresentationProbe extends ApplicationAdapter {
    static boolean failed;int checks;
    void check(boolean b,String message) {if(!b)throw new AssertionError(message);checks++;}
    public void create() {
        try {
            var assets=new GameAssets();var batch=new SpriteBatch();var camera=new OrthographicCamera();
            var map=new ForestMap(assets,42);var s=new GameSession(new GameConfig.Builder().population(0,60).build(),map.grid,map.spawnX,map.spawnY,null,null,42);
            var pickups=new PickupRenderer(assets);var night=new NightRenderer(assets.region("weapon_pixel"));var visible=new Array<WorldSprite>();
            int i=0;for(var type:PickupType.values()) {var item=s.pickups.spawn(type,map.spawnX-88+i++*44,map.spawnY,0);check(item!=null,"visible pickup placed");item.age=1;}
            for(float zoom:new float[]{1.1f,1.15f,1.2f})for(float strength:new float[]{0,.5f,1}) {
                float h=240/zoom,w=h*960/540;camera.setToOrtho(false,w,h);camera.position.set(map.spawnX,map.spawnY,0);camera.update();
                float left=map.spawnX-w/2,bottom=map.spawnY-h/2;
                ScreenUtils.clear(1,0,1,1);batch.setProjectionMatrix(camera.combined);batch.begin();map.renderGround(batch,camera);
                visible.clear();map.enqueueProps(visible,left,bottom,left+w,bottom+h);pickups.enqueue(s.pickups,visible,left,bottom,left+w,bottom+h);visible.sort(WorldSprite.BACK_TO_FRONT);
                for(var sprite:visible)sprite.draw(batch);batch.end();
                night.draw(batch,s.config.visuals,strength,w,h,w/2,h/2);
                camera.setToOrtho(false,960,540);batch.setProjectionMatrix(camera.combined);batch.begin();batch.setColor(1,1,1,1);batch.draw(assets.region("weapon_pixel"),8,520,100,12);batch.end();
                Pixmap pixels=Pixmap.createFromFrameBuffer(0,0,960,540);
                check((pixels.getPixel(20,525)&0xffffff00)==0xffffff00,"HUD marker remains full bright");
                int magenta=0;for(int py=0;py<540;py++)for(int px=0;px<960;px++)if((pixels.getPixel(px,py)&0xffffff00)==0xff00ff00)magenta++;
                check(magenta==0,"no magenta holes or atlas bleed");
                PixmapIO.writePNG(Gdx.files.local("../core/build/reports/task03b_1/pickups-zoom"+zoom+"-night"+strength+".png"),pixels,-1,true);pixels.dispose();
                check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"overlay and pickup GL");
            }
            check(assets.textureCount()==19,"bounded shared textures");batch.dispose();assets.dispose();
            System.out.println("CC0 PRESENTATION GL PASSED: "+checks);
        } catch(Throwable t) {failed=true;t.printStackTrace();}Gdx.app.exit();
    }
    public static void main(String[] args) {var c=new Lwjgl3ApplicationConfiguration();c.setWindowedMode(960,540);c.disableAudio(true);c.setTitle("CC0 presentation probe");new Lwjgl3Application(new PresentationProbe(),c);if(failed)System.exit(1);}
}
