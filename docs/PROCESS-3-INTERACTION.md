# PROCESS-3-INTERACTION.md — Quy trình nghiệp vụ: Notifications & Messages

## 1. Xem & đọc Notification

| Bước | UI                                        | Service                       | Bảng                                                                      |
|------|-------------------------------------------|-------------------------------|---------------------------------------------------------------------------|
| 1    | Tab `Notifications` (BottomNav, có badge) | `GetUnreadCount` query        | Count `notifications` where recipientId=auth.uid, isRead=false            |
| 2    | Mở tab                                    | `ListMyNotifications`         | Select `notifications` where recipientId=auth.uid ORDER BY createdAt DESC |
| 3    | Tap 1 notification                        | `MarkNotificationReadUseCase` | Update `notifications.isRead = true`                                      |
| 4    | Navigate tới target (idea/issue/comment)  | —                             | —                                                                         |

## 2. Accept Collab Request

| Bước | UI                                       | Service                                                                      | Bảng                           |
|------|------------------------------------------|------------------------------------------------------------------------------|--------------------------------|
| 1    | Notification COLLAB_REQUEST → nút Accept | —                                                                            | —                              |
| 2    | Confirm                                  | `AcceptCollabUseCase`: `UpdateIdea(id, collaboratorIds += actorId)` mutation | Update `ideas.collaboratorIds` |
| 3    | Tạo notification phản hồi                | `CreateNotification(recipientId=actorId, type=COLLAB_ACCEPTED)`              | Insert `notifications`         |
| 4    | Mark origin notification read            | `MarkNotificationRead`                                                       | Update `notifications`         |

## 3. Reject Collab Request

| Bước | UI                                                              | Service | Bảng                   |
|------|-----------------------------------------------------------------|---------|------------------------|
| 1    | Notification → Reject                                           | —       | —                      |
| 2    | `CreateNotification(type=COLLAB_REJECTED, recipientId=actorId)` |         | Insert `notifications` |
| 3    | Mark origin read                                                | —       | Update                 |

## 4. Bắt đầu hội thoại Direct (từ Profile)

| Bước | UI                               | Service                                     | Bảng                                                                              |
|------|----------------------------------|---------------------------------------------|-----------------------------------------------------------------------------------|
| 1    | `ProfileViewScreen` → "Nhắn tin" | —                                           | —                                                                                 |
| 2    | `conversation/new?peerId=X`      | `FindDirectConversation(peerId)` query      | Select `conversations` where type=DIRECT AND participantIds @> [auth.uid, peerId] |
| 3a   | Nếu tìm thấy                     | Navigate `conversation/{id}`                | —                                                                                 |
| 3b   | Nếu không có                     | `CreateDirectConversation(peerId)` mutation | Insert `conversations(id, type=DIRECT, participantIds=[auth.uid, peerId])`        |
| 4    | Navigate `conversation/{id}`     | —                                           | —                                                                                 |

## 5. Gửi Message

| Bước | UI                                                   | Service                                        | Bảng                                                                                                                     |
|------|------------------------------------------------------|------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| 1    | `ConversationScreen`: nhập text                      | —                                              | —                                                                                                                        |
| 2    | Tap Send                                             | Optimistic append vào list với state `SENDING` | —                                                                                                                        |
| 3    | Call `SendMessage(conversationId, content)` mutation | —                                              | Insert `messages(id, conversationId, senderId, content, createdAt)`; Update `conversations.lastMessageAt = request.time` |
| 4    | Thành công → đổi state message → `SENT`              | —                                              | —                                                                                                                        |
| 5    | Lỗi → state `FAILED`, nút retry                      | —                                              | —                                                                                                                        |

## 6. Xem danh sách hội thoại

| Bước | UI                                                        | Service                          | Bảng                                                                                  |
|------|-----------------------------------------------------------|----------------------------------|---------------------------------------------------------------------------------------|
| 1    | Tab `Messages`                                            | `ListMyConversations` query      | Select `conversations` where participantIds @> [auth.uid] ORDER BY lastMessageAt DESC |
| 2    | Mỗi item hiện peer avatar, lastMessage preview, timestamp | Join `users` + latest `messages` | Read `users`, `messages`                                                              |
| 3    | Tap item → Navigate `conversation/{id}`                   | —                                | —                                                                                     |

## 7. Polling (tạm thay realtime)

| Bước | UI                          | Service                                    | Bảng                 |
|------|-----------------------------|--------------------------------------------|----------------------|
| 1    | `ConversationScreen` mở     | Start polling `GetMessages(convId)` mỗi 5s | Read `messages`      |
| 2    | Background                  | Dừng polling khi onStop                    | —                    |
| 3    | `NotificationListScreen` mở | Poll mỗi 30s                               | Read `notifications` |
