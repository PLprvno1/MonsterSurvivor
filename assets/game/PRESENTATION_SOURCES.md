# Presentation additions, Task 03B.1

Both sources are Kenney CC0. Sources are untouched. Run
`java tools/PreparePresentationAssets.java` from the repository root to reproduce the
two new 256x256 RGBA atlases; every region has two pixels of clamped edge extrusion,
Nearest filtering and ClampToEdge wrapping. Source coordinates below are zero-based.
Existing world.png, pickups.png, catalog.json and their source mapping are untouched.

`dungeon-pickups.png/json`: `asset_sources/kenney_tiny-dungeon/Tilemap/tilemap_packed.png`,
16px packed cells: potion_red (7,9), potion_green (6,9), potion_blue (8,9),
pickup_chest (7,7). License copy: `licenses/Kenney-Tiny-Dungeon.txt`.
Damage uses red, Speed blue, Health green; Weapon uses a purple frame and gold mark.
Health drops are standalone green potions; Health boxes have a chest and plus mark.
The runtime draws 16px art at 1.5 scale with a static frame/glow; it does not recolor
the source icons. Only the existing short collection animation changes scale/height.
Physics, rewards, probabilities and lifetime are unchanged.

`world-expanded.png/json`: `asset_sources/kenney_roguelike-rpg-pack/Spritesheet/roguelikeSheet_transparent.png`,
16px cells with 1px spacing. License: `licenses/Kenney-RPG.txt`.

| Region | Source column,row | Canvas |
| --- | --- | --- |
| dirt / stone / sand | 6,0 / 7,0 / 8,0 | 16x16 |
| grass_variant / moss_ground | 5,1 / 9,1 | 16x16 |
| tree_teal / pine_teal | 15,10 / 18,10 | 16x32 |
| bush_flower / grass_tuft | 24,11 / 22,10 | 16x16 |
| flower_red / flower_white | 29,9 / 31,9 | 16x16 |
| dead_tree | 27,9 | 16x48 |
| rock_small / rock_moss | 55,21 / 55,22 | 16x16 |
| fence remnants | 48,23 | 16x16 |
| campfire / camp_barrel / camp_stool | 14,8 / 51,16 / 53,19 | 16x16 |

Each of dirt/stone/sand includes 16 generated edge variants, combining only its
source pixels and grass (5,0) with a three-pixel ordered transition. Bitmask N/E/S/W
is 1/2/4/8; set bits mean the same neighboring ground. Extrusion is applied after
the derived pixel selection. This avoids transparent or magenta seams without shaders.
Original ponds/shore/tree/pine/rock/bush/stump regions remain available.
They are copied losslessly from the existing verified source coordinates into this
same atlas, overriding the runtime region lookup so terrain/props do not alternate
between textures. The historical atlas/catalog files remain unchanged.

Collision-bearing water, trees and rocks use the collision grid. Flowers, tufts,
stumps, small stones, broken fence/dead-tree remnants and sparse camps are decoration
and do not add collision. Props are culled by existing 160-world-unit chunks.
No N3XUS/Tiny Questers/Willosuna pixels are processed by this asset tool.
