# CHECK-1-AUTH.md — Tiêu chuẩn nghiệm thu: Xác thực và Hồ sơ

WBS tham chiếu: [WBS.md](WBS.md) — kiểm thử cho nhóm công việc `3.0`.

> **Tiêu chuẩn nghiệm thu bắt buộc cho PLAN-1-AUTH và PLAN-4-FINALIZE.**
> Tất cả TC phải đạt ✅ trước khi PLAN tương ứng được tính hoàn thành.

> **Ký hiệu trạng thái:**
> - ✅ Code implemented + unit test xanh
> - 🔲 Code implemented, chờ test thủ công trên thiết bị/emulator
> - ⬜ Chưa implement

**PLAN-1-AUTH — Google Sign-In & Profile (do Sỹ):**

| ID     | Chức năng                          | Các bước                                               | Kết quả mong đợi                                                       | Trạng thái | Kết quả nghiệm thu |
|--------|------------------------------------|--------------------------------------------------------|------------------------------------------------------------------------|------------|--------------------|
| TC-A01 | Đăng nhập Google thành công        | Tap "Tiếp tục với Google" → chọn tài khoản             | Xác thực thành công, điều hướng theo trạng thái hồ sơ                  | ✅          |                    |
| TC-A02 | Google Sign-In huỷ giữa chừng      | Đóng popup Google                                      | Không thay đổi state, không crash                                      | ✅          |                    |
| TC-A03 | Mất mạng khi đăng nhập Google      | Airplane mode → login                                  | Hiển thị lỗi, cho phép thử lại                                         | ✅          |                    |
| TC-A04 | Tạo user lần đầu (Google)          | Đăng nhập Google với tài khoản chưa từng dùng app      | Gọi Firestore `users/{uid}` set/merge, tạo document user đầy đủ fields | ✅          |                    |
| TC-A05 | Hoàn thiện hồ sơ lần đầu bắt buộc  | Login lần đầu thiếu `displayName` → bấm tiếp tục       | Bị chặn, nút disabled, hiển thị lỗi validate                           | ✅          |                    |
| TC-A06 | Validate `displayName` quá ngắn    | Nhập `displayName` < 3 ký tự                           | Bị chặn, không gọi mutation                                            | ✅          |                    |
| TC-A07 | Validate `displayName` quá dài     | Nhập `displayName` > 50 ký tự                          | Bị chặn (UI cap 50), không gọi mutation                                | ✅          |                    |
| TC-A08 | Hoàn thiện hồ sơ lần đầu hợp lệ    | Nhập `displayName` 3..50 → lưu                         | `CompleteFirstProfile` thành công, điều hướng Home                     | ✅          |                    |
| TC-A09 | Duy trì phiên đăng nhập            | Đăng nhập xong tắt hẳn ứng dụng, mở lại                | Vẫn ở Home (không yêu cầu đăng nhập lại)                               | ✅          |                    |
| TC-A10 | Đăng xuất                          | Settings → "Sign out" → confirm dialog                 | Về màn hình Auth, DataStore cleared                                    | ✅          |                    |
| TC-A11 | Xem profile chính mình             | Icon AccountCircle trên HomeScreen → ProfileViewScreen | Hiển thị `displayName`, bio, skills; nút Edit hiện (isOwner)           | ✅          |                    |
| TC-A12 | Edit displayName hợp lệ            | ProfileEditScreen → đổi name (3..50) → Save            | UI cập nhật, `updateProfile` thành công, navigate back                 | ✅          |                    |
| TC-A13 | Upload avatar (R2)                 | Edit → tap avatar → chọn ảnh → tự động upload          | Upload R2 SigV4 → `avatarUrl` cập nhật                                 | ✅          |                    |
| TC-A14 | Upload avatar ảnh thô > 5MB        | Chọn ảnh raw > 5MB (trước nén ≤ 1MB)                   | `UploadAvatarUseCase` trả `ValidationException`, Snackbar              | ✅          |                    |
| TC-A15 | Xem profile user khác              | Điều hướng `profile/{id}` với id khác currentUser      | `ProfileViewScreen`, isOwner=false, không hiện nút Edit                | ✅          |                    |
| TC-A16 | Edit bio > 280 ký tự               | Nhập bio > 280 ký tự                                   | UI cap 280 + validate, nút Save disabled                               | ✅          |                    |
| TC-A17 | Chọn skills                        | ProfileEditScreen → tap skill chip                     | Toggle selected, `updateProfile` gửi đúng danh sách                    | ✅          |                    |
| TC-A18 | Xóa tài khoản                      | Settings → Delete account → confirm                    | Dialog hiện, placeholder — chưa implement xóa thật                     | ✅          |                    |
| TC-A19 | Không lộ token trong log           | `adb logcat` trong quá trình login                     | Không thấy `idToken`/access token plaintext                            | ✅          |                    |

**PLAN-4-FINALIZE — Email/Password Auth (do Sỹ):**

| ID     | Chức năng                            | Các bước                                                          | Kết quả mong đợi                                                           | Trạng thái | Kết quả nghiệm thu |
|--------|--------------------------------------|-------------------------------------------------------------------|----------------------------------------------------------------------------|------------|--------------------|
| TC-A20 | Đăng ký email thành công             | Register → username chưa dùng, email mới, password đủ mạnh, DOB hợp lệ → Đăng ký | Tài khoản Firebase Auth tạo, Firestore user doc upsert, điều hướng Home | 🔲          |                    |
| TC-A21 | Đăng ký username đã tồn tại          | Username trùng user khác                                          | Lỗi inline "Username đã được sử dụng"                                      | 🔲          |                    |
| TC-A22 | Đăng ký username format sai          | Username chứa ký tự đặc biệt ngoài `[a-z0-9._-]`                 | Lỗi inline "Username chỉ gồm a-z, 0-9, dấu chấm, gạch dưới, gạch ngang"  | 🔲          |                    |
| TC-A23 | Đăng ký username quá ngắn/dài        | Username < 3 hoặc > 30 ký tự                                      | Bị chặn, lỗi validate                                                      | 🔲          |                    |
| TC-A24 | Đăng ký password yếu                 | Password < 8 ký tự hoặc thiếu chữ hoa hoặc thiếu số              | Lỗi inline mô tả yêu cầu password                                          | 🔲          |                    |
| TC-A25 | Đăng ký confirm password không khớp | Password ≠ Confirm Password                                       | Lỗi inline "Mật khẩu không khớp", nút Đăng ký disabled                    | 🔲          |                    |
| TC-A26 | Đăng ký tuổi < 13                    | Chọn DOB < 13 năm trước hôm nay                                   | Bị chặn "Bạn phải đủ 13 tuổi để đăng ký"                                  | 🔲          |                    |
| TC-A27 | Đăng ký DOB tương lai                | Chọn ngày sinh > hôm nay                                          | Bị chặn, lỗi validate                                                      | 🔲          |                    |
| TC-A28 | Đăng nhập email/password thành công  | Login → nhập email + password đúng → Đăng nhập                   | Vào Home, phiên được duy trì                                               | 🔲          |                    |
| TC-A29 | Đăng nhập email sai mật khẩu         | Nhập sai password                                                 | Lỗi "Email hoặc mật khẩu không đúng"                                       | 🔲          |                    |
| TC-A30 | Chuyển màn hình Login ↔ Register     | Tap "Chưa có tài khoản? Đăng ký" và "Đã có tài khoản? Đăng nhập" | Điều hướng đúng, back stack hợp lệ (Register → back → Login, không thoát app) | 🔲       |                    |

| TỔNG PLAN-1 | | | | 19/19 ✅ | UI Fluent Minimalist finalized 05/05/2026 — 0 hardcoded dp, Surface containers added |
| TỔNG PLAN-4 (email-auth) | | | | 0/11 🔲 | Code implemented (commit 225d3d4), unit tests 27/27 pass. Chờ test thủ công trên thiết bị. |
