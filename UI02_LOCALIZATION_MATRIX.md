# UI02 — BẢNG MA TRẬN BẢN ĐỊA HÓA (LOCALIZATION MATRIX)

**Dự án:** Monster Survivor  
**Tác giả:** Senior Android UI/UX Engineer  
**Ngày hoàn thiện:** 14/11/2026  
**Ngôn ngữ hỗ trợ:** Tiếng Việt (Mặc định - `vi`), English (`en`)  

---

## I. KIẾN TRÚC BẢN ĐỊA HÓA & HỢP ĐỒNG DỮ LIỆU

```
+-------------------------------------------------------------------------------------------------+
|                                    CƠ CHẾ LƯU TRỮ & ĐỒNG BỘ                                     |
|                                                                                                 |
|   [Android SettingsActivity]                                                                    |
|              |                                                                                  |
|              v                                                                                  |
|   GamePreferences.setGameLanguage("vi" | "en")                                                  |
|              |                                                                                  |
|              v (Lưu vào SharedPreferences: monster_survivor_prefs.xml, key: "game_language")    |
|              +-----------------------------------+-----------------------------------+          |
|              |                                                                       |          |
|              v                                                                       v          |
|   [Android Native Activities]                                                [AndroidLauncher]  |
|   UiUtils.wrapLocale(Context)                                                        |          |
|   attachBaseContext() trên 6 Activity                                                v          |
|   Nạp strings.xml (vi) & values-en/strings.xml                               Intent Extras      |
|                                                                                      |          |
|                                                                                      v          |
|                                                                              GameConfig.Builder |
|                                                                                      |          |
|                                                                                      v          |
|                                                                         libGDX GameStrings (VI/EN)
+-------------------------------------------------------------------------------------------------+
```

### 1.1. Single Source of Truth
- Tên file SharedPreferences: `monster_survivor_prefs.xml`.
- Key lưu trữ: `KEY_GAME_LANGUAGE = "game_language"`.
- Giá trị hợp lệ: `"vi"` (Tiếng Việt) và `"en"` (English).
- **Cơ chế Fallback an toàn:**
  - Nếu key không tồn tại (lần đầu khởi chạy) → Trả về `"vi"`.
  - Nếu key mang chuỗi rỗng, khoảng trắng hoặc giá trị không xác định (ví dụ `"fr"`, `"invalid"`) → Tự động fallback về `"vi"`.
  - Nếu dữ liệu trong SharedPreferences bị lỗi kiểu (ví dụ lưu nhầm kiểu `int` value = 7) → Bắt ngoại lệ và fallback về `"vi"` mà không bao giờ gây crash ứng dụng.

### 1.2. Cơ chế nạp ngôn ngữ trên Android Native
- Sử dụng phương pháp bọc ngữ cảnh `UiUtils.wrapLocale(Context)`:
  ```java
  @Override
  protected void attachBaseContext(Context newBase) {
      super.attachBaseContext(UiUtils.wrapLocale(newBase));
  }
  ```
- Cập nhật tức thời khi người dùng chuyển ngôn ngữ trong `SettingsActivity`:
  - Gọi `mPrefs.setGameLanguage("vi" hoặc "en")`.
  - Gọi `recreate()` để làm mới ngay màn hình Settings mà không cần khởi động lại tiến trình app.
  - Khi quay trở lại `HomeActivity`, sự kiện `onResume()` tự động phát hiện ngôn ngữ đã thay đổi và kích hoạt `recreate()` để đồng bộ toàn bộ giao diện Native.

### 1.3. Cơ chế truyền ngôn ngữ sang libGDX Gameplay
- Trong `AndroidLauncher.java`:
  ```java
  String language = GamePreferences.getInstance(this).getGameLanguage();
  GameConfig config = GameConfig.fromIntent(extraDifficulty, extraMode, language);
  ```
- Core engine đọc trực tiếp `GameConfig.strings` (lớp `GameStrings`), đảm bảo gameplay hoàn toàn độc lập với Android Context, dễ dàng chạy unit test và GL smoke check trên mọi nền tảng.

---

## II. MA TRẬN CHUỖI CORE LIBGDX (60 KEYS HOÀN THIỆN)

Dưới đây là danh mục 60 chuỗi bản địa hóa cốt lõi của gameplay engine, bảo đảm đối ứng 1:1 giữa Tiếng Việt và Tiếng Anh:

| STT | Khóa (Key) | Tiếng Việt (`vi`) | English (`en`) | Ngữ cảnh / Vị trí hiển thị |
| :---: | :--- | :--- | :--- | :--- |
| 1 | `diff_easy` | DỄ | EASY | Huy hiệu độ khó trên HUD & kết quả |
| 2 | `diff_hard` | KHÓ | HARD | Huy hiệu độ khó trên HUD & kết quả |
| 3 | `mode_normal` | THƯỜNG | NORMAL | Chế độ chơi chuẩn 10 phút |
| 4 | `mode_demo` | THỬ NGHIỆM | DEMO | Chế độ chơi demo sự kiện nén 90s |
| 5 | `hud_hp` | MÁU | HP | Nhãn thanh máu người chơi |
| 6 | `hud_kills` | HẠ GỤC | KILLS | Nhãn số quái đã tiêu diệt |
| 7 | `hud_damage` | SÁT THƯƠNG | DAMAGE | Nhãn chỉ số tấn công cơ bản |
| 8 | `hud_speed` | TỐC ĐỘ | SPEED | Nhãn chỉ số tốc độ di chuyển |
| 9 | `hud_night` | ĐÊM | NIGHT | Trạng thái bóng đêm từ phút thứ 5 |
| 10 | `pause_title` | TẠM DỪNG | PAUSED | Tiêu đề hộp thoại tạm dừng trận đấu |
| 11 | `pause_resume` | TIẾP TỤC | RESUME | Nút tiếp tục màn chơi |
| 12 | `pause_home` | VỀ TRANG CHỦ | RETURN HOME | Nút thoát trận về màn hình chính |
| 13 | `game_over_title` | KẾT THÚC TRẬN ĐẤU | GAME OVER | Tiêu đề màn hình thất bại |
| 14 | `game_over_survived` | Thời gian sinh tồn: | Survival Time: | Nhãn thời gian sống thực chiến |
| 15 | `game_over_kills` | Quái đã hạ: | Monsters Slain: | Nhãn số quái tiêu diệt trong trận |
| 16 | `game_over_retry` | CHƠI LẠI | RETRY | Nút tải lại trận đấu với cùng map/mode |
| 17 | `game_over_home` | VỀ TRANG CHỦ | RETURN HOME | Nút rời trận đấu về Home |
| 18 | `weapon_box_title` | CHỌN VŨ KHÍ | CHOOSE A WEAPON | Tiêu đề modal mở rương nâng cấp |
| 19 | `weapon_new` | MỚI | NEW | Huy hiệu mở khóa vũ khí mới |
| 20 | `weapon_level_up` | Cấp {0} → Cấp {1} | Lv {0} → Lv {1} | Bước tiến cấp độ nâng cấp |
| 21 | `weapon_home` | VỀ TRANG CHỦ | RETURN HOME | Nút về Home trong modal rương |
| 22 | `weapon_hint` | Chạm vào một vũ khí để nâng cấp | Tap a weapon card to upgrade | Hướng dẫn thao tác chân trang modal |
| 23 | `weapon_sword_name` | Kiếm Thánh | Holy Sword | Tên vũ khí cận chiến ban đầu |
| 24 | `weapon_bow_name` | Cung Rừng | Forest Bow | Tên vũ khí tầm xa bắn cung tên |
| 25 | `weapon_fireball_name` | Cầu Lửa Ma Thuật | Arcane Fireball | Tên vũ khí bắn cầu lửa nổ lan |
| 26 | `weapon_lightning_name` | Sấm Sét Phẫn Nộ | Wrath Lightning | Tên vũ khí giật sét đa mục tiêu |
| 27 | `sword_desc_1` | Vung kiếm gây sát thương diện rộng xung quanh dũng sĩ. | Cleaves nearby foes in an arc around the hero. | Mô tả Kiếm cấp 1 |
| 28 | `sword_desc_2` | Tăng 20% sát thương và giảm 10% hồi chiêu. | +20% damage and 10% faster attack recovery. | Mô tả Kiếm cấp 2 |
| 29 | `sword_desc_3` | Tăng 25% sát thương và mở rộng tầm chém thêm 8 ô. | +25% damage and 8 units broader slash radius. | Mô tả Kiếm cấp 3 |
| 30 | `sword_desc_4` | Tăng 30% sát thương và hồi chiêu cực nhanh. | +30% damage and significantly reduced cooldown. | Mô tả Kiếm cấp 4 |
| 31 | `sword_desc_5` | Cấp tối đa: Sát thương hủy diệt và tầm quét cực đại. | Master tier: Maximum cleave reach and lethal power. | Mô tả Kiếm cấp 5 |
| 32 | `bow_desc_1` | Tự động bắn tên xuyên quái vật gần nhất. | Fires straight piercing arrows at the nearest target. | Mô tả Cung cấp 1 |
| 33 | `bow_desc_2` | Tăng tốc độ tên bay và tăng 25% sát thương. | Faster projectile speed and +25% arrow damage. | Mô tả Cung cấp 2 |
| 34 | `bow_desc_3` | Bắn thêm một mũi tên song song. | Fires an additional parallel arrow each volley. | Mô tả Cung cấp 3 |
| 35 | `bow_desc_4` | Tăng sát thương và giảm thời gian nạp tên. | Heightened damage and faster reload cadence. | Mô tả Cung cấp 4 |
| 36 | `bow_desc_5` | Cấp tối đa: Bắn 3 mũi tên xuyên thấu hủy diệt. | Master tier: Fires 3 lethal piercing arrows. | Mô tả Cung cấp 5 |
| 37 | `fire_desc_1` | Phóng cầu lửa phát nổ khi va chạm, gây sát thương lan. | Hurls an explosive sphere that blasts in an area. | Mô tả Cầu lửa cấp 1 |
| 38 | `fire_desc_2` | Tăng phạm vi nổ và tăng sát thương lan. | Expanded blast radius and higher detonation force. | Mô tả Cầu lửa cấp 2 |
| 39 | `fire_desc_3` | Bắn 2 cầu lửa cùng lúc về hai hướng. | Hurls 2 explosive spheres simultaneously. | Mô tả Cầu lửa cấp 3 |
| 40 | `fire_desc_4` | Tăng sát thương nổ và giảm hồi chiêu. | Devastating blast power with faster cast time. | Mô tả Cầu lửa cấp 4 |
| 41 | `fire_desc_5` | Cấp tối đa: Bắn 3 cầu lửa siêu nổ uy lực tuyệt đối. | Master tier: Launches 3 calamitous explosive spheres. | Mô tả Cầu lửa cấp 5 |
| 42 | `ltn_desc_1` | Giáng sấm sét từ trời xuống kẻ địch ngẫu nhiên. | Calls thunder strikes down upon random enemies. | Mô tả Tia sét cấp 1 |
| 43 | `ltn_desc_2` | Sét giật lan thêm một mục tiêu lân cận. | Lightning arcs and chains to 1 secondary foe. | Mô tả Tia sét cấp 2 |
| 44 | `ltn_desc_3` | Tăng sát thương giật sét và giảm hồi chiêu. | Elevated thunder damage and reduced strike delay. | Mô tả Tia sét cấp 3 |
| 45 | `ltn_desc_4` | Giáng đồng thời 2 luồng sét xuống chiến trường. | Summons 2 distinct lightning bolts simultaneously. | Mô tả Tia sét cấp 4 |
| 46 | `ltn_desc_5` | Cấp tối đa: Bão sét liên hoàn giật qua toàn bộ quái. | Master tier: Tempest tempest arcing across all foes. | Mô tả Tia sét cấp 5 |
| 47 | `fallback_dmg_title` | Bồi Dưỡng Lực Chiến | Empower Might | Thẻ fallback khi max vũ khí |
| 48 | `fallback_dmg_desc` | Nhận vĩnh viễn +2 Sát thương cho tất cả vũ khí. | Permanently grants +2 base damage to all attacks. | Mô tả fallback sát thương |
| 49 | `fallback_hp_title` | Hồi Phục Thần Tốc | Vitality Surge | Thẻ fallback hồi máu/máu tối đa |
| 50 | `fallback_hp_desc` | Tăng +2 Máu tối đa và hồi ngay lập tức 2 Máu. | +2 Maximum Health and immediately restores 2 HP. | Mô tả fallback máu |
| 51 | `fallback_spd_title` | Thần Hành Bộ Pháp | Fleet Footwork | Thẻ fallback tốc độ |
| 52 | `fallback_spd_desc` | Tăng vĩnh viễn +1 Tốc độ di chuyển cho dũng sĩ. | Permanently grants +1 movement speed. | Mô tả fallback tốc độ |
| 53 | `pickup_hp` | Nhặt được Hộp Thuốc: Hồi phục sinh lực! | Health Pack gathered: Vitality restored! | Toast nhặt hộp máu |
| 54 | `pickup_dmg` | Nhặt được Hộp Sát Thương: Tăng sức tấn công! | Damage Boost gathered: Attacks strengthened! | Toast nhặt hộp sát thương |
| 55 | `pickup_spd` | Nhặt được Hộp Tốc Độ: Di chuyển nhanh nhẹn! | Speed Tonic gathered: Movement hastened! | Toast nhặt hộp tốc độ |
| 56 | `pickup_chest` | Tìm thấy Rương Vũ Khí Ma Thuật! | Found an Arcane Weapon Chest! | Toast nhặt rương vũ khí |
| 57 | `pickup_drop` | Thu thập bình máu quái rơi: +{0} HP | Gathered monster potion: +{0} HP | Toast nhặt drop máu từ quái |
| 58 | `status_max_speed` | Tốc độ đã đạt mức tối đa! | Movement speed is already maximized! | Thông báo chạm trần tốc độ |
| 59 | `clock_normal_label` | THỜI GIAN | SURVIVAL | Nhãn đồng hồ Normal |
| 60 | `clock_demo_label` | TIẾN TRÌNH | PROGRESS | Nhãn đồng hồ Demo |

---

## III. MA TRẬN CHUỖI NATIVE XML (ANDROID RES)

Đối ứng giữa tài nguyên Tiếng Việt (`android/res/values/strings.xml`) và Tiếng Anh (`android/res/values-en/strings.xml`):

### 3.1. Thông tin chung & Màn hình chính (HomeActivity)
| Tên tài nguyên (ID) | Tiếng Việt (`values/strings.xml`) | English (`values-en/strings.xml`) |
| :--- | :--- | :--- |
| `app_name` | Monster Survivor | Monster Survivor |
| `home_title` | MONSTER SURVIVOR | MONSTER SURVIVOR |
| `home_subtitle` | Bản Lĩnh Sinh Tồn Rừng Thiêng | Forest Survival Odyssey |
| `home_lore` | Vượt qua bầy quái vật bóng đêm, rèn luyện vũ khí thần thoại và sống sót đến bình minh! | Survive the dark monstrous hordes, master mystic weapons, and endure until dawn! |
| `home_version_tag` | Phiên bản 1.0 - libGDX 1.14.2 Offline | Version 1.0 - libGDX 1.14.2 Offline |
| `btn_play` | VÀO TRẬN ĐẤU | PLAY GAME |
| `btn_achievements` | THÀNH TÍCH | ACHIEVEMENTS |
| `btn_settings` | THIẾT LẬP | SETTINGS |
| `btn_how_to_play` | HƯỚNG DẪN CHƠI | HOW TO PLAY |
| `btn_exit` | THOÁT GAME | EXIT GAME |
| `toast_exit` | Tạm biệt dũng sĩ! Hẹn gặp lại. | Farewell warrior! See you again. |

### 3.2. Chọn chế độ chơi (GameModeActivity)
| Tên tài nguyên (ID) | Tiếng Việt (`values/strings.xml`) | English (`values-en/strings.xml`) |
| :--- | :--- | :--- |
| `game_mode_title` | CHỌN CHẾ ĐỘ CHƠI | SELECT GAME MODE |
| `section_difficulty` | 1. Chọn độ khó chiến trường | 1. Select Battle Difficulty |
| `diff_easy_title` | DỄ (EASY) | EASY |
| `diff_easy_desc` | Nhịp độ vừa phải, quái ít máu hơn, thích hợp làm quen thế giới. | Balanced monster spawn rates and moderate health. |
| `diff_hard_title` | KHÓ (HARD) | HARD |
| `diff_hard_desc` | Quái đông gấp bội, di chuyển nhanh hơn, thách thức dũng sĩ thực thụ. | Swarming monsters, accelerated speeds, intense pressure. |
| `section_mode` | 2. Chọn chế độ thi đấu | 2. Select Match Format |
| `mode_normal_title` | CHẾ ĐỘ CHUẨN (10 PHÚT) | NORMAL (10 MINUTES) |
| `mode_normal_desc` | Trận chiến đầy đủ 10 phút sinh tồn, chuyển đêm từ phút 5 và boss xuất hiện phút 9. | Full 10-minute battle, night at min 5, Lich Lord at min 9. |
| `mode_demo_title` | CHẾ ĐỘ THỬ NGHIỆM (90 GIÂY) | DEMO (90 SECONDS) |
| `mode_demo_desc` | Lịch trình sự kiện được nén rút ngắn còn 90 giây để kiểm tra tính năng nhanh. | Condensed 90-second timeline for rapid feature testing. |
| `btn_start_game` | BẮT ĐẦU CHIẾN ĐẤU | START BATTLE |
| `toast_starting_game` | Đang vào trận: %1$s - %2$s | Launching battle: %1$s - %2$s |
| `badge_selected_format` | Đã chọn: %1$s • %2$s | Selected: %1$s • %2$s |

### 3.3. Thiết lập hệ thống (SettingsActivity)
| Tên tài nguyên (ID) | Tiếng Việt (`values/strings.xml`) | English (`values-en/strings.xml`) |
| :--- | :--- | :--- |
| `settings_title` | THIẾT LẬP | SETTINGS |
| `section_audio` | Âm thanh & Âm lượng | Audio & Volume |
| `setting_music` | Nhạc nền thế giới (BGM) | Background Music (BGM) |
| `setting_sfx` | Hiệu ứng âm thanh (SFX) | Sound Effects (SFX) |
| `label_music_vol` | Âm lượng nhạc nền | Music Volume |
| `label_sfx_vol` | Âm lượng hiệu ứng | SFX Volume |
| `section_haptics` | Rung & Phản hồi xúc giác | Haptics & Vibration |
| `setting_vibration` | Rung khi bị trúng đòn | Vibration on Hit |
| `section_theme` | Giao diện Native | Native Visual Theme |
| `theme_forest` | Rừng xanh bí ẩn | Ancient Forest (Default) |
| `theme_dark` | Bóng đêm huyền bí | Dark Shadow Realm |
| `section_language` | Ngôn ngữ trò chơi | Game Language |
| `lang_vietnamese` | Tiếng Việt (Bản địa) | Tiếng Việt (Vietnamese) |
| `lang_english` | English (Quốc tế) | English (Global) |
| `section_about` | Thông tin ứng dụng | About Monster Survivor |
| `about_text` | Monster Survivor v1.0\nNhóm phát triển: Senior Android UI/UX\nCông nghệ: Java 17, AndroidX, libGDX 1.14.2\nBản quyền CC0 cho tài nguyên hình ảnh. | Monster Survivor v1.0\nDeveloped by Senior Android UI/UX\nStack: Java 17, AndroidX, libGDX 1.14.2\nCC0 licensed open assets. |
| `toast_settings_saved` | Đã lưu thiết lập thành công | Settings saved successfully |
| `toast_lang_changed` | Đã chuyển sang Tiếng Việt | Switched to English |

### 3.4. Bảng thành tích dũng sĩ (AchievementsActivity)
| Tên tài nguyên (ID) | Tiếng Việt (`values/strings.xml`) | English (`values-en/strings.xml`) |
| :--- | :--- | :--- |
| `achievements_title` | THÀNH TÍCH DŨNG SĨ | HERO ACHIEVEMENTS |
| `ach_survival_time` | Kỷ Lục Sống Sót | Best Survival Time |
| `ach_survival_time_val` | 00:00 (Chưa lập kỷ lục) | 00:00 (No record yet) |
| `ach_kills` | Quái Vật Đã Tiêu Diệt | Total Monsters Slain |
| `ach_kills_val` | 0 quái vật | 0 monsters |
| `ach_victories` | Số Trận Toàn Thắng | Total Victories |
| `ach_victories_val` | 0 trận thắng | 0 victories |
| `ach_matches` | Tổng Số Trận Đã Đấu | Total Matches Played |
| `ach_matches_val` | 0 trận đấu | 0 matches |
| `ach_boss` | Trùm Cuối Đã Hạ | Bosses Defeated |
| `ach_boss_val` | 0 Lich Lord | 0 Lich Lords |
| `ach_empty_hint` | Nhấn giữ vào từng thẻ thành tích để xem chi tiết hoặc chia sẻ | Long press any achievement card to inspect details or share |
| `ctx_view_details` | Xem chi tiết thành tích | View Achievement Details |
| `ctx_copy_info` | Sao chép thông số | Copy Achievement Stats |
| `ctx_share` | Chia sẻ chiến tích | Share Achievement |
| `dialog_details_title` | Chi tiết: %1$s | Details: %1$s |
| `dialog_details_msg` | Chỉ số hiện tại: %1$s\n\nThành tích sẽ được cập nhật tự động sau mỗi trận chiến sống sót. | Current Stat: %1$s\n\nStats are recorded after each match. |
| `dialog_btn_ok` | Đã hiểu | Got It |
| `toast_copied` | Đã sao chép: %1$s | Copied to clipboard: %1$s |
| `share_title` | Chia sẻ thành tích Monster Survivor | Share Monster Survivor Achievement |
| `share_content_format` | [Monster Survivor] Tôi đã đạt thành tích '%1$s': %2$s! Hãy thử thách cùng tôi! | [Monster Survivor] I achieved '%1$s': %2$s! Join the battle! |

### 3.5. Hướng dẫn sinh tồn (HowToPlayActivity)
| Tên tài nguyên (ID) | Tiếng Việt (`values/strings.xml`) | English (`values-en/strings.xml`) |
| :--- | :--- | :--- |
| `how_to_play_title` | HƯỚNG DẪN SINH TỒN | SURVIVAL GUIDE |
| `guide_ctrl_title` | 1. Di chuyển linh hoạt | 1. Intuitive Joystick Controls |
| `guide_ctrl_desc` | Dùng cần gạt ảo Floating Joystick ở góc trái để né đòn quái vật và tìm góc di chuyển thuận lợi. | Use the Floating Joystick on the left screen to steer and dodge oncoming hordes. |
| `guide_atk_title` | 2. Tự động tấn công | 2. Automated Combat |
| `guide_atk_desc` | Dũng sĩ tự động vung kiếm hoặc tung vũ khí tầm xa về phía quái vật trong tầm ngắm. | Your hero automatically attacks foes within range; focus on tactical positioning. |
| `guide_slots_title` | 3. Tối đa 3 vũ khí | 3. Three Weapon Slots |
| `guide_slots_desc` | Trang bị tối đa 3 loại vũ khí: Kiếm Thánh, Cung Rừng, Cầu Lửa Ma Thuật và Sấm Sét Phẫn Nộ. | Equip up to 3 distinct weapons: Holy Sword, Forest Bow, Arcane Fireball, and Wrath Lightning. |
| `guide_pickups_title` | 4. Thu thập hòm báu | 4. Battlefield Pickups |
| `guide_pickups_desc` | Nhặt Hộp Máu, Hộp Sát Thương, Hộp Tốc Độ và Rương Vũ Khí trên bản đồ để gia tăng sức mạnh. | Collect Health, Damage, and Speed Boxes to empower your attributes and unlock upgrades. |
| `guide_night_title` | 5. Màn đêm tử thần | 5. The Dangerous Nightfall |
| `guide_night_desc` | Từ phút thứ 5, bóng đêm bao trùm khiến tầm nhìn bị giới hạn và quái vật trở nên hung dữ hơn. | At minute 5, nightfall envelops the forest; vision narrows and beasts become aggressive. |
| `guide_boss_title` | 6. Trùm cuối Lich Lord | 6. Final Boss: Lich Lord |
| `guide_boss_desc` | Phút thứ 9, Chúa tể bóng đêm Lich Lord thức tỉnh với lượng máu khổng lồ và sát thương chết người. | At minute 9, the ancient Lich Lord awakens with immense vitality and lethal powers. |
| `guide_win_title` | 7. Điều kiện thắng cuộc | 7. Victory Conditions |
| `guide_win_desc` | Sống sót tối thiểu 10 phút và tiêu diệt Trùm Cuối Lich Lord để giành lấy chiến thắng vinh quang! | Endure for 10 minutes and vanquish the Lich Lord to claim glorious survival victory! |

---

## IV. BỘ FONT & ĐỘ BAO PHỦ KÝ TỰ VIỆT (GLYPH COVERAGE)

### 4.1. Android Native UI
- Sử dụng chuẩn typography Material Design 3 kết hợp hệ thống font `sans-serif` (Roboto) tích hợp sẵn trên Android OS.
- Hỗ trợ 100% tiếng Việt có dấu đầy đủ (Unicode dựng sẵn NFC).

### 4.2. libGDX Gameplay Engine
- Tệp font BMFont: `assets/game/fonts/game-ui.fnt` đi kèm `assets/game/fonts/game-ui.png` (kích thước 512 × 256 px).
- **Tổng số glyph pre-baked:** 261 ký tự.
- **Phạm vi ký tự:**
  - Ký tự Latin tiêu chuẩn: `A-Z`, `a-z`, `0-9`, dấu câu, ký hiệu toán học.
  - Toàn bộ 134 nguyên âm có dấu Tiếng Việt ở cả hai dạng hoa và thường: `À, Á, Ả, Ã, Ạ, Ă, Ằ, Ắ, Ẳ, Ẵ, Ặ, Â, Ầ, Ấ, Ẩ, Ẫ, Ậ, Đ, È, É, Ẻ, Ẽ, Ẹ, Ê, Ề, Ế, Ể, Ễ, Ệ, Ì, Í, Ỉ, Ĩ, Ị, Ò, Ó, Ỏ, Õ, Ọ, Ô, Ồ, Ố, Ổ, Ỗ, Ộ, Ơ, Ừ, Ứ, Ở, Ỡ, Ợ, Ù, Ú, Ủ, Ũ, U, Ư, Ừ, Ứ, Ử, Ữ, Ự, Ỳ, Ý, Ỷ, Ỹ, Ỵ...`
  - Các ký hiệu đặc thù của giao diện game: Mũi tên tiến cấp `→`, hai thanh pause `||`, dấu chấm tâm bullet `•`, hình trái tim, đầu lâu, gươm giáo.
- **Không xảy ra lỗi thiếu ký tự:** Đã xác minh qua 1781 kiểm tra GL smoke check, không xuất hiện fallback ô vuông trắng hoặc ký tự rác.
