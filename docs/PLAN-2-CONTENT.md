# PLAN-2-CONTENT.md — Thực thi: Ý tưởng, Công việc, Bình luận

**Phụ trách:** Hải
**Tiêu chuẩn nghiệm thu:** [CHECK-2-CONTENT.md](CHECK-2-CONTENT.md) — TC-C01..TC-C24 (24 TC)
**Prerequisite:** PLAN-1-AUTH task 6 commit xong (để tránh conflict strings.xml).

---

## Tasks

```
1. Review diff IdeaCard.kt + HomeScreen.kt:
   - IdeaCard footer: icons ThumbUp+count, ThumbDown (no count), ChatBubble+count, Share
   - HomeScreen: 5 tab đúng thứ tự Home → My Ideas → Notifications → Messages → Profile
   - Scroll-aware TopAppBar animation (NestedScrollConnection) hoạt động
   Files: app/src/main/java/com/helios/redshark/ui/common/IdeaCard.kt
          app/src/main/java/com/helios/redshark/ui/home/HomeScreen.kt

2. Review diff IdeaDetailScreen.kt:
   - Nút Edit ẩn khi không phải owner
   - Nút "Xin tham gia" hiện đúng điều kiện (không phải author, không phải collaborator)
   - Comments section render đúng (optimistic update hoạt động)
   File: app/src/main/java/com/helios/redshark/ui/ideadetail/IdeaDetailScreen.kt

3. Rà soát các screen còn lại trong scope:
   - MyIdeasScreen.kt: dùng shared IdeaCard (không có card riêng cũ)
   - CreateIdeaScreen.kt / EditIdeaScreen.kt: validate title 3..120, form giữ state khi offline
   - CreateIssueScreen.kt / EditIssueScreen.kt: validate, giới hạn 20 active issues
   Files: app/src/main/java/com/helios/redshark/ui/myideas/MyIdeasScreen.kt
          app/src/main/java/com/helios/redshark/ui/createidea/CreateIdeaScreen.kt
          app/src/main/java/com/helios/redshark/ui/editidea/EditIdeaScreen.kt
          app/src/main/java/com/helios/redshark/ui/createissue/CreateIssueScreen.kt
          app/src/main/java/com/helios/redshark/ui/editissue/EditIssueScreen.kt

4. Rà soát IssueDetailScreen.kt:
   - State machine đúng: OPEN → IN_PROGRESS → CLOSED
   - Chiều ngược CLOSED → OPEN bị chặn ở UI (không cho chọn)
   File: app/src/main/java/com/helios/redshark/ui/issuedetail/IssueDetailScreen.kt

5. ./gradlew assembleDebug — xác nhận BUILD SUCCESSFUL

6. ./gradlew testDebugUnitTest — xác nhận không regression trong
   domain/usecase/idea/*, domain/usecase/issue/*, domain/usecase/comment/*

7. Stage + commit các file pending UI trên branch:
   - IdeaCard.kt, HomeScreen.kt, IdeaDetailScreen.kt
   - strings.xml (chỉ phần thay đổi idea_*, issue_*, action_*)
   Commit: feat(content): finalize Fluent UI for idea, issue, comment screens

8. Manual test pass toàn bộ TC-C01..TC-C24 trên emulator/device
   Ưu tiên test offline trước: TC-C21, TC-C22, TC-C23
   Xem chi tiết từng TC tại: docs/CHECK-2-CONTENT.md

9. Sửa mọi TC fail → re-run bước 5–6 → commit fix riêng cho từng bug

10. [TASK CUỐI] Cập nhật đồng bộ tài liệu:
    - CHECK-2-CONTENT.md: điền cột "Kết quả nghiệm thu" cho TC-C01..TC-C24
    - REPORT.md: điền kết quả kiểm thử PLAN-2 (số TC pass/fail, %)
    - WBS.md: đánh dấu PLAN-2-CONTENT complete
```

---

## Critical files

| File | Loại thay đổi |
|---|---|
| `ui/common/IdeaCard.kt` | Review + commit pending |
| `ui/home/HomeScreen.kt` | Review + commit pending |
| `ui/ideadetail/IdeaDetailScreen.kt` | Review + commit pending |
| `ui/myideas/MyIdeasScreen.kt` | Review |
| `ui/createidea/CreateIdeaScreen.kt` | Review |
| `ui/issuedetail/IssueDetailScreen.kt` | Review |
| `res/values/strings.xml` | Commit pending (phần content) |
