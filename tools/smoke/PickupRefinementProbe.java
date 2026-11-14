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

/** Before/after readbacks contain only Kenney CC0, default font and geometry. No actor art. */
public final class PickupRefinementProbe extends ApplicationAdapter {
    static boolean failed;static String phase="after";int checks;
    static final String OUT=System.getProperty("ms.report","../core/build/reports/task03b_2/");
    final GameSession[] rows=new GameSession[3];final PickupRenderer[] renderers=new PickupRenderer[3];
    final Array<WorldSprite> visible=new Array<>();
    GameAssets assets;AuditBatch batch;BitmapFont font;OrthographicCamera camera;NightRenderer night;
    float left,bottom,width,height;
    void check(boolean ok,String label) {if(!ok)throw new AssertionError(label);checks++;}
    static final class Draw {
        TextureRegion region;float x,y,w,h,r,g,b,a;
        Draw(TextureRegion region,float x,float y,float w,float h,Color c) {this.region=region;this.x=x;this.y=y;this.w=w;this.h=h;r=c.r;g=c.g;b=c.b;a=c.a;}
    }
    static final class AuditBatch extends SpriteBatch {
        boolean recording;final Array<Draw> draws=new Array<>();
        @Override public void draw(TextureRegion region,float x,float y,float w,float h) {
            if(recording)draws.add(new Draw(region,x,y,w,h,getColor()));super.draw(region,x,y,w,h);
        }
    }
    void scene(float strength,boolean audit) {
        Gdx.gl.glViewport(0,0,1140,540);
        ScreenUtils.clear(1,0,1,1);batch.setProjectionMatrix(camera.combined);batch.begin();
        String[] grounds={"grass","dirt","ground_stone_15"};
        for(int y=0;y<14;y++)for(int x=0;x<29;x++)batch.draw(assets.region("grass"),left+x*16,bottom+y*16,16,16);
        for(int row=0;row<3;row++) {
            var ground=assets.region(grounds[row]);
            for(int y=0;y<4;y++)for(int x=0;x<29;x++)batch.draw(ground,left+x*16,1608-90+row*60+y*16,16,16);
        }
        for(int row=0;row<3;row++) {
            visible.clear();renderers[row].enqueue(rows[row].pickups,visible,left,bottom,left+width,bottom+height);
            for(var sprite:visible) {
                batch.draws.clear();batch.recording=audit;sprite.draw(batch);batch.recording=false;
                if(audit && phase.equals("after"))inspect(sprite);
            }
        }
        batch.end();night.draw(batch,rows[0].config.visuals,strength,width,height,width/2,height/2);
        // Same labels and viewport in both versions; they are outside runtime gameplay.
        batch.setProjectionMatrix(camera.combined);batch.begin();font.getData().setScale(.48f);
        String[] labels={"DAMAGE","HEALTH","SPEED","HEALTH DROP","WEAPON"};
        for(int i=0;i<5;i++)font.draw(batch,labels[i],1608-132+i*66-15,1705);
        for(int row=0;row<3;row++)font.draw(batch,grounds[row],left+3,1608-60+row*60-12);
        batch.end();
    }
    void inspect(WorldSprite sprite) {
        String[] expectedRegions={"potion_red","potion_green","potion_blue","potion_green","pickup_chest"};
        int column=Math.round((sprite.x-(1608-132))/66);int main=0;float expected=column==3?12:16;
        check(sprite.frame==assets.region(expectedRegions[column]),"required icon for each pickup role");
        for(var d:batch.draws) {
            if(d.r==1 && d.g==1 && d.b==1 && d.a==1) {
                check(d.region==sprite.frame,"one source icon; no chest behind potions");
                check(d.w==expected && d.h==expected,"native aspect and requested scale");main++;
            }
            check(d.w<=18 && d.h<=18,"no large rectangular frame or glow");
        }
        check(main==1,"one full-color icon per pickup");
    }
    public void create() {
        try {
            assets=new GameAssets();batch=new AuditBatch();font=new BitmapFont();camera=new OrthographicCamera();
            height=240/1.15f;width=height*1140/540;left=1608-width/2;bottom=1608-height/2;
            var pixel=new PixelCamera(PixelCamera.Mode.SCREEN);pixel.update(1608,1608,width,height,1140,540,3200,3200);
            camera.setToOrtho(false,width,height);camera.position.set(pixel.cameraX,pixel.cameraY,0);camera.update();
            night=new NightRenderer(assets.region("weapon_pixel"));
            var grid=new CollisionGrid(200,200,16,new byte[40000]);var config=new GameConfig.Builder().population(0,60).build();
            for(int row=0;row<3;row++) {
                rows[row]=new GameSession(config,grid,1608,1608,null,null,42);renderers[row]=new PickupRenderer(assets);
                for(int i=0;i<5;i++) {
                    var item=rows[row].pickups.spawn(PickupType.values()[i],1608-132+i*66,1608-60+row*60,2);
                    check(item!=null,"fixed diagnostic placement");item.age=1;
                }
            }
            for(var type:PickupType.values()) {
                var region=assets.region(type.region);check(region.getRegionWidth()==16 && region.getRegionHeight()==16,"runtime region present");
                check(region.getTexture().getMinFilter()==Texture.TextureFilter.Nearest && region.getTexture().getMagFilter()==Texture.TextureFilter.Nearest,"Nearest preserved");
            }
            for(float strength:new float[]{0,1}) {
                scene(strength,true);check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"render has no GL error");
                var pixels=Pixmap.createFromFrameBuffer(0,0,1140,540);int magenta=0;
                for(int y=0;y<540;y++)for(int x=0;x<1140;x++)if((pixels.getPixel(x,y)&0xffffff00)==0xff00ff00)magenta++;
                check(magenta==0,"no magenta/transparent terrain holes");
                PixmapIO.writePNG(Gdx.files.local(OUT+phase+(strength==0?"-day.png":"-night.png")),pixels,-1,true);pixels.dispose();
            }
            // Short pickup-only comparison, not an AI/map/weapon stress test or Android FPS claim.
            double[] times=new double[60];
            for(int frame=0;frame<80;frame++) {long start=System.nanoTime();scene(0,false);Gdx.gl.glFinish();if(frame>=20)times[frame-20]=(System.nanoTime()-start)*1e-6;}
            java.util.Arrays.sort(times);double mean=java.util.Arrays.stream(times).average().orElseThrow();
            Gdx.files.local(OUT+phase+"-render.json").writeString("{\"meanMs\":"+mean+",\"p95Ms\":"+times[56]+",\"samples\":60,\"renderer\":\""+Gdx.gl.glGetString(GL20.GL_RENDERER)+"\"}",false,"UTF-8");
            check(assets.textureCount()==19,"no extra runtime textures");
            System.out.println("PICKUP REFINEMENT "+phase+" PASSED: "+checks+" checks; scene submit+finish meanMs="+mean+" p95Ms="+times[56]);
        }catch(Throwable t) {failed=true;t.printStackTrace();}finally {if(font!=null)font.dispose();if(batch!=null)batch.dispose();if(assets!=null)assets.dispose();}Gdx.app.exit();
    }
    public static void main(String[] args) {if(args.length>0)phase=args[0];var c=new Lwjgl3ApplicationConfiguration();c.setWindowedMode(1140,540);c.disableAudio(true);c.setTitle("Kenney pickup comparison");new Lwjgl3Application(new PickupRefinementProbe(),c);if(failed)System.exit(1);}
}
