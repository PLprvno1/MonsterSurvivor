# Task 13B — Warrior War Cry & Archer Poison Arrow

## Delivered

- Added `WARRIOR_WAR_CRY` and `ARCHER_POISON_ARROW` to the existing class-skill catalog and reward offers. Each is restricted to its class, upgrades through Lv1–5, and uses the existing two learned-skill limit plus three shared passive slots.
- War Cry activates when at least four living enemies have clear line of sight within 70 units. It deals one bounded AoE hit, pushes surviving ordinary enemies, then grants +1 direct damage to Warrior passive attacks and direct-hit mitigation for the configured duration. It does not block hits or mitigate DoT.
- Poison Arrow fires from the bounded projectile pool toward the nearest visible unpoisoned enemy in range, falling back to a poisoned target when needed. An accepted hit applies one Enemy-owned poison state; reapplication refreshes its power and timer. Poison ticks once per second through `Enemy.damage`, so boss guards and the normal one-time death callback remain in effect. Pause freezes the state; death and match end clear it.
- Added localized Vietnamese/English offer text, icons, War Cry/poison visuals, and the existing poison overlay. No assets or audio were added.

## Tuning

- War Cry cooldowns: `[18,17,16,15,14]s`; buff durations: `[4,4.5,5,5.5,6]s`; direct damage reduction: `[10,12,14,16,18]%`; direct damage bonus: `+1`.
- The design gives no numeric table for War Cry's pulse. To follow its listed level progression and current Warrior skill scale, the implementation uses pulse damage `[1,2,2,2,3]`, radius `[60,60,70,70,70]`, and knockback `[8,8,8,8,12]` units.
- Poison Arrow cooldowns: `[13,12,11,10,9]s`; direct damage `[1,1,2,2,3]`; speed `165`; range `150`; Poison deals `1` damage per second for `[2,2,2.5,2.5,3]s`, capped at `[2,2,2,2,3]` ticks per V2's detailed tuning.

## Files

- Core skill catalog/tuning/rewards: `ClassSkillId.java`, `ClassSkillTuning.java`, `ClassSkillSystem.java`, `SkillOffer.java`.
- Combat/status/lifecycle: `Enemy.java`, `EnemySystem.java`, `Player.java`, `GameSession.java`, `SwordCombat.java`, `WeaponSystem.java`.
- HUD and visuals: `GameStrings.java`, `HudStyle.java`, `ClassSkillRenderer.java`, `EnemySkillRenderer.java`.
- Focused checks: `ClassSkillChecks.java`, `ArcherSkillChecks.java`.

## Verification and remaining notes

- `:core:compileJava :android:compileDebugJavaWithJavac` — passed. Gradle emitted the existing Android SDK XML v4 compatibility warning.
- Focused checks — `ClassSkillChecks` passed 57 assertions; `ArcherSkillChecks` passed 49 assertions.
- No emulator, benchmark, long suite, APK/AAB, or Git commit/push was run.
- Poison tick damage remains fixed at 1 to follow V2's specific tuning; the broad level progression note in Task 13B mentions increasing tick damage at Lv2. No other known blocker remains.
