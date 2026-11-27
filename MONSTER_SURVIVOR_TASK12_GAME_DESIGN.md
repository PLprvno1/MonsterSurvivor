# MONSTER SURVIVOR — TASK 12 GAME DESIGN SPEC v1.0

**Purpose:** Implementation contract for Codex and Antigravity. Gameplay decisions below are approved as the starting design; implement rather than invent replacements. If a source incompatibility makes one detail impossible, preserve existing mechanics, document the precise conflict, and implement the nearest source-compatible solution without expanding scope.

## A. Character roster and invariants

| Stable character ID | Display VI / EN | Ownership | Start weapon | Character identity |
|---|---|---|---|---|
| `warrior` | Kiếm sĩ / Warrior | Free, owned and selected on first launch | Sword Lv1 | Short-range attacks, knockback, parry |
| `archer` | Cung thủ / Archer | Unlock 100 coins | Bow Lv1 | Marking, precision shots, area volleys |
| `mage` | Phù thủy / Mage | Unlock 220 coins | Fireball Lv1 | Slow, traps, shield, fireball acceleration |

**Non-negotiable:** Warrior IS current `player`. Keep `assets/game/tiny/player.png`, existing `player` SpriteSet, animations, sprite size, controls, HP10, damage stat1, speed stat10 (80 world units/sec), initial Sword1, Dash1, Ultimate1, all Warrior default constructors and current behavior byte-for-byte where practical. Do not swap Warrior sprite or rebalance it. For Archer/Mage use initial HP10/damage stat1/speed80 too; no hidden bonuses at creation. Preserve existing Easy/Hard, Normal/Demo/Endless, Boss, existing weapon/evolution configs, enemy skills and audio selection.

Character ID passes Native selection -> GameMode Intent/config -> `AndroidLauncher` -> `MonsterSurvivorGame` -> `GameSession`/render; validate ownership on Android launch AND core create. Missing/invalid/unowned ID falls back to `warrior` (not crash). Use string IDs, not enum ordinal for persisted data. `Retry` retains selected owned character, resets run progress; `Home` returns to menu without resetting ownership. Defaults in legacy paths always Warrior.

## B. Visual assets

- Warrior: existing Tiny Questers art, unchanged.
- First free option: GegX Archer https://gegx.itch.io/archer-hero-128 and GegX Mage https://gegx.itch.io/mage-hero-128, if the downloaded license/README agrees with store descriptions: free for games/commercial use, no independent redistribution. Both supply Idle/Walk/static in 8 directions, but not complete Attack/Hurt/Death. Do NOT claim those clips exist.
- If genuine files are inaccessible, do not fabricate or substitute monsters: finish code architecture and list the two needed ZIPs for the owner. Never buy packs without owner action.
- Optional higher fidelity paid alternative: Tiny Questers Elf Archer https://bobddadoo.itch.io/tiny-questers-elf-archer and Mage https://bobddadoo.itch.io/tiny-questers-mage; they match original Warrior better but are NOT assumed owned.
- For free GegX: import 4 directions only (nearest left/right/up/down from existing 8), select a small valid subset of Idle/Walk frames, crop transparent pixels WITHOUT destructive resize, preserve foot pivot as metadata, maintain original pixel art `Nearest`; scale display at render time to comparable visible height to Warrior, keep Player's established *world* collider and foot position. Package into at most two atlases; no 400 individual runtime textures. Missing attack: briefly use facing Walk/Idle pose plus matching projectile/muzzle FX on actual attack tick; Hurt: flash/tint; Death: existing non-loop fall/fade fallback; do not claim authored animation. If animation resources become available later, metadata can be replaced.
- Reuse `evolution-fx.png/json`, `enemy-fx.png/json` for class skills. FX identifiers below are semantic; bind to an ACTUAL verified clip/region before coding. Do not invent atlas clip names.
- License attribution files must accurately identify each imported source. Do not commit or distribute paid or otherwise restricted source packs.

## C. Weapon architecture and skill progression

- `WeaponLoadout` starts with exactly one class weapon; no permanent Sword for Archer or Mage. Existing max **3 automatic weapon slots**, current Lv1–5, existing evolutions unchanged. Class starter weapon uses one slot. `Bow` and `Fireball` are NOT duplicated as newly invented class skills; they remain real WeaponTypes with existing configs/evolution. `Lightning` stays a learnable weapon through Weapon Box. For Archer and Mage, do not offer Sword until matching Sword attack art/behavior is available; Warrior original weapon offer rules unchanged.
- First repair Sword assumptions: `WeaponSystem.update` gates SwordCombat by actual ownership; all references to `loadout.get(SWORD)` check null; `GameSession.chooseWeapon` calls sword cooldown only if Sword equipped. No invisible Sword damage.
- Existing manual Dash + Ultimate (both Lv1, level cap5) remain global, with same buttons, tuning, cooldown and iframe. Existing Skill Box timing and modal selection-token protections remain.
- Ten **new, class-exclusive, automatically triggered** learnable skills are a separate layer, NOT weapon slots and NOT two new touch buttons. Each class may equip up to **2 learned class skills per run**, with Lv1–5; e.g., Mage chooses 2 of 4. No new skill evolution this task. A skill not learned cannot trigger.
- Skill Box offers up to 3 valid picks chosen with existing seeded RNG: unlearned allowed class skill (only if class slots free), upgrade an acquired class skill below Lv5, upgrade Dash or Ultimate below Lv5. Prefer at least one class-skill offer when an empty class slot exists; include an eligible Dash/Ultimate offer when possible. No cross-class skill. Choice is one transaction; no reroll by tapping stale tokens. Once skills are capped, use existing Weapon Box or its stat fallback, never blank rewards.
- Player/weapon upgrades still use Weapon Boxes unchanged. All skill cooldown timers and temporary buffs run on RUNNING gameplay clock, freeze during Pause/modal, clear on Death/Home/Retry. For targeted auto-attacks/buffs, trigger only while an eligible enemy exists in range and skip without consuming cooldown when no valid target. Reactive Parry/Shield instead trigger at the direct-damage boundary and need no target search. One activation must not hit the same target multiple times unless specified.
- Automatic-skill target selection: closest alive LOS-valid enemy within given range, ties by stable enemy ID. Focus Shot gives first priority to active Boss within range/LOS. Aim at target position at *release* once; flying missiles use fixed direction and swept collision (no homing). Ground AoE snapshots one position before telegraph and never moves. Enemy statuses implemented as bounded per-Enemy effects; existing `PlayerStatuses` is **not** the implementation for debuffing enemies.
- All class-skill damage uses the existing player/team damage pipeline, respects Boss damage guard, collision/LOS, and inherits valid damage/pickup stat modifiers as appropriate. No team damage, no unbounded lists or object allocation in hot loops. Bound new class projectiles to 12, live class traps/ground areas to 4, class FX to 16; if a pool is full skip that instance safely, not the whole frame. Keep original projectile and FX pools separate from hostile pools. No new monster SFX.

## D. Exact skill catalog and tuning

Arrays in brackets give **Lv1, Lv2, Lv3, Lv4, Lv5**, in that order. Damage values are baseline skill damage before the existing player damage stat; do not multiply class ability damage by `WeaponConfig` levels. Use original game world units and real gameplay seconds.

### WARRIOR (3 candidates, equip up to 2)

**W1 — Xoáy Kiếm / Whirlwind**, `warrior_whirlwind`: auto circular melee strike centered on Warrior when >=1 enemy within its radius; cooldown `[7,6.5,6,5.5,5]` sec; radius `[30,34,38,42,46]`; per-target damage `[1,1,2,2,3]`; hits each enemy once per activation, wall/LOS checked. Visual rotating short slash pulse, NOT persistent Blade Orbit. FX: existing slash/spark.

**W2 — Sóng Kiếm / Sword Wave**, `warrior_wave`: automatically fires one straight crescent along the facing/nearest target; cooldown `[9,8.5,8,7.5,7]` sec; range 120, speed 145, damage `[1,1,2,2,3]`, max distinct enemies pierced `[2,2,3,3,4]`; lifetime <=0.9sec, wall collision first, dedup each victim, cannot home; existing slash-based projectile FX. Not a Dash.

**W3 — Đỡ Đòn Phản Kích / Parry Counter**, `warrior_parry`: reactive passive. When a direct hit is about to apply (not DOT, not one already canceled by Dash/invulnerability), and incoming strike originates within a 140-degree frontal cone relative to Warrior's last movement/facing, cancel ONE hit and counter once in radius 28. Cooldown `[14,13,12,11,10]` sec starts on a successful block; counter damage `[2,2,3,3,4]`; each target once, Boss guard respected. No trigger from behind or direct DOT tick. FX spark/slash and brief front shield arc. Never alter Dash iframe.

### ARCHER (3 candidates, equip up to 2)

**A1 — Đánh Dấu Thợ Săn / Hunter's Mark**, `archer_mark`: mark one valid nearest enemy (prefer alive Boss in range) within 130 LOS. Cooldown `[12,11,10,9,8]` sec; mark duration `[3,3.5,4,4.5,5]` sec. The first `[1,2,3,4,5]` DISTINCT Bow or Focus Shot hit events on marked enemy add exactly +1 direct damage each; consume one charge per accepted hit. Clear on enemy death, mark expiry and new mark. Non-Archer weapons do not consume or benefit. FX subtle target ring/spark. Never stack marks.

**A2 — Ngắm Bắn Tập Trung / Focus Shot**, `archer_focus`: one strong straight arrow auto-fired at nearest LOS enemy within 170 (active Boss priority), lock direction at release. Cooldown `[9,8.5,8,7.5,7]` sec, damage `[3,3,4,5,6]`, speed 180, range 170, no piercing, one damage event, wall-swept collision. FX existing arrow/arrow impact. Not a homing attack or duplicate Bow multi-volley.

**A3 — Mưa Tên / Arrow Rain**, `archer_rain`: locate the densest live-enemy cluster center within 125 LOS, tie by nearest enemy ID, snapshot center ON START and show 0.4sec warning; fire `[3,3,3,4,4]` pulses 0.22sec apart inside radius `[28,30,32,34,36]`, base damage 1 per pulse. Cooldown `[17,16,15,14,13]` sec; each enemy may receive at most 2 accepted hits PER full activation, regardless of overlapping pulses, Boss burst guard always respected. Visual arrow impacts; no long-lived Burning Ground.

### MAGE (4 candidates, equip up to 2)

**M1 — Băng Tiễn / Ice Shard**, `mage_ice_shard`: one straight projectile at closest valid enemy within 150, speed 160, no homing, stops on wall; cooldown `[7.5,7,6.5,6,5.5]`, damage `[1,1,2,2,3]`. Accepted hit applies Enemy movement slow `[20,22,25,27,30]%` for `[1.5,1.7,1.9,2.1,2.3]` sec; refresh max duration only, no stacks; Boss immune to slow but receives regular hit damage. FX ice_shard/slow overlay. Do not use PlayerStatuses to slow enemy.

**M2 — Bẫy Bí Thuật / Arcane Trap**, `mage_arcane_trap`: place one rune at nearest enemy's SNAPSHOTTED position within 120 LOS. Cooldown `[14,13,12,11,10]` sec; arming delay 0.5sec, total life 3.5sec; trigger when enemy footprint enters radius 19 AFTER arming, then explode radius `[25,28,31,34,37]`, one hit per target with damage `[2,2,3,3,4]`. Expires harmlessly if not triggered; cap 2 live runes, no stacking repeated hits, no wall-crossing damage. FX cast/rune + blast. Trap neither chases nor continuously damages.

**M3 — Khiên Phép / Magic Shield**, `mage_shield`: reactive passive blocks exactly ONE otherwise accepted DIRECT hit from any direction (not poison/burn tick), with no counterattack. Starts cooldown `[20,18,16,14,12]` sec only when block happens; show 0.35sec glow/short impact effect on successful block. Dash/invulnerability already blocking a hit must not consume shield. No extra HP, stacking shield or immortality. FX ring/spark.

**M4 — Dồn Năng Lượng / Arcane Channel**, `mage_channel`: auto-buff while at least one visible enemy within 130 LOS. Cooldown `[18,17,16,15,14]` sec; duration `[3,3.5,4,4.5,5]` sec. While active, native Fireball WEAPON cooldown ×`[0.90,0.85,0.80,0.75,0.70]` and +1 damage to Fireball's PRIMARY direct hit only. Do not add bonus to its splash/meteor/burning ground, other weapons or class skills. Does not accelerate itself; no overlap/stacking and no permanent base config edits. FX cast/glow.

**Implementation detail:** Active auto-skill events should use cooldown/target tables, bounded projectile/area FX and cached visuals. Use existing world collision and damage mechanics; no changes to `WeaponConfig`, `EvolutionConfig`, enemy stats or normal Boss cadence.

## E. Coins, ownership, and settlement

- Stable currency name: Xu / Coins. Character unlock prices Warrior0, Archer100, Mage220. Store in separate `monster_survivor_character_progress` SharedPreferences; keys by stable character ID; defaults unlocked `{warrior}`, selected `warrior`, coin balance0. Must survive Settings reset and normal app relaunch.
- Eligible coin gameplay: **real Normal and real Endless** (Easy/Hard), *not* Demo/Quick Play, preview, fixture, debug or diagnostic. Coins can drop in active eligible runs only. Task09 official Normal statistics remain COMPLETELY SEPARATE: accepting an Endless coin settlement must not mutate `official_v1` achievement counters. Endless earns currency but not Normal achievements.
- On ordinary enemy death transition, once per actor death ID, 12% chance of 1 coin (use gameplay seeded RNG; no reward on corpse repeat/summon despawn). Materialize a lightweight pickup at death, lifetime20s, max48 active coin entities, collected once inside pickup radius12; visible coin count on gameplay HUD. No wallet write on pickup. If pool full, skip new ordinary coin drop rather than create a leak.
- Mini Boss defeat gives +10 run coins directly at confirmed death; Lich Lord +25 directly at confirmed death (so game cannot enter Victory before collecting them); each Endless Boss confirmed death +12 directly. These amounts must be awarded once per Boss identity. Optional understated coin fly-text/FX; no new audio. Normal Victory completion adds +15 run coins once. No award on merely spawning Boss.
- At Game Over/Victory, or when user deliberately selects Return Home from Pause after confirmation, settle all **collected run coins** to wallet. Do not award coin objects still on the ground. Home-quit currency settlement is independent of MatchResult: it MUST NOT mark an unfinished Normal match as completed in Task09 statistics. Never grant on loading GameMode or displaying Game Over twice.
- Settlement API `creditRunCoins(matchUuid, amount, provenance)` checks normal/endless, not diagnostic/debug/preview, and confirms UUID and eligible session; saves **balance + processed UUID ledger** in one `commit()` and rejects duplicate UUID even after restart. Ensure both result and quit paths call the same idempotent settlement; each session UUID has at most ONE settlement. Clamp additions against overflow; cannot go negative. Currency not written for an active run on every pickup. Process kill before any result or explicit Home quit can lose unbanked run coins; document honestly (no complex autosave for Task12).
- Unlock API: atomic `purchase(characterId)` checks valid locked ID, balance >= cost, subtracts exactly cost and records unlocked ID in one commit; repeat purchases do nothing. Selection only for owned characters; verify again when launching. No Internet/backend, achievements reset, or simulated currency. UI locked character can preview but Start disabled until unlocked; after purchase auto-select purchased hero.

## F. UI workflow and localization

`Home -> Character Select -> GameMode(Easy/Hard + Normal/Demo/Endless) -> Gameplay -> GameOver/Victory -> Home/Retry`.

- Character Select: Warrior card default and `Available / Đã sở hữu`; Archer `100 Xu`, Mage `220 Xu` when locked; silhouette/actual preview, role description and starting weapon, selection frame for owned heroes; current wallet shown; `Unlock / Mở khóa` confirmation and failed-insufficient-coin message; `Select / Chọn` button. Cannot Start with locked character. Back retains previous selection. GameMode displays selected hero name briefly without disrupting UI04 styles.
- In gameplay: starting weapon slot shows Sword/Bow/Fireball based on class, alongside original Dash/Ultimate. Up to TWO small class skill icons visible only when learned, with level/cooldown; coin pickups produce discrete wallet-in-run indicator. Don't add touch buttons for automatic skills; retain joystick/multitouch. Existing 3 weapon slots unchanged.
- Native UI owned by Antigravity; Codex should implement only minimum data contracts/navigation hooks, no style overhaul. VI/EN for class/skill names and clear concise descriptions. Do not expose debug flags, technical descriptions, or fake preview achievements in normal UI.

## G. Phased implementation (no agents concurrently modifying same repo)

**12B1 — Core + assets.** Add `CharacterId`/profiles, class selection data path, legacy Warrior default, Bow/Mage start paths, remove Sword hard-assumption, load actual free Archer/Mage atlas when available with correct pivot/fallback. *Do not implement 10 skills or currency.* Do not touch Warrior texture. Only compile and a few focused source checks; no emulator. Report concise blocker if ZIP unavailable; don't fabricate assets.

**12B2a — Skill infrastructure + Warrior.** Implement Skill Box class-skill candidate/selection and max two class slots, cooldown/target/render infrastructure, then exactly W1–W3. Keep Dash/Ultimate options.

**12B2b — Archer.** Implement exactly A1–A3 using shared 12B2a infrastructure; do not duplicate generic systems.

**12B2c — Mage.** Implement exactly M1–M4 using shared infrastructure and bounded Enemy slow; do not alter enemy presets or PlayerStatuses. Each subphase: compile and 2–4 focused checks, no emulator automation or long suites.

**12B3 — Coins & unlocking.** Implement real drops/collection, boss payouts, settlement on legitimate result/Home, separate persistent ownership wallet/UUID idempotency, validate character selection. Do not modify Task09 official Normal stat logic, audio or unrelated UX. Compile and focused duplicate/eligibility check only.

**UI07 — Antigravity:** Character Select and Unlock screen, wallet display, active skill HUD polish if needed, bilingual strings/icons, consistent fantasy theme; no gameplay algorithm or persistence rewrite. Owner performs emulator checks after each phase.

Every phase: no automatic APK/AAB export, release, publishing, Docker, Git commit/push, large automated testing, benchmark or unrelated refactor. Deliver only changed source and a concise report of what was changed and any blockers.
