# PROCESS-3-INTERACTION.md — Quy trình nghiệp vụ: Thông báo và Nhắn tin

WBS tham chiếu: [WBS.md](WBS.md) — nhóm công việc `5.0`.

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
| 3    | Tạo thông báo phản hồi                    | `CreateNotification(recipientId=actorId, type=COLLAB_ACCEPTED)`              | Thêm bản ghi `notifications`   |
| 4    | Đánh dấu đã đọc thông báo gốc             | `MarkNotificationRead`                                                       | Cập nhật `notifications`       |

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
| 1    | Tab `Messages`                                            | `ListMyConversations` query      | Select `conversations` where participantIds @> [auth.uid] ORDER BY lastMessageAt DESC |
| 2    | Mỗi item hiện peer avatar, lastMessage preview, timestamp | Join `users` + latest `messages` | Read `users`, `messages`                                                              |
| 3    | Nhấn vào từng mục -> điều hướng `conversation/{id}`       | —                                | —                                                                                     |

## 7. Cập nhật theo chu kỳ (tạm thay thời gian thực)

| Bước | Giao diện                    | Dịch vụ                                    | Bảng                 |
|------|-----------------------------|--------------------------------------------|----------------------|
| 1    | `ConversationScreen` mở     | Bắt đầu cập nhật theo chu kỳ `GetMessages(convId)` mỗi 5 giây | Đọc `messages`      |
| 2    | Chạy nền                    | Dừng cập nhật theo chu kỳ khi onStop                           | —                    |
| 3    | `NotificationListScreen` mở | Cập nhật theo chu kỳ mỗi 30 giây           | Đọc `notifications`  |
