import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.assets.*;
import com.example.monstersurvivor.assets.GameAssets.*;
import com.example.monstersurvivor.enemy.*;

/** Actual GL load/frame/animation coverage, with a gallery restricted to the newly licensed atlas. */
public final class EnemyAssetSmoke extends ApplicationAdapter {
    static boolean failed;int checks;
    void check(boolean b,String s) {if(!b)throw new AssertionError(s);checks++;}
    public void create() {
        try {
            var assets=new GameAssets();var batch=new SpriteBatch();var camera=new OrthographicCamera();camera.setToOrtho(false,800,600);batch.setProjectionMatrix(camera.combined);
            Gdx.gl.glViewport(0,0,800,600);ScreenUtils.clear(.1f,.13f,.16f,1);batch.begin();int frames=0;
            for(var d:EnemyRegistry.all()) {
                var set=assets.sprite(d.spriteId);check(set.displayScale==d.scale,"definition/asset scale agrees "+d.id);
                check(set.feetX==d.feetX && set.feetY==d.feetY,"definition/asset pivot agrees "+d.id);
                for(var state:new State[]{State.IDLE,State.WALK,State.ATTACK,State.HURT,State.DEATH})for(var dir:Direction.values()) {
                    check(set.has(state,dir),"required animation exists "+d.id+" "+state+" "+dir);
                    var clip=set.clip(state,dir);check(clip.loop==(state==State.IDLE || state==State.WALK),"correct loop mode "+d.id);
                    for(var r:clip.animation.getKeyFrames()) {
                        check(Math.min(r.getU(),r.getU2())>=0 && Math.min(r.getV(),r.getV2())>=0
                            && Math.max(r.getU(),r.getU2())<=1 && Math.max(r.getV(),r.getV2())<=1,"frame within texture "+d.id);
                        batch.draw(r,300,250,r.getRegionWidth(),r.getRegionHeight());frames++;
                    }
                    if(d.id.startsWith("mc_") || d.id.startsWith("sc_")) {
                        for(var r:clip.animation.getKeyFrames()) {
                            var f=(FrameRegion)r;float expectedX=r.isFlipX()?r.getRegionWidth()-set.feetX:set.feetX;
                            check(Math.abs(f.originX-expectedX)<.001f && Math.abs(f.originY-(r.getRegionHeight()-set.feetY))<.001f,"stable foot anchor across states");
                        }
                    }
                }
                if(d.id.startsWith("mc_"))for(var dir:Direction.values())check(set.has(State.SPECIAL,dir) && !set.clip(State.SPECIAL,dir).loop,"special clip retained, non-looping");
            }
            batch.end();check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"all imported frames render without GL error");
            check(assets.textureCount()==19,"one additional atlas, shared by all new residents");
            // Clear before the only readback, so no existing restricted actor art can be exported.
            ScreenUtils.clear(.1f,.13f,.16f,1);batch.begin();int index=0;var font=new BitmapFont();font.getData().setScale(.58f);
            for(var d:EnemyRegistry.all())if((d.id.startsWith("mc_") && !d.elite) || d.id.startsWith("sc_")) {
                var set=assets.sprite(d.spriteId);var frame=(FrameRegion)set.frame(State.IDLE,Direction.DOWN,0);
                float x=50+(index%8)*100,y=540-(index/8)*95,scale=2;
                batch.draw(frame,x-frame.originX*scale,y-frame.originY*scale,frame.getRegionWidth()*scale,frame.getRegionHeight()*scale);
                font.draw(batch,d.id.replace("mc_","").replace("sc_","SC "),x-45,y-15);index++;
            }
            batch.end();var pixmap=Pixmap.createFromFrameBuffer(0,0,800,600);PixmapIO.writePNG(Gdx.files.local("../core/build/reports/task04a/roster-new-assets.png"),pixmap,-1,true);pixmap.dispose();font.dispose();
            var max=com.badlogic.gdx.utils.BufferUtils.newIntBuffer(1);Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE,max);
            String data="{\"checks\":"+checks+",\"species\":"+EnemyRegistry.all().size()+",\"frameDraws\":"+frames+",\"textures\":"+assets.textureCount()+",\"rgbaBytes\":"+assets.textureBytes()+",\"glMaxTextureSize\":"+max.get(0)+"}";
            Gdx.files.local("../core/build/reports/task04a/asset-gl.json").writeString(data,false,"UTF-8");System.out.println("ASSET GL PASSED "+data);batch.dispose();assets.dispose();
        }catch(Throwable t) {failed=true;t.printStackTrace();}Gdx.app.exit();
    }
    public static void main(String[] args) {var c=new Lwjgl3ApplicationConfiguration();c.setWindowedMode(800,600);c.disableAudio(true);c.setTitle("Enemy import verification");new Lwjgl3Application(new EnemyAssetSmoke(),c);if(failed)System.exit(1);}
}
