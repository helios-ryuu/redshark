# PLAN-4-INTERACTION.md — Giai đoạn 4: Thông báo và Nhắn tin

**Thời gian:** 05/05/2026 – 11/05/2026 (giai đoạn tương tác)
**Mục tiêu:** Hoàn thiện nhóm tính năng tương tác giữa người dùng.
**WBS tham chiếu:** [WBS.md](WBS.md) — nhóm công việc `5.0`.
**Trạng thái:** ✅ Code hoàn tất 25/04/2026 — implement sớm trước lịch. Chờ kiểm thử thủ công theo CHECK-3-INTERACTION.md.

**Phân công theo WBS:**
- Phụ trách nhóm tương tác: **Nam** (lập trình chính, chịu trách nhiệm commit chính).
- Thành viên phối hợp: **Hải**, **Sỹ**.

> **Tiền điều kiện:** Giai đoạn 3 (Nội dung) đã hoàn thành — notification `ISSUE_CREATED` và `COMMENT` đã được phát ra từ `CreateIssueUseCase` / `CreateCommentUseCase`.

### Hạ tầng đã sẵn (từ giai đoạn 3)
- `domain/model/Notification.kt`, `domain/repository/NotificationRepository.kt`
- `data/remote/firestore/dto/NotificationDto.kt`, `data/mapper/NotificationMapper.kt`
- `data/repository/NotificationRepositoryImpl.kt` (đã wired vào Hilt)
- Firestore index `notifications(recipientId ASC, createdAt DESC)` + rule recipient-only đã có

### Hoàn thành (WBS 5.0) — 25/04/2026

**Domain:**
- [x] `domain/model/Conversation.kt` — enum `ConversationType.DIRECT`, data class `Conversation`
- [x] `domain/model/Message.kt` — data class `Message`, `SendMessageInput`
- [x] `domain/repository/MessageRepository.kt` — `getConversations`, `getMessages`, `sendMessage`, `findDirectConversation`, `createDirectConversation`
- [x] `IdeaRepository.addCollaborator(ideaId, userId)` — dùng cho AcceptCollab
- [x] `domain/usecase/notification/GetNotificationsUseCase.kt`
- [x] `domain/usecase/notification/MarkNotificationReadUseCase.kt`
- [x] `domain/usecase/notification/GetUnreadCountUseCase.kt`
- [x] `domain/usecase/notification/RequestCollabUseCase.kt`
- [x] `domain/usecase/notification/AcceptCollabUseCase.kt`
- [x] `domain/usecase/notification/RejectCollabUseCase.kt`
- [x] `domain/usecase/message/GetConversationsUseCase.kt`
- [x] `domain/usecase/message/GetMessagesUseCase.kt`
- [x] `domain/usecase/message/SendMessageUseCase.kt`
- [x] `domain/usecase/message/FindOrCreateDirectConversationUseCase.kt`

**Data:**
- [x] `data/remote/firestore/dto/ConversationDto.kt`
- [x] `data/remote/firestore/dto/MessageDto.kt`
- [x] `data/mapper/ConversationMapper.kt`
- [x] `data/mapper/MessageMapper.kt`
- [x] `data/repository/MessageRepositoryImpl.kt` — Firestore real-time listeners
- [x] `data/repository/IdeaRepositoryImpl.addCollaborator()` — `FieldValue.arrayUnion`
- [x] `core/di/RepositoryModule` — bind `MessageRepository → MessageRepositoryImpl`

**UI:**
- [x] `ui/notification/NotificationViewModel.kt`
- [x] `ui/notification/NotificationListScreen.kt` — hiện tab, badge, Accept/Reject cho COLLAB_REQUEST
- [x] `ui/message/MessageViewModel.kt`
- [x] `ui/message/ConversationListScreen.kt` — danh sách hội thoại sort theo `lastMessageAt`
- [x] `ui/message/ConversationScreen.kt` — gửi tin optimistic, real-time listener, scroll tự động
- [x] `ui/feature/home/HomeScreen.kt` — mở rộng thành 4 tab: Feed / Ideas / Notifications (badge) / Messages
- [x] `ui/ideadetail/IdeaDetailViewModel.kt` — thêm `requestCollab()`, `CollabRequestState`
- [x] `ui/ideadetail/IdeaDetailScreen.kt` — nút "Xin tham gia" (FR-IDEA-07 SHOULD)
- [x] `ui/navigation/Routes.kt` — thêm NOTIFICATIONS, MESSAGES, CONVERSATION, CONVERSATION_NEW
- [x] `ui/navigation/NavGraph.kt` — wired tất cả route mới

## 1. Tính năng
### Thông báo
- Tab Notifications + huy hiệu số chưa đọc trên BottomNav
- Real-time qua Firestore snapshot listener (thay cho polling FCM)
- Các loại:
  - `ISSUE_CREATED` — chủ ý tưởng nhận khi có công việc mới
  - `COLLAB_REQUEST` — chủ ý tưởng nhận yêu cầu tham gia
  - `COLLAB_ACCEPTED` / `COLLAB_REJECTED` — người gửi yêu cầu nhận phản hồi
  - `COMMENT` — chủ ý tưởng nhận khi có bình luận
- Nút Chấp nhận / Từ chối yêu cầu cộng tác → cập nhật `ideas.collaboratorIds`

### Nhắn tin (DIRECT 1-1)
- Tab Messages: danh sách hội thoại sắp theo `lastMessageAt`
- Tab Messages có nút tạo hội thoại mới (FAB)
- Màn hình Conversation: danh sách tin nhắn và ô nhập liệu
- Tạo hội thoại từ route `conversation/new?peerId=` (gọi `FindOrCreateDirectConversationUseCase`)
- Real-time qua Firestore snapshot listener

## 2. Màn hình
| Route                      | Screen                                   |
|----------------------------|------------------------------------------|
| `notifications` (tab)      | `NotificationListScreen` (embedded tab)  |
| `messages` (tab)           | `ConversationListScreen` (embedded tab)  |
| `conversation/{id}`        | `ConversationScreen`                     |
| `conversation/new?peerId=` | Redirect → `ConversationScreen`          |

## 3. Ca sử dụng
- `GetNotificationsUseCase`, `MarkNotificationReadUseCase`, `GetUnreadCountUseCase`
- `AcceptCollabUseCase`, `RejectCollabUseCase`, `RequestCollabUseCase`
- `GetConversationsUseCase`
- `GetMessagesUseCase`, `SendMessageUseCase`
- `FindOrCreateDirectConversationUseCase`

## 4. Tiêu chí chấp nhận
- [x] Huy hiệu thông báo cập nhật khi đánh dấu đã đọc
- [x] Gửi tin nhắn → hiển thị ngay trong danh sách (cập nhật lạc quan)
- [x] Không tạo trùng hội thoại DIRECT giữa 2 người dùng
- [x] Chấp nhận cộng tác → người dùng được thêm vào `collaboratorIds`, người gửi yêu cầu nhận `COLLAB_ACCEPTED`
