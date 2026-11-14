N3XUS STARTER (FREE) v1.1 - top-down pixel kit: 4 characters, 3 pets, 1 rideable mount, 6 monsters
https://n3xus.dev

NEW IN 1.1
  The 4 characters (and the riders on the Black Panther) use the redrawn look of N3XUS Characters 2.0.

Everything in one art style, animated in 4 directions, ready for top-down games and game jams.
Free for commercial use with credit: "Art by N3XUS - n3xus.dev" (see LICENSE.txt).

FOLDERS
  characters/<name>/     N3XUS Dev, Netrunner (cyberpunk), Arcane Mage, Iron Knight (fantasy)
  pets/<name>/           Shiba Inu, Capybara, Robo-Dog
  mounts/black-panther/  Black Panther, with the 4 starter characters riding (riders/)
  monsters/<name>/       Acid Slime, Bat, Ghost, Skeleton, Zombie, Goblin
  previews/              animated previews and contact sheets

CHARACTERS (32x64)
  <name>-32x64.png     192x512, frames of 32x64 (6 columns x 8 rows)
                       rows 0-3 walk, rows 4-7 talk (frame 0 = idle); directions down, left, right, up
                       the figure is 48 px tall, feet on row 47 of the frame
  <name>-topdown.png   768x256, 24 columns x 4 rows: idle, walk, talk, talk (right, up, left, down)
  <name>-portrait.png  64x64 face for dialogue boxes

PETS (32x32)
  <name>-sheet.png         192x768: walk, run, idle, sit, sleep, happy x 4 directions, 6 frames
  <name>-companion.png     192x256: walk + idle, for follower pets
  <name>-companion-3f.png  96x128: 3 frames x 4 directions (default WorkAdventure companion)

MOUNT (64x64)
  black-panther-sheet.png   384x1024: walk, gallop, idle, special (roar) x 4 directions, 6 frames
  black-panther-front.png   the parts that go in front of the rider
  black-panther-saddle.json saddle point of every frame;  black-panther-sheet.json  Aseprite JSON
  riders/                   ready-made sheets with each starter character riding

MONSTERS
  32x32: acid-slime, bat, ghost      32x64: skeleton, zombie, goblin
  <name>-sheet.png    6 columns x 20 rows (192x640 or 192x1280)
                        rows  0-3  idle
                        rows  4-7  walk (flyers: fly)
                        rows  8-11 attack  (frame 3 = the hit)
                        rows 12-15 hurt    (frame 0 = white flash)
                        rows 16-19 death   (frame 5 = body on the ground; hold it)
                      directions in each block: down, left, right, up
                      feet on row 28 (32x32) or row 47 (32x64), same line as the pets and characters
  <name>-sheet.json   Aseprite JSON (json-array): one tag per animation_direction (attack_left...),
                      the time of every frame, and a 'hitbox' slice on the hit frames

SUGGESTED SPEED
  Characters: walk 10-12 fps, talk 8 fps.  Pets: walk 10-12 fps, idle 6 fps.  Mount: walk 10 fps, gallop 12 fps.
  Monsters: use the frame times in the JSON (attack and death play once).
  Import as a sprite sheet with a 32x32, 32x64 or 64x64 cell, no spacing, no margin.

WANT MORE?
  N3XUS Pixel Pack - 25 characters, 4 colors each        https://samura133.itch.io/n3xus-pixel-pack
  N3XUS Pets       - 14 pets, 4 colors each              https://samura133.itch.io/n3xus-pets
  N3XUS Mounts     - 10 mounts, 250 rider sheets         https://samura133.itch.io/n3xus-mounts
  N3XUS Complete Pack - all of the above in one bundle   https://samura133.itch.io/n3xus-complete-pack
  Follow N3XUS on itch.io to get new monsters and packs first.

LICENSE
  See LICENSE.txt. Free for personal and commercial projects with credit "Art by N3XUS - n3xus.dev";
  do not resell or redistribute the files (link to the official page instead).
