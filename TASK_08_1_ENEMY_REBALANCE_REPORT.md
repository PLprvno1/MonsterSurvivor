# Task 08.1 — Enemy Skill Aggression & Combat Rebalance

Ngày 16/11/2026. Đã đối chiếu báo cáo Task08, bảng assignment và animation catalog hiện tại.

**Trước:49/82 có skill,33 NONE. Sau:67/82 có skill,15 NONE.** Thêm18 loài, dùng Attack/Special thật đủ bốn hướng và FX đã import; không thêm texture hoặc âm thanh.

| Nhóm bổ sung | Loài → skill |
|---|---|
| Nọc gai | Giant Bee, Bumble Guard → STINGER |
| Bào tử | Red Cap, SpriteCanvas Mushroom → SPORE |
| Cận chiến vật lý | Red Crab, Rock Crab, Owlbear Cub → CLAW_SLAM; Living Armor → SPIRIT_SLAM |
| Mảnh tinh thể | Crystal Cube, Rock Crab/crystal → CRYSTAL_SHARD; dùng ice_shard FX nhưng không áp Slow |
| Ném vật | Gargoyle, Lizardfolk/desert → ROCK; Ratfolk Thief → COIN |
| Phun độc | Frill Lizard, Lizardfolk → POISON |
| Đạn quạt | Pufferfish → THORN_FAN; Antlion → DUST_FAN |
| Nhổ bóng nước | Giant Frog → BUBBLE |

Ba preset mới: STINGER, CRYSTAL_SHARD, CLAW_SLAM. Không biến tất cả thành caster; các loài nhỏ, thú đuổi cắn và Wild Boar legacy charge vẫn giữ policy cũ.

| Tuning thực tế | Easy | Hard |
|---|---:|---:|
| Windup projectile | 0,432–0,648s | 0,368–0,552s |
| Windup melee/Slam | 0,778–0,886s | 0,662–0,754s |
| Windup targeted AoE | 0,864–0,886s | 0,736–0,754s |
| Cooldown projectile | 4,54–9,50s | 3,02–6,34s |
| Cooldown melee/Slam | 5,18–7,99s | 3,46–5,33s |
| Cooldown targeted AoE | 8,42–8,64s | 5,62–5,76s |
| Global start gate | 0,42s, trước0,75s | 0,24s, trước0,50s |
| Recovery projectile / melee-AoE | 0,18 /0,26s | 0,12 /0,20s |

Projectile speed **95–135 world units/s**, trước60–85; Arrow/Stinger135, Fireball/Magic125, Ice/Thorn120, Spore100, Toxic95. Giảm cả mốc mở skill: cơ bản15–45, fan75–150, burst300 thay420; không đổi spawn/unlock của species. Initial cooldown giảm1,5–3,72s xuống0,45–0,99s. Stand-off86/96 thay76; quái ranged giữ khoảng cách để có cơ hội bắn. Animation recovery dùng đúng thời lượng mới.

Caster cap đầu progress<90 là3/4, sau đó8/10; telegraph vẫn3/4, projectile24, FX24, poison area3. Giữ min fire distance48, margin HUD/camera, LOS, fan gaps24°/18°, tối đa3/5 viên, damage1 và cảnh báo AoE cố định. Không đổi EnemyProjectiles: vector chốt khi phóng, swept collision, immunity/Dash và owner cleanup giữ nguyên. Không tăng cap40/60 hoặc hai A* mỗi frame.

**File sửa:** bốn file `EnemySkillDefinition.java`, `EnemySkillAssignments.java`, `EnemySkillInstance.java`, `EnemySkillSystem.java` trong `core/.../enemy/skill/`; generator `tools/checks/EnemySkillManifest.java`; [ENEMY_SKILL_ASSIGNMENT.md](ENEMY_SKILL_ASSIGNMENT.md) đã cập nhật82 dòng, thêm cột speed. Không sửa Native UI/Task09, Boss, Evolution, Dash/Ultimate, Victory, audio hoặc asset runtime.

**Kiểm tra:** compile `:core:compileJava :android:compileDebugJavaWithJavac --offline --console=plain` thành công, exit0; còn cảnh báo SDK XML có sẵn. [Compile log](core/build/reports/task08_1/compile.log). Smoke JVM ngắn **17 PASS**: mapping18 loài mới, animation/FX thật, timing, Hard nhanh hơn, fixed velocity, Dash iframe/né ngang, swept wall collision, slam snapshot và caps. Trong cùng scene6 caster/4s: Easy6 starts, Hard10; first release0,48/0,42s. Đây là kiểm tra logic ngắn, không phải benchmark. [Smoke log](core/build/reports/task08_1/smoke.log).

Chưa phát hiện lỗi trong compile/smoke; chưa kiểm chứng cảm giác combat, khả năng đọc telegraph hoặc FPS trên Pixel_4 sau rebalance. Không chạy emulator automation, stress, benchmark, Studio Run; chủ dự án tự chạy gameplay. Không commit/push hoặc export APK/AAB.
