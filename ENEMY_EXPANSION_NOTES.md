# Future enemy integration (Task 03B.1)

No new enemy is registered in EnemyType, spawn stages or the runtime asset catalog.

| Role | Intended behavior | Existing integration seam | Asset status |
| --- | --- | --- | --- |
| Ghost | Special approach through forest obstacles; bounded approach and contact attack | Flying navigation layer, EnemySystem movement policy | N3XUS ghost deferred |
| Goblin | Fast ground melee with readable windup | Ground A*, EnemyStats, melee contact policy | N3XUS goblin deferred |
| Skeleton | Future ranged attacker with LOS, projectile lifetime and owner/team filtering | Ground A*, CombatGeometry, separate enemy projectile owner | N3XUS skeleton deferred |
| Zombie | Slow, durable crowd pressure | Ground A*, per-stage HP, bounded separation | N3XUS zombie deferred |

The local source is `asset_sources/N3XUS-Starter-FREE-v1.1/N3XUS-Starter-FREE-v1.1/`.
Its LICENSE.txt was read before any image processing. It explicitly prohibits using artwork
to train, fine-tune or evaluate AI models, or as prompts for them. It also requires credit
and prohibits standalone asset redistribution. No N3XUS image was opened, decoded, packed,
uploaded to an AI tool, or added to runtime by this task. The local README describes
`monsters/ghost`, `monsters/goblin`, `monsters/skeleton`, `monsters/zombie`, four directions,
five states, sheets and JSON metadata; these descriptions are not verified animation imports.

Safe alternative for prototypes: Kenney Tiny Dungeon (CC0), visually inspected packed
16px sheet: ghost `(0,9)`, skull/skeleton candidate `(1,10)`, green humanoid candidate
`(4,9)`. These are static icons, not complete directional attack/death sets. Do not label
them complete Goblin/Zombie/Skeleton animations. A human can separately clear suitable
licensed assets outside the AI pipeline, or commission an explicitly AI-compatible set.
Existing Bat/Slime remain Tiny Questers; no N3XUS replacements.

Before enabling a role: audit license and required credit; validate actual states,
directions, pivots, hitboxes and playback; pack with extrusion/Nearest; implement one
bounded movement/attack policy; verify collision, damage ownership, unique death/drop,
population caps, pause/modal and Retry. Only then deliberately revise spawn stages.

# Future evolution contract (Task 03C; not implemented)

Evolution requires explicit eligibility after normal level 5 and must replace the owned
weapon in its existing slot. It needs a meaningful gameplay change and unmistakably
different animation/VFX, rather than damage alone. Preserve three unique slots and
the token/pending-pickup reward transaction. WeaponConfig.Level already carries volley,
initial-bolt and visual parameters; pooled projectile/effect snapshots preserve an
attack's presentation when the weapon upgrades. Add an explicit evolved state/config
when implementing 03C; do not disguise evolution as normal level 6.
