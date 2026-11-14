import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.example.monstersurvivor.assets.GameAssets;
/** Pure terrain framebuffer readback. Does not render or export Tiny Questers art. */
public class TerrainProbe extends ApplicationAdapter {
    private static boolean failed;
    private static final String OUT=System.getProperty("ms.report","../core/build/reports/task01b2/");
    public void create(){
        try {
            GameAssets assets=new GameAssets();SpriteBatch batch=new SpriteBatch();
            OrthographicCamera camera=new OrthographicCamera();TextureRegion grass=assets.region("grass");
            var allowed=new java.util.HashSet<Integer>();
            Texture t=grass.getTexture();t.getTextureData().prepare();Pixmap src=t.getTextureData().consumePixmap();
            for(int y=0;y<16;y++)for(int x=0;x<16;x++)allowed.add(src.getPixel(grass.getRegionX()+x,grass.getRegionY()+y));
            if(t.getTextureData().disposePixmap())src.dispose();
            long totalBad=0;
            for(float h:new float[]{239,240,241,240.01f,239.7f})for(float phase:new float[]{0,.1f,.5f,.9999f}){
                camera.setToOrtho(false,h*Gdx.graphics.getWidth()/Gdx.graphics.getHeight(),h);camera.position.set(808+phase,808+phase,0);camera.update();
                ScreenUtils.clear(1,0,1,1);batch.setProjectionMatrix(camera.combined);batch.begin();
                for(int y=0;y<100;y++)for(int x=0;x<100;x++)batch.draw(grass,x*16,y*16);
                batch.end();Pixmap pixels=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
                int bad=0;for(int y=0;y<pixels.getHeight();y++)for(int x=0;x<pixels.getWidth();x++)if(!allowed.contains(pixels.getPixel(x,y)))bad++;
                if(bad>0){
                    var colors=new java.util.TreeMap<String,Integer>();
                    for(int y=0;y<pixels.getHeight();y++)for(int x=0;x<pixels.getWidth();x++)if(!allowed.contains(pixels.getPixel(x,y)))colors.merge(Integer.toHexString(pixels.getPixel(x,y)),1,Integer::sum);
                    System.out.println("TERRAIN h="+h+" phase="+phase+" unexpectedPixels="+bad+" colors="+colors);
                    PixmapIO.writePNG(Gdx.files.local(OUT+"terrain-after.png"),pixels,-1,true);
                }
                if(h==239.7f && phase==.9999f)PixmapIO.writePNG(Gdx.files.local(OUT+"terrain-after.png"),pixels,-1,true);
                totalBad+=bad;pixels.dispose();
            }
            System.out.println("TERRAIN TOTAL unexpectedPixels="+totalBad);if(totalBad!=0)throw new AssertionError("Atlas bleeding remains");batch.dispose();assets.dispose();
        }catch(Throwable t){failed=true;t.printStackTrace();}Gdx.app.exit();
    }
    public static void main(String[] args){Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();c.setTitle("Terrain probe");c.setWindowedMode(960,540);c.disableAudio(true);new Lwjgl3Application(new TerrainProbe(),c);if(failed)System.exit(1);}
}
