package com.example.monstersurvivor.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.*;
import java.util.Locale;

/** One owner for textures and prebuilt animation clips. No file IO in the render loop. */
public final class GameAssets implements Disposable {
    public enum Direction { DOWN, LEFT, RIGHT, UP;
        public static Direction fromVector(float x, float y) {
            return Math.abs(x) > Math.abs(y) ? (x < 0 ? LEFT : RIGHT) : (y < 0 ? DOWN : UP);
        }
    }
    public enum State { IDLE, WALK, ATTACK, HURT, DEATH, VICTORY, DASH, RUN, SPECIAL, SPAWN, STUN, DISSOLVE, ENTRANCE, SLAM }

    /** Immutable foot anchor travels with its region when clips have different canvases. */
    public static final class FrameRegion extends TextureRegion {
        public final float originX, originY;
        FrameRegion(Texture texture, JsonValue r, float feetX, float feetY, boolean mirror) {
            super(texture,r.getInt(0),r.getInt(1),r.getInt(2),r.getInt(3));
            originX=mirror ? getRegionWidth()-feetX : feetX;
            originY=getRegionHeight()-feetY;
            if (mirror) flip(true,false);
        }
    }

    public static final class Clip {
        public final Animation<TextureRegion> animation;
        public final boolean loop;
        Clip(Animation<TextureRegion> animation, boolean loop) {
            this.animation = animation; this.loop = loop;
            animation.setPlayMode(loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
        }
        public TextureRegion frame(float time) { return animation.getKeyFrame(time); }
    }

    public static final class SpriteSet {
        public final String id, title;
        public final int width, height;
        public final float feetX, feetY, displayScale;
        private final Clip[][] clips = new Clip[State.values().length][Direction.values().length];
        SpriteSet(JsonValue v) {
            id = v.getString("id"); title = v.getString("title");
            width = v.getInt("frameWidth"); height = v.getInt("frameHeight");
            feetX = v.getFloat("feetX"); feetY = v.getFloat("feetY");
            displayScale = v.getFloat("displayScale",1);
        }
        public boolean has(State state, Direction direction) { return clips[state.ordinal()][direction.ordinal()] != null; }
        public Clip clip(State state, Direction direction) {
            Clip found = clips[state.ordinal()][direction.ordinal()];
            // No dedicated Dash is shipped: future Dash movement can reuse Walk without inventing art.
            if (found == null && state == State.DASH) found = clips[State.WALK.ordinal()][direction.ordinal()];
            return found != null ? found : clips[State.IDLE.ordinal()][direction.ordinal()];
        }
        public TextureRegion frame(State state, Direction direction, float time) { return clip(state, direction).frame(time); }
    }

    private final AssetManager manager = new AssetManager();
    private Texture weaponPixel;
    private final ObjectMap<String, SpriteSet> sets = new ObjectMap<>();
    private final ObjectMap<String, TextureRegion> terrain = new ObjectMap<>();
    private final ObjectMap<String, Animation<TextureRegion>> evolution=new ObjectMap<>();
    private final ObjectMap<String, Animation<TextureRegion>> enemyFx=new ObjectMap<>();
    public TextureRegion evolutionFrame(String name,float age) {return evolution.get(name).getKeyFrame(age,true);}
    public TextureRegion enemyFrame(String name,float age){return enemyFrame(name,age,true);}
    public TextureRegion enemyFrame(String name,float age,boolean loop){var clip=enemyFx.get(name);if(clip==null)throw new GdxRuntimeException("Unknown enemy FX: "+name);return clip.getKeyFrame(age,loop);}
    public GameAssets() {
        try {
            JsonValue root = new JsonReader().parse(Gdx.files.internal("game/catalog.json"));
            JsonValue enemies=new JsonReader().parse(Gdx.files.internal("game/enemy-catalog.json"));
            JsonValue characters=new JsonReader().parse(Gdx.files.internal("game/characters.json"));
            for (JsonValue path = root.get("textures").child; path != null; path = path.next)
                manager.load(path.asString(), Texture.class);
            for (JsonValue path = characters.get("textures").child; path != null; path = path.next)
                manager.load(path.asString(), Texture.class);
            manager.load("game/pickups.png",Texture.class);
            manager.load("game/dungeon-pickups.png",Texture.class);
            manager.load("game/world-expanded.png",Texture.class);
            manager.load("game/evolution-fx.png",Texture.class);
            manager.load("game/enemy-fx.png",Texture.class);
            for(JsonValue path=enemies.get("textures").child;path!=null;path=path.next)manager.load(path.asString(),Texture.class);
            manager.finishLoading();
            Texture hostile=manager.get("game/enemy-fx.png",Texture.class);hostile.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            terrain.put("enemy_pixel",new TextureRegion(hostile,511,511,1,1));
            for(JsonValue clip=new JsonReader().parse(Gdx.files.internal("game/enemy-fx.json")).get("clips").child;clip!=null;clip=clip.next){
                Array<TextureRegion> frames=new Array<>();for(JsonValue r=clip.get("frames").child;r!=null;r=r.next)frames.add(new TextureRegion(hostile,r.getInt(0),r.getInt(1),r.getInt(2),r.getInt(3)));
                Animation<TextureRegion> animation=new Animation<>(1/clip.getFloat("fps"),frames);
                if(clip.name.startsWith("coin_shot_") && !clip.name.endsWith("_impact"))animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
                enemyFx.put(clip.name,animation);
            }
            Texture fx=manager.get("game/evolution-fx.png",Texture.class);fx.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            terrain.put("evolution_pixel",new TextureRegion(fx,255,255,1,1));
            for(JsonValue clip=new JsonReader().parse(Gdx.files.internal("game/evolution-fx.json")).get("clips").child;clip!=null;clip=clip.next) {
                Array<TextureRegion> frames=new Array<>();for(JsonValue r=clip.get("frames").child;r!=null;r=r.next)frames.add(new TextureRegion(fx,r.getInt(0),r.getInt(1),r.getInt(2),r.getInt(3)));
                evolution.put(clip.name,new Animation<>(1/clip.getFloat("fps"),frames));
            }
            for (JsonValue path = root.get("textures").child; path != null; path = path.next) {
                Texture texture = manager.get(path.asString(), Texture.class);
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            }
            Texture world = manager.get("game/world.png", Texture.class);
            Texture pickups=manager.get("game/pickups.png",Texture.class);
            pickups.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            pickups.setWrap(Texture.TextureWrap.ClampToEdge,Texture.TextureWrap.ClampToEdge);
            JsonValue pickupRegions=new JsonReader().parse(Gdx.files.internal("game/pickups.json")).get("regions");
            for(JsonValue r=pickupRegions.child;r!=null;r=r.next)
                terrain.put(r.name,new TextureRegion(pickups,r.getInt(0),r.getInt(1),r.getInt(2),r.getInt(3)));
            for (JsonValue r = root.get("regions").child; r != null; r = r.next)
                terrain.put(r.name, new TextureRegion(world, r.getInt(0), r.getInt(1), r.getInt(2), r.getInt(3)));
            loadRegions("dungeon-pickups");
            loadRegions("world-expanded");
            for(JsonValue path=enemies.get("textures").child;path!=null;path=path.next) {
                Texture texture=manager.get(path.asString(),Texture.class);
                texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
                texture.setWrap(Texture.TextureWrap.ClampToEdge,Texture.TextureWrap.ClampToEdge);
            }
            for(JsonValue path=characters.get("textures").child;path!=null;path=path.next) {
                Texture texture=manager.get(path.asString(),Texture.class);
                texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
                texture.setWrap(Texture.TextureWrap.ClampToEdge,Texture.TextureWrap.ClampToEdge);
            }
            loadSprites(root);loadSprites(enemies);loadSprites(characters);
            com.badlogic.gdx.graphics.Pixmap pixel=new com.badlogic.gdx.graphics.Pixmap(1,1,com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            try {pixel.setColor(1,1,1,1);pixel.fill();weaponPixel=new Texture(pixel);}finally {pixel.dispose();}
            weaponPixel.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            weaponPixel.setWrap(Texture.TextureWrap.ClampToEdge,Texture.TextureWrap.ClampToEdge);
            terrain.put("weapon_pixel",new TextureRegion(weaponPixel));
        } catch (RuntimeException e) { manager.dispose();if(weaponPixel!=null)weaponPixel.dispose();throw e; }
    }
    private void loadSprites(JsonValue root) {
        for (JsonValue v = root.get("sprites").child; v != null; v = v.next) {
                SpriteSet set = new SpriteSet(v);
                if(sets.containsKey(set.id))throw new GdxRuntimeException("Duplicate sprite id: "+set.id);
                Texture texture = manager.get(v.getString("texture"), Texture.class);
                for (JsonValue a = v.get("animations").child; a != null; a = a.next) {
                    State state = State.valueOf(a.getString("state").toUpperCase(Locale.ROOT));
                    Direction dir = Direction.valueOf(a.getString("direction").toUpperCase(Locale.ROOT));
                    Array<TextureRegion> frames = new Array<>(true, a.get("frames").size, TextureRegion[]::new);
                    for (JsonValue r = a.get("frames").child; r != null; r = r.next) {
                        JsonValue rect=r.isObject()?r.get("rect"):r;
                        Texture page=r.isObject()?manager.get(r.getString("texture"),Texture.class):texture;
                        if(rect.getInt(0)<0 || rect.getInt(1)<0 || rect.getInt(2)<=0 || rect.getInt(3)<=0
                            || rect.getInt(0)+rect.getInt(2)>page.getWidth() || rect.getInt(1)+rect.getInt(3)>page.getHeight())
                            throw new GdxRuntimeException("Frame out of bounds: "+set.id);
                        frames.add(new FrameRegion(page,rect,a.getFloat("feetX",set.feetX),
                            a.getFloat("feetY",set.feetY),a.getBoolean("mirrorX",false)));
                    }
                    if (frames.isEmpty()) throw new GdxRuntimeException("Empty animation: " + set.id);
                    set.clips[state.ordinal()][dir.ordinal()] = new Clip(new Animation<>(1f / a.getFloat("fps"), frames), a.getBoolean("loop"));
                }
                for (Direction dir : Direction.values())
                    if (!set.has(State.IDLE, dir)) throw new GdxRuntimeException("Missing idle direction: " + set.id + "/" + dir);
                sets.put(set.id, set);
        }
    }
    public SpriteSet sprite(String id) {
        SpriteSet result = sets.get(id);
        if (result == null) throw new GdxRuntimeException("Unknown sprite: " + id);
        return result;
    }
    private void loadRegions(String name) {
        Texture texture=manager.get("game/"+name+".png",Texture.class);
        texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        texture.setWrap(Texture.TextureWrap.ClampToEdge,Texture.TextureWrap.ClampToEdge);
        JsonValue regions=new JsonReader().parse(Gdx.files.internal("game/"+name+".json")).get("regions");
        for(JsonValue r=regions.child;r!=null;r=r.next)terrain.put(r.name,new TextureRegion(texture,r.getInt(0),r.getInt(1),r.getInt(2),r.getInt(3)));
    }
    public TextureRegion region(String name) {
        TextureRegion result = terrain.get(name);
        if (result == null) throw new GdxRuntimeException("Unknown terrain: " + name);
        return result;
    }
    public int textureCount() { return manager.getLoadedAssets()+(weaponPixel==null?0:1); }
    /** RGBA estimate for owned game textures; excludes font, driver and CPU copies. Diagnostics only. */
    public long textureBytes() {
        long bytes=weaponPixel==null?0:4;
        for(String name:manager.getAssetNames()) {Texture texture=manager.get(name,Texture.class);bytes+=(long)texture.getWidth()*texture.getHeight()*4;}
        return bytes;
    }
    @Override public void dispose() { manager.dispose();if(weaponPixel!=null)weaponPixel.dispose(); }
}
