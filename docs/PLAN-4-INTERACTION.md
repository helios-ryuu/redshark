# PLAN-4-INTERACTION.md — Giai đoạn 4: Thông báo và Nhắn tin

**Thời gian:** 05/05/2026 – 11/05/2026 (giai đoạn tương tác)
**Mục tiêu:** Hoàn thiện nhóm tính năng tương tác giữa người dùng.
**WBS tham chiếu:** [WBS.md](WBS.md) — nhóm công việc `5.0`.

**Phân công theo WBS:**
- Phụ trách nhóm tương tác: **Nam** (lập trình chính, chịu trách nhiệm commit chính).
- Thành viên phối hợp: **Hải**, **Sỹ**.

> **Tiền điều kiện:** Giai đoạn 3 (Nội dung) đã hoàn thành — notification `ISSUE_CREATED` và `COLLAB_REQUEST` cần Firestore collection schema tương ứng từ giai đoạn 3.

## 1. Tính năng
### Thông báo
- Tab Notifications + huy hiệu số chưa đọc trên BottomNav
- Cập nhật theo thời gian thực bằng Firestore snapshot listener (chưa tích hợp FCM)
- Các loại:
  - `ISSUE_CREATED` — chủ ý tưởng nhận khi có công việc mới
  - `COLLAB_REQUEST` — chủ ý tưởng nhận yêu cầu tham gia
  - `COLLAB_ACCEPTED` / `COLLAB_REJECTED` — người gửi yêu cầu nhận phản hồi
  - `COMMENT` — chủ ý tưởng nhận khi có bình luận
- Nút Chấp nhận / Từ chối yêu cầu cộng tác -> cập nhật `ideas.collaboratorIds`
- Chấp nhận `COLLAB_REQUEST` sẽ `FindOrCreateDirectConversation(actorId)` và điều hướng vào chat DIRECT

### Nhắn tin (DIRECT 1-1)
- Tab Messages: danh sách hội thoại sắp theo `lastMessageAt`
- Tab Messages có nút tạo hội thoại mới (FAB)
- Màn hình Conversation: danh sách tin nhắn và ô nhập liệu
- Tạo hội thoại từ hồ sơ người dùng (nút "Nhắn tin")
  - `FindDirectConversation(peerId)` → nếu null, `CreateDirectConversation(peerId)`
- Tự cập nhật theo thời gian thực khi màn hình đang mở (Firestore listener)

## 2. Màn hình
| Route                      | Screen                                   |
|----------------------------|------------------------------------------|
| `notifications`            | `NotificationListScreen`                 |
| `messages`                 | `ConversationListScreen`                 |
| `conversation/{id}`        | `ConversationScreen`                     |
| `conversation/new?peerId=` | `ConversationNewScreen` (redirect logic) |

## 3. Ca sử dụng
- `GetNotificationsUseCase`, `MarkNotificationReadUseCase`
- `RequestCollabUseCase`, `AcceptCollabUseCase`, `RejectCollabUseCase`
- `GetConversationsUseCase`
- `GetMessagesUseCase`, `SendMessageUseCase`
- `FindOrCreateDirectConversationUseCase`

## 4. Tiêu chí chấp nhận
- [x] Huy hiệu thông báo cập nhật khi đánh dấu đã đọc
- [x] Gửi tin nhắn -> hiển thị ngay trong danh sách (cập nhật lạc quan)
- [x] Không tạo trùng hội thoại DIRECT giữa 2 người dùng
- [x] Chấp nhận cộng tác -> người dùng được thêm vào `collaboratorIds`, người gửi yêu cầu nhận `COLLAB_ACCEPTED`
- [x] Chấp nhận cộng tác -> tạo/mở hội thoại DIRECT giữa owner và requester
- [ ] Request collab từ `IdeaDetailScreen` gọi trực tiếp `requestCollab(...)` từ UI

## 5. Cập nhật tiến độ (22/04/2026)
- Đã hoàn thành phần lõi của `5.1` và `5.2`: notifications, unread badge, accept/reject collab, direct conversations, gửi tin nhắn optimistic, chống trùng hội thoại.
- Đã cập nhật route điều hướng và title conversation để quay lại đúng tab Messages.
- Điểm cần chốt thêm trước khi đóng hoàn toàn giai đoạn 4: expose nút/entrypoint request collab ở `IdeaDetailScreen` để khớp đầy đủ quy trình mục 0 trong `PROCESS-3-INTERACTION.md`.
