package com.example.monstersurvivor.pickup;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;
import com.example.monstersurvivor.assets.GameAssets;
import com.example.monstersurvivor.render.WorldSprite;

/** Reuses the existing world y-sort, culling, source pivots and SpriteBatch. */
public final class PickupRenderer {
    private static float displayScale(PickupType type) {return type==PickupType.HEALTH_DROP?.75f:1;}
    private final LongMap<ItemSprite> sprites=new LongMap<>(40);
    private final GameAssets assets;
    private int revision=-1;
    private PickupSystem previous;
    public PickupRenderer(GameAssets assets) {this.assets=assets;}
    public void enqueue(PickupSystem system,Array<WorldSprite> visible,float left,float bottom,float right,float top) {
        if(previous!=system) {sprites.clear();revision=-1;previous=system;}
        if(revision!=system.revision) {
            revision=system.revision;
            for(int i=0;i<system.size();i++)remember(system.get(i));
            for(int i=0;i<system.effectCount();i++)remember(system.effect(i));
            var entries=sprites.entries();while(entries.hasNext)if(entries.next().value.seen!=revision)entries.remove();
        }
        var values=sprites.values();
        while(values.hasNext) {
            ItemSprite sprite=values.next();Pickup p=sprite.item;
            if((p.collected || !p.blinking(system.config.blinkSeconds)) && sprite.visible(left,bottom,right,top))visible.add(sprite);
        }
    }
    private void remember(Pickup item) {
        ItemSprite sprite=sprites.get(item.id);
        if(sprite==null) {sprite=new ItemSprite(item);sprites.put(item.id,sprite);}sprite.seen=revision;
    }
    private final class ItemSprite extends WorldSprite {
        final Pickup item;
        int seen;
        ItemSprite(Pickup p) {super(assets.region(p.type.region),p.x,p.y,8,4,displayScale(p.type));item=p;}
        @Override public boolean visible(float left,float bottom,float right,float top) {
            // Include the one-pixel silhouette, small badge and existing collection rise.
            float half=9*scale;
            return x+half>left && x-half<right && y+16*scale+4.2f>bottom && y-5*scale<top;
        }
        @Override public void draw(SpriteBatch batch) {
            float old=batch.getPackedColor();
            float fade=item.collected?Math.max(0,1-item.effectAge/.3f):Math.min(1,.55f+item.age*2);
            float rise=item.collected?item.effectAge*14:0,s=scale*fade,px=Math.round(x),py=Math.round(y)+rise;
            var white=assets.region("weapon_pixel");
            batch.setColor(.02f,.025f,.035f,.22f*fade);
            batch.draw(white,px-3*s,py-2*s,6*s,2*s);
            // A faint silhouette follows the sprite's alpha; no surrounding rectangular frame.
            batch.setColor(.04f,.05f,.08f,.4f*fade);
            batch.draw(frame,px-9*s,py-4*s,16*s,16*s);batch.draw(frame,px-7*s,py-4*s,16*s,16*s);
            batch.draw(frame,px-8*s,py-5*s,16*s,16*s);batch.draw(frame,px-8*s,py-3*s,16*s,16*s);
            batch.setColor(1,1,1,fade);
            batch.draw(frame,px-8*s,py-4*s,16*s,16*s);
            if(item.type==PickupType.HEALTH_BOX) {
                batch.setColor(.7f,1,.7f,fade);batch.draw(white,px+4*s,py+11*s,3*s,s);batch.draw(white,px+5*s,py+10*s,s,3*s);
            } else if(item.type==PickupType.WEAPON_BOX) {
                batch.setColor(.85f,.5f,1,fade);batch.draw(white,px+4*s,py+10*s,s,4*s);batch.draw(white,px+3*s,py+10*s,3*s,s);
            }
            batch.setPackedColor(old);
        }
    }
}
