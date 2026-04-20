# PLAN-2-AUTH.md — Giai đoạn 2: Xác thực và Hồ sơ

**Thời gian:** 16/04/2026 – 21/04/2026 (giai đoạn chuyển tiếp sau init)
**Mốc quan trọng:** Bắt buộc có ít nhất 1 commit tính năng xác thực trước **21/04/2026** (chậm nhất 20/04/2026).
**WBS tham chiếu:** [WBS.md](WBS.md) — nhóm công việc `3.0`.
**Trạng thái:** 🔄 Đang triển khai — `feature/auth-google-signin` (16/04/2026)

**Phân công theo WBS:**
- Phụ trách nhóm xác thực và hồ sơ: **Sỹ** (lập trình chính, chịu trách nhiệm commit chính).
- Thành viên phối hợp: **Nam**, **Hải**.

> **Tiền điều kiện:** Tạo nhánh `develop` từ `main` trước khi mở `feature/auth-*`. Xem hướng dẫn tại [GIT.md — Thiết lập nhánh develop ban đầu](GIT.md).
> ✅ Nhánh `develop` đã tạo và `feature/auth-google-signin` đã mở từ `develop` ngày 16/04/2026.

## 0. Phụ thuộc cần bổ sung vào `libs.versions.toml`

Giai đoạn 1 chỉ cấu hình Compose cơ bản. Trước khi bắt đầu code auth, cần bổ sung:

```toml
[versions]
# Đã có:
# agp, kotlin, composeBom, coreKtx, lifecycleRuntimeKtx, activityCompose

# Bổ sung mới:
hilt            = "2.56.1"
ksp             = "2.3.20-2.0.1"            # phải khớp kotlin version
navigationCompose = "2.9.0"
lifecycleViewModel = "2.10.0"
credentialsAuth = "1.5.0"
googleid        = "1.1.1"
firebaseBom     = "33.13.0"
timber          = "5.0.1"
okhttp          = "4.12.0"

[libraries]
# Hilt
hilt-android           = { group = "com.google.dagger", name = "hilt-android",           version.ref = "hilt" }
hilt-compiler          = { group = "com.google.dagger", name = "hilt-android-compiler",  version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt",   name = "hilt-navigation-compose", version = "1.2.0" }

# Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# ViewModel
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewModel" }

# Credential Manager (Google Sign-In)
androidx-credentials            = { group = "androidx.credentials", name = "credentials",                    version.ref = "credentialsAuth" }
androidx-credentials-play       = { group = "androidx.credentials", name = "credentials-play-services-auth", version.ref = "credentialsAuth" }
googleid                        = { group = "com.google.android.libraries.identity.googleid", name = "googleid", version.ref = "googleid" }

# Firebase (dùng BoM để đồng bộ version)
firebase-bom  = { group = "com.google.firebase", name = "firebase-bom",  version.ref = "firebaseBom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }

# Logging
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }

# OkHttp (R2 upload — SigV4)
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }

[plugins]
# Bổ sung mới:
kotlin-android  = { id = "org.jetbrains.kotlin.android",   version.ref = "kotlin" }
hilt-android    = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp             = { id = "com.google.devtools.ksp",        version.ref = "ksp" }
google-services = { id = "com.google.gms.google-services", version = "4.4.2" }
```

> **Lưu ý:** Firebase Data Connect Android SDK (cho `dataconnect:sdk:generate`) được thêm ở giai đoạn tích hợp FDC, không cần ngay cho auth cơ bản.

## 1. Phạm vi chức năng
- Đăng nhập Google (One-Tap / GIS)
- Hoàn thiện hồ sơ lần đầu (bắt buộc `displayName` 3..50)
- Đăng xuất
- Xem / Chỉnh sửa profile (displayName, avatar, bio, skills)
- Upload avatar lên Cloudflare R2
- Xem profile công khai của user khác

## 2. Màn hình
| Route           | Screen               | Ghi chú                                        |
|-----------------|----------------------|------------------------------------------------|
| `auth/google`   | `GoogleSignInScreen` | Bắt đầu Google Sign-In                         |
| `profile/setup` | `ProfileSetupScreen` | Hoàn thiện hồ sơ lần đầu (`displayName` 3..50) |
| `profile/{id}`  | `ProfileViewScreen`  | Profile công khai                              |
| `profile/edit`  | `ProfileEditScreen`  | Chỉnh sửa                                      |
| `settings`      | `SettingsScreen`     | Logout, delete account                         |

## 3. Components
- `FirebaseAuthSource` — wrapper `FirebaseAuth.getInstance()`
- `GoogleSignInHelper` — Credential Manager API
- `AuthRepository` / `AuthRepositoryImpl`
- `ProfileRepository` / `ProfileRepositoryImpl`
- `MediaRepository` (R2 upload)
- UseCases: `SignInGoogleUseCase`, `CompleteFirstProfileUseCase`, `SignOutUseCase`, `ObserveAuthStateUseCase`, `UpdateProfileUseCase`, `UploadAvatarUseCase`

## 4. Luồng xử lý (tóm tắt)

### 4.1 Sign-in Google
```
GoogleSignInHelper.requestCredential()
  → GoogleAuthProvider.getCredential(idToken)
  → signInWithCredential()
  → if new user: UpsertUser mutation
  → GetMe
  → nếu thiếu displayName hợp lệ: profile/setup
  → nếu profile hợp lệ: home
```

### 4.2 Hoàn thiện hồ sơ lần đầu
```
ProfileSetupScreen
  → validate displayName (3..50)
  → UpdateProfile mutation
  → home
```

### 4.3 Upload avatar
```
ProfileEditScreen → pick image
  → ImageCompressor.compress(1MB, 512x512)
  → R2Client.putObject(bucket, key="avatars/{uid}.jpg", body)
  → UpdateProfile mutation (avatarUrl)
```

## 5. Acceptance Criteria

> Trạng thái kiểm tra thủ công tính đến 16/04/2026:

- [ ] Đăng nhập Google trên emulator (thêm SHA-1 debug vào Firebase)
- [x] Login lần đầu thiếu `displayName` → bắt buộc vào `profile/setup` — logic trong `AuthViewModel`
- [x] `displayName` chỉ chấp nhận 3..50 ký tự — validate trong `CompleteFirstProfileUseCase` + UI
- [x] `onAuthStateChanged` persist qua restart app — `FirebaseAuthSource.observeAuthState()` + DataStore
- [ ] Avatar hiển thị sau upload (R2 public URL hoặc presigned) — R2 upload chưa implement
- [x] Không lộ token đăng nhập trong logcat release — `Timber.DebugTree` chỉ plant trong DEBUG

### Tiến độ triển khai (16/04/2026)
- [x] `libs.versions.toml` — bổ sung Hilt, KSP, Navigation, Credentials, Firebase, DataStore, Timber, OkHttp
- [x] `RedSharkApp.kt` — `@HiltAndroidApp` + Timber setup
- [x] `core/util/Result.kt`, `core/error/AppException.kt`, `core/error/ErrorMapper.kt`
- [x] `core/di/` — AppModule, FirebaseModule, R2Module, RepositoryModule
- [x] `domain/model/User.kt`
- [x] `domain/repository/AuthRepository.kt`, `ProfileRepository.kt`
- [x] `domain/usecase/auth/` — 4 use cases (SignIn, Observe, SignOut, CompleteFirstProfile)
- [x] `data/remote/firebase/FirebaseAuthSource.kt`, `GoogleSignInHelper.kt` (One Tap + fallback)
- [x] `data/repository/AuthRepositoryImpl.kt`, `ProfileRepositoryImpl.kt`
- [x] `data/local/datastore/UserPreferences.kt`
- [x] `ui/navigation/Routes.kt`, `NavGraph.kt`
- [x] `ui/feature/auth/GoogleSignInScreen.kt`, `ProfileSetupScreen.kt`, `AuthViewModel.kt`
- [x] Unit tests: `SignInGoogleUseCaseTest`, `ObserveAuthStateUseCaseTest`, `CompleteFirstProfileUseCaseTest`, `SignOutUseCaseTest`
- [ ] Profile edit + avatar upload (giai đoạn tiếp theo)

## 6. Rủi ro / Mitigation
- **Google Sign-In SHA-1:** test debug + release keystore
- **R2 CORS:** Android không cần CORS nhưng signed URL phải TTL < 15 phút
