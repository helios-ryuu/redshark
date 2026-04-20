# PROJECT CHARTER — RedShark Android Native (Kotlin)

## 1. Tên dự án
**RedShark Android Native** — Ứng dụng di động cộng tác theo dõi Idea/Issue, xây dựng bằng Kotlin Android Native.

## 2. Mục tiêu (Objectives)
| # | Mục tiêu | Chỉ số đo lường |
|---|----------|----------------|
| O1 | Hoàn thiện ứng dụng Android Native bằng Kotlin + Jetpack Compose | 100% màn hình trong phạm vi đề tài được triển khai |
| O2 | Giữ nguyên backend Firebase Data Connect (PostgreSQL) + Firebase Auth | 0 breaking change trên schema |
| O3 | Tích hợp Cloudflare R2 (S3-compatible) cho media/avatar | Upload/Download < 3s trên 4G |
| O4 | Áp dụng Clean Architecture + MVVM | Code coverage unit test ≥ 60% |
| O5 | Bảo đảm không lộ thông tin nhạy cảm trong source/log | 0 thông tin nhạy cảm lộ trên Git history và log release |

## 3. Phạm vi (Scope)

### In-scope
- Authentication (Google Sign-In + hoàn thiện hồ sơ lần đầu)
- Idea/Issue CRUD + soft delete + max 20 issues/user
- Comments trên Idea
- Notifications (in-app) + Collab Request
- Messages (1-1 DIRECT conversation)
- Profile (view, edit, avatar upload R2)
- Tag/Skill lookup + filtering

### Out-of-scope
- Backend server riêng (giữ FDC)
- iOS / Web
- Push notification FCM (giai đoạn sau)
- Group conversation (> 2 members)
- Payment / subscription

## 4. Stakeholders

| Vai trò | Tên | Trách nhiệm |
|---------|-----|-------------|
| Project Manager | Sỹ | Lập kế hoạch, quản lý tiến độ, báo cáo, QA cuối |
| Android Developer | Nam | Triển khai UI/Compose, ViewModel, Navigation |
| Android Developer | Hải | Triển khai Data layer (FDC SDK, R2, Auth) |
| Product Owner | Giảng viên môn NT118.Q22 | Phê duyệt requirement, chấm điểm |
| End User | Sinh viên / nhóm dự án nhỏ | Sử dụng app để track idea/issue |

## 5. Rủi ro dự kiến (Risks)

| ID | Rủi ro | Mức độ | Hướng xử lý |
|----|-------|--------|-------------|
| R1 | Firebase Data Connect Android SDK chưa stable | Cao | Theo dõi release notes, fallback sang Apollo GraphQL client nếu cần |
| R2 | Cloudflare R2 chi phí vượt plan miễn phí | Thấp | Giới hạn avatar ≤ 1MB, nén client-side |
| R3 | Google Sign-In SHA-1 config sai khi build debug/release | Trung bình | Checklist cấu hình SHA-1 trước khi test/release |
| R4 | Timeline 6 tuần quá ngắn cho 3 người | Cao | Ưu tiên Phase 2 (Auth) hoàn thành trước 20/04 |
| R5 | Lộ token đăng nhập trong log/debug | Cao | Cấm log token, rà soát log trước release |
| R6 | Kotlin team chưa quen Jetpack Compose | Trung bình | 1 tuần ramp-up ở Phase 1 |

## 6. Tiêu chí thành công (Success Criteria)
- [ ] APK release build chạy ổn định trên Android 8+ (API 26+)
- [ ] Đầy đủ 7 luồng nghiệp vụ: Auth, Idea, Issue, Comment, Notification, Message, Profile
- [ ] Không có crash > 1% trong smoke test 100 lượt sử dụng
- [ ] Manual test đạt ≥ 95% test case PASS
- [ ] Deliverables: APK + source code + tài liệu trong `/docs` + báo cáo

## 7. Ngân sách & Tài nguyên
- **Nhân lực:** 3 người × 6 tuần (05/04/2026 – 17/05/2026)
- **Hạ tầng:** Firebase Spark plan (free tier), Cloudflare R2 free 10GB
- **Công cụ:** Android Studio, Figma, Postman, Firebase Console
