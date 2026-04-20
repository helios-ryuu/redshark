# PLAN-5-FINAL.md — Phase 5: QA, Cleanup & Release

**Thời gian:** 11/05/2026 – 17/05/2026 (Tuần 6)
**Mục tiêu:** Kiểm thử toàn diện, dọn dẹp, release APK ổn định.

## 1. Kiểm thử cuối
- [ ] Chạy toàn bộ test case trong [CHECK-1-AUTH.md](CHECK-1-AUTH.md), [CHECK-2-CONTENT.md](CHECK-2-CONTENT.md), [CHECK-3-INTERACTION.md](CHECK-3-INTERACTION.md)
- [ ] Smoke test trên 3 thiết bị: Android 8 (API 26), Android 12 (API 31), Android 14 (API 34)
- [ ] Test ngắt mạng đột ngột (airplane mode)
- [ ] Test khi Firebase / R2 trả 5xx → UI hiển thị error state
- [ ] Stress test: tạo 30 issue liên tiếp → chặn đúng ở thứ 21
- [ ] Performance: cold start < 3s trên emulator mid-tier

## 2. Dọn dẹp

### 2.1 File rác cần xóa
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
- [ ] `google-services.json` **có thể** commit (không chứa secret thực sự) nhưng khuyến nghị gitignore để tránh public repo phơi project ID
- [ ] `release.keystore` **tuyệt đối** không commit
- [ ] R2 token: đảm bảo scope chỉ WRITE vào 1 bucket, không master key
- [ ] Xoá mọi `TODO: hardcoded` còn sót

## 4. Release checklist
- [ ] Tăng `versionCode`, `versionName`
- [ ] Generate signed APK với `release.keystore`
- [ ] ProGuard không phá Composable (`@Keep` annotation khi cần)
- [ ] Test install APK trên device sạch
- [ ] Tag Git `v1.0.0` → GitHub Release upload APK

## 5. Code Style reminder (áp dụng xuyên suốt)
- **Đơn giản, tường minh, dễ hiểu** — không over-engineer
- Kotlin naming convention (xem [PLAN-1-FOUNDATION.md](PLAN-1-FOUNDATION.md))
- GraphQL field camelCase, REST endpoint kebab-case
- Không có hàm > 40 dòng không có lý do
- Mọi public API có KDoc (chỉ "Why", không lặp "What")

## 6. Bàn giao
- [ ] Source code Git + README setup
- [ ] APK release
- [ ] Báo cáo [REPORT.md](REPORT.md) hoàn chỉnh
- [ ] Slide demo
