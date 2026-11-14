package com.example.monstersurvivor.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ScreenUtils;
import com.example.monstersurvivor.assets.GameAssets.*;
import com.example.monstersurvivor.render.WorldSprite;

/** Opt-in local GPU verification. Enable with a local file, never in the normal frame loop. */
public final class AssetVisualCheck {
    private AssetVisualCheck() { }
    public static void runIfRequested(GameAssets assets) {
        if (!Gdx.files.local("asset-visual-check").exists()) return;
        FrameBuffer target=new FrameBuffer(Pixmap.Format.RGBA8888,960,540,false);
        SpriteBatch batch=new SpriteBatch();
        try {
            TextureRegion grass=assets.region("grass");Texture texture=grass.getTexture();
            TextureData data=texture.getTextureData();data.prepare();Pixmap source=data.consumePixmap();
            IntSet palette=new IntSet();
            for(int y=0;y<16;y++)for(int x=0;x<16;x++)palette.add(source.getPixel(grass.getRegionX()+x,grass.getRegionY()+y));
            if(data.disposePixmap())source.dispose();
            OrthographicCamera camera=new OrthographicCamera();long bad=0;int samples=0;
            target.begin();
            try {
                for(float height:new float[]{239,240,241,240.01f,239.7f})for(float phase:new float[]{0,.1f,.5f,.9999f}) {
                    camera.setToOrtho(false,height*960/540,height);camera.position.set(808+phase,808+phase,0);camera.update();
                    ScreenUtils.clear(1,0,1,1);batch.setProjectionMatrix(camera.combined);batch.begin();
                    for(int y=40;y<61;y++)for(int x=30;x<71;x++)batch.draw(grass,x*16,y*16);
                    batch.end();
                    Pixmap pixels=Pixmap.createFromFrameBuffer(0,0,960,540);
                    for(int y=0;y<540;y++)for(int x=0;x<960;x++)if(!palette.contains(pixels.getPixel(x,y)))bad++;
                    pixels.dispose();samples++;
                }
                int rendered=0;
                camera.setToOrtho(false,320,180);camera.update();batch.setProjectionMatrix(camera.combined);
                for(String id:new String[]{"player","tiny_slime","tiny_bat"}) {
                    SpriteSet set=assets.sprite(id);
                    for(State state:new State[]{State.IDLE,State.WALK,State.ATTACK,State.HURT,State.DEATH,State.VICTORY})
                        for(Direction dir:Direction.values())if(set.has(state,dir))
                            for(TextureRegion frame:set.clip(state,dir).animation.getKeyFrames()) {
                                WorldSprite actor=new WorldSprite(frame,160,90,0,0,set.displayScale);
                                batch.begin();actor.draw(batch);batch.end();rendered++;
                            }
                }
                int error=Gdx.gl.glGetError();
                Gdx.app.log("MonsterSurvivor","AssetVisualCheck terrainSamples="+samples+" unexpectedPixels="+bad+" tinyFramesRendered="+rendered+" glError="+error);
                if(bad!=0||error!=GL20.GL_NO_ERROR)throw new IllegalStateException("GPU asset verification failed");
            } finally { target.end(); }
        } finally {
            batch.dispose();target.dispose();
            Gdx.gl.glViewport(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
        }
    }
}
