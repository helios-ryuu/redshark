# CHECK-3-INTERACTION.md — Tiêu chuẩn nghiệm thu: Thông báo và Nhắn tin

WBS tham chiếu: [WBS.md](WBS.md) — kiểm thử cho nhóm công việc `5.0`.

> **Tiêu chuẩn nghiệm thu bắt buộc cho PLAN-3-INTERACTION (TC-N/M) và PLAN-4-FINALIZE (TC-X).**
> - PLAN-3 hoàn thành khi TC-N01..N08 + TC-M01..M10 đều ✅.
> - PLAN-4 hoàn thành khi TC-X01..X07 đều ✅.

> **Ký hiệu trạng thái:**
> - ✅ Code implemented + test thủ công đã xác nhận
> - 🔲 Code implemented, chờ test thủ công trên thiết bị/emulator
> - ⬜ Chưa implement hoặc chưa có code

**PLAN-3-INTERACTION — Notification (do Nam):**

| ID     | Chức năng                       | Các bước                                    | Kết quả mong đợi                                                              | Trạng thái | Kết quả nghiệm thu |
|--------|---------------------------------|---------------------------------------------|-------------------------------------------------------------------------------|------------|--------------------|
| TC-N01 | Nhận notification ISSUE_CREATED | User A tạo issue trên idea của B → B mở app | B thấy notification mới, badge +1                                             | ✅          | Pass               |
| TC-N02 | Badge số chưa đọc               | Có 3 notification chưa đọc                  | BottomNav hiển thị "3"                                                        | ✅          | Pass               |
| TC-N03 | Mark as read                    | Tap vào notification                        | `isRead = true`, badge giảm 1                                                 | ✅          | Pass               |
| TC-N04 | Gửi Collab Request              | User A vào idea B → "Xin tham gia"          | B nhận notification COLLAB_REQUEST                                            | ✅          | Pass               |
| TC-N05 | Accept Collab                   | B → notification → Accept                   | A được thêm vào `collaboratorIds`, A nhận COLLAB_ACCEPTED                     | ✅          | Pass               |
| TC-N06 | Reject Collab                   | B → Reject                                  | A nhận COLLAB_REJECTED, A không vào collaboratorIds                           | ✅          | Pass               |
| TC-N07 | Notification refresh            | Snapshot listener tự cập nhật              | List cập nhật real-time khi có notification mới                               | ✅          | Pass               |
| TC-N08 | Notification empty              | Tài khoản mới                               | Hiển thị empty state "Không có thông báo nào."                                | ✅          | Pass               |

**PLAN-3-INTERACTION — Message (do Nam):**

| ID     | Chức năng                       | Các bước                                    | Kết quả mong đợi                                                              | Trạng thái | Kết quả nghiệm thu |
|--------|---------------------------------|---------------------------------------------|-------------------------------------------------------------------------------|------------|--------------------|
| TC-M01 | Mở tab Messages                 | Tab Messages                                | Hiện danh sách conversation sort theo lastMessageAt DESC                      | ✅          | Pass               |
| TC-M02 | Tạo direct conversation         | `conversation/new?peerId=` → redirect       | Nếu chưa có → tạo mới và vào conversation; nếu có → vào conversation hiện tại | ✅          | Pass               |
| TC-M03 | Không tạo hội thoại trùng lặp   | Lặp lại TC-M02                              | Vẫn dùng hội thoại cũ, không tạo mới                                          | ✅          | Pass               |
| TC-M04 | Gửi message hợp lệ              | Nhập text → Send                            | Hiện ngay trong list (optimistic), `SendMessage` OK                           | ✅          | Pass               |
| TC-M05 | Gửi message rỗng                | Text trống                                  | Nút Send disabled                                                             | ✅          | Pass               |
| TC-M06 | Gửi message > 2000 ký tự        | 2001 ký tự                                  | `SendMessageUseCase` ném ValidationException, hiển thị lỗi                   | ✅          | Pass               |
| TC-M07 | Nhận message realtime           | User A gửi → B mở conversation              | Message xuất hiện ở B ngay (Firestore snapshot listener)                      | ✅          | Pass               |
| TC-M08 | Mất mạng khi gửi                | Airplane → Send                             | Optimistic item hiện, khi có mạng lại → listener đồng bộ                     | ✅          | Pass               |
| TC-M09 | Scroll load lịch sử             | Conversation dài → scroll lên               | Tất cả message cũ load từ Firestore (hiện chưa phân trang)                   | ✅          | Pass               |
| TC-M10 | Back navigation                 | Trong conversation → Back                   | Về list Messages, lastMessagePreview cập nhật                                 | ✅          | Pass               |

| **TỔNG PLAN-3** | | | | **18/18 ✅** | UI Fluent Minimalist finalized 05/05/2026 — NotificationItem/ConversationItem Surface+BorderStroke pattern applied |

**PLAN-4-FINALIZE — Cross-cutting NFR (do Sỹ):**

> Chạy cùng đợt kiểm thử hồi quy 12-14/05 (WBS 6.0).

| ID     | Chức năng                   | Các bước                                    | Kết quả mong đợi                                                  | Trạng thái | Kết quả nghiệm thu |
|--------|-----------------------------|---------------------------------------------|-------------------------------------------------------------------|------------|--------------------|
| TC-X01 | Dark mode                   | Đổi theme hệ thống → Dark                   | UI tự động đổi, không có text trắng trên nền trắng; kiểm tra cả Login/Register | 🔲 |               |
| TC-X02 | Rotate màn hình             | Xoay thiết bị                               | State giữ nguyên, không reload (`rememberSaveable` trên RegisterScreen) | 🔲          |                    |
| TC-X03 | Back button system          | Android Back trong sub-screen               | Pop đúng stack; Register → Back → Login (không thoát app)         | 🔲          |                    |
| TC-X04 | Deep link idea              | Mở `redshark://idea/{id}`                   | Navigate vào IdeaDetail                                           | 🔲          |                    |
| TC-X05 | R2 download/hiển thị avatar | Avatar có URL R2                            | Coil load thành công, cache lần 2                                 | 🔲          |                    |
| TC-X06 | Thời gian khởi động nguội   | Tắt hẳn ứng dụng → mở lại                   | < 3 giây đến màn hình Home (người dùng đã đăng nhập)             | 🔲          |                    |
| TC-X07 | Memory leak                 | Vào ra 10 màn hình                          | Leak Canary: 0 leak                                               | 🔲          |                    |

| **TỔNG PLAN-4 (NFR)** | | | | **0/7 🔲** | Infrastructure ready (commit 225d3d4): LeakCanary added, R8 enabled, deep link intent-filter in manifest. Chờ test thủ công. |
