# PROCESS-2-CONTENT.md — Quy trình nghiệp vụ: Ý tưởng, Công việc, Bình luận

WBS tham chiếu: [WBS.md](WBS.md) — nhóm công việc `4.0`.

## 1. Tạo Idea

| Bước | Giao diện                                     | Dịch vụ                                                                      | Bảng                                                                                        |
|------|----------------------------------------------|------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| 1    | Tab `Ideas` → FAB                            | —                                                                            | —                                                                                           |
| 2    | `CreateIdeaScreen`: title, description, tags | —                                                                            | —                                                                                           |
| 3    | Submit                                       | `CreateIdeaUseCase` → `IdeaRepository.create(input)` → `CreateIdea` mutation | Insert `ideas(id, authorId=auth.uid, title, description, tagIds, status=ACTIVE, createdAt)` |
| 4    | Invalidate React Query `["ideas", "mine"]`   | —                                                                            | —                                                                                           |
| 5    | Navigate `idea/{id}`                         | `GetIdeaDetail`                                                              | Read `ideas` + join `users`, `tags`, `issues`                                               |

## 2. Tạo Issue trong Idea

| Bước | Giao diện                          | Dịch vụ                                       | Bảng                                                                                                                |
|------|-----------------------------------|-----------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| 1    | `IdeaDetailScreen` → "Thêm Issue" | —                                             | —                                                                                                                   |
| 2    | `CreateIssueScreen` open          | `CountMyActiveIssuesUseCase`                  | Count `issues` where authorId=auth.uid, status IN {OPEN, IN_PROGRESS}, deletedAt IS NULL                            |
| 3    | Nếu ≥ 20 → toast, chặn            | —                                             | —                                                                                                                   |
| 4    | Người dùng nhập biểu mẫu → Gửi    | `CreateIssueUseCase` → `CreateIssue` mutation | Insert `issues(id, ideaId, authorId, title, description, priority, status=OPEN)`                                    |
| 5    | Tạo notification                  | `CreateNotification` mutation                 | Insert `notifications(recipientId=idea.authorId, actorId=auth.uid, type=ISSUE_CREATED, targetType=ISSUE, targetId)` |
| 6    | Back về Idea Detail               | Invalidate `["issues", ideaId]`               | —                                                                                                                   |

## 3. Chuyển trạng thái Issue

| Bước | UI                                    | Service                                                                                       | Bảng                                |
|------|---------------------------------------|-----------------------------------------------------------------------------------------------|-------------------------------------|
| 1    | `IssueDetailScreen` → dropdown Status | —                                                                                             | —                                   |
| 2    | Chọn status mới                       | Client validate state machine (OPEN→IN_PROGRESS→CLOSED, OPEN→CANCELLED)                       | —                                   |
| 3    | Confirm                               | `UpdateIssueStatus(id, status)` mutation (where authorId = auth.uid OR assigneeId = auth.uid) | Update `issues.status`, `updatedAt` |
| 4    | Refresh UI                            | —                                                                                             | —                                   |

## 4. Soft delete Idea/Issue

| Bước | UI                    | Service                                               | Bảng                                                        |
|------|-----------------------|-------------------------------------------------------|-------------------------------------------------------------|
| 1    | Detail → Menu → "Xóa" | —                                                     | —                                                           |
| 2    | Confirm dialog        | —                                                     | —                                                           |
| 3    | OK                    | `SoftDeleteIdea(id)` / `SoftDeleteIssue(id)` mutation | Update `deletedAt = request.time` where authorId = auth.uid |
| 4    | Pop back              | Invalidate list                                       | Filter `deletedAt IS NULL` trên client                      |

## 5. Gửi Comment

| Bước | UI                                               | Service                                                       | Bảng                                                        |
|------|--------------------------------------------------|---------------------------------------------------------------|-------------------------------------------------------------|
| 1    | `IdeaDetailScreen` → `CommentSection` input      | —                                                             | —                                                           |
| 2    | Gõ text (1..1000)                                | Client validate length                                        | —                                                           |
| 3    | Tap Send                                         | Optimistic update: append tạm vào list                        | —                                                           |
| 4    | Call                                             | `CreateComment(ideaId, content)` mutation                     | Insert `comments(id, ideaId, authorId, content, createdAt)` |
| 5    | Tạo notification                                 | `CreateNotification(type=COMMENT, recipientId=idea.authorId)` | Insert `notifications`                                      |
| 6    | Thành công → giữ comment; lỗi → rollback + retry | —                                                             | —                                                           |

## 6. Gửi Collab Request

| Bước | UI                                                                             | Service                                                                               | Bảng                   |
|------|--------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|------------------------|
| 1    | `IdeaDetailScreen` (user khác không phải author/collaborator) → "Xin tham gia" | —                                                                                     | —                      |
| 2    | Submit                                                                         | `CreateNotification(type=COLLAB_REQUEST, recipientId=idea.authorId, targetId=ideaId)` | Insert `notifications` |
| 3    | UI disable nút (đã gửi)                                                        | —                                                                                     | —                      |
