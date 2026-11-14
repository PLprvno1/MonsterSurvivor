Pixel RPG Monsters Top-Down (Free Demo)
=======================================

A free sample of Pixel RPG Monsters Top-Down: 12 monsters and 2 bosses with
  every animation in the four directions, projectiles, impacts, attack
  warnings and damage numbers, plus two playable Godot 4 demos.

NEW IN v2 (free update)
  2 new monsters (fire elemental, lesser demon) and the new boss lich lord
    with its phase 2, the 5 new animations for the demo monsters, all the
    effects and the new Godot 4 demo (demo_v2/).

WHAT'S INSIDE
  12 monsters (32 x 32) in 8 families and 2 bosses (64 x 64)
    Meadow: honey slime, wild boar, giant bee
    Forest: red cap
    River & Coast: red crab, giant frog
    Mountain & Caves: rock golem
    Castle: living armor, mimic
    Ruins & Graveyard: bone hound
    Demons: lesser demon
    Elementals: fire elemental
    Bosses: boar king, lich lord
  15 complete sprite sets: 0 elite, 0 champion (with aura) and 1 boss phase
    2
  10 animations x 4 directions: 628 strips and 3,604 frames
  12 body types
  109 effect strips (674 frames): projectiles in 4 directions, impacts and
    area shockwaves for 11 elements, attack warnings and damage number pop-
    ups, plus single damage digits in 5 colors
  sheets + JSON, strips, single frames, .aseprite, x1/x2/x4, Godot 4
    SpriteFrames for every set and playable Godot 4 demos

ANIMATIONS (every monster, every direction)
  v1: idle 4/6, walk 6, attack 5/6, hurt 3, death 5/6
  run 6 frames (NEW): a fast charge or dash with a forward lean, bounce and
    dust (loops)
  special 6 frames (NEW): a ranged or special attack: it charges an orb of
    its element and fires it (pair it with the projectile of its element in
    fx/)
  spawn 7 frames (NEW): rises out of a glowing pit in the ground
  stun 6 frames (NEW): dazed, swaying with stars circling over its head
    (loops)
  dissolve 7 frames (NEW): a second death: it turns into energy, dissolves
    into sparks and leaves a small pile of ash
  entrance 10 frames (NEW, bosses): boss intro: rises from the pit and roars
  slam 8 frames (NEW, bosses): boss area attack: wind-up and ground slam
    (pair it with an area shockwave)
  phase_shift 6 frames (NEW, phase 2 sets): phase-two bosses: the
    transformation from phase 1 to phase 2
  run, special, stun and slam play in place: move the monster in your game
    while they play. spawn, dissolve, death and entrance stay on their last
    frame.

FOLDERS (ROOT = the willosuna_rpg_monsters/ folder)
  ROOT/x1/<family>/<monster>/<name>_sheet.png   one sheet per set: one row
    per animation and direction
  ROOT/x1/<family>/<monster>/<name>_sheet.json   frame rectangles + tags
    (TexturePacker JSON hash)
  ROOT/x1/<family>/<monster>/strips/<name>_<anim>_<dir>.png   one strip per
    animation and direction
  ROOT/x1/<family>/<monster>/frames/<name>/<anim>_<dir>/frame_NN.png
    single frames
  ROOT/x1/<family>/<v1 monster>/<name>_v2_sheet.png and .json   NEW: the v2
    animations of the v1 monsters and their elites, in their own sheet so
    the v1 sheet stays the same; their strips and frames sit next to the v1
    ones
  <name>_champion_sheet.png   NEW: champion sets (every animation, v1 and
    v2, in one sheet)
  ROOT/x1/bosses/<boss>/<boss>_phase2_sheet.png   NEW: phase 2 of the new
    bosses
  ROOT/x1/fx/   NEW: projectiles/<element>/<element>_<dir>.png,
    impacts/<element>.png, area/<element>_shockwave.png,
    telegraphs/circle_<size>.png and line_<length>_<dir>.png,
    damage_numbers/<color>/<glyph>.png and damage_numbers/popups/; single
    frames in fx/frames/
  ROOT/x2/ and ROOT/x4/   sheets, JSON and strips at 2x and 4x (nearest);
    single frames are at x1
  ROOT/aseprite/<family>/<name>.aseprite   one tag per animation+direction
    (v1 sets: <name>_v2.aseprite has the new animations)
  ROOT/godot/spriteframes/<family>/<name>.tres   Godot 4 SpriteFrames; for
    v1 sets <name>_v2.tres has the new animations and <name>_full.tres has
    all of them; godot/spriteframes/fx/ has projectiles, impacts, area,
    telegraphs and damage_popups
  ROOT/manifest.json   every set: family, variant, element, frame size, feet
    line, animations, frames, fps, loop, sheet row, file paths and the
    projectile, impact and area effect that go with it
  <family> = meadow, forest, river, mountain, castle, ruins, snow, undead,
    demons, elementals, insects, swamp, constructs, frost, slimes (bosses
    are in bosses/)
  <name> = the monster, <monster>_elite, <monster>_champion or
    <boss>_phase2;  <dir> = down, left, right, up
  <element> = fire, water, earth, air, ice, poison, shadow, hellfire,
    arcane, lightning, nature
  project.godot + demo/ (v1 demo) + demo_v2/ (new demo, main scene).
    demo/map/ is the demo map from Pixel RPG World Tileset, demo/heroes/ and
    demo_v2/heroes/ have heroes from Pixel RPG Heroes Top-Down; they are
    there only so the demos run.

SIZES AND PIVOT
  Monsters: 32 x 32 frames, feet on y = 28. Bosses: 64 x 64 frames, feet on
    y = 56. Pivot = bottom centre. Every frame of a monster uses the same
    canvas, so animations swap without jumping. All four directions are
    drawn (nothing is mirrored). Projectiles are 16 x 16 (pointing in their
    direction), impacts 32 x 32, area shockwaves 128 x 64 (centre them on
    the boss's feet).

HOW TO USE
  GODOT 4 (4.3+): open the zip folder as a project and press Play
    (demo_v2/). The v1 demo is in demo/monster_demo.tscn.
  Drop any SpriteFrames on an AnimatedSprite2D (animations are named like
    special_left) and set offset.y = width / 2 - feet line (32 px: -12, 64
    px: -24). Fire the projectile of the monster's element (manifest:
    projectile) on the middle frame of special.
  ASEPRITE: one .aseprite per set, one tag per animation and direction,
    durations already set.
  UNITY: import a sheet, Sprite Mode = Multiple, Filter = Point, Compression
    = None, slice Grid By Cell Size 32 x 32 (64 for bosses), custom pivot
    (0.5, 0.125). Build clips with the rows, frame counts and fps from
    manifest.json or the sheet JSON.
  GAMEMAKER: import <name>_<anim>_<dir>.png from strips/ as a sprite with N
    frames, origin (16, 28) or (32, 56).
  RPG MAKER, PHASER, DEFOLD, LOVE2D AND OTHERS: use the sheets with the
    JSON, the strips or the single frames.
  THE TILESET AND THE HEROES (sold separately):
  https://willosuna.itch.io/pixel-rpg-world-tileset
  https://willosuna.itch.io/pixel-rpg-heroes-top-down

LICENSE
  See LICENSE.txt. Commercial and personal use, royalty free, no credit
    required. Do not resell or redistribute the files as an asset pack.

THE FULL PACK
  103 monsters, 14 bosses, 357 sprite sets and 83,744 frames:
  https://willosuna.itch.io/pixel-rpg-monsters-top-down

ENJOYING IT?
  If this pack helps your game, a quick rating on the itch.io page is the
    best way to support it and keeps the free updates coming.
  https://willosuna.itch.io/pixel-rpg-monsters-top-down/rate

