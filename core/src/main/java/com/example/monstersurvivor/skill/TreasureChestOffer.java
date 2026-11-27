package com.example.monstersurvivor.skill;

import com.example.monstersurvivor.weapon.WeaponOffer;

/** Immutable, token-bound adapter that presents legacy weapon and skill rewards as passive/active skills. */
public final class TreasureChestOffer {
    public enum Kind { PASSIVE_WEAPON, PASSIVE_CLASS, ACTIVE, EVOLUTION, STAT }
    public final Kind kind;
    public final WeaponOffer weapon;
    public final SkillOffer skill;
    public final String stableId,title,summary,benefit,detail,icon;
    public final float r,g,b;
    public final int fromLevel,toLevel;

    private TreasureChestOffer(Kind kind,WeaponOffer weapon,SkillOffer skill,String stableId,String title,
                               String summary,String benefit,String detail,String icon,float r,float g,float b,
                               int fromLevel,int toLevel) {
        this.kind=kind;this.weapon=weapon;this.skill=skill;this.stableId=stableId;this.title=title;
        this.summary=summary;this.benefit=benefit;this.detail=detail;this.icon=icon;
        this.r=r;this.g=g;this.b=b;this.fromLevel=fromLevel;this.toLevel=toLevel;
    }
    public static TreasureChestOffer weapon(WeaponOffer offer) {
        boolean evolution=offer.kind==WeaponOffer.Kind.EVOLUTION;
        boolean stat=offer.weapon==null;
        return new TreasureChestOffer(evolution?Kind.EVOLUTION:stat?Kind.STAT:Kind.PASSIVE_WEAPON,
            offer,null,stat?"stat_"+offer.kind.name().toLowerCase(java.util.Locale.ROOT):
                (evolution?"evolution_":"passive_")+offer.weapon.name().toLowerCase(java.util.Locale.ROOT),
            offer.title,"",offer.description,"",offer.icon,offer.r,offer.g,offer.b,offer.fromLevel,offer.toLevel);
    }
    public static TreasureChestOffer skill(SkillOffer offer) {
        boolean active=offer.classSkill==null;
        boolean evolution=offer.evolution;
        return new TreasureChestOffer(active?Kind.ACTIVE:evolution?Kind.EVOLUTION:Kind.PASSIVE_CLASS,null,offer,offer.stableId(),
            offer.title,offer.summary,offer.benefit,offer.detail,"",evolution?1:active?.35f:.95f,evolution?.8f:active?.82f:.7f,evolution?.25f:active?1:.3f,
            offer.fromLevel,offer.toLevel);
    }
    public boolean evolvedOffer(){return kind==Kind.EVOLUTION;}
}
