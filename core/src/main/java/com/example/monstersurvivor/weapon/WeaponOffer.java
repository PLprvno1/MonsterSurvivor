package com.example.monstersurvivor.weapon;

import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.localization.*;
import static com.example.monstersurvivor.localization.GameStrings.Key.*;

/** Immutable UI offer snapshot; revalidated on commit, never a callback into a previous session. */
public final class WeaponOffer {
    public enum Kind { WEAPON,DAMAGE,MAX_HP,SPEED }
    public final Kind kind;
    public final WeaponType weapon;
    public final int fromLevel,toLevel;
    public final String title,description,icon;
    public final float r,g,b;
    private WeaponOffer(Kind kind,WeaponType weapon,int from,String title,String description,String icon,float r,float g,float b) {
        this.kind=kind;this.weapon=weapon;fromLevel=from;toLevel=from+1;this.title=title;this.description=description;this.icon=icon;this.r=r;this.g=g;this.b=b;
    }
    public static WeaponOffer weapon(WeaponType type,WeaponLoadout loadout) {
        return weapon(type,loadout,GameStrings.of(GameLanguage.VI));
    }
    public static WeaponOffer weapon(WeaponType type,WeaponLoadout loadout,GameStrings strings) {
        var owned=loadout.get(type);int from=owned==null?0:owned.level();
        String label=from==0?strings.format(NEW_WEAPON,strings.weaponName(type)):strings.format(UPGRADE_WEAPON,strings.weaponName(type),from,from+1);
        return new WeaponOffer(Kind.WEAPON,type,from,label,strings.weaponDescription(type,from>0),strings.weaponShort(type),type.r,type.g,type.b);
    }
    public static WeaponOffer stat(Kind kind) {
        return stat(kind,GameStrings.of(GameLanguage.VI));
    }
    public static WeaponOffer stat(Kind kind,GameStrings strings) {
        return switch(kind) {
            case DAMAGE -> new WeaponOffer(kind,null,0,strings.get(FALLBACK_DAMAGE),strings.get(FALLBACK_DAMAGE_DESC),strings.get(DAMAGE_SHORT),1,.65f,.45f);
            case MAX_HP -> new WeaponOffer(kind,null,0,strings.get(FALLBACK_HP),strings.get(FALLBACK_HP_DESC),strings.get(HP),.5f,1,.55f);
            case SPEED -> new WeaponOffer(kind,null,0,strings.get(FALLBACK_SPEED),strings.get(FALLBACK_SPEED_DESC),strings.get(SPEED_SHORT),.5f,.8f,1);
            default -> throw new IllegalArgumentException("Stat offer required");
        };
    }
    public boolean valid(WeaponLoadout loadout,Player player) {
        if(!player.alive())return false;
        return switch(kind) {
            case WEAPON -> loadout.canGrant(weapon) && (loadout.get(weapon)==null?0:loadout.get(weapon).level())==fromLevel;
            case DAMAGE -> player.stats.damage()<Integer.MAX_VALUE;
            case MAX_HP -> player.maxHp()<Integer.MAX_VALUE;
            case SPEED -> player.stats.speed()<20;
        };
    }
    public boolean apply(WeaponLoadout loadout,Player player) {
        if(!valid(loadout,player))return false;
        return switch(kind) {
            case WEAPON -> loadout.grant(weapon);
            case DAMAGE -> player.stats.addDamage(2)>0;
            case MAX_HP -> {int gain=player.stats.addMaxHp(2);player.heal(gain);yield gain>0;}
            case SPEED -> player.stats.addSpeed()>0;
        };
    }
}
