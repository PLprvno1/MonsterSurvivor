# Task 13C — Class Passive Evolutions

## Evolution rules

- A class passive becomes eligible at Lv5 and evolves only when selected from the passive branch of the Treasure Chest. It stays Lv5 and keeps its current slot.
- Warrior, Archer and Mage can evolve only their own class skills. The shared limit remains three evolved passives across class skills and legacy weapons.
- The chest includes an eligible evolution in its passive offers, avoids duplicate offers, and rechecks the cap and ownership when the choice is committed.
- Evolution does not add another slot or activate automatically. Pause/modal clocks and match cleanup continue through the existing session lifecycle.

## Eleven new evolutions

| Class passive | Evolution | Implemented effect |
|---|---|---|
| Warrior — Whirlwind | Double Sword Cyclone | Repeats the Lv5 360° hit after 0.28s. The second pulse deals 75% damage, floored with a minimum of 1. Each pulse hits a target once; the Lv5 cooldown remains 5s. |
| Warrior — Sword Wave | Triple Sword Qi | Fires three non-homing waves at −12°, 0° and +12°. Each uses Lv5 damage, speed 145, range 120 and the Lv5 lane pierce budget. A shared cast ledger limits each target to two hits; walls stop the waves. |
| Warrior — Parry Counter | Sacred Counter Shield | Blocks one direct hit from any direction, then counters once per enemy within radius 34 using Lv5 damage. Cooldown stays 10s; DoT and Dash iframe behavior are unchanged. |
| Warrior — War Cry | War God's Cry | Keeps the 14s Lv5 cooldown and adds 1s to the Lv5 buff (6s to 7s). Adds a no-damage knockback pulse within radius 40 against ordinary enemies only; the original damage pulse still occurs once. |
| Archer — Hunter's Mark | Predator's Mark | When the marked enemy dies, transfers once to the nearest visible enemy within radius 65. It keeps remaining time and at most two unused bonus hits. |
| Archer — Focus Shot | Heartpiercer | The straight, swept projectile pierces up to two targets. The second takes 70% of Lv5 damage, floored with a minimum of 1. Hunter's Mark and the Boss damage guard still apply. |
| Archer — Arrow Rain | Heavenly Rain | Adds one pulse to the Lv5 cycle (five total at 0.22s spacing after the existing 0.4s warning). Each target can take at most three accepted hits per activation; no ground fire is created. |
| Archer — Poison Arrow | Infectious Poison | A death while carrying Poison Arrow poison spreads the remaining poison damage/ticks/time to up to two nearest visible enemies within radius 45. Spread applications are marked non-spreading, so they cannot chain. |
| Mage — Ice Shard | Ice Shatter Chain | An accepted Lv5 hit creates three one-hit shards from the impact. They use half damage and half slow strength, with the existing slow duration, projectile speed/range and wall collision. The implementation uses a forward fan at −45°, 0° and +45° because the design does not specify shard angles. |
| Mage — Arcane Trap | Arcane Matrix | A triggered Lv5 trap deals two radius-37 pulses 0.35s apart. Each pulse checks LOS and hits each enemy once. The trap occupies one of the existing two trap slots until the second pulse. |
| Mage — Magic Shield | Spell Shield | A successful direct block emits one radius-30 shockwave for 1 damage. The Lv5 shield cooldown remains 12s and starts only on a successful block. |

The Mage shard damage/slow percentages use integer floor with a minimum of 1. Poison spread inherits the poison state remaining at death; secondary applications cannot spread. No new assets or audio were added; existing slash, spark, arrow, ice, poison, rune/blast and shield FX are reused.

## Four existing evolutions

The weapon evolution implementation and tuning were left in place: Sword → Blade Orbit, Bow → Arrow Storm, Fireball → Inferno Meteor, and Lightning → Thunder Web. The Fireball evolution continues to own the existing Arcane Channel passive behavior; Arcane Channel remains outside the Mage reward catalog. Focused checks confirmed all four still reach Lv5, remain eligible, retain Lv5 after evolution, and have localized names/descriptions. Their combat/render code was not refactored.

## Files changed

- `core/src/main/java/com/example/monstersurvivor/skill/ClassSkillSystem.java`
- `core/src/main/java/com/example/monstersurvivor/skill/ClassSkillTuning.java`
- `core/src/main/java/com/example/monstersurvivor/skill/SkillOffer.java`
- `core/src/main/java/com/example/monstersurvivor/skill/TreasureChestOffer.java`
- `core/src/main/java/com/example/monstersurvivor/skill/TreasureChestSelection.java`
- `core/src/main/java/com/example/monstersurvivor/skill/ClassSkillRenderer.java`
- `core/src/main/java/com/example/monstersurvivor/entity/Enemy.java`
- `core/src/main/java/com/example/monstersurvivor/gameplay/GameSession.java`
- `core/src/main/java/com/example/monstersurvivor/localization/GameStrings.java`
- `core/src/main/java/com/example/monstersurvivor/ui/SkillRewardHud.java`
- `core/src/main/java/com/example/monstersurvivor/ui/WeaponHud.java`
- `tools/checks/ClassSkillEvolutionChecks.java`

## Verification

- `:core:compileJava :android:compileDebugJavaWithJavac` — passed. Gradle printed the existing Android SDK XML version warning.
- `python tools/run_skill_checks.py ClassSkillEvolutionChecks ClassSkillChecks ArcherSkillChecks` — passed: 181, 57 and 49 assertions.
- No emulator, benchmark, APK/AAB export or long suite was run.
- The repository's older `EvolutionChecks` and `EvolutionBalance` helpers are not compatible with the post-13A class-specific loadout/unified chest API, so they could not verify the full legacy combat scenarios. Their gaps are limited to the old helper coverage; the weapon evolution implementation itself was not changed.

## Remaining work

No known implementation blocker for the 11 evolutions. Visual timing and feel still need the project's manual gameplay review. Task 13D remains responsible for the item shop and potion inventory/use flows (Healing, Speed and Cooldown potions); those systems were not started here.
