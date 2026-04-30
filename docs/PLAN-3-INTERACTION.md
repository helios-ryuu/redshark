# PLAN-3-INTERACTION.md — Thực thi: Thông báo & Nhắn tin

**Phụ trách:** Nam
**Tiêu chuẩn nghiệm thu:** [CHECK-3-INTERACTION.md](CHECK-3-INTERACTION.md) — TC-N01..N08 + TC-M01..M10 (18 TC)
**HARD DEADLINE: Toàn bộ tasks hoàn thành trước 18:00 ngày 03/05/2026.**

---

## Tasks

```
1. Review diff ConversationListScreen.kt — xác nhận tile layout:
   - AvatarImage bên trái (AvatarMd, hình tròn)
   - Preview text bold nếu hasUnread = true và tin từ peer
   - Prefix "Bạn: " nếu lastMessageSenderId == currentUserId
   - HorizontalDivider phân tách tile (không dùng Card với elevation)
   File: app/src/main/java/com/helios/redshark/ui/message/ConversationListScreen.kt

2. Review diff ConversationScreen.kt — xác nhận:
   - Bubble sent: màu primary mạnh, shape bottomEnd không round
   - Bubble received: màu surface, shape bottomStart không round
   - Input bar phẳng (flat), nút Send disabled khi text rỗng (TC-M05)
   - Optimistic update: message hiện ngay trước khi Firestore confirm (TC-M04)
   File: app/src/main/java/com/helios/redshark/ui/message/ConversationScreen.kt

3. Rà soát MessageViewModel.kt:
   - Validate message ≤ 2000 ký tự tại SendMessageUseCase (không chỉ UI) — TC-M06
   - FindOrCreateDirectConversationUseCase không tạo conversation trùng — TC-M03
   File: app/src/main/java/com/helios/redshark/ui/message/MessageViewModel.kt

4. Rà soát NotificationListScreen.kt + NotificationViewModel.kt:
   - Visual diff read/unread (TC-N01..TC-N03): ví dụ chữ bold hoặc background khác
   - Badge số chưa đọc đồng bộ BottomNav (TC-N02)
   - Empty state đúng chuỗi (TC-N08) — dùng stringResource không hardcode
   - Accept/Reject collab cập nhật collaboratorIds đúng (TC-N05, TC-N06)
   Files: app/src/main/java/com/helios/redshark/ui/notification/NotificationListScreen.kt
          app/src/main/java/com/helios/redshark/ui/notification/NotificationViewModel.kt

5. Stage + commit các file pending UI trên branch:
   - ConversationListScreen.kt, ConversationScreen.kt
   - libs.versions.toml (nếu có thay đổi liên quan interaction)
   Commit: feat(interaction): finalize Fluent UI for conversation and notification screens

6. ./gradlew assembleDebug — xác nhận BUILD SUCCESSFUL

7. ./gradlew testDebugUnitTest — xác nhận không regression trong
   domain/usecase/message/* và domain/usecase/notification/*

8. Manual test TC-N01..TC-N08 (Notification — 8 TCs):
   - Cần 2 tài khoản đồng thời cho TC-N04..N06 (collab request/accept/reject)
   - Setup: User A và User B trên 2 emulator hoặc emulator + device thật
   Xem chi tiết tại: docs/CHECK-3-INTERACTION.md

9. Manual test TC-M01..TC-M10 (Message — 10 TCs):
   - TC-M07, TC-M08: cần 2 tài khoản + test airplane mode
   - TC-M09: cần conversation có ≥ 30 messages để verify scroll
   Xem chi tiết tại: docs/CHECK-3-INTERACTION.md

10. Sửa mọi TC fail → re-run bước 6–7 → commit fix
    Ghi rõ TC ID trong commit message: fix(interaction): resolve TC-M08 offline sync

11. [TASK CUỐI] Cập nhật đồng bộ tài liệu:
    - CHECK-3-INTERACTION.md: cập nhật trạng thái TC-N01..N08 + TC-M01..M10
      (🔲 → ✅ nếu pass; ghi "FAIL: <mô tả ngắn>" nếu chưa fix kịp deadline)
    - REPORT.md: điền kết quả kiểm thử PLAN-3 (số TC pass/fail, %)
    - WBS.md: đánh dấu PLAN-3-INTERACTION complete
```

---

## Critical files

| File | Loại thay đổi |
|---|---|
| `ui/message/ConversationListScreen.kt` | Review + commit pending |
| `ui/message/ConversationScreen.kt` | Review + commit pending |
| `ui/message/MessageViewModel.kt` | Review |
| `ui/notification/NotificationListScreen.kt` | Review |
| `ui/notification/NotificationViewModel.kt` | Review |
| `gradle/libs.versions.toml` | Commit pending nếu có thay đổi |

---

## Ghi chú deadline

- Tasks 1–7 (review, commit, build): hoàn thành trước 12:00 ngày 03/05.
- Tasks 8–10 (manual test): hoàn thành trước 17:00 ngày 03/05.
- Task 11 (doc sync): hoàn thành trước 18:00 ngày 03/05.
