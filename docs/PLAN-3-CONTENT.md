# PLAN-3-CONTENT.md — Giai đoạn 3: Ý tưởng, Công việc, Bình luận

**Thời gian:** 22/04/2026 – 04/05/2026 (giai đoạn Content)
**Mục tiêu:** Hoàn thiện toàn bộ nghiệp vụ cốt lõi (nội dung) của app.
**WBS tham chiếu:** [WBS.md](WBS.md) — nhóm công việc `4.0`.

> **Tiền điều kiện:** Giai đoạn 2 (Auth) đã hoàn thành — ít nhất 1 commit `feat(auth)` đã merge vào `develop` trước 21/04. Giai đoạn 3 bắt đầu từ `develop` sau khi mốc auth được chốt.

## 1. Tính năng
### Ideas
- List "My Ideas" (tab Ideas) + FAB tạo mới
- Xem chi tiết (title, description, tags, collaborators, danh sách Issues, Comments)
- Tạo / Sửa / Soft delete
- Chuyển trạng thái: ACTIVE → CLOSED / CANCELLED
- Gửi Collab Request (tạo notification type `COLLAB_REQUEST`)

### Issues
- Tab Home = list OPEN issues từ user khác
- Tạo issue gắn với 1 idea (ràng buộc tối đa 20 active/user)
- Sửa / Soft delete (chỉ author)
- State machine: OPEN → IN_PROGRESS → CLOSED; OPEN → CANCELLED
- Assignee (optional)

### Comments
- Hiển thị danh sách comment trên Idea Detail
- Gửi comment mới (1..1000 ký tự)

## 2. Màn hình
| Route                  | Screen                                |
|------------------------|---------------------------------------|
| `home`                 | `HomeScreen` (feed Issues)            |
| `ideas`                | `MyIdeasScreen`                       |
| `idea/create`          | `CreateIdeaScreen`                    |
| `idea/{id}`            | `IdeaDetailScreen` (+ CommentSection) |
| `idea/edit/{id}`       | `EditIdeaScreen`                      |
| `issue/create?ideaId=` | `CreateIssueScreen`                   |
| `issue/{id}`           | `IssueDetailScreen`                   |
| `issue/edit/{id}`      | `EditIssueScreen`                     |

## 3. UseCases (trích)
- `GetHomeFeedUseCase`
- `GetMyIdeasUseCase`
- `GetIdeaDetailUseCase`
- `CreateIdeaUseCase` / `UpdateIdeaUseCase` / `DeleteIdeaUseCase`
- `CreateIssueUseCase` (check `CountMyActiveIssues ≤ 20`)
- `UpdateIssueStatusUseCase` (validate state transition)
- `DeleteIssueUseCase`
- `GetCommentsUseCase` / `CreateCommentUseCase`
- `RequestCollabUseCase`

## 4. Acceptance Criteria
- [ ] Validate title 3–120, description ≤ 5000
- [ ] Khi tạo issue vượt quá 20 → toast "Đạt giới hạn 20 issue"
- [ ] Soft delete không mất dữ liệu trên DB, chỉ ẩn UI
- [ ] Khi tạo issue, tự gửi notification `ISSUE_CREATED` cho chủ idea
- [ ] Trạng thái chuyển sai (CLOSED → OPEN) bị chặn client + server

## 5. Phân công theo WBS
- Phụ trách nhóm nội dung: **Hải** (lập trình chính, chịu trách nhiệm commit chính).
- Nam: phối hợp UI + ViewModel cho Ideas/Issues/Comments.
- Sỹ: phối hợp review, kiểm thử, cập nhật tài liệu liên quan.
