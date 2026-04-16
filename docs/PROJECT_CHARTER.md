# PROJECT_CHARTER.md — Tuyên ngôn dự án RedShark Android Native (Kotlin)

## 1. Tên dự án
**RedShark Android Native** — Ứng dụng di động cộng tác theo dõi Idea/Issue, xây dựng bằng Kotlin Android Native.

## 2. Mục tiêu
| #  | Mục tiêu                                                              | Chỉ số đo lường                                         |
|----|-----------------------------------------------------------------------|---------------------------------------------------------|
| O1 | Hoàn thiện ứng dụng Android Native bằng Kotlin + Jetpack Compose      | 100% màn hình trong phạm vi đề tài được triển khai      |
| O2 | Giữ nguyên backend Firebase Data Connect (PostgreSQL) + Firebase Auth | 0 breaking change trên schema                           |
| O3 | Tích hợp Cloudflare R2 (S3-compatible) cho media/avatar               | Upload/Download < 3s trên 4G                            |
| O4 | Áp dụng Clean Architecture + MVVM                                     | Code coverage unit test ≥ 60%                           |
| O5 | Bảo đảm không lộ thông tin nhạy cảm trong mã nguồn/nhật ký            | 0 thông tin nhạy cảm lộ trên lịch sử Git và nhật ký phát hành |

## 3. Phạm vi

### Trong phạm vi
- Xác thực (Google Sign-In + hoàn thiện hồ sơ lần đầu)
- Ý tưởng/Công việc CRUD + xóa mềm + tối đa 20 công việc đang mở/người dùng
- Comments trên Idea
- Notifications (in-app) + Collab Request
- Messages (1-1 DIRECT conversation)
- Profile (view, edit, avatar upload R2)
- Tag/Skill lookup + filtering

### Ngoài phạm vi
- Backend server riêng (giữ FDC)
- iOS / Web
- Push notification FCM (giai đoạn sau)
- Group conversation (> 2 members)
- Payment / subscription

## 4. Các bên liên quan

| Vai trò | Tên | Trách nhiệm |
|---------|-----|-------------|
| Quản lý dự án | Sỹ | Lập kế hoạch, quản lý tiến độ, báo cáo, kiểm thử cuối kỳ |
| Lập trình viên Android | Nam | Triển khai giao diện Compose, ViewModel, điều hướng |
| Lập trình viên Android | Hải | Triển khai tầng dữ liệu (FDC SDK, R2, xác thực) |
| Đại diện nghiệp vụ | Giảng viên môn NT118.Q22 | Phê duyệt yêu cầu, đánh giá kết quả |
| Người dùng cuối | Sinh viên / nhóm dự án nhỏ | Sử dụng ứng dụng để theo dõi ý tưởng/công việc |

## 5. Rủi ro dự kiến (Risks)

| ID | Rủi ro                                                  | Mức độ     | Hướng xử lý                                                         |
|----|---------------------------------------------------------|------------|---------------------------------------------------------------------|
| R1 | Firebase Data Connect Android SDK chưa ổn định          | Cao        | Theo dõi ghi chú phát hành, chuyển phương án sang Apollo GraphQL client nếu cần |
| R2 | Cloudflare R2 chi phí vượt plan miễn phí                | Thấp       | Giới hạn avatar ≤ 1MB, nén client-side                              |
| R3 | Cấu hình SHA-1 cho Google Sign-In sai khi biên dịch debug/phát hành | Trung bình | Danh sách kiểm tra cấu hình SHA-1 trước khi kiểm thử/phát hành |
| R4 | Tiến độ 9 tuần tương đối gấp cho nhóm 3 người           | Cao        | Ưu tiên có ít nhất 1 commit tính năng xác thực trước 21/04          |
| R5 | Lộ mã định danh đăng nhập trong nhật ký                 | Cao        | Không ghi token vào log, rà soát log trước phát hành                |
| R6 | Nhóm chưa quen Jetpack Compose                          | Trung bình | Dành 1 tuần làm quen trong giai đoạn 1                              |

## 6. Tiêu chí thành công
- [ ] Bản dựng APK phát hành chạy ổn định trên Android 8+ (API 26+)
- [ ] Đầy đủ 7 luồng nghiệp vụ: Auth, Idea, Issue, Comment, Notification, Message, Profile
- [ ] Không có tỷ lệ sự cố vượt quá 1% trong kiểm tra nhanh 100 lượt sử dụng
- [ ] Kiểm thử thủ công đạt ≥ 95% trường hợp đạt
- [ ] Sản phẩm bàn giao: APK + mã nguồn + tài liệu trong `/docs` + báo cáo

WBS tham chiếu: [WBS.md](WBS.md) (truy vết công việc, phụ thuộc và trách nhiệm).

## 7. Ngân sách & Tài nguyên
- **Nhân lực:** 3 người × 9 tuần (16/03/2026 – 17/05/2026)
- **Hạ tầng:** Firebase Spark plan (free tier), Cloudflare R2 free 10GB
- **Công cụ:** Android Studio, Figma, Postman, Firebase Console
