package com.example.monstersurvivor.boss;

import com.example.monstersurvivor.enemy.EnemyDefinition;
import com.example.monstersurvivor.localization.GameStrings.Key;

/** Boss identity/body mapping is separate from the ordinary 82-species registry. */
public record BossDefinition(Kind kind,EnemyDefinition body,Key nameKey) {
    public enum Kind { MINI, LICH_LORD, ENDLESS }
}
