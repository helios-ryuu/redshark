# PLAN-3-CONTENT.md — Phase 3: Ideas, Issues, Comments

**Thời gian:** 21/04/2026 – 30/04/2026 (Tuần 3 + Tuần 4)
**Mục tiêu:** Hoàn thiện toàn bộ nghiệp vụ cốt lõi (nội dung) của app.

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

## 5. Phân công gợi ý
- Nam: UI + ViewModel (Home, Ideas, Issues, Comments)
- Hải: Repository + FDC mutations + validation logic
- Sỹ: review PR, test case, cập nhật docs
