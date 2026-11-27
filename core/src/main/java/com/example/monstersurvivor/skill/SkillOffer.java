package com.example.monstersurvivor.skill;

import com.example.monstersurvivor.config.SkillConfig;
import com.example.monstersurvivor.localization.GameStrings;
import static com.example.monstersurvivor.localization.GameStrings.Key.*;

/** Immutable token-bound snapshot for either a global active skill or a class skill. */
public final class SkillOffer {
    /** Null only for a class-skill offer; retained for Dash/Ultimate call sites. */
    public final SkillType type;
    /** Null only for Dash/Ultimate. */
    public final ClassSkillId classSkill;
    public final boolean evolution;
    public final int fromLevel,toLevel;
    public final String title,benefit,detail,summary;

    SkillOffer(SkillType type,ActiveSkillSystem skills,GameStrings strings) {
        this.type=type;classSkill=null;evolution=false;fromLevel=skills.view(type).level();toLevel=fromLevel+1;
        title=strings.format(SKILL_UPGRADE_TITLE,strings.get(type==SkillType.DASH?SKILL_DASH:SKILL_ULTIMATE),fromLevel,toLevel);
        SkillConfig c=skills.config();summary=strings.get(type==SkillType.DASH?SKILL_DASH_SUMMARY:SKILL_ULTIMATE_SUMMARY);
        if(type==SkillType.DASH) {
            var a=c.dash(fromLevel);var b=c.dash(toLevel);
            benefit=strings.format(SKILL_DASH_BENEFIT,a.cooldown,b.cooldown,a.distance,b.distance);
            detail=strings.format(SKILL_DASH_IFRAME,a.invulnerability,b.invulnerability);
        } else {
            var a=c.ultimate(fromLevel);var b=c.ultimate(toLevel);
            benefit=strings.format(SKILL_BURST_BENEFIT,a.cooldown,b.cooldown,a.radius,b.radius);
            detail=strings.format(SKILL_BURST_DAMAGE,a.damageMultiplier,b.damageMultiplier,a.knockback,b.knockback);
        }
    }
    SkillOffer(ClassSkillId id,ClassSkillSystem skills,GameStrings strings) {
        type=null;classSkill=id;evolution=false;fromLevel=skills.level(id);toLevel=fromLevel+1;
        String name=strings.get(nameKey(id));title=fromLevel==0?strings.format(SKILL_NAME_LEVEL,name,toLevel):strings.format(SKILL_UPGRADE_TITLE,name,fromLevel,toLevel);
        float nextCooldown=ClassSkillTuning.cooldown(id,toLevel);int nextDamage=ClassSkillTuning.damage(id,toLevel);
        if(id==ClassSkillId.WARRIOR_WHIRLWIND) {
            summary=strings.get(CLASS_WHIRLWIND_SUMMARY);
            if(fromLevel==0) {
                benefit=strings.format(CLASS_NEW_COOLDOWN_RADIUS,nextCooldown,ClassSkillTuning.whirlwindRadius(toLevel));
                detail=strings.format(CLASS_NEW_DAMAGE,nextDamage);
            } else {
                benefit=strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
                detail=strings.format(CLASS_WHIRLWIND_DETAIL,ClassSkillTuning.whirlwindRadius(fromLevel),ClassSkillTuning.whirlwindRadius(toLevel),ClassSkillTuning.damage(id,fromLevel),nextDamage);
            }
        } else if(id==ClassSkillId.WARRIOR_SWORD_WAVE) {
            summary=strings.get(CLASS_WAVE_SPEC);
            if(fromLevel==0) {
                benefit=strings.format(CLASS_NEW_COOLDOWN,nextCooldown);
                detail=strings.format(CLASS_NEW_WAVE_DETAIL,nextDamage,ClassSkillTuning.wavePierce(toLevel));
            } else {
                benefit=strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
                detail=strings.format(CLASS_WAVE_DETAIL,ClassSkillTuning.damage(id,fromLevel),nextDamage,ClassSkillTuning.wavePierce(fromLevel),ClassSkillTuning.wavePierce(toLevel));
            }
        } else if(id==ClassSkillId.WARRIOR_PARRY) {
            summary=strings.get(CLASS_PARRY_SUMMARY);
            if(fromLevel==0) {
                benefit=strings.format(CLASS_NEW_COOLDOWN,nextCooldown);
                detail=strings.format(CLASS_NEW_PARRY_DETAIL,nextDamage);
            } else {
                benefit=strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
                detail=strings.format(CLASS_PARRY_DETAIL,ClassSkillTuning.damage(id,fromLevel),nextDamage);
            }
        } else if(id==ClassSkillId.WARRIOR_WAR_CRY) {
            summary=strings.get(CLASS_WAR_CRY_SUMMARY);
            benefit=fromLevel==0?strings.format(CLASS_NEW_COOLDOWN,nextCooldown):strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
            detail=strings.format(CLASS_WAR_CRY_DETAIL,
                fromLevel==0?0:ClassSkillTuning.damage(id,fromLevel),nextDamage,
                fromLevel==0?0:ClassSkillTuning.warCryRadius(fromLevel),ClassSkillTuning.warCryRadius(toLevel),
                fromLevel==0?0:ClassSkillTuning.warCryKnockback(fromLevel),ClassSkillTuning.warCryKnockback(toLevel),
                fromLevel==0?0:ClassSkillTuning.warCryDuration(fromLevel),ClassSkillTuning.warCryDuration(toLevel),
                fromLevel==0?0:ClassSkillTuning.warCryDamageReductionPercent(fromLevel),ClassSkillTuning.warCryDamageReductionPercent(toLevel));
        } else if(id==ClassSkillId.ARCHER_MARK) {
            summary=strings.get(CLASS_ARCHER_MARK_SUMMARY);
            benefit=fromLevel==0?strings.format(CLASS_NEW_COOLDOWN,nextCooldown):strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
            detail=strings.format(CLASS_ARCHER_MARK_DETAIL,fromLevel==0?0:ClassSkillTuning.markCharges(fromLevel),ClassSkillTuning.markCharges(toLevel),
                fromLevel==0?0:ClassSkillTuning.markDuration(fromLevel),ClassSkillTuning.markDuration(toLevel));
        } else if(id==ClassSkillId.ARCHER_FOCUS) {
            summary=strings.get(CLASS_ARCHER_FOCUS_SUMMARY);
            benefit=fromLevel==0?strings.format(CLASS_NEW_COOLDOWN,nextCooldown):strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
            detail=strings.format(CLASS_ARCHER_FOCUS_DETAIL,fromLevel==0?0:ClassSkillTuning.damage(id,fromLevel),nextDamage);
        } else if(id==ClassSkillId.ARCHER_RAIN) {
            summary=strings.get(CLASS_ARCHER_RAIN_SUMMARY);
            benefit=fromLevel==0?strings.format(CLASS_NEW_COOLDOWN,nextCooldown):strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
            detail=strings.format(CLASS_ARCHER_RAIN_DETAIL,fromLevel==0?0:ClassSkillTuning.rainPulses(fromLevel),ClassSkillTuning.rainPulses(toLevel),
                fromLevel==0?0:ClassSkillTuning.rainRadius(fromLevel),ClassSkillTuning.rainRadius(toLevel));
        } else if(id==ClassSkillId.ARCHER_POISON_ARROW) {
            summary=strings.get(CLASS_ARCHER_POISON_SUMMARY);
            benefit=fromLevel==0?strings.format(CLASS_NEW_COOLDOWN,nextCooldown):strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
            detail=strings.format(CLASS_ARCHER_POISON_DETAIL,
                fromLevel==0?0:ClassSkillTuning.damage(id,fromLevel),nextDamage,ClassSkillTuning.poisonTickDamage(),
                fromLevel==0?0:ClassSkillTuning.poisonTicks(fromLevel),ClassSkillTuning.poisonTicks(toLevel),
                fromLevel==0?0:ClassSkillTuning.poisonDuration(fromLevel),ClassSkillTuning.poisonDuration(toLevel));
        } else if(id==ClassSkillId.MAGE_ICE_SHARD) {
            summary=strings.get(CLASS_MAGE_ICE_SUMMARY);
            benefit=fromLevel==0?strings.format(CLASS_NEW_COOLDOWN,nextCooldown):strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
            detail=strings.format(CLASS_MAGE_ICE_DETAIL,fromLevel==0?0:ClassSkillTuning.damage(id,fromLevel),nextDamage,
                fromLevel==0?0:ClassSkillTuning.iceSlowPercent(fromLevel),ClassSkillTuning.iceSlowPercent(toLevel),
                fromLevel==0?0:ClassSkillTuning.iceSlowDuration(fromLevel),ClassSkillTuning.iceSlowDuration(toLevel));
        } else if(id==ClassSkillId.MAGE_ARCANE_TRAP) {
            summary=strings.get(CLASS_MAGE_TRAP_SUMMARY);
            benefit=fromLevel==0?strings.format(CLASS_NEW_COOLDOWN,nextCooldown):strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
            detail=strings.format(CLASS_MAGE_TRAP_DETAIL,fromLevel==0?0:ClassSkillTuning.damage(id,fromLevel),nextDamage,
                fromLevel==0?0:ClassSkillTuning.trapExplosionRadius(fromLevel),ClassSkillTuning.trapExplosionRadius(toLevel));
        } else if(id==ClassSkillId.MAGE_SHIELD) {
            summary=strings.get(CLASS_MAGE_SHIELD_SUMMARY);
            benefit=fromLevel==0?strings.format(CLASS_NEW_COOLDOWN,nextCooldown):strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
            detail=strings.get(CLASS_MAGE_SHIELD_DETAIL);
        } else {
            summary=strings.get(CLASS_MAGE_CHANNEL_SUMMARY);
            benefit=fromLevel==0?strings.format(CLASS_NEW_COOLDOWN,nextCooldown):strings.format(CLASS_COOLDOWN,ClassSkillTuning.cooldown(id,fromLevel),nextCooldown);
            detail=strings.format(CLASS_MAGE_CHANNEL_DETAIL,
                fromLevel==0?0:ClassSkillTuning.channelDuration(fromLevel),ClassSkillTuning.channelDuration(toLevel),
                fromLevel==0?1:ClassSkillTuning.channelFireballCooldownMultiplier(fromLevel),ClassSkillTuning.channelFireballCooldownMultiplier(toLevel));
        }
    }
    SkillOffer(ClassSkillId id,GameStrings strings) {
        type=null;classSkill=id;evolution=true;fromLevel=ClassSkillTuning.MAX_LEVEL;toLevel=ClassSkillTuning.MAX_LEVEL;
        title=strings.format(EVOLUTION_TITLE,strings.get(id.nameKey()),strings.evolvedClassSkillName(id));
        summary=strings.evolvedClassSkillDescription(id);benefit="";detail="";
    }
    private static GameStrings.Key nameKey(ClassSkillId id) {
        return switch(id) {
            case WARRIOR_WHIRLWIND -> CLASS_WHIRLWIND;
            case WARRIOR_SWORD_WAVE -> CLASS_WAVE;
            case WARRIOR_PARRY -> CLASS_PARRY;
            case WARRIOR_WAR_CRY -> CLASS_WAR_CRY;
            case ARCHER_MARK -> CLASS_ARCHER_MARK;
            case ARCHER_FOCUS -> CLASS_ARCHER_FOCUS;
            case ARCHER_RAIN -> CLASS_ARCHER_RAIN;
            case ARCHER_POISON_ARROW -> CLASS_ARCHER_POISON;
            case MAGE_ICE_SHARD -> CLASS_MAGE_ICE;
            case MAGE_ARCANE_TRAP -> CLASS_MAGE_TRAP;
            case MAGE_SHIELD -> CLASS_MAGE_SHIELD;
            case MAGE_CHANNEL -> CLASS_MAGE_CHANNEL;
            default -> throw new IllegalArgumentException("Unimplemented class skill offer: "+id);
        };
    }
    public String stableId(){return classSkill==null?type.name().toLowerCase(java.util.Locale.ROOT):(evolution?"evolution_":"")+classSkill.stableId();}
}
