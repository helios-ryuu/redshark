# PLAN-5-FINAL.md — Giai đoạn 5: Kiểm thử, Dọn dẹp và Phát hành

**Thời gian:** 12/05/2026 – 17/05/2026 (giai đoạn hoàn thiện)
**Mục tiêu:** Kiểm thử toàn diện, dọn dẹp, phát hành APK ổn định.
**WBS tham chiếu:** [WBS.md](WBS.md) — nhóm công việc `6.0` và `7.0`.

**Phân công theo WBS:**
- `6.2.1` (Hiệu năng, độ ổn định): owner **Hải**.
- `6.2.2` (Bảo mật, quản lý cấu hình): owner **Sỹ**.
- `7.1.1` (Cleanup kỹ thuật): owner **Sỹ**.

## 1. Kiểm thử cuối
- [ ] Chạy toàn bộ trường hợp kiểm thử trong [CHECK-1-AUTH.md](CHECK-1-AUTH.md), [CHECK-2-CONTENT.md](CHECK-2-CONTENT.md), [CHECK-3-INTERACTION.md](CHECK-3-INTERACTION.md)
- [ ] Kiểm tra nhanh trên 3 thiết bị: Android 8 (API 26), Android 12 (API 31), Android 14 (API 34)
- [ ] Kiểm thử ngắt mạng đột ngột (airplane mode)
- [ ] Kiểm thử khi Firebase / R2 trả 5xx -> giao diện hiển thị trạng thái lỗi
- [ ] Kiểm thử tải nặng: tạo 30 công việc liên tiếp -> chặn đúng ở lần thứ 21
- [ ] Hiệu năng: khởi động nguội < 3 giây trên trình giả lập tầm trung

## 2. Dọn dẹp

### 2.1 Tệp rác cần xóa
- [ ] `app/build/`, `.gradle/` (đã gitignore)
- [ ] Log `Log.d` / `println()` dư thừa → thay bằng Timber với BuildConfig guard
- [ ] File `.keep`, `draft-*.kt`, `TODO.md` rác
- [ ] Image/asset chưa dùng (phân tích bằng `lint` / Android Studio "Remove Unused Resources")
- [ ] Dependency trùng lặp trong `libs.versions.toml`

### 2.2 Log
- Release build: `Timber.plant(ReleaseTree)` chỉ log WARN+
- Kiểm tra không `Log.d` nào in email/password/token

### 2.3 Config thừa
- `buildTypes { release }` bật `minifyEnabled = true`, `proguardFiles`
- Xóa `BuildConfig.DEBUG` điều kiện chết

## 3. Rà soát bảo mật & Secret
- [ ] Toàn bộ biến cấu hình nhạy cảm nằm trong `local.properties` — xem [SECRET.md](SECRET.md)
- [ ] Chạy `gitleaks detect --source . --verbose` → 0 findings
- [ ] `google-services.json` **có thể** commit (không chứa biến mật thực sự) nhưng khuyến nghị đưa vào gitignore để tránh lộ thông tin dự án khi mở kho mã
- [ ] `release.keystore` **tuyệt đối** không commit
- [ ] R2 token: đảm bảo scope chỉ WRITE vào 1 bucket, không master key
- [ ] Xoá mọi `TODO: hardcoded` còn sót

## 4. Danh sách kiểm tra phát hành
- [ ] Tăng `versionCode`, `versionName`
- [ ] Generate signed APK với `release.keystore`
- [ ] ProGuard không phá Composable (`@Keep` annotation khi cần)
- [ ] Test install APK trên device sạch
- [ ] Tag Git `v1.0.0` → GitHub Release upload APK

## 5. Nhắc lại quy chuẩn mã nguồn (áp dụng xuyên suốt)
- **Đơn giản, tường minh, dễ hiểu** — không over-engineer
- Kotlin naming convention (xem [PLAN-1-FOUNDATION.md](PLAN-1-FOUNDATION.md))
- GraphQL field camelCase, REST endpoint kebab-case
- Không có hàm > 40 dòng không có lý do
- Mọi public API có KDoc (chỉ "Why", không lặp "What")

## 6. Bàn giao
- [ ] Mã nguồn Git + README hướng dẫn thiết lập
- [ ] APK phát hành
- [ ] Báo cáo [REPORT.md](REPORT.md) hoàn chỉnh
- [ ] Slide demo
