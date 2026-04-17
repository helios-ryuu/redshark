# PLAN-2-AUTH.md — Giai đoạn 2: Xác thực và Hồ sơ

**Thời gian:** 16/04/2026 – 21/04/2026 (giai đoạn chuyển tiếp sau init)
**Mốc quan trọng:** Bắt buộc có ít nhất 1 commit tính năng xác thực trước **21/04/2026** (chậm nhất 20/04/2026).
**WBS tham chiếu:** [WBS.md](WBS.md) — nhóm công việc `3.0`.
**Trạng thái:** ✅ Code hoàn tất — `feature/auth-google-signin` (cập nhật 16/04/2026, chờ manual test)

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
firebase-bom       = { group = "com.google.firebase", name = "firebase-bom",       version.ref = "firebaseBom" }
firebase-auth      = { group = "com.google.firebase", name = "firebase-auth" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore" }

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
  → if new user: Firestore users/{uid} set/merge (upsertUser)
  → Firestore users/{uid} get (getUser)
  → nếu thiếu displayName hợp lệ: profile/setup
  → nếu profile hợp lệ: home
```

### 4.2 Hoàn thiện hồ sơ lần đầu
```
ProfileSetupScreen
  → validate displayName (3..50)
  → Firestore users/{uid} update (updateProfile)
  → home
```

### 4.3 Upload avatar
```
ProfileEditScreen → pick image
  → ImageCompressor.compress(1MB, 512x512)
  → R2Client.putObject(bucket, key="avatars/{uid}.jpg", body)
  → Firestore users/{uid} update (avatarUrl)
```

## 5. Acceptance Criteria

> Trạng thái kiểm tra thủ công tính đến 16/04/2026:

- [ ] Đăng nhập Google trên emulator (thêm SHA-1 debug vào Firebase) — cần test thiết bị
- [x] Login lần đầu thiếu `displayName` → bắt buộc vào `profile/setup` — logic trong `AuthViewModel`
- [x] `displayName` chỉ chấp nhận 3..50 ký tự — validate trong `CompleteFirstProfileUseCase` + `UpdateProfileUseCase` + UI
- [x] `onAuthStateChanged` persist qua restart app — `FirebaseAuthSource.observeAuthState()` + DataStore
- [x] Avatar upload R2 — `R2Client` (OkHttp + AWS SigV4), `UploadAvatarUseCase`, `MediaRepositoryImpl` — cần test thiết bị
- [x] Không lộ token đăng nhập trong logcat release — `Timber.DebugTree` chỉ plant trong DEBUG

### Tiến độ triển khai (16/04/2026)
- [x] `libs.versions.toml` — bổ sung Hilt, KSP, Navigation, Credentials, Firebase, DataStore, Timber, OkHttp, MaterialIconsExtended
- [x] `RedSharkApp.kt` — `@HiltAndroidApp` + Timber setup
- [x] `core/util/Result.kt`, `core/error/AppException.kt`, `core/error/ErrorMapper.kt`
- [x] `core/di/` — AppModule, FirebaseModule, R2Module, RepositoryModule (bind Auth, Profile, Media, Firestore)
- [x] `domain/model/User.kt`
- [x] `domain/repository/AuthRepository.kt`, `ProfileRepository.kt`, `MediaRepository.kt`
- [x] `domain/usecase/auth/` — 6 use cases (SignIn, Observe, SignOut, CompleteFirstProfile, UpdateProfile, UploadAvatar)
- [x] `data/remote/firebase/FirebaseAuthSource.kt`, `GoogleSignInHelper.kt` (One Tap + fallback)
- [x] `data/remote/firestore/FirestoreSource.kt` (interface), `FirestoreSourceImpl.kt` (upsertUser, getUser, updateProfile), `dto/UserDto.kt`
- [x] `data/remote/r2/R2Client.kt` — PUT object với AWS SigV4 signing
- [x] `data/repository/AuthRepositoryImpl.kt` — sign-in + upsertUser Firestore call
- [x] `data/repository/ProfileRepositoryImpl.kt` — wired với FirestoreSource
- [x] `data/repository/MediaRepositoryImpl.kt` — uploadAvatar via R2Client
- [x] `data/mapper/UserMapper.kt` — FirebaseUser + UserDto → domain User
- [x] `data/local/datastore/UserPreferences.kt`
- [x] `ui/navigation/Routes.kt`, `NavGraph.kt` — tất cả 6 routes (auth, setup, home, profile/{id}, profile/edit, settings)
- [x] `ui/feature/auth/GoogleSignInScreen.kt`, `ProfileSetupScreen.kt`, `AuthViewModel.kt`
- [x] `ui/feature/home/HomeScreen.kt` — TopAppBar với icon profile + settings
- [x] `ui/feature/profile/ProfileViewModel.kt`, `ProfileViewScreen.kt`, `ProfileEditScreen.kt`
- [x] `ui/feature/settings/SettingsScreen.kt` — sign out (dialog confirm) + delete account placeholder
- [x] Unit tests: `SignInGoogleUseCaseTest`, `ObserveAuthStateUseCaseTest`, `CompleteFirstProfileUseCaseTest`, `SignOutUseCaseTest`, `UpdateProfileUseCaseTest`, `UploadAvatarUseCaseTest`
- [x] `FirestoreSourceImpl` — Firestore SDK: `upsertUser` (set/merge), `getUser` (get), `updateProfile` (update)
- [x] `FirebaseModule` — provide `FirebaseFirestore.getInstance()` via Hilt
- [x] `libs.versions.toml` — thêm `firebase-firestore`

## 6. Rủi ro / Mitigation
- **Google Sign-In SHA-1:** test debug + release keystore
- **R2 CORS:** Android không cần CORS nhưng signed URL phải TTL < 15 phút
