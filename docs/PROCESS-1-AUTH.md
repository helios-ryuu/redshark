# PROCESS-1-AUTH.md — Quy trình nghiệp vụ: Xác thực và Hồ sơ

WBS tham chiếu: [WBS.md](WBS.md) — nhóm công việc `3.0`.

## 1. Đăng nhập Google

| Bước | Tương tác giao diện                   | Dịch vụ được gọi                                                       | Bảng/thuộc tính ảnh hưởng                                |
|------|--------------------------------------|------------------------------------------------------------------------|----------------------------------------------------------|
| 1    | `AuthScreen` → "Tiếp tục với Google" | `GoogleSignInHelper.requestCredential()`                               | —                                                        |
| 2    | Người dùng chọn tài khoản            | Google trả `idToken`                                                   | —                                                        |
| 3    | Đổi credential                       | `GoogleAuthProvider.getCredential(idToken)` → `signInWithCredential()` | Firebase Auth                                            |
| 4    | Sau khi Firebase trả UID             | `DataConnectSource.upsertUser(uid, email, displayName?)`               | Insert/Update `users(id, email, displayName, createdAt)` |
| 5    | `onAuthStateChanged` fire            | `GetMe` query                                                          | Select `users` where id = auth.uid                       |
| 6    | Nếu thiếu `displayName` hợp lệ       | Điều hướng tới `profile/setup`                                         | —                                                        |
| 7    | Nếu hồ sơ đã hợp lệ                  | Điều hướng tới `home`                                                  | —                                                        |

## 2. Hoàn thiện hồ sơ lần đầu

| Bước | Giao diện                                     | Dịch vụ                                                     | Bảng                                    |
|------|----------------------------------------------|--------------------------------------------------------------|-----------------------------------------|
| 1    | `ProfileSetupScreen` hiện sau login đầu tiên | —                                                            | —                                       |
| 2    | User nhập `displayName` (3..50 ký tự)        | Client validate độ dài + trim                                | —                                       |
| 3    | Bấm "Tiếp tục"                               | `CompleteFirstProfileUseCase` → `UpdateProfile(displayName)` | Update `users.displayName`, `updatedAt` |
| 4    | Thành công                                   | Navigate `home`                                              | —                                       |
| 5    | Thất bại mạng                                | Hiển thị lỗi + cho phép retry                                | —                                       |

## 3. Upload Avatar (R2)

| Bước | Giao diện                         | Dịch vụ                                                         | Bảng/lưu trữ                          |
|------|----------------------------------|-----------------------------------------------------------------|---------------------------------------|
| 1    | `ProfileEditScreen` → pick image | `ImagePicker` (system)                                          | —                                     |
| 2    | Nén ảnh                          | `ImageCompressor.compress(maxMB=1, 512x512)`                    | —                                     |
| 3    | Upload                           | `R2Client.putObject("avatars/{uid}.jpg", bytes)` (S3 API SigV4) | Cloudflare R2 bucket `redshark-media` |
| 4    | Lấy URL                          | Build public URL hoặc presigned                                 | —                                     |
| 5    | Cập nhật DB                      | `UpdateProfile(avatarUrl)` mutation                             | Update `users.avatarUrl`, `updatedAt` |
| 6    | UI refresh                       | `GetMe` invalidate                                              | —                                     |

## 4. Đăng xuất

| Bước | UI                             | Service                   | Bảng            |
|------|--------------------------------|---------------------------|-----------------|
| 1    | `SettingsScreen` → "Đăng xuất" | Confirm dialog            | —               |
| 2    | Confirm                        | `FirebaseAuth.signOut()`  | Firebase Auth   |
| 3    | Clear cache                    | `UserPreferences.clear()` | Local DataStore |
| 4    | `onAuthStateChanged(null)`     | NavGraph → Auth           | —               |

## 5. Xem profile user khác

| Bước | UI                                      | Service                 | Bảng         |
|------|-----------------------------------------|-------------------------|--------------|
| 1    | Tap avatar/tên user trong Comment/Issue | Navigate `profile/{id}` | —            |
| 2    | `ProfileViewScreen` load                | `GetUserById(id)` query | Read `users` |
| 3    | Hiển thị info + nút "Nhắn tin"          | —                       | —            |
