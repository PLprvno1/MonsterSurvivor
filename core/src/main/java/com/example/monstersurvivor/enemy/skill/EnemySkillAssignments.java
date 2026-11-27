package com.example.monstersurvivor.enemy.skill;

import static com.example.monstersurvivor.enemy.skill.EnemySkillDefinition.*;

/** Explicit variant exceptions, then shared species-family policies. Unlisted species deliberately remain melee. */
public final class EnemySkillAssignments {
    private EnemySkillAssignments() { }
    public static EnemySkillDefinition get(String id,String family){
        return switch(id){
            case "mc_crystal_cube_ember","mc_flytrap_walker_fire","mc_dust_devil_ash"->FIREBALL;
            case "mc_crystal_cube_toxic","mc_mushroom_folk_toxic"->TOXIC;
            case "mc_swamp_hag_frost","mc_dust_devil_snow"->FROST_FAN;
            case "mc_pumpkin_head_winter"->SNOWBALL;
            case "mc_ash_hound_frost"->ICE;
            case "mc_flame_skull_soul"->SHADOW_BURST;
            case "mc_wraith_ember"->FIRE_BURST;
            case "mc_lily_snapper_desert"->DUST_FAN;
            case "mc_rock_crab_crystal"->CRYSTAL_SHARD;
            case "mc_lizardfolk_desert"->ROCK;
            default->switch(family){
                case "mc_kobold_miner"->ROCK;
                case "mc_mimic_jar"->COIN;
                case "mc_flytrap_walker"->SEED;
                case "mc_thorn_sprite"->THORN_FAN;
                case "mc_mushroom_folk"->SPORE;
                case "mc_bog_lurker"->POISON;
                case "mc_swamp_hag"->MAGIC_FAN;
                case "mc_dust_devil"->DUST_FAN;
                case "mc_frost_fox"->ICE;
                case "mc_frost_moth"->FROST_FAN;
                case "mc_yeti_cub"->SNOWBALL;
                case "mc_lily_snapper"->BUBBLE;
                case "mc_pumpkin_head","mc_ash_hound"->FIREBALL;
                case "mc_flame_skull"->FIRE_BURST;
                case "mc_magma_slug"->LAVA;
                case "mc_wraith"->SHADOW;
                case "mc_mummy_cat"->CURSE_AOE;
                case "mc_moss_golem","mc_cyclops","mc_crab_knight","rock_golem","sc_golem"->SLAM;
                case "mc_living_armor","mc_jelly"->SPIRIT_SLAM;
                case "mc_goblin_bomber"->TARGET_BOMB;
                case "sc_goblin"->ARROW;
                case "giant_bee","mc_bumble_guard"->STINGER;
                case "red_cap","sc_mushroom"->SPORE;
                case "giant_frog"->BUBBLE;
                case "red_crab","mc_rock_crab","mc_owlbear_cub"->CLAW_SLAM;
                case "living_armor"->SPIRIT_SLAM;
                case "mc_crystal_cube"->CRYSTAL_SHARD;
                case "mc_gargoyle"->ROCK;
                case "mc_pufferfish"->THORN_FAN;
                case "mc_antlion"->DUST_FAN;
                case "mc_frill_lizard","mc_lizardfolk"->POISON;
                case "mc_ratfolk_thief"->COIN;
                default->NONE;
            };
        };
    }
}
