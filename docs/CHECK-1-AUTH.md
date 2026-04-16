# CHECK-1-AUTH.md — Kiểm thử thủ công: Xác thực và Hồ sơ

WBS tham chiếu: [WBS.md](WBS.md) — kiểm thử cho nhóm công việc `3.0`.

| ID     | Chức năng                         | Các bước                                               | Kết quả mong đợi                                              | Trạng thái |
|--------|-----------------------------------|--------------------------------------------------------|---------------------------------------------------------------|------------|
| TC-A01 | Đăng nhập Google thành công       | Tap "Tiếp tục với Google" → chọn tài khoản             | Xác thực thành công, điều hướng theo trạng thái hồ sơ         | ⬜          |
| TC-A02 | Google Sign-In huỷ giữa chừng     | Đóng popup Google                                      | Không thay đổi state, không crash                             | ⬜          |
| TC-A03 | Mất mạng khi đăng nhập Google     | Airplane mode → login                                  | Hiển thị "Không có kết nối", cho phép thử lại                 | ⬜          |
| TC-A04 | Tạo user lần đầu                  | Đăng nhập Google với tài khoản chưa từng dùng app      | Tạo record `users` qua `UpsertUser`                           | ⬜          |
| TC-A05 | Hoàn thiện hồ sơ lần đầu bắt buộc | Login lần đầu, để trống `displayName` rồi bấm tiếp tục | Bị chặn, hiển thị lỗi validate                                | ⬜          |
| TC-A06 | Validate `displayName` quá ngắn   | Nhập `displayName` < 3 ký tự                           | Bị chặn, không gọi mutation                                   | ⬜          |
| TC-A07 | Validate `displayName` quá dài    | Nhập `displayName` > 50 ký tự                          | Bị chặn, không gọi mutation                                   | ⬜          |
| TC-A08 | Hoàn thiện hồ sơ lần đầu hợp lệ   | Nhập `displayName` 3..50 → lưu                         | `UpdateProfile` thành công, điều hướng Home                   | ⬜          |
| TC-A09 | Duy trì phiên đăng nhập           | Đăng nhập xong tắt hẳn ứng dụng, mở lại                | Vẫn ở Home (không yêu cầu đăng nhập lại)                      | ⬜          |
| TC-A10 | Đăng xuất                         | Tab Settings → Đăng xuất                               | Về màn hình Auth, `GetMe` không được gọi                      | ⬜          |
| TC-A11 | Xem profile chính mình            | Tab Settings hoặc Profile                              | Hiển thị `displayName`, avatar, bio, skills                   | ⬜          |
| TC-A12 | Edit displayName hợp lệ           | Profile Edit → đổi name (3..50) → Lưu                  | UI cập nhật, mutation `UpdateProfile` thành công              | ⬜          |
| TC-A13 | Upload avatar (R2)                | Edit → chọn ảnh < 1MB → Lưu                            | Ảnh nén → upload R2 → `avatarUrl` cập nhật → hiển thị ảnh mới | ⬜          |
| TC-A14 | Upload avatar lỗi mạng            | Tắt wifi → chọn ảnh → Save                             | Hiển thị lỗi "Không thể upload, thử lại", state rollback      | ⬜          |
| TC-A15 | Upload avatar > 5MB               | Chọn ảnh lớn                                           | Nén xuống ≤ 1MB rồi upload thành công                         | ⬜          |
| TC-A16 | Xem profile user khác             | Tap avatar user trong comment                          | Vào `ProfileViewScreen`, hiển thị info, có nút "Nhắn tin"     | ⬜          |
| TC-A17 | Edit bio > 280 ký tự              | Nhập bio dài                                           | Validate client, chặn lưu                                     | ⬜          |
| TC-A18 | Chọn skills                       | Edit → tap skill chip                                  | Toggle selected, lưu thành công                               | ⬜          |
| TC-A19 | Xóa tài khoản                     | Settings → Delete account → confirm                    | Xóa Firebase Auth + soft delete user, logout                  | ⬜          |
| TC-A20 | Không lộ token trong log          | `adb logcat` trong quá trình login                     | Không thấy `idToken`/access token plaintext                   | ⬜          |
