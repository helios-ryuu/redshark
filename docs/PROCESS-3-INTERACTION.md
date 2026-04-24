# PROCESS-3-INTERACTION.md — Quy trình nghiệp vụ: Thông báo và Nhắn tin

WBS tham chiếu: [WBS.md](WBS.md) — nhóm công việc `5.0`.

## Trạng thái đối chiếu với mã nguồn (22/04/2026)

| Mục | Trạng thái | Ghi chú |
|-----|------------|---------|
| 0. Gửi yêu cầu cộng tác | ⚠️ Chưa khớp hoàn toàn | Có `RequestCollabUseCase`, nhưng chưa thấy điểm bấm gọi `requestCollab(...)` từ `IdeaDetailScreen`. |
| 1. Xem và đọc thông báo | ✅ Khớp | Có list + badge unread + mark read. |
| 2. Chấp nhận yêu cầu cộng tác | ✅ Khớp | Accept cập nhật collaborator, tạo/mở DIRECT conversation và gửi `COLLAB_ACCEPTED`. |
| 3. Từ chối yêu cầu cộng tác | ✅ Khớp | Reject gửi `COLLAB_REJECTED` và mark read thông báo gốc. |
| 4. Bắt đầu hội thoại từ hồ sơ | ✅ Khớp | Luồng `conversation/new?peerId=` và điều hướng vào `conversation/{id}` đã có. |
| 5. Gửi tin nhắn | ⚠️ Khớp một phần | Có optimistic `SENDING`/`FAILED`, nhưng chưa có nút retry riêng cho tin `FAILED`. |
| 6. Danh sách hội thoại | ✅ Khớp | Có tab Messages, sort theo `lastMessageAt`, và FAB tạo hội thoại mới. |
| 7. Cập nhật dữ liệu | ⚠️ Khác thiết kế cũ | Đang dùng realtime `addSnapshotListener` thay cho polling 5s/30s. |

## 0. Gửi yêu cầu cộng tác (từ Idea Detail)

| Bước | Giao diện                        | Dịch vụ                                                                 | Bảng                   |
|------|----------------------------------|-------------------------------------------------------------------------|------------------------|
| 1    | `IdeaDetailScreen` → "Xin tham gia" | `RequestCollabUseCase(ideaId, requesterId)`                             | Read `ideas`           |
| 2    | Validate                           | Chặn tự yêu cầu trên idea của chính mình hoặc khi đã là collaborator    | —                      |
| 3    | Tạo notification                   | `CreateNotification(type=COLLAB_REQUEST, recipientId=idea.authorId)`    | Insert `notifications` |

## 1. Xem và đọc thông báo

| Bước | Giao diện                                  | Dịch vụ                       | Bảng                                                                      |
|------|-------------------------------------------|-------------------------------|---------------------------------------------------------------------------|
| 1    | Tab `Notifications` (BottomNav, có badge) | `GetUnreadCount` query        | Count `notifications` where recipientId=auth.uid, isRead=false            |
| 2    | Mở tab                                    | `ListMyNotifications`         | Select `notifications` where recipientId=auth.uid ORDER BY createdAt DESC |
| 3    | Tap 1 notification                        | `MarkNotificationReadUseCase` | Update `notifications.isRead = true`                                      |
| 4    | Điều hướng tới đối tượng đích (idea/issue/comment) | —                     | —                                                                         |

## 2. Chấp nhận yêu cầu cộng tác

| Bước | Giao diện                                 | Dịch vụ                                                                      | Bảng                           |
|------|------------------------------------------|------------------------------------------------------------------------------|--------------------------------|
| 1    | Notification COLLAB_REQUEST → nút Accept | —                                                                            | —                              |
| 2    | Xác nhận                                  | `AcceptCollabUseCase`: `UpdateIdea(id, collaboratorIds += actorId)` mutation | Cập nhật `ideas.collaboratorIds` |
| 3    | Tạo/mở hội thoại DIRECT                   | `FindOrCreateDirectConversation(actorId)`                                    | Read/insert `conversations`    |
| 4    | Tạo thông báo phản hồi                    | `CreateNotification(recipientId=actorId, type=COLLAB_ACCEPTED)`              | Thêm bản ghi `notifications`   |
| 5    | Đánh dấu đã đọc thông báo gốc             | `MarkNotificationRead`                                                       | Cập nhật `notifications`       |

## 3. Từ chối yêu cầu cộng tác

| Bước | Giao diện                                                        | Dịch vụ | Bảng                   |
|------|-----------------------------------------------------------------|---------|------------------------|
| 1    | Notification → Reject                                           | —       | —                      |
| 2    | `CreateNotification(type=COLLAB_REJECTED, recipientId=actorId)` |         | Insert `notifications` |
| 3    | Đánh dấu đã đọc thông báo gốc                                   | —       | Cập nhật               |

## 4. Bắt đầu hội thoại trực tiếp (từ hồ sơ người dùng)

| Bước | Giao diện                         | Dịch vụ                                     | Bảng                                                                              |
|------|----------------------------------|---------------------------------------------|-----------------------------------------------------------------------------------|
| 1    | `ProfileViewScreen` → "Nhắn tin" | —                                           | —                                                                                 |
| 2    | `conversation/new?peerId=X`      | `FindDirectConversation(peerId)` query      | Select `conversations` where type=DIRECT AND participantIds @> [auth.uid, peerId] |
| 3a   | Nếu tìm thấy                     | Điều hướng `conversation/{id}`              | —                                                                                 |
| 3b   | Nếu không có                     | `CreateDirectConversation(peerId)` mutation | Insert `conversations(id, type=DIRECT, participantIds=[auth.uid, peerId])`        |
| 4    | Điều hướng `conversation/{id}`   | —                                           | —                                                                                 |

## 5. Gửi tin nhắn

| Bước | Giao diện                                             | Dịch vụ                                        | Bảng                                                                                                                     |
|------|------------------------------------------------------|------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| 1    | `ConversationScreen`: nhập text                      | —                                              | —                                                                                                                        |
| 2    | Nhấn Send                                             | Cập nhật lạc quan vào danh sách với trạng thái `SENDING` | —                                                                                                               |
| 3    | Gọi `SendMessage(conversationId, content)` mutation   | —                                              | Thêm bản ghi `messages(...)`; cập nhật `conversations.lastMessageAt = request.time` |
| 4    | Thành công -> đổi trạng thái tin nhắn thành `SENT`    | —                                              | —                                                                                                                        |
| 5    | Lỗi -> trạng thái `FAILED`, hiển thị nút thử lại      | —                                              | —                                                                                                                        |

## 6. Xem danh sách hội thoại

| Bước | Giao diện                                                  | Dịch vụ                          | Bảng                                                                                  |
|------|-----------------------------------------------------------|----------------------------------|---------------------------------------------------------------------------------------|
| 1    | Tab `Messages`                                            | `ListMyConversations` query      | Select `conversations` where participantIds @> [auth.uid]                            |
| 2    | Mỗi item hiện peer avatar, lastMessage preview, timestamp | Join `users` + latest `messages` | Read `users`, `messages`                                                              |
| 3    | Nút "Tạo cuộc trò chuyện" (FAB)                          | Chọn peer -> `conversation/new?peerId=`                                   | Read `users`, read/insert `conversations` |
| 4    | Nhấn vào từng mục -> điều hướng `conversation/{id}`       | —                                | —                                                                                     |

## 7. Cập nhật theo thời gian thực (Snapshot Listener)

| Bước | Giao diện                    | Dịch vụ                                             | Bảng                  |
|------|-----------------------------|-----------------------------------------------------|-----------------------|
| 1    | `ConversationScreen` mở     | Theo dõi realtime `GetMessages(convId)` qua listener | Đọc `messages`        |
| 2    | Chạy nền                    | Dừng listener khi màn hình bị hủy (`awaitClose`)    | —                     |
| 3    | `NotificationListScreen` mở | Theo dõi realtime list + unread count               | Đọc `notifications`   |

> Ghi chú: Nếu cần tối ưu pin/network ở giai đoạn sau, có thể bổ sung cơ chế polling hoặc throttling theo lifecycle.
