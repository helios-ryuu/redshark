# PLAN-4-INTERACTION.md — Phase 4: Notifications & Messages

**Thời gian:** 01/05/2026 – 10/05/2026 (Tuần 5)
**Mục tiêu:** Hoàn thiện nhóm tính năng tương tác giữa user.

## 1. Tính năng
### Notifications
- Tab Notifications + badge số chưa đọc trên BottomNav
- Poll mỗi 30s hoặc refresh-on-focus (chưa tích hợp FCM)
- Các loại:
  - `ISSUE_CREATED` — idea owner nhận khi có issue mới
  - `COLLAB_REQUEST` — idea owner nhận yêu cầu tham gia
  - `COLLAB_ACCEPTED` / `COLLAB_REJECTED` — requester nhận phản hồi
  - `COMMENT` — idea owner nhận khi có comment
- Nút Accept / Reject collab request → cập nhật `ideas.collaboratorIds`

### Messages (DIRECT 1-1)
- Tab Messages: danh sách hội thoại sắp theo `lastMessageAt`
- Màn hình Conversation: list message, input gửi tin
- Tạo conversation từ Profile (nút "Nhắn tin")
  - `FindDirectConversation(peerId)` → nếu null, `CreateDirectConversation(peerId)`
- Auto poll mỗi 5s khi màn hình mở (chưa dùng realtime)

## 2. Màn hình
| Route                      | Screen                                   |
|----------------------------|------------------------------------------|
| `notifications`            | `NotificationListScreen`                 |
| `messages`                 | `ConversationListScreen`                 |
| `conversation/{id}`        | `ConversationScreen`                     |
| `conversation/new?peerId=` | `ConversationNewScreen` (redirect logic) |

## 3. UseCases
- `GetNotificationsUseCase`, `MarkNotificationReadUseCase`
- `AcceptCollabUseCase`, `RejectCollabUseCase`
- `GetConversationsUseCase`
- `GetMessagesUseCase`, `SendMessageUseCase`
- `FindOrCreateDirectConversationUseCase`

## 4. Acceptance Criteria
- [ ] Badge notification cập nhật khi mark read
- [ ] Gửi message → hiện ngay trong list (optimistic update)
- [ ] Không tạo trùng conversation DIRECT giữa 2 user
- [ ] Accept collab → user được thêm vào `collaboratorIds`, requester nhận `COLLAB_ACCEPTED`
