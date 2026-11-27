# Task 11 – Performance

- `EnemySystem.move` bỏ kiểm tra collision cho bước đứng yên và trục không dịch chuyển.
- `ProjectileSystem.update` dùng broadphase AABB cho đoạn bay projectile trước phép swept-hit chính xác; giới hạn trúng, thứ tự va chạm, tường và damage không đổi.
- Compile `:core:compileJava :android:compileDebugJavaWithJavac --offline`: thành công.
- Smoke `WeaponChecks`: phần chọn vũ khí và projectile chạy qua; suite dừng ở fixture cũ `lightningAndClocks` (mảng 2 phần tử nhưng duyệt 3 `GameMode`, sau khi thêm Endless). Không sửa fixture.
- Không đo FPS trước/sau và không chạy emulator; chưa có số liệu định lượng về mức tăng hiệu năng.
