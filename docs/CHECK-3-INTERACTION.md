# CHECK-3-INTERACTION.md — Kiểm thử thủ công: Thông báo và Nhắn tin

WBS tham chiếu: [WBS.md](WBS.md) — kiểm thử cho nhóm công việc `5.0`.

> **Lưu ý phạm vi:** Các test TC-X01..TC-X07 ở cuối tài liệu thuộc kiểm thử cross-cutting (NFR/giai đoạn 5 dọn dẹp), không phải phần Tương tác. Giữ ở đây để tránh tách thêm tệp; chạy cùng đợt kiểm thử hồi quy 12-14/05 (TIMELINE Tuần 9).

> **Ký hiệu trạng thái:**
> - ✅ Code implemented + test thủ công đã xác nhận
> - 🔲 Code implemented, chờ test thủ công trên thiết bị/emulator
> - ⬜ Chưa implement hoặc chưa có code

| ID     | Chức năng                       | Các bước                                    | Kết quả mong đợi                                                              | Trạng thái |
|--------|---------------------------------|---------------------------------------------|-------------------------------------------------------------------------------|------------|
| TC-N01 | Nhận notification ISSUE_CREATED | User A tạo issue trên idea của B → B mở app | B thấy notification mới, badge +1                                             | 🔲          |
| TC-N02 | Badge số chưa đọc               | Có 3 notification chưa đọc                  | BottomNav hiển thị "3"                                                        | 🔲          |
| TC-N03 | Mark as read                    | Tap vào notification                        | `isRead = true`, badge giảm 1                                                 | 🔲          |
| TC-N04 | Gửi Collab Request              | User A vào idea B → "Xin tham gia"          | B nhận notification COLLAB_REQUEST                                            | 🔲          |
| TC-N05 | Accept Collab                   | B → notification → Accept                   | A được thêm vào `collaboratorIds`, A nhận COLLAB_ACCEPTED                     | 🔲          |
| TC-N06 | Reject Collab                   | B → Reject                                  | A nhận COLLAB_REJECTED, A không vào collaboratorIds                           | 🔲          |
| TC-N07 | Notification refresh            | Snapshot listener tự cập nhật              | List cập nhật real-time khi có notification mới                               | 🔲          |
| TC-N08 | Notification empty              | Tài khoản mới                               | Hiển thị empty state "Không có thông báo nào."                                | 🔲          |
| TC-M01 | Mở tab Messages                 | Tab Messages                                | Hiện danh sách conversation sort theo lastMessageAt DESC                      | 🔲          |
| TC-M02 | Tạo direct conversation         | `conversation/new?peerId=` → redirect       | Nếu chưa có → tạo mới và vào conversation; nếu có → vào conversation hiện tại | 🔲          |
| TC-M03 | Không tạo hội thoại trùng lặp   | Lặp lại TC-M02                              | Vẫn dùng hội thoại cũ, không tạo mới                                          | 🔲          |
| TC-M04 | Gửi message hợp lệ              | Nhập text → Send                            | Hiện ngay trong list (optimistic), `SendMessage` OK                           | 🔲          |
| TC-M05 | Gửi message rỗng                | Text trống                                  | Nút Send disabled                                                             | 🔲          |
| TC-M06 | Gửi message > 2000 ký tự        | 2001 ký tự                                  | `SendMessageUseCase` ném ValidationException, hiển thị lỗi                   | 🔲          |
| TC-M07 | Nhận message realtime           | User A gửi → B mở conversation              | Message xuất hiện ở B ngay (Firestore snapshot listener)                      | 🔲          |
| TC-M08 | Mất mạng khi gửi                | Airplane → Send                             | Optimistic item hiện, khi có mạng lại → listener đồng bộ                     | 🔲          |
| TC-M09 | Scroll load lịch sử             | Conversation dài → scroll lên               | Tất cả message cũ load từ Firestore (hiện chưa phân trang)                   | 🔲          |
| TC-M10 | Back navigation                 | Trong conversation → Back                   | Về list Messages, lastMessagePreview cập nhật                                 | 🔲          |
| TC-X01 | Dark mode                       | Đổi theme hệ thống → Dark                   | UI tự động đổi, không có text trắng trên nền trắng                            | ⬜          |
| TC-X02 | Rotate màn hình                 | Xoay thiết bị                               | State giữ nguyên, không reload                                                | ⬜          |
| TC-X03 | Back button system              | Android Back trong sub-screen               | Pop đúng stack                                                                | ⬜          |
| TC-X04 | Deep link idea                  | Mở `redshark://idea/{id}`                   | Navigate vào IdeaDetail                                                       | ⬜          |
| TC-X05 | R2 download/hiển thị avatar     | Avatar có URL R2                            | Coil load thành công, cache lần 2                                             | ⬜          |
| TC-X06 | Thời gian khởi động nguội       | Tắt hẳn ứng dụng → mở lại                   | < 3 giây đến màn hình Home (người dùng đã đăng nhập)                          | ⬜          |
| TC-X07 | Memory leak                     | Vào ra 10 màn hình                          | Leak Canary: 0 leak                                                           | ⬜          |
