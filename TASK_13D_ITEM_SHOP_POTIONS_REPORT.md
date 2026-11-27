# Task 13D — Shop & Potions

## Implemented

- Added a three-potion shop reachable from Home and Game Mode. It uses `CharacterProgressManager`'s existing coin balance; purchases debit coins and add one item in the same SharedPreferences commit. Inventory is capped at 9 per potion and survives app restarts.
- Added durable consume-before-effect flow: Android commits the item decrement on a worker thread, then posts the result to the game thread. A request lock prevents repeated taps from consuming twice.
- Added Health (10 coins, restore up to 4 HP), Speed (15 coins, +25% normal movement for 8 gameplay seconds), and Cooldown (20 coins, ×1.25 cooldown progress for 8 gameplay seconds). Cooldown applies to Dash, Ultimate, learned class skills and weapon passives; combined Fireball evolution modifiers are capped at ×1.40. Dash distance/iframes and cast, DoT, and effect clocks stay on normal gameplay time.
- Added three distinct HUD buttons below the passive slots with inventory counts and active/disabled states. Demo, diagnostics, Pause, reward modal, full HP, active same-type buffs and empty inventory reject use. Buffs freeze with gameplay and clear on death/session end; unused inventory remains saved.

## Files

- Core: new `consumable/PotionType`, `PotionSystem`, `PotionUseBridge`, and `ui/PotionHud`; updated `CharacterProgressLedger`, `GameSession`, `GameScreen`, `MonsterSurvivorGame`, `Player`, `GameInput`, `GameHud`, `ActiveSkillSystem`, `ClassSkillSystem`, `WeaponSystem`, and `GameStrings`.
- Android: updated `CharacterProgressManager`, `AndroidLauncher`, `HomeActivity`, `GameModeActivity`, and manifest; added `ItemShopActivity`, shop layout/menu, potion icons, and VI/EN strings.
- Added `tools/checks/PotionChecks.java`.

## Verification

- Passed `:core:compileJava :android:compileDebugJavaWithJavac`.
- Passed focused `PotionChecks`: 64 assertions covering wallet transactions/caps, inventory restore values, healing, buffs, Pause/Resume, Demo/diagnostic rejection, HUD hit geometry, Dash/Ultimate cooldowns and Dash distance, and Treasure Chest modal behavior.
- Manual device checks remain for SharedPreferences persistence across process restart, shop touch flow, and HUD placement across Android safe areas. No emulator was run.

The older `UpgradeChecks` and `SkillChecks` helpers still reference the retired `skillSelection`/`choosingWeapon` APIs, so those helpers could not compile. The focused potion checks cover the current Treasure Chest and active-skill paths instead.
