# CODEX ACHIEVEMENTS BACKEND HANDOFF SPECIFICATION

**Từ:** Antigravity (Senior Android UI/UX Engineer)  
**Tới:** Codex (Core Gameplay & Systems Engineer)  
**Dự án:** `MonsterSurvivor` (`D:\Android\MonsterSurvivor`)  
**Mã bàn giao:** UI04-HANDOFF-ACHIEVEMENTS-01  
**Ngày lập:** 16/11/2026  

---

## 1. MỤC TIÊU & HIỆN TRẠNG PHÍA UI

Trong nhiệm vụ **UI04**, Antigravity đã hoàn thiện toàn diện 100% giao diện **Bảng Vàng Thành Tích (Achievements UI)** tại:
- [activity_achievements.xml](file:///d:/Android/MonsterSurvivor/android/res/layout/activity_achievements.xml): Layout chuẩn Landscape cho Pixel 4 với thanh tiến độ tổng quan `ProgressBar`, banner Empty State chân thực, 5 thẻ thống kê trọn đời và 6 thẻ thành tích cá nhân (trạng thái Khóa / Mở Khóa).
- [AchievementsActivity.java](file:///d:/Android/MonsterSurvivor/android/src/main/java/com/example/monstersurvivor/android/AchievementsActivity.java): Binding dữ liệu động, ContextMenu xem chi tiết, sao chép và chia sẻ qua Intent.
- [AchievementManager.java](file:///d:/Android/MonsterSurvivor/android/src/main/java/com/example/monstersurvivor/android/model/AchievementManager.java): Model `AchievementItem` và các hàm truy xuất dữ liệu lưu trữ SharedPreferences (`monster_survivor_stats`).

**NGUYÊN NHÂN DỮ LIỆU HIỆN TẠI ĐANG Ở TRẠNG THÁI TRỐNG (EMPTY STATE):**
- Trong core, `GameSession.java` đã tạo bản ghi bất biến `MatchResult` khi trận đấu kết thúc (`GAME_OVER` hoặc `VICTORY`):
  ```java
  // core/.../gameplay/GameSession.java line 249
  result = new MatchResult(id, won, elapsed(), progress(), kills,
                           bosses.miniDefeated(), bosses.finalDefeated(),
                           config.difficulty, config.mode, config.language);
  ```
- Tuy nhiên, hiện tại **chưa có cầu nối (bridge)** từ `GameScreen` / `MonsterSurvivorGame` truyền đối tượng `MatchResult` này sang `AndroidLauncher`, do đó hàm `AchievementManager.recordMatchResult(...)` chưa từng được kích hoạt sau trận đấu.
- Theo đúng quy tắc đạo đức kỹ thuật của dự án: **UI04 tuyệt đối không tạo số liệu giả hay mock dữ liệu.** Khi chưa có dữ liệu trận đấu thực sự, UI hiển thị empty state trung thực và chờ kết nối backend từ Codex.

---

## 2. HỢP ĐỒNG DỮ LIỆU CẦN CODEX KẾT NỐI

```
+--------------------------------------------------------------------------------------------------+
|                                    LUỒNG DỮ LIỆU HÀN GẮN ĐỀ XUẤT                                |
|                                                                                                  |
|   [GameSession] (Core)                                                                           |
|         |                                                                                        |
|         |-- Kết thúc trận (Player chết -> GAME_OVER, hoặc Lich Lord bị tiêu diệt -> VICTORY)     |
|         v                                                                                        |
|   MatchResult snapshot (sessionId, won, elapsed, kills, miniBossDefeated, finalBossDefeated...)  |
|         |                                                                                        |
|         v                                                                                        |
|   [GameScreen] / [MonsterSurvivorGame]                                                           |
|         |                                                                                        |
|         v (Truyền callback an toàn về Android Launcher qua UI Thread)                            |
|   [AndroidLauncher.java]                                                                         |
|         |                                                                                        |
|         v (Kiểm tra chống ghi trùng bằng sessionId)                                              |
|   [AchievementManager.recordMatchResult(elapsed, kills, won, finalBossDefeated)]                 |
|         |                                                                                        |
|         v                                                                                        |
|   SharedPreferences ("monster_survivor_stats.xml")                                               |
|         |                                                                                        |
|         v                                                                                        |
|   [AchievementsActivity] tự động hiển thị tiến độ và mở khóa huy hiệu khi người chơi vào sảnh!   |
+--------------------------------------------------------------------------------------------------+
```

### 2.1. Nguồn sự kiện & Snapshot cần gửi
Đối tượng `com.example.monstersurvivor.gameplay.MatchResult` hiện đã có sẵn các trường:
```java
public record MatchResult(
    long sessionId,
    boolean victory,
    float survivedSeconds,
    float progressSeconds,
    int kills,
    boolean miniBossDefeated,
    boolean finalBossDefeated,
    Difficulty difficulty,
    GameMode mode,
    GameLanguage language
) {}
```

### 2.2. Điểm đón nhận phía Android Native
Trong `AndroidLauncher.java`, khi khởi tạo `MonsterSurvivorGame`, bổ sung callback nhận `MatchResult`:
```java
// Đề xuất chữ ký khởi tạo trong MonsterSurvivorGame:
public MonsterSurvivorGame(
    GameConfig config,
    Runnable onGameExit,
    AudioEnvironment audio,
    DiagnosticRun diagnostics,
    Consumer<MatchResult> onMatchCompleted // <-- CODEX BỔ SUNG HOOK NÀY
)
```

Tại `AndroidLauncher.java`:
```java
private long mLastRecordedSessionId = -1;

private void onMatchCompleted(MatchResult result) {
    if (result == null) return;
    // Chống ghi trùng (idempotency guard)
    if (result.sessionId() == mLastRecordedSessionId) {
        return;
    }
    mLastRecordedSessionId = result.sessionId();

    runOnUiThread(() -> {
        AchievementManager.getInstance(this).recordMatchResult(
            Math.round(result.survivedSeconds()),
            result.kills(),
            result.victory(),
            result.finalBossDefeated()
        );
    });
}
```

---

## 3. CÁC QUY TẮC BẢO TOÀN DỮ LIỆU & CHỐNG GHI TRÙNG (IDEMPOTENCY)

1. **Khóa phiên (Session Lock):**
   Mỗi phiên chơi có `sessionId` duy nhất. Chỉ được ghi nhận vào `AchievementManager` đúng 1 lần cho mỗi `sessionId`.
2. **Xử lý Chơi Lại (Retry):**
   Khi người chơi bấm `CHƠI LẠI (RETRY)` trên màn Game Over hoặc Victory:
   - Trận cũ đã được ghi nhận 1 lần duy nhất ngay tại thời điểm kết thúc.
   - Lệnh Retry sinh ra một `GameSession` mới với `sessionId` mới độc lập.
   - Tuyệt đối không gọi lại callback ghi nhận của trận cũ.
3. **Xử lý Tạm dừng & Vòng đời thiết bị (Lifecycle Pause/Resume):**
   - Người dùng bấm nút Pause, đưa app ra nền (minimize) hoặc nhận cuộc gọi: Không phát sinh sự kiện ghi nhận kết quả.
   - Khi dũng sĩ chưa hết máu và Lich Lord chưa bị hạ gục: Không được ghi nhận trận dở dang.
4. **Quy chuẩn Thời gian Sinh tồn (Survival Time):**
   - Với chế độ **THƯỜNG (NORMAL):** Ghi nhận `result.survivedSeconds()` (số giây sống sót thực tế không tính thời gian tạm dừng).
   - Với chế độ **THỬ NGHIỆM (DEMO):** Sự kiện nén trong 90 giây; chỉ ghi nhận thời gian thực dũng sĩ sống sót (`survivedSeconds`), không ghi thời gian tiến trình nén ảo (`progressSeconds = 600s`), để bảo đảm tính trung thực tuyệt đối của Bảng vàng thành tích.
5. **Chế độ Chẩn đoán (Diagnostic Runs):**
   - Các bài test tự động hoặc chạy với `DiagnosticRun != NONE`: Tuyệt đối không ghi nhận kết quả vào SharedPreferences thực tế của người dùng (`monster_survivor_stats.xml`).

---

## 4. QUY TẮC MỞ KHÓA 6 THÀNH TÍCH DŨNG SĨ

Phía UI đã sẵn sàng ánh xạ 6 thành tích sau dựa trên các chỉ số được lưu trong `AchievementManager`:

| STT | Mã Thành Tích | Tên Hiển Thị (VI / EN) | Điều Kiện Đạt Tự Động |
| :---: | :--- | :--- | :--- |
| 1 | `first_blood` | Khởi Đầu Sinh Tồn / First Blood | `totalKills >= 1` |
| 2 | `novice_survivor` | Chiến Binh Kiên Cường / Novice Survivor | `bestSurvivalSeconds >= 180` (Sống sót $\ge$ 3 phút) |
| 3 | `monster_slayer` | Thợ Săn Quái Vật / Monster Slayer | `totalKills >= 100` |
| 4 | `centurion_slayer` | Đồ Tể Chiến Trường / Centurion Slayer | `totalKills >= 500` |
| 5 | `lich_bane` | Khắc Tinh Bóng Đêm / Bane of the Lich | `bossDefeatedCount >= 1` (Hạ Lich Lord) |
| 6 | `legendary_victor` | Dũng Sĩ Toàn Thắng / Legendary Victor | `totalVictories >= 1` (Thắng trận chuẩn 10 phút) |

---

## 5. CHECKLIST KIỂM THỬ DÀNH CHO CODEX

Sau khi Codex nối hook:
- [ ] Chơi trận mới, dũng sĩ tiêu diệt 1 quái rồi để chết: Bảng thành tích ghi nhận 1 trận, 1 kill, mở khóa thành tích `First Blood`.
- [ ] Bấm `Retry`: Trận cũ không bị ghi nhận thêm lần thứ 2.
- [ ] Tạm dừng trận đấu, Home ra ngoài: Không làm sai lệch số liệu.
- [ ] Chạy `python tools/android_task08_baseline.py`: Xác nhận các bài test chẩn đoán không làm thay đổi SharedPreferences của người dùng.
