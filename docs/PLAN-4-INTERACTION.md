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
- Cập nhật theo chu kỳ mỗi 30 giây hoặc làm mới khi ứng dụng quay lại vùng nhìn (chưa tích hợp FCM)
- Các loại:
  - `ISSUE_CREATED` — chủ ý tưởng nhận khi có công việc mới
  - `COLLAB_REQUEST` — chủ ý tưởng nhận yêu cầu tham gia
  - `COLLAB_ACCEPTED` / `COLLAB_REJECTED` — người gửi yêu cầu nhận phản hồi
  - `COMMENT` — chủ ý tưởng nhận khi có bình luận
- Nút Chấp nhận / Từ chối yêu cầu cộng tác -> cập nhật `ideas.collaboratorIds`

### Nhắn tin (DIRECT 1-1)
- Tab Messages: danh sách hội thoại sắp theo `lastMessageAt`
- Màn hình Conversation: danh sách tin nhắn và ô nhập liệu
- Tạo hội thoại từ hồ sơ người dùng (nút "Nhắn tin")
  - `FindDirectConversation(peerId)` → nếu null, `CreateDirectConversation(peerId)`
- Tự cập nhật theo chu kỳ mỗi 5 giây khi màn hình đang mở (chưa dùng thời gian thực)

## 2. Màn hình
| Route                      | Screen                                   |
|----------------------------|------------------------------------------|
| `notifications`            | `NotificationListScreen`                 |
| `messages`                 | `ConversationListScreen`                 |
| `conversation/{id}`        | `ConversationScreen`                     |
| `conversation/new?peerId=` | `ConversationNewScreen` (redirect logic) |

## 3. Ca sử dụng
- `GetNotificationsUseCase`, `MarkNotificationReadUseCase`
- `AcceptCollabUseCase`, `RejectCollabUseCase`
- `GetConversationsUseCase`
- `GetMessagesUseCase`, `SendMessageUseCase`
- `FindOrCreateDirectConversationUseCase`

## 4. Tiêu chí chấp nhận
- [ ] Huy hiệu thông báo cập nhật khi đánh dấu đã đọc
- [ ] Gửi tin nhắn -> hiển thị ngay trong danh sách (cập nhật lạc quan)
- [ ] Không tạo trùng hội thoại DIRECT giữa 2 người dùng
- [ ] Chấp nhận cộng tác -> người dùng được thêm vào `collaboratorIds`, người gửi yêu cầu nhận `COLLAB_ACCEPTED`
