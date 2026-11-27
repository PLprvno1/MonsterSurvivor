# Task 12B2a – Warrior Skills

## Implemented

- Added shared class-skill IDs, per-run two-slot loadout, seeded Skill Box offers (up to three), level snapshots, stale-token checks and class ownership filtering. Archer/Mage IDs are reserved but unavailable until their phases implement them.
- Skill Boxes retain Dash/Ultimate choices. When no legal skill upgrade remains, collection follows the existing Weapon Box/stat reward path. Cooldowns and FX advance only during RUNNING; death/home clears timers and projectiles, and Retry creates a fresh loadout.
- Added the specified Warrior tables and behavior for **W1 Whirlwind**, **W2 Sword Wave**, and **W3 Parry Counter**. Skill damage uses the class-skill base value with the current Player damage stat; WeaponConfig levels are not applied. W2 uses a fixed 12-projectile pool and swept wall/enemy collision; class FX are capped at 16.
- W3 is wired after Player iframe checks. Direct contact, boss attacks and enemy projectiles/area attacks now pass their source position to that boundary; poison/burn ticks still use the non-direct damage path. Existing attack patterns and damage values are unchanged.
- Reused existing `slash` and `spark` frames from `assets/game/evolution-fx`; added small HUD indicators for learned class skills with level/readiness/cooldown. They add no input controls or weapon slots.

## Main files

- New: `ClassSkillId.java`, `ClassSkillTuning.java`, `ClassSkillSystem.java`, `ClassSkillRenderer.java` under `core/src/main/java/com/example/monstersurvivor/skill/`.
- Updated: `GameSession.java`, `Player.java`, `SkillOffer.java`, `SkillSelection.java`, `GameScreen.java`, `GameStrings.java`, `SkillHud.java`, `SkillRewardHud.java`, `HudStyle.java`.
- Direct-hit source routing: `SwordCombat.java`, `BossSystem.java`, `BossProjectiles.java`, `EnemyProjectiles.java`, `EnemySkillSystem.java`.
- Added focused logic check: `tools/checks/ClassSkillChecks.java`.

## Verification

- `:core:compileJava :android:compileDebugJavaWithJavac` — passed.
- `python tools/run_skill_checks.py ClassSkillChecks` — passed, 45 assertions covering tuning, slot/class filtering, Skill Box offers and tokens, W1, W2 piercing, pause cooldowns, and W3 front/rear/Dash/DoT behavior.
- No emulator or long test suite was run. No Native Lobby, coin/unlock, Archer/Mage skill behavior, or weapon progression was changed.
