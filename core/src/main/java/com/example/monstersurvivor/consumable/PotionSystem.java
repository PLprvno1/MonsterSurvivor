package com.example.monstersurvivor.consumable;

import com.example.monstersurvivor.entity.Player;

/** Session-owned inventory mirror and gameplay-time potion effects. */
public final class PotionSystem {
    public enum Availability { USABLE, APPLIED, SAVE_FAILED, DISABLED, NOT_RUNNING, EMPTY, HEALTH_FULL, ALREADY_ACTIVE }
    private final int[] counts=new int[PotionType.values().length];
    private final boolean enabled;
    private float speedRemaining,cooldownRemaining;

    public PotionSystem(int[] initialCounts,boolean enabled) {
        this.enabled=enabled;
        if(initialCounts!=null)for(PotionType type:PotionType.values()) {
            int value=type.ordinal()<initialCounts.length?initialCounts[type.ordinal()]:0;
            counts[type.ordinal()]=Math.max(0,Math.min(PotionType.MAX_INVENTORY,value));
        }
    }
    public int count(PotionType type){return type==null?0:counts[type.ordinal()];}
    public int[] counts(){return counts.clone();}
    public boolean enabled(){return enabled;}
    public float speedRemaining(){return speedRemaining;}
    public float cooldownRemaining(){return cooldownRemaining;}
    public float speedProgress(){return speedRemaining/PotionType.SPEED.duration();}
    public float cooldownProgress(){return cooldownRemaining/PotionType.COOLDOWN.duration();}

    public Availability availability(PotionType type,Player player,boolean running) {
        if(!enabled)return Availability.DISABLED;
        if(!running || player==null || !player.alive())return Availability.NOT_RUNNING;
        if(type==null)return Availability.DISABLED;
        if(type==PotionType.HEALTH && player.hp()>=player.maxHp())return Availability.HEALTH_FULL;
        if(type==PotionType.SPEED && speedRemaining>0)return Availability.ALREADY_ACTIVE;
        if(type==PotionType.COOLDOWN && cooldownRemaining>0)return Availability.ALREADY_ACTIVE;
        if(counts[type.ordinal()]<=0)return Availability.EMPTY;
        return Availability.USABLE;
    }
    /** Called only after the platform confirms durable inventory consumption. */
    public boolean activate(PotionType type,Player player,boolean running) {
        if(availability(type,player,running)!=Availability.USABLE)return false;
        if(type==PotionType.HEALTH && player.heal(type.healing())<=0)return false;
        if(type==PotionType.SPEED)speedRemaining=type.duration();
        else if(type==PotionType.COOLDOWN)cooldownRemaining=type.duration();
        counts[type.ordinal()]--;return true;
    }
    public float speedBoostSeconds(float dt){return speedRemaining<=0?0:Math.min(Math.max(0,dt),speedRemaining);}
    /** Cooldown factor ×1.25 only while its gameplay timer is active, with a hard ×1.40 ceiling. */
    public float cooldownStep(float dt) {
        if(dt<=0)return 0;
        float step=dt+Math.min(dt,cooldownRemaining)*(PotionType.COOLDOWN.multiplier()-1);
        return Math.min(step,dt*PotionType.MAX_COMBINED_COOLDOWN_MULTIPLIER);
    }
    public void advance(float dt) {
        if(dt<=0 || !Float.isFinite(dt))return;
        speedRemaining=Math.max(0,speedRemaining-dt);cooldownRemaining=Math.max(0,cooldownRemaining-dt);
    }
    /** Buffs never cross retry, death, victory, or session end; inventory is retained by the owner. */
    public void clearEffects(){speedRemaining=cooldownRemaining=0;}
}
