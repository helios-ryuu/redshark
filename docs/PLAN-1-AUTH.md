# PLAN-1-AUTH.md — Thực thi: Xác thực & Hồ sơ

**Phụ trách:** Sỹ
**Tiêu chuẩn nghiệm thu:** [CHECK-1-AUTH.md](CHECK-1-AUTH.md) — TC-A01..TC-A19 (19 TC)
**Prerequisite:** Branch `feature/ui-fluent-overhaul` checkout; build xanh trước khi bắt đầu.

---

## Tasks

```
1. Review diff GoogleSignInScreen.kt + ProfileSetupScreen.kt: Fluent spec đúng
   (không gradient, dùng Dimens.*, stringResource — không hardcode dp hay string literal)
   Files: app/src/main/java/com/helios/redshark/ui/auth/GoogleSignInScreen.kt
          app/src/main/java/com/helios/redshark/ui/auth/ProfileSetupScreen.kt

2. Rà soát ProfileEditScreen.kt + ProfileViewScreen.kt:
   - Skill chips dùng AssistChip/FilterChip (Fluent spec)
   - Bio field maxLength = 280
   - Nút Edit ẩn khi isOwner = false
   Files: app/src/main/java/com/helios/redshark/ui/profile/ProfileEditScreen.kt
          app/src/main/java/com/helios/redshark/ui/profile/ProfileViewScreen.kt

3. Rà soát AuthViewModel.kt + ProfileViewModel.kt:
   - Validate displayName 3..50 ở tầng UseCase (không hardcode trong ViewModel)
   - Validate bio ≤ 280 trước khi gọi UpdateProfileUseCase
   - Không có Log.d in token / email plaintext
   Files: app/src/main/java/com/helios/redshark/ui/auth/AuthViewModel.kt
          app/src/main/java/com/helios/redshark/ui/profile/ProfileViewModel.kt

4. ./gradlew assembleDebug — xác nhận BUILD SUCCESSFUL

5. ./gradlew testDebugUnitTest — xác nhận không regression trong domain/usecase/auth/*

6. Stage + commit các file pending UI trên branch:
   - GoogleSignInScreen.kt, ProfileSetupScreen.kt
   - strings.xml (chỉ phần thay đổi auth_* và profile_*)
   Commit: feat(auth): finalize Fluent UI for auth and profile screens

7. Manual test pass toàn bộ TC-A01..TC-A19 trên emulator/device
   Xem chi tiết từng TC tại: docs/CHECK-1-AUTH.md

8. Sửa mọi TC fail → re-run bước 4–5 → commit fix riêng cho từng bug

9. [TASK CUỐI] Cập nhật đồng bộ tài liệu:
   - CHECK-1-AUTH.md: điền cột "Kết quả nghiệm thu" cho TC-A01..TC-A19
   - REPORT.md: điền kết quả kiểm thử PLAN-1 (số TC pass/fail, %)
   - WBS.md: đánh dấu PLAN-1-AUTH complete
```

---

## Critical files

| File | Loại thay đổi |
|---|---|
| `ui/auth/GoogleSignInScreen.kt` | Review + commit pending |
| `ui/auth/ProfileSetupScreen.kt` | Review + commit pending |
| `ui/profile/ProfileEditScreen.kt` | Review |
| `ui/profile/ProfileViewScreen.kt` | Review |
| `ui/auth/AuthViewModel.kt` | Review |
| `ui/profile/ProfileViewModel.kt` | Review |
| `res/values/strings.xml` | Commit pending (phần auth/profile) |

---

## Lưu ý phối hợp

Sỹ commit `strings.xml` (phần auth/profile) trước khi Hải commit phần content (PLAN-2 task 7) để tránh merge conflict trên cùng file.
