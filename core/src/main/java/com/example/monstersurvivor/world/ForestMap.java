package com.example.monstersurvivor.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.assets.GameAssets;
import com.example.monstersurvivor.render.WorldSprite;

/** Per-match forest rendering, with the serialized fixture retained for isolated debug comparisons. */
public final class ForestMap {
    public final int width, height, tileSize;
    public final long seed;
    public final int walkableTiles,repairs;
    public final boolean fallback;
    public final float spawnX, spawnY;
    public final CollisionGrid grid;
    public final Array<WorldSprite> props = new Array<>(1024);
    private final TextureRegion[] ground;
    private final Array<WorldSprite>[] propChunks;
    private static final int CHUNK_SIZE=160;
    private final int chunkColumns,chunkRows;
    /** Legacy fixture: isolated debug exhibits, camera and controlled before/after profiling. */
    public ForestMap(GameAssets assets) {this(assets,(ForestGenerator.Layout)null);}
    public ForestMap(GameAssets assets,long seed) {this(assets,generate(seed));}
    private static ForestGenerator.Layout generate(long seed) {
        try {return ForestGenerator.generate(seed);}
        catch(RuntimeException error) {Gdx.app.error("MonsterSurvivor","Map fallback seed="+seed,error);return ForestGenerator.fallback(seed);}
    }
    private ForestMap(GameAssets assets,ForestGenerator.Layout layout) {
        if(layout==null) {
            JsonValue root=new JsonReader().parse(Gdx.files.internal("game/forest.json"));
            width=root.getInt("width");height=root.getInt("height");tileSize=root.getInt("tileSize");seed=root.getLong("seed",10101);
            spawnX=root.getFloat("spawnX");spawnY=root.getFloat("spawnY");fallback=false;repairs=0;
            ground=new TextureRegion[width*height];byte[] masks=new byte[ground.length];
            TextureRegion[] regions=new TextureRegion[root.get("regionOrder").size];
            for(int i=0;i<regions.length;i++)regions[i]=assets.region(root.get("regionOrder").getString(i));
            if(root.get("ground").size!=ground.length || root.get("collision").size!=masks.length)throw new GdxRuntimeException("Inconsistent forest layers");
            JsonValue tiles=root.get("ground").child,blocked=root.get("collision").child;int free=0;
            for(int i=0;i<ground.length;i++,tiles=tiles.next,blocked=blocked.next) {ground[i]=regions[tiles.asInt()];masks[i]=(byte)blocked.asInt();if(masks[i]==0)free++;}
            grid=new CollisionGrid(width,height,tileSize,masks);walkableTiles=free;
            for(JsonValue p=root.get("props").child;p!=null;p=p.next)
                props.add(new WorldSprite(assets.region(p.getString("region")),p.getFloat("x"),p.getFloat("y"),8,2));
        } else {
            width=ForestGenerator.WIDTH;height=ForestGenerator.HEIGHT;tileSize=ForestGenerator.TILE_SIZE;seed=layout.seed;
            spawnX=(ForestGenerator.CENTER_X+.5f)*tileSize;spawnY=(ForestGenerator.CENTER_Y+.5f)*tileSize;
            grid=layout.grid;walkableTiles=layout.walkableTiles;repairs=layout.repairs;fallback=layout.fallback;
            ground=new TextureRegion[width*height];for(int i=0;i<ground.length;i++)ground[i]=assets.region(layout.terrain[i]);
            for(int i=0;i<layout.props.size;i++) {
                ForestGenerator.Prop prop=layout.props.get(i);props.add(new WorldSprite(assets.region(prop.region),prop.x,prop.y,8,2,prop.scale));
            }
        }
        chunkColumns=(width*tileSize+CHUNK_SIZE-1)/CHUNK_SIZE;chunkRows=(height*tileSize+CHUNK_SIZE-1)/CHUNK_SIZE;
        @SuppressWarnings("unchecked") Array<WorldSprite>[] chunks=(Array<WorldSprite>[])new Array<?>[chunkColumns*chunkRows];
        propChunks=chunks;
        for(int i=0;i<props.size;i++) {
            WorldSprite prop=props.get(i);int index=(int)(prop.y/CHUNK_SIZE)*chunkColumns+(int)(prop.x/CHUNK_SIZE);
            if(propChunks[index]==null)propChunks[index]=new Array<>(32);propChunks[index].add(prop);
        }
        if (!grid.canOccupy(spawnX, spawnY, 4, 3)) throw new GdxRuntimeException("Blocked player spawn");
    }
    public float pixelWidth() { return width * tileSize; }
    public float pixelHeight() { return height * tileSize; }
    public void enqueueProps(Array<WorldSprite> visible,float left,float bottom,float right,float top) {
        int minX=Math.max(0,MathUtils.floor((left-64)/CHUNK_SIZE)),maxX=Math.min(chunkColumns-1,MathUtils.floor((right+64)/CHUNK_SIZE));
        int minY=Math.max(0,MathUtils.floor((bottom-64)/CHUNK_SIZE)),maxY=Math.min(chunkRows-1,MathUtils.floor((top+64)/CHUNK_SIZE));
        for(int row=minY;row<=maxY;row++)for(int col=minX;col<=maxX;col++) {
            Array<WorldSprite> chunk=propChunks[row*chunkColumns+col];if(chunk==null)continue;
            for(int i=0;i<chunk.size;i++) {WorldSprite prop=chunk.get(i);if(prop.visible(left,bottom,right,top))visible.add(prop);}
        }
    }
    public void renderGround(SpriteBatch batch, OrthographicCamera camera) {
        int minX = Math.max(0, MathUtils.floor((camera.position.x - camera.viewportWidth / 2) / tileSize));
        int maxX = Math.min(width - 1, MathUtils.floor((camera.position.x + camera.viewportWidth / 2) / tileSize));
        int minY = Math.max(0, MathUtils.floor((camera.position.y - camera.viewportHeight / 2) / tileSize));
        int maxY = Math.min(height - 1, MathUtils.floor((camera.position.y + camera.viewportHeight / 2) / tileSize));
        for (int y = minY; y <= maxY; y++) for (int x = minX; x <= maxX; x++)
            batch.draw(ground[y * width + x], x * tileSize, y * tileSize);
    }
}
