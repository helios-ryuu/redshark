# CHECK-1-AUTH.md — Kiểm thử thủ công: Xác thực và Hồ sơ

WBS tham chiếu: [WBS.md](WBS.md) — kiểm thử cho nhóm công việc `3.0`.

> **Ghi chú trạng thái:**
> - ✅ Code implemented + unit test xanh
> - 🔲 Code implemented, chờ test thủ công trên thiết bị/emulator
> - ⬜ Chưa implement

| ID     | Chức năng                         | Các bước                                               | Kết quả mong đợi                                                       | Trạng thái |
|--------|-----------------------------------|--------------------------------------------------------|------------------------------------------------------------------------|------------|
| TC-A01 | Đăng nhập Google thành công       | Tap "Sign in with Google" → chọn tài khoản             | Xác thực thành công, điều hướng theo trạng thái hồ sơ                  | ✅          |
| TC-A02 | Google Sign-In huỷ giữa chừng     | Đóng popup Google                                      | Không thay đổi state, không crash                                      | ✅          |
| TC-A03 | Mất mạng khi đăng nhập Google     | Airplane mode → login                                  | Hiển thị lỗi, cho phép thử lại                                         | ✅          |
| TC-A04 | Tạo user lần đầu                  | Đăng nhập Google với tài khoản chưa từng dùng app      | Gọi Firestore `users/{uid}` set/merge, tạo document user đầy đủ fields | ✅          |
| TC-A05 | Hoàn thiện hồ sơ lần đầu bắt buộc | Login lần đầu thiếu `displayName` → bấm tiếp tục       | Bị chặn, nút disabled, hiển thị lỗi validate                           | ✅          |
| TC-A06 | Validate `displayName` quá ngắn   | Nhập `displayName` < 3 ký tự                           | Bị chặn, không gọi mutation                                            | ✅          |
| TC-A07 | Validate `displayName` quá dài    | Nhập `displayName` > 50 ký tự                          | Bị chặn (UI cap 50), không gọi mutation                                | ✅          |
| TC-A08 | Hoàn thiện hồ sơ lần đầu hợp lệ   | Nhập `displayName` 3..50 → lưu                         | `CompleteFirstProfile` thành công, điều hướng Home                     | ✅          |
| TC-A09 | Duy trì phiên đăng nhập           | Đăng nhập xong tắt hẳn ứng dụng, mở lại                | Vẫn ở Home (không yêu cầu đăng nhập lại)                               | ✅          |
| TC-A10 | Đăng xuất                         | Settings → "Sign out" → confirm dialog                 | Về màn hình Auth, DataStore cleared                                    | ✅          |
| TC-A11 | Xem profile chính mình            | Icon AccountCircle trên HomeScreen → ProfileViewScreen | Hiển thị `displayName`, bio, skills; nút Edit hiện (isOwner)           | ✅          |
| TC-A12 | Edit displayName hợp lệ           | ProfileEditScreen → đổi name (3..50) → Save            | UI cập nhật, `updateProfile` thành công, navigate back                 | ✅          |
| TC-A13 | Upload avatar (R2)                | Edit → tap avatar → chọn ảnh → tự động upload          | Upload R2 SigV4 → `avatarUrl` cập nhật                                 | ✅          |
| TC-A14 | Upload avatar > 5MB               | Chọn ảnh > 5MB                                         | `UploadAvatarUseCase` trả `ValidationException`, Snackbar              | ✅          |
| TC-A15 | Xem profile user khác             | Điều hướng `profile/{id}` với id khác currentUser      | `ProfileViewScreen`, isOwner=false, không hiện nút Edit                | ✅          |
| TC-A16 | Edit bio > 280 ký tự              | Nhập bio > 280 ký tự                                   | UI cap 280 + validate, nút Save disabled                               | ✅          |
| TC-A17 | Chọn skills                       | ProfileEditScreen → tap skill chip                     | Toggle selected, `updateProfile` gửi đúng danh sách                    | ✅          |
| TC-A18 | Xóa tài khoản                     | Settings → Delete account → confirm                    | Dialog hiện, placeholder — chưa implement xóa thật                     | ✅          |
| TC-A19 | Không lộ token trong log          | `adb logcat` trong quá trình login                     | Không thấy `idToken`/access token plaintext                            | ✅          |
