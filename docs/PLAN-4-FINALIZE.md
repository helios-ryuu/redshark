# PLAN-4-FINALIZE.md — Thực thi: Email/Password Auth + NFR + Release

**Phụ trách:** Sỹ
**Tiêu chuẩn nghiệm thu:** [CHECK-1-AUTH.md](CHECK-1-AUTH.md) — TC-A20..TC-A30; [CHECK-3-INTERACTION.md](CHECK-3-INTERACTION.md) — TC-X01..TC-X07
**Prerequisites:** PLAN-1, PLAN-2, PLAN-3 đều complete và merge về develop.

---

## Phần 1 — Tính năng Đăng ký / Đăng nhập Email-Password

```
1. [SCHEMA] Xác nhận docs/SCHEMA.md collection `users` có 3 trường mới:
   username, dateOfBirth, authProvider — nếu chưa cập nhật thì update ngay.

2. [DOMAIN] Cập nhật domain/model/User.kt:
   Thêm: username: String, dateOfBirth: Instant?, authProvider: AuthProvider (enum {GOOGLE, EMAIL})

3. [DOMAIN] Cập nhật domain/repository/AuthRepository.kt, thêm 3 phương thức:
   suspend fun signInEmailPassword(email: String, password: String): Result<User>
   suspend fun signUpEmailPassword(username: String, email: String, password: String,
                                  dateOfBirth: Instant): Result<User>
   suspend fun isUsernameAvailable(username: String): Boolean

4. [DOMAIN] Tạo 3 use case mới trong domain/usecase/auth/:
   - SignInEmailPasswordUseCase.kt
       validate: email format hợp lệ, password ≥ 8 ký tự
       gọi: authRepository.signInEmailPassword(email, password)
   - SignUpEmailPasswordUseCase.kt
       validate: username format [a-z0-9._-] + length 3..30
                 email format hợp lệ
                 password: ≥ 8 ký tự + ≥ 1 chữ hoa + ≥ 1 số
                 dateOfBirth: không là tương lai; tuổi ≥ 13
       gọi: isUsernameAvailable → nếu trùng throw UsernameAlreadyTakenException
            authRepository.signUpEmailPassword(...)
   - CheckUsernameAvailabilityUseCase.kt
       Firestore query: users where username == value; trả Boolean

5. [DATA] Cập nhật data layer:
   - data/remote/firestore/dto/UserDto.kt: thêm username, dateOfBirth, authProvider
   - data/mapper/UserMapper.kt: map 3 trường mới cả 2 chiều (DTO ↔ domain)
   - data/remote/firebase/FirebaseAuthSource.kt: thêm
       suspend fun signInEmailPassword(email: String, password: String): FirebaseUser
       suspend fun createUserEmailPassword(email: String, password: String): FirebaseUser
   - data/repository/AuthRepositoryImpl.kt: implement 3 phương thức mới:
       signUpEmailPassword: createUserEmailPassword → upsert Firestore doc với
                            username, dateOfBirth, authProvider = "EMAIL"
       signInEmailPassword: FirebaseAuth signIn → load user doc từ Firestore
       isUsernameAvailable: query Firestore users where username == value

6. [UI] Cập nhật ui/auth/GoogleSignInScreen.kt → màn hình Login chung:
   Layout mới (từ trên xuống, scroll):
   - Logo / headline "RedShark"
   - OutlinedTextField: Email
   - OutlinedTextField: Password (PasswordVisualTransformation, toggle icon show/hide)
   - Button "Đăng nhập" (primary, full-width) → gọi signInEmail()
   - Row(Divider + Text "hoặc" + Divider)
   - OutlinedButton "Tiếp tục với Google" (full-width)
   - TextButton "Chưa có tài khoản? Đăng ký" → navigate(Routes.REGISTER)
   Lỗi hiện qua InlineErrorText ngay dưới field tương ứng.

7. [UI] Tạo mới ui/auth/RegisterScreen.kt:
   Layout (Column với verticalScroll):
   - OutlinedTextField: Username
       trailingIcon: loading khi đang check; icon available/taken sau check
       InlineErrorText nếu taken hoặc format sai
   - OutlinedTextField: Email
   - OutlinedTextField: Password (show/hide)
   - OutlinedTextField: Confirm Password
       InlineErrorText nếu không khớp
   - DatePickerDialog trigger: "Ngày sinh" → chọn date → hiện "DD/MM/YYYY"
       InlineErrorText nếu tuổi < 13 hoặc tương lai
   - Button "Đăng ký" (disabled nếu form có lỗi hoặc đang validate)
   - TextButton "Đã có tài khoản? Đăng nhập" → popBackStack

8. [UI] Cập nhật ui/auth/AuthViewModel.kt:
   Thêm state riêng cho email login và register:
   - signInEmail(email, password): gọi SignInEmailPasswordUseCase → emit loading/success/error
   - signUpEmail(username, email, password, dob): gọi SignUpEmailPasswordUseCase
   - checkUsername(username): gọi CheckUsernameAvailabilityUseCase (debounce 500ms)
   UiState dùng data class(isLoading, errorMessage, data) — nhất quán với pattern hiện tại.

9. [NAV] Cập nhật navigation:
   - ui/navigation/Routes.kt: thêm const val REGISTER = "auth/register"
   - ui/navigation/NavGraph.kt: thêm composable(Routes.REGISTER) { RegisterScreen(...) }
   Back stack auth: Register popBackStack về Login, không push Home.

10. [STRINGS] Bổ sung vào res/values/strings.xml (prefix auth_*):
    auth_label_email, auth_label_password, auth_label_confirm_password
    auth_label_username, auth_label_dob, auth_hint_username
    auth_action_login, auth_action_register, auth_action_switch_register, auth_action_switch_login
    auth_error_username_taken, auth_error_username_format, auth_error_username_length
    auth_error_password_weak, auth_error_password_mismatch
    auth_error_dob_invalid, auth_error_dob_underage

11. ./gradlew assembleDebug — BUILD SUCCESSFUL sau toàn bộ thay đổi

12. ./gradlew testDebugUnitTest — thêm + chạy unit test cho 3 use case mới:
    - SignInEmailPasswordUseCase: email format invalid, password < 8 ký tự, Firebase error
    - SignUpEmailPasswordUseCase: username format sai, DOB tuổi < 13, password yếu, username trùng
    - CheckUsernameAvailabilityUseCase: trả true (available) và false (taken)

13. Manual test TC-A20..TC-A30 trên emulator/device
    Xem chi tiết tại: docs/CHECK-1-AUTH.md — phần PLAN-4-FINALIZE
```

---

## Phần 2 — NFR & Release

```
14. TC-X01 Dark mode: bật Dark theme emulator → kiểm tra 5 tab + LoginScreen + RegisterScreen
    Không hardcode màu; mọi text/background dùng MaterialTheme.colorScheme.*
    Files: ui/theme/Theme.kt, ui/theme/Color.kt

15. TC-X02 Rotation: xoay tại HomeScreen, IdeaDetailScreen, ConversationScreen,
    LoginScreen, RegisterScreen → ViewModel state giữ nguyên, không reload, không crash

16. TC-X03 System back: test pop stack tại:
    CreateIdea→Home, Conversation→ConversationList, ProfileEdit→ProfileView, Register→Login
    File: ui/navigation/NavGraph.kt

17. TC-X04 Deep link: verify Routes.kt có pattern redshark://idea/{id}
    Test: adb shell am start -W -a android.intent.action.VIEW -d "redshark://idea/TEST_ID"
    Xác nhận navigate đến IdeaDetailScreen với ideaId đúng.

18. TC-X05 R2 avatar: Coil load thành công → đóng → mở lại → cache hit (không có request mới)
    File: ui/common/AvatarImage.kt

19. TC-X06 Cold start < 3s: force-stop app → mở lại (user đã đăng nhập)
    Đo: adb logcat | grep "Displayed" hoặc Android Studio Profiler
    Target: < 3000ms đến màn hình Home

20. TC-X07 LeakCanary 0 leaks:
    Thêm vào gradle/libs.versions.toml: leakcanary = "<latest>"
    Thêm vào app/build.gradle.kts: debugImplementation(libs.leakcanary)
    Điều hướng 10 màn hình: Auth→Home→IdeaDetail→CreateIdea→ProfileEdit→
    ConversationList→Conversation→Notification→MyIdeas→Settings
    Xác nhận LeakCanary notification: 0 leaks detected

21. Dọn kỹ thuật:
    - Xóa mọi Log.d / println() không cần thiết trong source (không bỏ Timber log hợp lệ)
    - Loại dependency trùng lặp trong gradle/libs.versions.toml
    - ./gradlew lint → xử lý tất cả warning mức Error (không để suppress)

22. Release hardening:
    - app/build.gradle.kts: minifyEnabled = true trong buildTypes { release }
    - Xác nhận proguardFiles trỏ đúng rules file
    - gitleaks detect --source . --verbose → 0 findings

23. Full regression spot-check:
    - CHECK-1-AUTH: TC-A01, TC-A03, TC-A09, TC-A13, TC-A19
    - CHECK-2-CONTENT: TC-C01, TC-C07, TC-C10, TC-C21, TC-C24
    - CHECK-3-INTERACTION: xác nhận TC-N01..N08 + TC-M01..M10 đều ✅

24. Cập nhật versionCode + versionName = "1.0.0" trong app/build.gradle.kts

25. Generate signed APK:
    Build → Generate Signed Bundle/APK → APK → release keystore → release variant
    Install trên device sạch → smoke test (login Google + login email)

26. Git tag + upload:
    git tag v1.0.0 -m "Release v1.0.0 — Production"
    git push origin v1.0.0
    Upload APK lên GitHub Releases với tag v1.0.0

27. [TASK CUỐI] Cập nhật đồng bộ tài liệu:
    - CHECK-1-AUTH.md: TC-A20..TC-A30 → điền kết quả nghiệm thu
    - CHECK-3-INTERACTION.md: TC-X01..TC-X07 → ✅
    - SCHEMA.md: xác nhận bảng users phản ánh đúng username/dateOfBirth/authProvider
    - REPORT.md: Bảng 5.3 (NFR), Bảng 5.4 (cold start ms, APK size MB),
                 kết quả tổng hợp cuối cùng CHECK-1/2/3
    - WBS.md: đánh dấu WBS 6.0 + WBS 7.0 complete; ghi ngày tag v1.0.0
```

---

## Critical files (tính năng mới)

| File | Loại thay đổi |
|---|---|
| `domain/model/User.kt` | Thêm 3 field mới |
| `domain/repository/AuthRepository.kt` | Thêm 3 phương thức |
| `domain/usecase/auth/SignInEmailPasswordUseCase.kt` | Tạo mới |
| `domain/usecase/auth/SignUpEmailPasswordUseCase.kt` | Tạo mới |
| `domain/usecase/auth/CheckUsernameAvailabilityUseCase.kt` | Tạo mới |
| `data/remote/firestore/dto/UserDto.kt` | Thêm 3 field |
| `data/mapper/UserMapper.kt` | Map 3 field mới |
| `data/remote/firebase/FirebaseAuthSource.kt` | Thêm 2 phương thức |
| `data/repository/AuthRepositoryImpl.kt` | Implement 3 phương thức mới |
| `ui/auth/GoogleSignInScreen.kt` | Cập nhật layout login |
| `ui/auth/RegisterScreen.kt` | Tạo mới |
| `ui/auth/AuthViewModel.kt` | Thêm state + actions |
| `ui/navigation/Routes.kt` | Thêm REGISTER route |
| `ui/navigation/NavGraph.kt` | Thêm RegisterScreen composable |
| `res/values/strings.xml` | Thêm auth_* strings mới |
| `gradle/libs.versions.toml` | Thêm LeakCanary dep |
| `app/build.gradle.kts` | LeakCanary + minifyEnabled |
| `docs/SCHEMA.md` | Xác nhận bảng users |

---

## Schema changes tóm tắt

| Trường mới | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| `username` | String | NOT NULL, UNIQUE, 3..30, `[a-z0-9._-]` | Enforce app-side |
| `dateOfBirth` | Timestamp? | NULL cho Google user cũ | Tuổi ≥ 13 enforce tại UseCase |
| `authProvider` | String | {GOOGLE, EMAIL}, NOT NULL | Backfill user cũ = "GOOGLE" |
