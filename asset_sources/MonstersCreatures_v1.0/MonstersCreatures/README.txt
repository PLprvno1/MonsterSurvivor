================================================================
 MONSTERS & CREATURES   (16x16 top-down asset pack)
 Pack 9 of the free series - v1.0
================================================================

WHAT IS IT
40 new top-down RPG monsters with full 4-direction animations, 6
bosses (48x48 / 64x64) with intro roar, two attacks and a phase-2
look, projectiles + impacts, combat FX, status effects, 46 monster
drops, coins/gems/chests/hearts, a combat UI and a bestiary.
Same 16x16 grid, 3/4 top-down view and 58-colour palette as packs
1-8 (no new colours).

HONEST COUNT: 3456 unique ORIGINAL sprites & frames  (we say "3400+")
How it was counted (code, not by hand):
- every counted sheet is cut into its frame cells and md5-deduplicated
  across the WHOLE pack - a frame that appears in several files counts
  once; empty cells are never counted.
- NOT in the headline (listed separately):
    mirrored left-facing frames ........ 939
    90-degree rotated projectile frames . 112
    recolour variants .................. 2108  (27 elite variants, below)
    near-duplicates (1-3 px from another
    frame, e.g. death-smoke specks) ..... 351
  Everything together: 6966 unique cells.
- strips, single PNGs, scenes, GIFs and previews are copies of the
  counted sheets and are not counted again.

Original frames per category:
  Monsters            2468
  Bosses               331
  Projectiles FX       221
  Status Effects        96
  Loot Drops           128
  Combat UI            119
  Bestiary              93

Overlap with packs 2/3 (checked in code): packs 2/3 already have
slimes, bats, rats, wolves (incl. ice wolf), spiders, beetles,
scorpions, snakes, ghosts, wisps, imps, mushroom monster, golems,
mimic chest, skeletons, goblins, orcs, frogs, leeches, crabs, sea
serpent, harpy, hornets, carnivorous plant, boar, yeti, treant and the
bosses slime king, lich, dragon, crystal golem, kraken. This pack
therefore uses NEW species (crystal CUBE instead of a slime, mimic
JAR instead of a chest, bat SWARM, hermit rock crab, bone hound, roller
bug, jackalope, flytrap walker, bog lurker, giant mosquito, frill
lizard, frost fox, yeti CUB, flame skull, crab knight, ...) and new
boss designs: Elder Treant 64px, Gel Monarch (crowned gel cube),
Dragon Whelp, Bone Archmage (light violet robes), Tide Kraken (rises
out of the water), Golem King.

----------------------------------------------------------------
FOLDERS
----------------------------------------------------------------
01_Monsters_<Biome>/<name>/
    <name>_<W>x<H>_sheet.png   full sheet (layout below)
    strips/<name>_<action>_<dir>_<N>f.png
    recolors/<name>_<variant>_recolor_<W>x<H>_sheet.png
02_Bosses/<name>/<name>_<S>x<S>_sheet.png + strips/
03_Projectiles_FX/  Projectiles/ (right/left/up/down + Impacts/),
                    Combat_FX/, Shadows/ (solid + dithered, 16/32/64)
04_Status_Effects/  12 icons (16x16) + animated overlays 16 & 32 px
05_Loot_Drops/      Drops/ (46 icons + sheet), Pickups/ (coins, gems,
                    piles, hearts, orbs, sparkle), Chests/
06_Combat_UI/       Health_Bars/, Boss_Bar/ (bar + name plates),
                    Damage_Numbers/, Markers/, Loot_Popup/, Banners/
07_Bestiary/        16x16 icon per monster & boss (+ sheet), a
                    192x128 page per creature + blank page
Example_Scenes/     8 scenes (320x176) + 2x GIFs
Previews/           overview pages (1280x720)
Palette/            palette_58.png / .gpl / .hex

----------------------------------------------------------------
MONSTER SHEET LAYOUT (all 40 monsters)
----------------------------------------------------------------
Rows = actions x directions, top to bottom:
  idle (4f), walk (4f), attack (5f), hurt (3f), death (6f),
  special (5f)   -> 6 actions x 4 directions = 24 rows
  inside each action: down, up, right, left (left = mirrored right)
Columns = frames, left to right (sheet is 6 columns wide; unused
cells are empty). Frame size = the cell size in the file name
(16x16, 16x32 or 32x32).
Attack frames: 0-1 wind-up, 2 = HIT frame (spawn the hitbox /
projectile here), 3 follow-through, 4 recover.
Hurt: 0 = white flash + knock-back, 1-2 recover.
Death: 0 flash, 1-3 collapse, 4-5 smoke poof (destroy after 5).
Pivot / feet point: bottom-centre of the cell (x = W/2, y = H-2).
Flyers (sprite, moth, mosquito, jelly, skulls...) hover: put the
shadow (03_Projectiles_FX/Shadows) at the feet point.
Loops: idle & walk loop (every frame changes pixels - checked).

MONSTERS BY BIOME
  Forest    (6): mushroom_folk, thorn_sprite, jackalope, moss_golem, owlbear_cub, flytrap_walker
  Cave      (9): crystal_cube, rock_crab, bat_swarm, mimic_jar, bone_hound, roller_bug, kobold_miner, living_armor, gargoyle
  Swamp     (5): bog_lurker, giant_mosquito, swamp_hag, lily_snapper, lizardfolk
  Desert    (4): antlion, mummy_cat, dust_devil, frill_lizard
  Snow      (3): frost_fox, yeti_cub, frost_moth
  Volcanic  (3): magma_slug, flame_skull, ash_hound
  Coast     (3): crab_knight, jelly, pufferfish
  Night     (3): pumpkin_head, shadow_cat, wraith
  Plains    (4): goblin_bomber, bumble_guard, ratfolk_thief, cyclops

STATS + SUGGESTED HITBOX (x, y, w, h inside the cell, top-left origin)
  mushroom_folk    Forest    16x32  HP  14  DMG  3  normal hitbox x3 y22 10x8  special: spore_cloud
  thorn_sprite     Forest    16x16  HP   6  DMG  2  fast   hitbox x3 y6 10x8  special: thorn_volley
  jackalope        Forest    16x16  HP   8  DMG  2  fast   hitbox x3 y6 10x8  special: antler_charge
  moss_golem       Forest    32x32  HP  60  DMG  8  slow   hitbox x6 y12 20x16  special: root_slam
  owlbear_cub      Forest    16x16  HP  16  DMG  4  normal hitbox x3 y6 10x8  special: screech
  flytrap_walker   Forest    16x32  HP  18  DMG  4  slow   hitbox x3 y22 10x8  special: seed_spit
  crystal_cube     Cave      16x16  HP  20  DMG  3  slow   hitbox x3 y6 10x8  special: split
  rock_crab        Cave      16x16  HP  18  DMG  3  slow   hitbox x3 y6 10x8  special: shell_hide
  bat_swarm        Cave      16x16  HP   6  DMG  1  fast   hitbox x3 y6 10x8  special: scatter
  mimic_jar        Cave      16x16  HP  16  DMG  4  normal hitbox x3 y6 10x8  special: coin_spit
  bone_hound       Cave      32x32  HP  22  DMG  5  fast   hitbox x6 y12 20x16  special: reassemble
  roller_bug       Cave      16x16  HP  14  DMG  3  normal hitbox x3 y6 10x8  special: roll
  kobold_miner     Cave      16x16  HP  10  DMG  3  normal hitbox x3 y6 10x8  special: dig
  living_armor     Cave      16x32  HP  36  DMG  6  slow   hitbox x3 y22 10x8  special: spirit_slash
  gargoyle         Cave      16x16  HP  22  DMG  4  normal hitbox x3 y6 10x8  special: stone_skin
  bog_lurker       Swamp     16x16  HP  16  DMG  3  slow   hitbox x3 y6 10x8  special: submerge
  giant_mosquito   Swamp     16x16  HP   6  DMG  2  fast   hitbox x3 y6 10x8  special: drain
  swamp_hag        Swamp     16x32  HP  24  DMG  5  slow   hitbox x3 y22 10x8  special: hex_bolt
  lily_snapper     Swamp     16x16  HP  20  DMG  4  slow   hitbox x3 y6 10x8  special: bubble_spit
  lizardfolk       Swamp     16x32  HP  26  DMG  5  normal hitbox x3 y22 10x8  special: tail_whip
  antlion          Desert    16x16  HP  16  DMG  4  slow   hitbox x3 y6 10x8  special: burrow
  mummy_cat        Desert    16x16  HP  10  DMG  2  fast   hitbox x3 y6 10x8  special: curse
  dust_devil       Desert    16x32  HP  14  DMG  3  fast   hitbox x3 y22 10x8  special: sand_blast
  frill_lizard     Desert    16x16  HP  12  DMG  3  fast   hitbox x3 y6 10x8  special: frill_display
  frost_fox        Snow      16x16  HP  12  DMG  3  fast   hitbox x3 y6 10x8  special: ice_breath
  yeti_cub         Snow      16x16  HP  18  DMG  3  normal hitbox x3 y6 10x8  special: snowball
  frost_moth       Snow      16x16  HP   8  DMG  2  normal hitbox x3 y6 10x8  special: frost_dust
  magma_slug       Volcanic  16x16  HP  20  DMG  4  slow   hitbox x3 y6 10x8  special: lava_trail
  flame_skull      Volcanic  16x16  HP  10  DMG  4  fast   hitbox x3 y6 10x8  special: fire_burst
  ash_hound        Volcanic  32x32  HP  30  DMG  6  fast   hitbox x6 y12 20x16  special: flame_breath
  crab_knight      Coast     16x32  HP  30  DMG  5  normal hitbox x3 y22 10x8  special: shell_guard
  jelly            Coast     16x16  HP   8  DMG  3  normal hitbox x3 y6 10x8  special: shock_pulse
  pufferfish       Coast     16x16  HP  10  DMG  3  normal hitbox x3 y6 10x8  special: puff_up
  pumpkin_head     Night     16x32  HP  26  DMG  5  normal hitbox x3 y22 10x8  special: flame_spit
  shadow_cat       Night     16x16  HP  10  DMG  3  fast   hitbox x3 y6 10x8  special: shadow_blink
  wraith           Night     16x32  HP  20  DMG  4  normal hitbox x3 y22 10x8  special: soul_lantern
  goblin_bomber    Plains    16x16  HP  10  DMG  6  normal hitbox x3 y6 10x8  special: big_bomb
  bumble_guard     Plains    16x16  HP   8  DMG  2  normal hitbox x3 y6 10x8  special: pollen_cloud
  ratfolk_thief    Plains    16x16  HP  10  DMG  3  fast   hitbox x3 y6 10x8  special: smoke_bomb
  cyclops          Plains    16x32  HP  40  DMG  7  slow   hitbox x3 y22 10x8  special: ground_pound

WHAT THEY DO
  mushroom_folk    Parasol-capped forest folk with a twig spear; puffs sleepy spores.  [recolours: toxic]
  thorn_sprite     Bramble ball with leaf wings that fires a ring of thorns.
  jackalope        Hopping hare with antlers; charges in a straight line.  [recolours: snow]
  moss_golem       Moss-covered stone mound; slams the ground and calls up roots.  [recolours: autumn]
  owlbear_cub      Fluffy owl-faced bear cub; swipes and screeches to stun.
  flytrap_walker   Uprooted flytrap that chomps and spits hard seeds.  [recolours: fire]
  crystal_cube     Gelatinous cube full of crystals; splits in two when hit hard.  [recolours: toxic, ember]
  rock_crab        Hermit crab under a boulder shell; hides to block damage.  [recolours: crystal]
  bat_swarm        Three cave bats that move as one and scatter when hit.
  mimic_jar        A clay jar with teeth; hops after you and spits coins.  [recolours: gold]
  bone_hound       Skeletal hound with ghost-fire eyes; falls apart and rebuilds itself.
  roller_bug       Armored pill bug that curls up and rolls at you.  [recolours: lava]
  kobold_miner     Little kobold with a candle helmet and a pickaxe.  [recolours: blue]
  living_armor     Empty plate armor driven by a ghost light; wide spirit slashes.  [recolours: gold, dark]
  gargoyle         Stone imp with bat wings; turns to statue to shrug off hits.
  bog_lurker       Mud mound with eye stalks; sinks into the bog and ambushes.
  giant_mosquito   Dive-stabbing mosquito; its belly swells red when it drains.  [recolours: plague]
  swamp_hag        Hooded bog witch with a toad-lantern staff; throws green hexes.  [recolours: frost]
  lily_snapper     Snapping turtle with a lily pad growing on its shell.  [recolours: desert]
  lizardfolk       Scaled marsh warrior with a spear and a whipping tail.  [recolours: desert]
  antlion          Sand-pit larva with huge mandibles; burrows and waits.
  mummy_cat        Bandaged temple cat with glowing eyes; curses the unwary.  [recolours: shadow]
  dust_devil       A living whirlwind of sand that blinds with a sand blast.  [recolours: ash, snow]
  frill_lizard     Desert lizard that flares a red frill to stun before it bites.
  frost_fox        White fox with a tail of ice crystals; breathes a frost cone.  [recolours: arctic_night]
  yeti_cub         Round fluffy yeti cub that throws snowballs.
  frost_moth       Pale moth that sheds freezing dust from its wings.  [recolours: luna]
  magma_slug       Crusted slug with lava cracks; leaves burning puddles.
  flame_skull      Floating skull crowned with fire; explodes in a fire ring.  [recolours: soul]
  ash_hound        Charcoal hound with a burning mane; breathes fire.  [recolours: frost]
  crab_knight      Armored crab soldier with a trident and a claw shield.  [recolours: royal]
  jelly            Drifting jellyfish that stings and sends out a shock ring.  [recolours: sea_glass]
  pufferfish       Beached pufferfish that flops around and inflates into a spiky ball.
  pumpkin_head     Scarecrow with a carved pumpkin head and a sickle.  [recolours: winter]
  shadow_cat       Inky cat that dissolves into shadow and reappears.
  wraith           Grey hooded wraith carrying a soul lantern.  [recolours: ember]
  goblin_bomber    Goggled goblin that lobs fused bombs.
  bumble_guard     Big fuzzy bee guarding wildflower meadows; sneeze-inducing pollen.
  ratfolk_thief    Hooded rat thief with a dagger; vanishes in a smoke bomb.
  cyclops          Young one-eyed brute with a big club; pounds the ground.

RECOLOUR (ELITE) VARIANTS - same frames, other colours, listed as recolours:
  mushroom_folk: toxic
  jackalope: snow
  moss_golem: autumn
  flytrap_walker: fire
  crystal_cube: toxic, ember
  rock_crab: crystal
  mimic_jar: gold
  roller_bug: lava
  kobold_miner: blue
  living_armor: gold, dark
  giant_mosquito: plague
  swamp_hag: frost
  lily_snapper: desert
  lizardfolk: desert
  mummy_cat: shadow
  dust_devil: ash, snow
  frost_fox: arctic_night
  frost_moth: luna
  flame_skull: soul
  ash_hound: frost
  crab_knight: royal
  jelly: sea_glass
  pumpkin_head: winter
  wraith: ember

----------------------------------------------------------------
BOSS SHEET LAYOUT (front view, facing the player)
----------------------------------------------------------------
One row per animation, top to bottom (example: elder_treant; the two
attack rows are named after each boss's attacks, see the table):
  idle, walk, branch_slam, root_spikes, hurt, death, intro_roar
  phase 2: p2_idle, p2_walk, p2_branch_slam, p2_root_spikes, p2_hurt
idle 4f, walk 4f, attacks 6f (hit frame = 3), hurt 3f, death 8f,
intro_roar 6f; phase-2 rows use the same frame counts.
Pivot: bottom-centre. The Tide Kraken's sprite includes its own
water ring - place it on sea tiles (waterline 16 px above the cell
bottom).
  elder_treant   64x64  HP   900  DMG 14  attacks: branch_slam, root_spikes
      Ancient forest guardian; slams branches and raises root spikes. Phase 2: autumn fire canopy, burning eyes.
  gel_monarch    48x48  HP   600  DMG 10  attacks: royal_slam, spawn_cubes
      Crowned gel cube full of swallowed treasure; jump-slams and spits mini cubes. Phase 2: enraged red gel.
  dragon_whelp   48x48  HP   700  DMG 12  attacks: fire_breath, tail_swipe
      Chubby teal young dragon; fire breath and tail swipes. Phase 2: ember scales, blue flame.
  bone_archmage  48x48  HP   650  DMG 12  attacks: orb_barrage, bone_spikes
      Crowned skeleton mage in violet robes; orb barrages and bone spikes. Phase 2: frost-blue robes, floats higher.
  tide_kraken    64x64  HP  1000  DMG 15  attacks: tentacle_slam, ink_spray
      Barnacled kraken that rises from the water; tentacle slams and ink. Phase 2: angry red.
  golem_king     64x64  HP  1200  DMG 16  attacks: fist_quake, rock_throw
      Crowned brick golem with a crystal core; quake slams and thrown rocks. Phase 2: cracked core glowing red.

----------------------------------------------------------------
STAT SUGGESTION TABLE (starting point, tune to taste)
----------------------------------------------------------------
  tier        HP      DMG   speed (px/s)  examples
  swarm/small 6-10    1-3   60-80 fast    bat swarm, mosquito, thorn sprite
  standard    12-20   3-4   40-50         mushroom folk, mimic jar, kobold
  bruiser     22-40   5-7   25-35 slow    living armor, cyclops, crab knight
  large 32px  30-60   6-8   30-60         moss golem, bone hound, ash hound
  boss        600-1200 10-16 20-40        phase 2 at 50% HP (+25% DMG)
  elite recolour: x1.5 HP, x1.25 DMG, + the elite aura FX.

----------------------------------------------------------------
PROJECTILES, FX, STATUS, LOOT, UI
----------------------------------------------------------------
Projectiles: arrow, rock, fireball, ice_shard, poison_spit, magic_orb, web, sonic_wave, bone_shard, thorn, seed, bubble, ink_blob, snowball, bomb, hex_bolt, spore, shadow_orb, lava_glob, coin_shot.
  _right/_left/_up/_down strips (left = mirror, up/down = 90-degree
  rotations) + Impacts/<name>_impact_4f. Frame counts are in the file
  names (2f = 2 unique frames, pingpong_3f = play 0-1-2-1).
Combat FX: spawn_32x32, teleport_16x32, death_poof_16x16, death_poof_32x32, smoke_16x32, slash_white_32x32, slash_fire_32x32, slash_spirit_32x32, hit_spark_16x16, explosion_32x32, ground_quake_32x32, heal_16x32, shield_bubble_32x32, shockwave_64x32, claw_marks_16x16, bite_16x16, elite_aura_32x32.
Status: poison, burn, freeze, stun, sleep, charm, bleed, slow, haste, shield, weaken, wet - icon + 4-frame overlay (16 and 32).
Loot: 46 drop icons (one per monster/boss, file name = item name),
  copper/silver/gold coins (spin) + piles, 5 gems (spin), hearts
  small/normal/big, mana/xp/soul orbs, drop sparkle, 4 chests with a
  5-frame open animation + loot burst (32x32).
UI: health bars 16x4 / 32x6 (+ elite), boss bar 200x16 (normal +
  phase 2) + name plate per boss, damage digits 0-9 (normal, crit 2x,
  heal, poison, enemy) + words (MISS, CRIT!, BLOCK, IMMUNE, KO!, +XP),
  target marker, ground ring, aggro "!" / "?" / sleep / tamed /
  elite icons (pop-in 3f), loot popup, 8 banners.
Bestiary: 16x16 icon per creature (biome-coloured badge) + a filled
  192x128 book page for every monster/boss (+ blank page).

----------------------------------------------------------------
ENGINE TIPS
----------------------------------------------------------------
- Import with Point / Nearest filtering, no compression, no mipmaps,
  pixels-per-unit 16.
- UNITY: Sprite Mode Multiple, Grid By Cell Size (16x16, 16x32,
  32x32, 48 or 64), pivot Bottom Center. Make one clip per action+dir
  (8-10 fps idle/walk, 12 fps attack). Use an Animation Event on
  attack frame 2 for the hit. Left clips can use flipX instead of the
  left rows.
- GODOT 4: AnimatedSprite2D + SpriteFrames "Add frames from sprite
  sheet" (h-frames 6, v-frames 24 for monsters); offset.y = -H/2 + 2
  so the feet sit on the node position. flip_h for left if you prefer.
- GAMEMAKER: import strips (Import Strip Image, frame count in the
  file name), origin Bottom Centre, image_xscale = -1 for left.
- TILED: the scenes use pack 1/2/3 ground tiles; put monsters on an
  object layer (tile objects anchor bottom-left - add W/2 to x).
- Draw order: sort by feet y (y-sort). Shadows below, status
  overlays and markers above the sprite.

----------------------------------------------------------------
PAIRS WITH (same grid, style and palette)
----------------------------------------------------------------
1 Cozy Village & Farm, 2 Fantasy Dungeon & Adventure, 3 Wild Lands,
4 Cozy Interiors & Town Life, 5 Seasons & Festivals, 6 Farm & Ranch
Life, 7 Villagers & Portraits, 8 Town Builder. The ground tiles,
trees and the player characters in the example scenes come from
packs 1-3 and are not part of this pack's count.

Made with AI assistance - programmatically generated pixel art,
reviewed and curated.
