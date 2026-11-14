package com.example.monstersurvivor.entity;

/** Original API aliases. The full roster lives in EnemyRegistry, not in this legacy enum. */
public enum EnemyType {
    SLIME("tiny_slime", false), BAT("tiny_bat", true), WILD_BOAR("wild_boar", false);
    public final String spriteId;
    public final boolean flying;
    EnemyType(String spriteId, boolean flying) { this.spriteId=spriteId; this.flying=flying; }
}
