package com.example.monstersurvivor.enemy.skill;

import com.example.monstersurvivor.entity.Player;

/** Three fixed slots, finite refresh-only durations; damage always goes through Player.damage. */
public final class PlayerStatuses {
    public enum Type { POISON,BURN,SLOW }
    public static final Type[] TYPES=Type.values();
    private final float[] remaining=new float[3],ticks=new float[3];
    public float remaining(Type type){return remaining[type.ordinal()];}
    public boolean active(Type type){return remaining(type)>0;}
    public float movementMultiplier(){return active(Type.SLOW)?.75f:1;}
    public void apply(Type type){
        if(type==null)return;int i=type.ordinal();
        if(remaining[i]<=0)ticks[i]=1;
        remaining[i]=type==Type.POISON?2.4f:type==Type.BURN?1.8f:1.5f;
    }
    public void update(float dt,Player player){
        if(!player.alive()){clear();return;}
        for(int i=0;i<3;i++)if(remaining[i]>0){
            float step=Math.min(dt,remaining[i]);remaining[i]=Math.max(0,remaining[i]-dt);
            if(i<2){ticks[i]-=step;if(ticks[i]<=.00001f){ticks[i]+=1;player.damage(1);}}
            if(!player.alive()){clear();return;}
        }
    }
    public void clear(){java.util.Arrays.fill(remaining,0);java.util.Arrays.fill(ticks,0);}
}
