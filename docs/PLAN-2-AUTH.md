# PLAN-2-AUTH.md — Giai đoạn 2: Xác thực và Hồ sơ

**Thời gian:** 16/04/2026 – 21/04/2026 (giai đoạn chuyển tiếp sau init)
**Mốc quan trọng:** Bắt buộc có ít nhất 1 commit tính năng xác thực trước **21/04/2026** (chậm nhất 20/04/2026).
**WBS tham chiếu:** [WBS.md](WBS.md) — nhóm công việc `3.0`.

**Phân công theo WBS:**
- Phụ trách nhóm xác thực và hồ sơ: **Sỹ** (lập trình chính, chịu trách nhiệm commit chính).
- Thành viên phối hợp: **Nam**, **Hải**.

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
- [ ] Đăng nhập Google trên emulator (thêm SHA-1 debug vào Firebase)
- [ ] Login lần đầu thiếu `displayName` → bắt buộc vào `profile/setup`
- [ ] `displayName` chỉ chấp nhận 3..50 ký tự
- [ ] `onAuthStateChanged` persist qua restart app
- [ ] Avatar hiển thị sau upload (R2 public URL hoặc presigned)
- [ ] Không lộ token đăng nhập trong logcat release

## 6. Rủi ro / Mitigation
- **Google Sign-In SHA-1:** test debug + release keystore
- **R2 CORS:** Android không cần CORS nhưng signed URL phải TTL < 15 phút
