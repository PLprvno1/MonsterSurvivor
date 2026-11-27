# Task 12B1 – Character Integration

## Result

Added stable `warrior` / `archer` / `mage` profiles. Missing or invalid IDs resolve to Warrior in the Android launch path and core. Existing constructors still select Warrior. Warrior continues to use the existing `player` sprite, stats, controls, Dash, Ultimate, Sword Lv1, and original combat path.

GameMode now has a minimal character spinner and passes the selected ID through `AndroidLauncher`, `MonsterSurvivorGame`, `GameScreen`, and `GameSession`; Retry keeps the same class. Archer starts with Bow Lv1 and Mage with Fireball Lv1. Their reward pools and weapon runtime no longer assume Sword exists; the 3-slot, level, and Evolution rules remain in place. No coins or unlock checks are present yet, so all three classes are selectable in this phase.

## Assets and animation mapping

- **Archer:** freddyka sampler, four-direction 32×32 art, imported as `assets/game/characters/archer.png` (256×640). Mapped Idle, Walk, Attack, Hit→Hurt, and Death in Down/Left/Right/Up: 20 clips, 113 frames total. The atlas preserves source pixels; rendering uses nearest-neighbor. License permits personal/commercial games and modification; standalone asset redistribution is prohibited. Credit is appreciated, not required. License copy is in `assets/game/licenses/freddyka-Archer-License.txt`.
- **Mage:** GandalfHardcore Wizard sheet, imported as `assets/game/characters/mage.png` (192×624). Mapped four-direction Idle, Walk, and Attack plus the supplied single-facing Death animation reused as a direction fallback. No Hurt clip is supplied, so the existing sprite-set fallback displays Idle while Hurt. The source notes allow commercial/non-commercial game use and modification, and prohibit asset redistribution; no credit requirement is stated. Terms are recorded in `assets/game/licenses/GandalfHardcore-Mage-Asset-Notes.txt`.
- Both class textures are loaded from the separate `assets/game/characters.json` catalog with nearest filtering. The source files under `asset_sources` and the Warrior art/catalog entry were left untouched.

## Files changed

- Core: `CharacterId.java`, `CharacterProfile.java`, `GameAssets.java`, `Player.java`, `GameSession.java`, `GameScreen.java`, `MonsterSurvivorGame.java`, `WeaponLoadout.java`, `WeaponSystem.java`, `WeaponRenderer.java`.
- Android: `GameModeActivity.java`, `AndroidLauncher.java`, `activity_game_mode.xml`, `values/strings.xml`, `values-en/strings.xml`.
- Assets and notes: `characters.json`, `characters/archer.png`, `characters/mage.png`, and the two license/terms files above.
- Focused check: `tools/checks/CharacterClassChecks.java`.

## Verification and next phase

- `:core:compileJava :android:compileDebugJavaWithJavac` completed successfully.
- `CharacterClassChecks`: 23 assertions passed, covering all three Lv1 starters, base stats, Warrior fallback, Archer/Mage attack pose and weapon fire, Warrior Sword damage, and no Sword eligibility/offers for Archer/Mage. Catalog validation confirmed required Idle directions and every atlas frame is in bounds.
- A combined run with the older `WeaponChecks` stopped in that existing check at `WeaponChecks.java:115`: it allocates two mode results but iterates three `GameMode` values. That check was not changed for this task.
- Remaining for later phases: offline coin drops/settlement, persistent ownership and purchase gates, coin-based character shop, new class skills and skill HUD. Asset-specific remaining items are authored Mage Hurt art and directional Mage Death art.
