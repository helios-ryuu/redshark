# PLAN-4-FINALIZE.md — Nhật ký thực thi: Email Auth + NFR + UI Polish + Release

**Phụ trách:** Sỹ
**Ngày hoàn thành implement:** 05/05/2026
**Tiêu chuẩn nghiệm thu:** [CHECK-1-AUTH.md](CHECK-1-AUTH.md) — TC-A20..A30 (11 TC) + [CHECK-3-INTERACTION.md](CHECK-3-INTERACTION.md) — TC-X01..X07 (7 TC)
**Branch:** `feature/plan4-finalize` → merge vào `develop`

---

## Trạng thái: IMPLEMENT HOÀN THÀNH — Chờ kiểm thử thủ công (Tuần 9, 12–17/05)

| Nhóm | TC | Trạng thái |
|------|----|-----------|
| Email/Password Auth | TC-A20..A30 (11 TC) | 🔲 Code implement xong + 27/27 unit tests pass; chờ manual |
| NFR | TC-X01..X07 (7 TC) | 🔲 Infrastructure ready; chờ manual |

---

## Commits thực thi (branch feature/plan4-finalize)

| Hash | Commit message |
|------|----------------|
| `d660634` | `feat(ui): finalize fluent minimalist design across all screens (WBS 4B)` |
| `e88b2dd` | `feat(auth): add email/password domain use cases and data layer` |
| `75bc4cc` | `test(auth): unit tests for email/password use cases (27/27 pass)` |
| `556c81c` | `feat(auth): unified login screen + register screen with email/password` |
| `d472a8d` | `fix(ui): replace all hardcoded dp/string literals + polish IssueDetailScreen` |
| `225d3d4` | `feat(core): NFR setup + release hardening (TC-X01..X07 ready)` |

---

## Tóm tắt thay đổi đã thực hiện

### Domain Layer
- `domain/model/User.kt` — thêm `username: String?`, `dateOfBirth: LocalDate?`, `authProvider: String = "GOOGLE"`
- `domain/repository/AuthRepository.kt` — thêm `signUpEmailPassword`, `signInEmailPassword`, `checkUsernameAvailability`
- `core/error/AppException.kt` — thêm `ConflictException` cho username đã tồn tại
- `domain/usecase/auth/SignInEmailPasswordUseCase.kt` — NEW: validate email (pure Kotlin regex) + password ≥ 8 ký tự
- `domain/usecase/auth/SignUpEmailPasswordUseCase.kt` — NEW: validate displayName (3..50), username format `^[a-z0-9._-]+$` (3..30), email regex, tuổi ≥ 13, password (≥8 + uppercase + digit), kiểm tra username khả dụng
- `domain/usecase/auth/CheckUsernameAvailabilityUseCase.kt` — NEW: validate format rồi query Firestore

### Data Layer
- `data/remote/firestore/dto/UserDto.kt` — thêm 3 trường (backward-compatible nhờ `@IgnoreExtraProperties`)
- `data/mapper/UserMapper.kt` — map `Timestamp ↔ LocalDate` (UTC) cho `dateOfBirth`
- `data/remote/firebase/FirebaseAuthSource.kt` — thêm `signUpEmailPassword()`, `signInEmailPassword()` với `createUserWithEmailAndPassword` / `signInWithEmailAndPassword`
- `data/remote/firestore/FirestoreSourceImpl.kt` — thêm `upsertEmailUser()`, `isUsernameAvailable()`
- `data/repository/AuthRepositoryImpl.kt` — implement 3 phương thức mới của `AuthRepository`

### Unit Tests (27/27 PASS)
- `SignInEmailPasswordUseCaseTest.kt` — 7 test cases (email trống, format sai, password ngắn, success, ...)
- `SignUpEmailPasswordUseCaseTest.kt` — 12 test cases (username ngắn/sai format/đã tồn tại, tuổi <13, password yếu, success, ...)
- `CheckUsernameAvailabilityUseCaseTest.kt` — 8 test cases (available, taken, format sai, length sai, ...)

### UI Layer
- `ui/auth/GoogleSignInScreen.kt` — Redesign thành unified Login screen (LoginViewModel): email + password field (show/hide icon) + Google button + link đến Register
- `ui/auth/RegisterScreen.kt` — NEW: Display Name (char counter), Username (real-time availability debounce 300ms), Email, Date of Birth (DatePickerDialog), Password (strength indicator 4 mức), Confirm Password; dùng `rememberSaveable` để giữ state khi xoay màn hình
- `ui/auth/LoginViewModel.kt` — NEW: sealed `LoginUiState`, `LoginDestination`
- `ui/auth/RegisterViewModel.kt` — NEW: sealed `RegisterUiState`, `UsernameAvailability` enum, debounce check
- `ui/navigation/Routes.kt` — `const val REGISTER = "auth/register"`
- `ui/navigation/NavGraph.kt` — thêm composable route `REGISTER`

### UI Polish (0 hardcoded dp/string)
- `ui/theme/Dimens.kt` — `ButtonProgressIndicatorSize`, `ButtonProgressIndicatorStroke`, `NotificationSheetMaxHeight`
- `ui/createidea/CreateIdeaScreen.kt` / `ui/createissue/CreateIssueScreen.kt` — `18.dp`/`2.dp` → Dimens tokens
- `ui/home/HomeScreen.kt` — `480.dp` → `Dimens.NotificationSheetMaxHeight`
- `ui/ideadetail/IdeaDetailScreen.kt` — `RoundedCornerShape(24.dp)` → `Dimens.InputFieldCorner`
- `ui/home/HomeFeedScreen.kt` — hardcoded subtitle → `stringResource`
- `ui/common/IdeaCard.kt` — `"Today"/"Yesterday"` → `stringResource`
- `ui/issuedetail/IssueDetailScreen.kt` — full Material3 polish: `verticalScroll`, description `OutlinedCard`, assignee `AvatarImage`, "View Idea" `Surface` tile, status `FilterChip`, `IssuePriorityPill`, `HorizontalDivider`
- `res/values/strings.xml` — +44 string keys (auth_*, home_feed_empty_subtitle)

### NFR / Release Hardening
- `app/AndroidManifest.xml` — deep link intent-filter `redshark://idea/{id}` (đã có từ trước, xác nhận TC-X04 ready)
- `app/build.gradle.kts` — `isMinifyEnabled = true`, `isShrinkResources = true`, `versionName = "1.0.0"`, `debugImplementation leakcanary:2.14`
- `app/proguard-rules.pro` — keep rules: Firestore DTOs, Kotlin annotations, stack trace, Hilt entry points

---

## Điều kiện hoàn thành PLAN-4

- [ ] TC-A20..A30: 11/11 thủ công pass (12–14/05/2026)
- [ ] TC-X01..X07: 7/7 thủ công pass (15/05/2026)
- [ ] `./gradlew assembleRelease` BUILD SUCCESSFUL (16/05/2026)
- [ ] APK ký số trên device sạch — smoke test login Google + login email (16/05/2026)
- [ ] Tag `v1.0.0` trên `main` (17/05/2026)

---

## Schema changes

| Trường mới | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| `username` | String? | UNIQUE, 3..30, `^[a-z0-9._-]+$` | NULL cho Google users cũ |
| `dateOfBirth` | Timestamp? | Tuổi ≥ 13 khi đăng ký | NULL cho Google users cũ |
| `authProvider` | String | `"GOOGLE"` \| `"EMAIL"` | Default `"GOOGLE"` cho docs cũ |

---

## Người thực hiện

| Thành viên | Vai trò |
|---|---|
| **Sỹ** | Lead — toàn bộ implement PLAN-4 |
| **Hải** | Review tính năng email-register, phối hợp test |
| **Nam** | Phối hợp NFR: dark mode, rotation, back nav |
