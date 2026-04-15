# REQUIREMENT-1-FUNCTIONAL.md — Yêu cầu chức năng

## 1. Module Authentication (FR-AUTH)

| ID         | Yêu cầu                                                                                            | Ưu tiên |
|------------|----------------------------------------------------------------------------------------------------|---------|
| FR-AUTH-01 | Người dùng đăng nhập bằng Google (Credential Manager)                                              | MUST    |
| FR-AUTH-02 | Hệ thống tự động tạo record `users` khi đăng nhập lần đầu                                          | MUST    |
| FR-AUTH-03 | Luồng hoàn thiện hồ sơ lần đầu bắt buộc nhập `displayName` hợp lệ (3..50 ký tự) trước khi vào Home | MUST    |
| FR-AUTH-04 | Người dùng đăng xuất, xóa session local                                                            | MUST    |
| FR-AUTH-05 | Session được persist qua lần mở app kế tiếp                                                        | MUST    |
| FR-AUTH-06 | Người dùng xóa tài khoản (Firebase + soft-delete record)                                           | SHOULD  |

## 2. Module Profile (FR-PROF)

| ID         | Yêu cầu                                                     | Ưu tiên |
|------------|-------------------------------------------------------------|---------|
| FR-PROF-01 | Xem profile của bản thân (displayName, avatar, bio, skills) | MUST    |
| FR-PROF-02 | Chỉnh sửa profile                                           | MUST    |
| FR-PROF-03 | Upload avatar lên Cloudflare R2 (tự động nén ≤ 1MB)         | MUST    |
| FR-PROF-04 | Xem profile công khai của người dùng khác                   | MUST    |
| FR-PROF-05 | Chọn nhiều skill từ danh mục `skills`                       | SHOULD  |

## 3. Module Idea (FR-IDEA)

| ID         | Yêu cầu                                               | Ưu tiên |
|------------|-------------------------------------------------------|---------|
| FR-IDEA-01 | Tạo idea (title, description, tags)                   | MUST    |
| FR-IDEA-02 | Xem chi tiết idea kèm issues, comments, collaborators | MUST    |
| FR-IDEA-03 | Sửa idea (chỉ author)                                 | MUST    |
| FR-IDEA-04 | Soft delete idea (chỉ author)                         | MUST    |
| FR-IDEA-05 | Chuyển trạng thái ACTIVE → CLOSED/CANCELLED           | MUST    |
| FR-IDEA-06 | Liệt kê "My Ideas"                                    | MUST    |
| FR-IDEA-07 | Gửi Collab Request tới author                         | SHOULD  |

## 4. Module Issue (FR-ISSUE)

| ID          | Yêu cầu                                     | Ưu tiên |
|-------------|---------------------------------------------|---------|
| FR-ISSUE-01 | Tạo issue gắn với 1 idea                    | MUST    |
| FR-ISSUE-02 | Giới hạn tối đa 20 issue active / user      | MUST    |
| FR-ISSUE-03 | Xem chi tiết issue                          | MUST    |
| FR-ISSUE-04 | Sửa issue (author)                          | MUST    |
| FR-ISSUE-05 | Soft delete issue (author)                  | MUST    |
| FR-ISSUE-06 | Chuyển trạng thái theo state machine hợp lệ | MUST    |
| FR-ISSUE-07 | Gán assignee                                | SHOULD  |
| FR-ISSUE-08 | Home feed: OPEN issues từ user khác         | MUST    |

## 5. Module Comment (FR-CMT)

| ID        | Yêu cầu                                  | Ưu tiên |
|-----------|------------------------------------------|---------|
| FR-CMT-01 | Gửi comment trên idea (1..1000 ký tự)    | MUST    |
| FR-CMT-02 | Xem danh sách comment theo thời gian     | MUST    |
| FR-CMT-03 | Notification COMMENT gửi tới author idea | SHOULD  |

## 6. Module Notification (FR-NOTI)

| ID         | Yêu cầu                                                                        | Ưu tiên |
|------------|--------------------------------------------------------------------------------|---------|
| FR-NOTI-01 | Hiển thị danh sách thông báo của user                                          | MUST    |
| FR-NOTI-02 | Badge số chưa đọc trên BottomNav                                               | MUST    |
| FR-NOTI-03 | Mark as read khi tap                                                           | MUST    |
| FR-NOTI-04 | Loại: ISSUE_CREATED, COLLAB_REQUEST, COLLAB_ACCEPTED, COLLAB_REJECTED, COMMENT | MUST    |
| FR-NOTI-05 | Accept/Reject collab từ notification                                           | MUST    |
| FR-NOTI-06 | Tự refresh khi focus app (polling 30s)                                         | SHOULD  |

## 7. Module Message (FR-MSG)

| ID        | Yêu cầu                                             | Ưu tiên |
|-----------|-----------------------------------------------------|---------|
| FR-MSG-01 | Tạo hội thoại DIRECT 1-1 từ Profile                 | MUST    |
| FR-MSG-02 | Không cho phép trùng lặp conversation giữa 2 user   | MUST    |
| FR-MSG-03 | Gửi/nhận message text (1..2000 ký tự)               | MUST    |
| FR-MSG-04 | Danh sách conversation sort theo lastMessageAt DESC | MUST    |
| FR-MSG-05 | Polling 5s khi mở conversation                      | SHOULD  |
| FR-MSG-06 | Optimistic update khi gửi                           | SHOULD  |

## 8. Module Lookup (FR-LKP)

| ID        | Yêu cầu                                | Ưu tiên |
|-----------|----------------------------------------|---------|
| FR-LKP-01 | Danh sách tags PUBLIC (không cần auth) | MUST    |
| FR-LKP-02 | Danh sách skills PUBLIC                | MUST    |
| FR-LKP-03 | Filter ideas/issues theo tag           | SHOULD  |
