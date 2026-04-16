# TIMELINE.md — Tiến độ dự án

**Thời gian:** 16/03/2026 → 17/05/2026 (9 tuần, 63 ngày).
**Nhóm:** 3 thành viên — **Sỹ (PM)**, **Nam**, **Hải**.
**Mốc bắt buộc:**
- **15/04/2026:** commit khởi tạo dự án (Sỹ). ✓ *Đã hoàn thành — commit `chore(init)` ngày 15/04.*
- **Trước 21/04/2026:** bắt buộc có ít nhất 1 commit tính năng xác thực.

## 1. Sơ đồ giai đoạn

```
Tuần 1 ─ 16/03 → 22/03 │ Giai đoạn 0a: Khảo sát & Yêu cầu
Tuần 2 ─ 23/03 → 29/03 │ Giai đoạn 0b: Kiến trúc & Lược đồ dữ liệu
Tuần 3 ─ 30/03 → 05/04 │ Giai đoạn 0c: Kế hoạch & Quy trình
Tuần 4 ─ 06/04 → 12/04 │ Giai đoạn 1: Chuẩn bị nền tảng
Tuần 5 ─ 13/04 → 20/04 │ Giai đoạn 2a: Lõi xác thực     ◄── Mốc xác thực trước 21/04
Tuần 6 ─ 21/04 → 27/04 │ Giai đoạn 2b Hồ sơ + 3a Ý tưởng
Tuần 7 ─ 28/04 → 04/05 │ Giai đoạn 3b: Công việc + Bình luận
Tuần 8 ─ 05/05 → 11/05 │ Giai đoạn 4: Thông báo + Nhắn tin
Tuần 9 ─ 12/05 → 17/05 │ Giai đoạn 5: Kiểm thử, dọn dẹp, phát hành
```

## 2. Bảng phân công theo tuần

### Tuần 1-4 — 16/03 → 12/04 — **Giai đoạn 0-1: Khảo sát đến chuẩn bị nền tảng**

| Thành viên  | Công việc                                                                                                                         |
|-------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **Sỹ (PM)** | Chủ trì nền tảng: khởi tạo khung Android, kiểm tra biên dịch cục bộ, tích hợp Firebase/FDC/R2, chuẩn hóa GIT/WBS/STRUCTURE/SECRET |
| **Nam**     | Phối hợp khung giao diện (theme + navigation), rà soát cấu trúc nhóm chức năng và quy ước đặt tên                                 |
| **Hải**     | Phối hợp kiểm thử tích hợp tầng dữ liệu/lưu trữ, hỗ trợ chuẩn hóa cấu hình và tài liệu kỹ thuật                                   |

### Tuần 5 — 13/04 → 20/04 — **Giai đoạn 2a: Xác thực** *(mốc bắt buộc trước 21/04)*

| Thành viên | Công việc                                                                                                                     |
|------------|-------------------------------------------------------------------------------------------------------------------------------|
| **Sỹ**     | Phụ trách nhóm xác thực: triển khai luồng Google Sign-In + hoàn thiện hồ sơ lần đầu; chốt commit mốc `feat(auth)` trước 21/04 |
| **Nam**    | Phối hợp giao diện xác thực/hồ sơ ban đầu và sơ đồ điều hướng                                                                 |
| **Hải**    | Phối hợp tầng dữ liệu xác thực (`FirebaseAuthSource`, `AuthRepositoryImpl`) và kiểm tra ràng buộc nghiệp vụ                   |

**🎯 Mốc bắt buộc (20/04/2026):** có ít nhất 1 commit `feat(auth): ...` (trước 21/04/2026).

### Tuần 6 — 21/04 → 27/04 — **Giai đoạn 2b Hồ sơ + 3a Ý tưởng**

| Thành viên | Công việc                                                                                        |
|------------|--------------------------------------------------------------------------------------------------|
| **Sỹ**     | Hoàn tất phần Hồ sơ/Ảnh đại diện thuộc nhóm xác thực, rà soát mốc bàn giao sang nhóm nội dung    |
| **Nam**    | Phối hợp giao diện cho nhóm nội dung: Ý tưởng (`MyIdeas`, `CreateIdea`, `IdeaDetail`)            |
| **Hải**    | Phụ trách nhóm nội dung: triển khai tầng dữ liệu/ràng buộc nghiệp vụ và hoàn thiện luồng Ý tưởng |

### Tuần 7 — 28/04 → 04/05 — **Giai đoạn 3b: Công việc + Bình luận**

| Thành viên | Công việc                                                                                                                            |
|------------|--------------------------------------------------------------------------------------------------------------------------------------|
| **Sỹ**     | Kiểm thử CHECK-2-CONTENT, cập nhật PROCESS-2, theo dõi tiến độ                                                                       |
| **Nam**    | Phối hợp giao diện cho nhóm nội dung: `IdeaDetailScreen`, `IssueDetailScreen`, `CreateIssueScreen`, `CommentSection`                 |
| **Hải**    | Phụ trách nhóm nội dung: triển khai tầng dữ liệu/ràng buộc (`IssueRepositoryImpl`, `CommentRepositoryImpl`, kiểm tra máy trạng thái) |

### Tuần 8 — 05/05 → 11/05 — **Giai đoạn 4: Thông báo + Nhắn tin**

| Thành viên | Công việc                                                                                                                |
|------------|--------------------------------------------------------------------------------------------------------------------------|
| **Sỹ**     | Kiểm thử CHECK-3-INTERACTION; cập nhật báo cáo tiến độ                                                                   |
| **Nam**    | Phụ trách nhóm tương tác: `NotificationRepositoryImpl`, `MessageRepositoryImpl`, `FindOrCreateDirectConversationUseCase` |
| **Hải**    | Phối hợp giao diện nhóm tương tác (`NotificationList`, `ConversationList`, `ConversationScreen`)                         |

### Tuần 9 — 12/05 → 17/05 — **Giai đoạn 5: Kiểm thử, dọn dẹp, phát hành**

| Ngày     | Thành viên | Công việc                                                                                         |
|----------|------------|---------------------------------------------------------------------------------------------------|
| 12–14/05 | Cả nhóm    | Kiểm thử hồi quy CHECK-1/2/3 trên 3 thiết bị; sửa lỗi mức P0/P1                                   |
| 15/05    | Sỹ + Nam   | Dọn dẹp kỹ thuật (phụ trách chính: Sỹ) + hoàn thiện giao diện dark mode/empty/error/accessibility |
| 16/05    | Sỹ + Hải   | Tạo APK ký số; đo hiệu năng/độ ổn định; cập nhật REPORT                                           |
| 17/05    | Cả nhóm    | Kiểm tra nhanh bản phát hành, gắn nhãn `v1.0.0`, nộp bài và dự phòng hotfix                       |

## 3. Sản phẩm bàn giao theo từng giai đoạn

WBS tham chiếu: [WBS.md](WBS.md) (đối chiếu tuần triển khai với các nhóm công việc 1.0-7.0).

| Giai đoạn | Sản phẩm bàn giao                                                         |
|-----------|---------------------------------------------------------------------------|
| Tuần 1-4  | Bộ tài liệu nền + cấu hình môi trường + chuẩn kiến trúc                   |
| Tuần 5    | Commit khởi tạo (15/04) + ít nhất 1 commit tính năng xác thực trước 21/04 |
| Tuần 6    | Hồ sơ + Ý tưởng CRUD                                                      |
| Tuần 7    | Công việc + Bình luận CRUD, máy trạng thái                                |
| Tuần 8    | Thông báo + Nhắn tin                                                      |
| Tuần 9    | APK phát hành đã ký số + báo cáo + slide                                  |

## 4. Vùng đệm rủi ro

- Mỗi giai đoạn có tối thiểu **1 ngày dự phòng** để sửa lỗi.
- Nếu mốc xác thực trước 21/04 có rủi ro: ưu tiên Google Sign-In + duy trì phiên đăng nhập + hoàn thiện hồ sơ lần đầu (`displayName` 3..50), dời các phần hoàn thiện không bắt buộc sang Tuần 6.
