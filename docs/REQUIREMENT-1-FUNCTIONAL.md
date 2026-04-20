# REQUIREMENT-1-FUNCTIONAL.md — Yêu cầu chức năng

WBS tham chiếu: [WBS.md](WBS.md) — ánh xạ chính theo `3.0`, `4.0`, `5.0`.

## 1. Nhóm chức năng xác thực (FR-AUTH)

| ID         | Yêu cầu                                                                                            | Ưu tiên |
|------------|----------------------------------------------------------------------------------------------------|---------|
| FR-AUTH-01 | Người dùng đăng nhập bằng Google (Credential Manager)                                              | MUST    |
| FR-AUTH-02 | Hệ thống tự động tạo bản ghi `users` khi đăng nhập lần đầu                                         | MUST    |
| FR-AUTH-03 | Luồng hoàn thiện hồ sơ lần đầu bắt buộc nhập `displayName` hợp lệ (3..50 ký tự) trước khi vào Home | MUST    |
| FR-AUTH-04 | Người dùng đăng xuất, xóa phiên đăng nhập cục bộ                                                   | MUST    |
| FR-AUTH-05 | Phiên đăng nhập được duy trì qua lần mở ứng dụng kế tiếp                                           | MUST    |
| FR-AUTH-06 | Người dùng xóa tài khoản (Firebase + xóa mềm bản ghi)                                              | SHOULD  |

## 2. Nhóm chức năng hồ sơ (FR-PROF)

| ID         | Yêu cầu                                                     | Ưu tiên |
|------------|-------------------------------------------------------------|---------|
| FR-PROF-01 | Xem profile của bản thân (displayName, avatar, bio, skills) | MUST    |
| FR-PROF-02 | Chỉnh sửa profile                                           | MUST    |
| FR-PROF-03 | Upload avatar lên Cloudflare R2 (tự động nén ≤ 1MB)         | MUST    |
| FR-PROF-04 | Xem profile công khai của người dùng khác                   | MUST    |
| FR-PROF-05 | Chọn nhiều kỹ năng từ danh mục `skills`                     | SHOULD  |

## 3. Nhóm chức năng ý tưởng (FR-IDEA)

| ID         | Yêu cầu                                                      | Ưu tiên |
|------------|--------------------------------------------------------------|---------|
| FR-IDEA-01 | Tạo ý tưởng (title, description, tags)                       | MUST    |
| FR-IDEA-02 | Xem chi tiết ý tưởng kèm công việc, bình luận, cộng tác viên | MUST    |
| FR-IDEA-03 | Sửa ý tưởng (chỉ tác giả)                                    | MUST    |
| FR-IDEA-04 | Xóa mềm ý tưởng (chỉ tác giả)                                | MUST    |
| FR-IDEA-05 | Chuyển trạng thái ACTIVE → CLOSED/CANCELLED                  | MUST    |
| FR-IDEA-06 | Liệt kê danh sách "Ý tưởng của tôi"                          | MUST    |
| FR-IDEA-07 | Gửi yêu cầu cộng tác tới tác giả                             | SHOULD  |

## 4. Nhóm chức năng công việc (FR-ISSUE)

| ID          | Yêu cầu                                          | Ưu tiên |
|-------------|--------------------------------------------------|---------|
| FR-ISSUE-01 | Tạo công việc gắn với 1 ý tưởng                  | MUST    |
| FR-ISSUE-02 | Giới hạn tối đa 20 công việc đang mở/người dùng  | MUST    |
| FR-ISSUE-03 | Xem chi tiết công việc                           | MUST    |
| FR-ISSUE-04 | Sửa công việc (tác giả)                          | MUST    |
| FR-ISSUE-05 | Xóa mềm công việc (tác giả)                      | MUST    |
| FR-ISSUE-06 | Chuyển trạng thái theo máy trạng thái hợp lệ     | MUST    |
| FR-ISSUE-07 | Gán người thực hiện                              | SHOULD  |
| FR-ISSUE-08 | Bảng tin Home: công việc OPEN từ người dùng khác | MUST    |

## 5. Nhóm chức năng bình luận (FR-CMT)

| ID        | Yêu cầu                                    | Ưu tiên |
|-----------|--------------------------------------------|---------|
| FR-CMT-01 | Gửi bình luận trên ý tưởng (1..1000 ký tự) | MUST    |
| FR-CMT-02 | Xem danh sách bình luận theo thời gian     | MUST    |
| FR-CMT-03 | Thông báo COMMENT gửi tới tác giả ý tưởng  | SHOULD  |

## 6. Nhóm chức năng thông báo (FR-NOTI)

| ID         | Yêu cầu                                                                        | Ưu tiên |
|------------|--------------------------------------------------------------------------------|---------|
| FR-NOTI-01 | Hiển thị danh sách thông báo của người dùng                                    | MUST    |
| FR-NOTI-02 | Huy hiệu số chưa đọc trên BottomNav                                            | MUST    |
| FR-NOTI-03 | Đánh dấu đã đọc khi nhấn vào thông báo                                         | MUST    |
| FR-NOTI-04 | Loại: ISSUE_CREATED, COLLAB_REQUEST, COLLAB_ACCEPTED, COLLAB_REJECTED, COMMENT | MUST    |
| FR-NOTI-05 | Chấp nhận/Từ chối yêu cầu cộng tác từ thông báo                                | MUST    |
| FR-NOTI-06 | Tự làm mới khi ứng dụng được mở lại vùng nhìn (chu kỳ 30 giây)                 | SHOULD  |

## 7. Nhóm chức năng nhắn tin (FR-MSG)

| ID        | Yêu cầu                                               | Ưu tiên |
|-----------|-------------------------------------------------------|---------|
| FR-MSG-01 | Tạo hội thoại DIRECT 1-1 từ hồ sơ người dùng          | MUST    |
| FR-MSG-02 | Không cho phép trùng lặp hội thoại giữa 2 người dùng  | MUST    |
| FR-MSG-03 | Gửi/nhận tin nhắn văn bản (1..2000 ký tự)             | MUST    |
| FR-MSG-04 | Danh sách hội thoại sắp theo `lastMessageAt` giảm dần | MUST    |
| FR-MSG-05 | Cập nhật theo chu kỳ 5 giây khi mở hội thoại          | SHOULD  |
| FR-MSG-06 | Cập nhật lạc quan khi gửi                             | SHOULD  |

## 8. Nhóm chức năng tra cứu (FR-LKP)

| ID        | Yêu cầu                                   | Ưu tiên |
|-----------|-------------------------------------------|---------|
| FR-LKP-01 | Danh sách thẻ PUBLIC (không cần xác thực) | MUST    |
| FR-LKP-02 | Danh sách kỹ năng PUBLIC                  | MUST    |
| FR-LKP-03 | Lọc ý tưởng/công việc theo thẻ            | SHOULD  |
