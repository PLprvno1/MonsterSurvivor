package com.example.monstersurvivor.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.input.GameInput;
import com.example.monstersurvivor.screen.GameScreen;

/** Opt-in local measurement, absent in normal play. Buffered floats, one CSV write after sampling. */
public final class CameraTrace {
    public final PixelCamera.Mode mode;
    private static final float[][] AXES={{0,0},{.22f,0},{-.22f,0},{0,.22f},{0,-.22f},
        {.15556349f,.15556349f},{-.15556349f,.15556349f},{.15556349f,-.15556349f},{-.15556349f,-.15556349f},
        {1,0},{0,1},{.70710678f,.70710678f},{-1,0},{1,0},{0,-1},{0,1}};
    private static final int COLS=18, MAX_ROWS=8192;
    private final float[] samples=new float[MAX_ROWS*COLS];
    private final Vector2 touch=new Vector2();
    private final float warmup,segmentSeconds;
    private final boolean exit;
    private final boolean raster;
    private int segment=-2,rows;
    private long start,frameStart;
    private boolean done;
    private CameraTrace(JsonValue json) {
        mode=PixelCamera.Mode.valueOf(json.getString("mode"));warmup=json.getFloat("warmup",10);
        segmentSeconds=json.getFloat("segmentSeconds",2);exit=json.getBoolean("exit",true);
        raster=json.getBoolean("raster",false);
    }
    public static CameraTrace load() {
        FileHandle file=Gdx.files.local("camera-check.json");
        return file.exists() ? new CameraTrace(new JsonReader().parse(file)) : null;
    }
    public void beforeFrame(GameScreen screen) {
        if(done)return;
        frameStart=System.nanoTime();if(start==0)start=frameStart;
        float time=(frameStart-start)*1e-9f;
        int next=time<warmup ? -1 : (int)((time-warmup)/segmentSeconds);
        if(next>=AXES.length){finish(screen);return;}
        if(next==segment)return;
        segment=next;
        Player p=screen.getPlayer();p.x=screen.getMap().spawnX;p.y=screen.getMap().spawnY;
        if(next==12)p.x=24;
        if(next==13)p.x=1576;
        if(next==14)p.y=24;
        if(next==15)p.y=1576;
        p.walking=false;p.animationTime=0;
        GameInput input=screen.getControls();input.reset();
        float ax=next<0?0:AXES[next][0],ay=next<0?0:AXES[next][1];
        touch.set(GameInput.JOY_X+GameInput.JOY_RADIUS*ax,GameInput.JOY_Y+GameInput.JOY_RADIUS*ay);
        screen.getHud().project(touch);
        input.touchDown(Math.round(touch.x),Gdx.graphics.getHeight()-Math.round(touch.y),23,0);
    }
    public void afterFrame(GameScreen screen,PixelCamera view,float delta,float step) {
        if(done||segment<0)return;
        if(rows>=MAX_ROWS){finish(screen);return;}
        Player p=screen.getPlayer();float sx=Gdx.graphics.getWidth()/screen.getCamera().viewportWidth;
        float sy=Gdx.graphics.getHeight()/screen.getCamera().viewportHeight;
        float halfW=screen.getCamera().viewportWidth/2,halfH=screen.getCamera().viewportHeight/2;
        if(!screen.getMap().grid.canOccupy(p.x,p.y,Player.HALF_WIDTH,Player.HALF_HEIGHT)
            ||view.cameraX<halfW-.001f||view.cameraX>screen.getMap().pixelWidth()-halfW+.001f
            ||view.cameraY<halfH-.001f||view.cameraY>screen.getMap().pixelHeight()-halfH+.001f)
            throw new IllegalStateException("Camera trace collision/boundary invariant failed");
        int i=rows++*COLS;
        samples[i++]=(frameStart-start)*1e-9f;samples[i++]=segment;samples[i++]=delta;samples[i++]=step;
        samples[i++]=(System.nanoTime()-frameStart)*1e-6f;
        samples[i++]=screen.getControls().axisX();samples[i++]=screen.getControls().axisY();
        samples[i++]=p.x;samples[i++]=p.y;samples[i++]=view.playerX;samples[i++]=view.playerY;
        samples[i++]=screen.getCamera().position.x;samples[i++]=screen.getCamera().position.y;
        samples[i++]=sx;samples[i++]=sy;
        samples[i++]=(view.playerX-screen.getCamera().position.x)*sx;
        samples[i++]=(view.playerY-screen.getCamera().position.y)*sy;
        samples[i]=screen.getWorldDrawCalls();
    }
    private void finish(GameScreen screen) {
        done=true;
        StringBuilder csv=new StringBuilder(rows*180);
        csv.append("time,segment,delta,step,cpuMs,axisX,axisY,simX,simY,renderX,renderY,cameraX,cameraY,scaleX,scaleY,offsetXpx,offsetYpx,drawCalls\n");
        for(int r=0;r<rows;r++)for(int c=0;c<COLS;c++)csv.append(samples[r*COLS+c]).append(c==COLS-1?'\n':',');
        Gdx.files.local("camera-trace.csv").writeString(csv.toString(),false,"UTF-8");
        if(raster)rasterCheck(screen);
        Gdx.app.log("MonsterSurvivor","CameraTrace complete mode="+mode+" rows="+rows);
        if(exit)Gdx.app.exit();
    }
    /** Only CC0 terrain is read back. Compares texture pattern after integer screen translation. */
    private static void rasterCheck(GameScreen screen) {
        int width=Gdx.graphics.getWidth(),height=Gdx.graphics.getHeight();
        FrameBuffer target=new FrameBuffer(Pixmap.Format.RGBA8888,width,height,false);
        SpriteBatch batch=new SpriteBatch();OrthographicCamera camera=new OrthographicCamera();
        TextureRegion grass=screen.getAssets().region("grass");
        TextureData td=grass.getTexture().getTextureData();td.prepare();Pixmap source=td.consumePixmap();
        IntSet palette=new IntSet();
        for(int y=0;y<16;y++)for(int x=0;x<16;x++)palette.add(source.getPixel(grass.getRegionX()+x,grass.getRegionY()+y));
        if(td.disposePixmap())source.dispose();
        StringBuilder result=new StringBuilder();
        try {
            target.begin();
            try {
                for(PixelCamera.Mode mode:PixelCamera.Mode.values()) {
                    PixelCamera v=new PixelCamera(mode);Pixmap reference=null;float initial=0;
                    long compared=0,mismatched=0,bleeding=0;
                    try {
                        for(int f=0;f<32;f++) {
                            v.update(808+f*.113f,808,screen.getCamera().viewportWidth,screen.getCamera().viewportHeight,width,height,1600,1600);
                            camera.setToOrtho(false,screen.getCamera().viewportWidth,screen.getCamera().viewportHeight);
                            camera.position.set(v.cameraX,v.cameraY,0);camera.update();
                            Gdx.gl.glClearColor(1,0,1,1);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                            batch.setProjectionMatrix(camera.combined);batch.begin();
                            for(int y=30;y<72;y++)for(int x=20;x<82;x++)batch.draw(grass,x*16,y*16);
                            batch.end();
                            Pixmap image=Pixmap.createFromFrameBuffer(width/2-160,height/2-64,320,128);
                            for(int y=0;y<128;y++)for(int x=0;x<320;x++)if(!palette.contains(image.getPixel(x,y)))bleeding++;
                            if(reference==null){reference=image;initial=v.cameraX;continue;}
                            int shift=Math.round((v.cameraX-initial)*width/camera.viewportWidth);
                            for(int y=8;y<120;y++)for(int x=16;x<304;x++)if(x+shift>=0&&x+shift<320){
                                compared++;if(image.getPixel(x,y)!=reference.getPixel(x+shift,y))mismatched++;
                            }
                            image.dispose();
                        }
                    } finally { if(reference!=null)reference.dispose(); }
                    result.append(mode).append(',').append(compared).append(',').append(mismatched).append(',').append(bleeding).append('\n');
                }
            } finally { target.end(); }
        } finally { batch.dispose();target.dispose();Gdx.gl.glViewport(0,0,width,height); }
        Gdx.files.local("camera-raster.csv").writeString("mode,compared,mismatched,bleeding\n"+result,false,"UTF-8");
        Gdx.app.log("MonsterSurvivor","CameraRaster\n"+result);
    }
}
