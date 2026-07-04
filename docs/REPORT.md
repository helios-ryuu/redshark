# BÁO CÁO ĐỒ ÁN REDSHARK ANDROID NATIVE

**Môn học:** NT118.Q22 - Lập trình ứng dụng di động  
**Nhóm thực hiện:** Sỹ (PM), Nam, Hải  
**Thời gian:** 16/03/2026 - 30/05/2026  
**Sản phẩm:** Ứng dụng Android native quản lý ý tưởng, công việc và cộng tác nhóm nhỏ

## 1. Tổng Quan Đề Tài

RedShark là ứng dụng Android gốc hỗ trợ sinh viên và nhóm dự án nhỏ quản lý vòng đời ý tưởng, chia nhỏ thành công việc, thảo luận, nhận thông báo, nhắn tin trực tiếp và theo dõi đóng góp cá nhân. Ứng dụng được xây dựng bằng Kotlin, Jetpack Compose, Firebase Authentication, Cloud Firestore và Cloudflare R2.

### 1.1 Đặt vấn đề

Các nhóm nhỏ thường cần một công cụ nhẹ, dễ dùng trên điện thoại và tập trung vào luồng làm việc thực tế: ghi nhận ý tưởng, tạo công việc, thảo luận, trao đổi nhanh và nhìn lại mức độ đóng góp. Nhiều công cụ phổ biến như Jira hoặc Trello mạnh nhưng thiên về web, có nhiều cấu hình và không tối ưu cho bài toán mobile-first của nhóm sinh viên.

### 1.2 Mục tiêu

| # | Mục tiêu | Chỉ số đo lường |
|---|---|---|
| O1 | Hoàn thiện app Android native bằng Kotlin + Jetpack Compose | 100% màn hình trong phạm vi đề tài có UI và logic thật |
| O2 | Dùng Firebase Auth + Cloud Firestore làm backend chính | Schema/rules/indexes được mô tả và build pass |
| O3 | Tích hợp Cloudflare R2 cho avatar và media idea | Upload qua R2 client, dữ liệu media lưu metadata trong Firestore |
| O4 | Áp dụng Clean Architecture + MVVM | Domain/usecase/repository tách lớp, có unit test |
| O5 | Hoàn thiện tài liệu bàn giao | README, REPORT, PROCESS, SCHEMA, DELIVERABLE đồng bộ trạng thái repo |

### 1.3 Phạm vi

Trong phạm vi: xác thực Google và Email/Password, hồ sơ, idea, media, issue, comment, notification, direct message, share idea nhiều người nhận, contribution graph cá nhân, deploy/reset dữ liệu.

Ngoài phạm vi: iOS, web, thanh toán, hội thoại nhóm nhiều người, FCM push notification, backend server riêng.

### 1.4 Thành viên và trách nhiệm

| Thành viên | Vai trò | Trách nhiệm chính |
|---|---|---|
| Ngô Tiến Sỹ | PM, Android, kiểm thử, tài liệu | Quản lý tiến độ, auth/profile, tài liệu, kiểm thử cuối |
| Phạm Tuấn Hải | Android | Idea, media, issue, comment, collab request |
| Nguyễn Văn Nam | Android | Notification, message, phối hợp UI/ViewModel |

## 2. Cơ Sở Công Nghệ

### 2.1 Kotlin Android Native và Jetpack Compose

Kotlin là ngôn ngữ chính thức cho Android, hỗ trợ null-safety, coroutine và code concise. Jetpack Compose giúp xây dựng UI khai báo, dễ chia component, dễ gắn state từ ViewModel và phù hợp với Material 3.

### 2.2 Firebase Authentication

Firebase Authentication cung cấp định danh người dùng với Google Sign-In và Email/Password. UID từ Firebase Auth là khóa chính liên kết với `users/{uid}` trong Firestore. Luồng đăng ký email kiểm tra username, email, mật khẩu và ngày sinh trước khi tạo user.

### 2.3 Cloud Firestore

Firestore là cơ sở dữ liệu NoSQL dạng document. RedShark dùng collection chính: `users`, `ideas`, `issues`, `comments`, `notifications`, `conversations`, `messages` và subcollection `ideas/{ideaId}/reactions`. Security Rules kiểm soát owner, participant và collaborator.

### 2.4 Cloudflare R2

Cloudflare R2 lưu avatar và media idea. Ứng dụng upload object qua API tương thích S3, metadata được lưu trong Firestore để UI render lại ảnh/video.

### 2.5 Kiến trúc Clean Architecture + MVVM

Luồng chính: UI event -> ViewModel -> UseCase -> Repository -> Firebase/R2 -> StateFlow -> Compose UI.

| Layer | Vai trò |
|---|---|
| UI | Compose screen, component dùng chung, theme, navigation |
| Domain | Model, repository interface, use case, business rule |
| Data | Repository implementation, DTO, mapper, Firebase/Firestore/R2 |
| Core | DI, error mapping, network checker, result wrapper |

## 3. Phân Tích Và Thiết Kế

### 3.1 Actor

| Actor | Mô tả |
|---|---|
| Visitor | Người chưa đăng nhập, có thể vào flow đăng nhập/đăng ký |
| User | Người đã đăng nhập, dùng toàn bộ chức năng nghiệp vụ |
| Idea Author | Chủ ý tưởng, có quyền sửa/xóa/trạng thái và duyệt cộng tác |
| Collaborator | Người tham gia idea, có quyền upload media theo rules |

### 3.2 Nhóm chức năng

| Nhóm | Chức năng |
|---|---|
| Auth | Google Sign-In, đăng ký/đăng nhập email, kiểm tra username, đăng xuất |
| Profile | Xem/sửa hồ sơ, avatar R2, bio, skills, graph đóng góp |
| Idea | Tạo/xem/sửa/xóa mềm, status ACTIVE/CLOSED/CANCELLED, reaction |
| Media | Upload ảnh/video cho idea bởi author/collaborator |
| Issue | CRUD issue, priority, assignee, state machine, giới hạn 20 issue active |
| Comment | Realtime comments, optimistic update, hiển thị tác giả |
| Notification | Badge unread, collab request, accept/reject, comment/issue notification |
| Message | Direct conversation, unread badge, deep link idea |
| Share | Chọn nhiều user/conversation, tự tạo direct conversation khi cần |

### 3.3 Thiết kế dữ liệu

Chi tiết schema, rules và indexes nằm trong [SCHEMA.md](SCHEMA.md). Điểm quan trọng:

- `ideas` và `issues` dùng soft delete qua `deletedAt`.
- `conversations` là direct chat giữa 2 UID; conversation mới dùng deterministic UUID từ cặp UID để tránh tạo trùng.
- `messages` lưu nội dung text và deep link `redshark://idea/{ideaId}`.
- Contribution graph không thêm collection mới; dữ liệu được tổng hợp từ `ideas`, `issues`, `comments`.

### 3.4 Thiết kế UI/UX

UI dùng Material 3, theme RedShark và các token trong `Dimens`, `Color`, `Shape`, `Type`. Đợt hoàn thiện cuối tập trung UX repair:

- Bottom navigation: Home, Ideas, Messages, Profile.
- Bottom sheet cho notification, comment, share.
- Component thống nhất: `IdeaCard`, `IssueCard`, `AvatarImage`, `StateContent`, `StatusPill`.
- Các state loading/error/empty/content có UI thật, không còn placeholder chức năng.
- Share sheet hỗ trợ search, multi-select, trạng thái partial failure.
- Profile hiển thị contribution graph 12 tuần giống contribution calendar.

## 4. Hiện Thực

### 4.1 Môi trường

| Thành phần | Phiên bản/công nghệ |
|---|---|
| Language | Kotlin 2.3.20 |
| Android | minSdk 26, target/compileSdk 36 |
| Build | Gradle 9.4.1, AGP 9.2.0 |
| UI | Jetpack Compose, Material 3 |
| DI | Hilt |
| Async | Coroutines, Flow |
| Backend | Firebase Auth, Cloud Firestore |
| Storage | Cloudflare R2, OkHttp |
| Image | Coil |
| Test | JUnit, MockK, kotlinx-coroutines-test |

### 4.2 Module chính

Auth/Profile:
- Google Sign-In qua Credential Manager.
- Email/Password validate email, username, password, date of birth.
- Avatar upload qua R2, profile lưu `avatarUrl`.

Idea/Issue:
- Idea có status, media, reaction, collaborator.
- Issue có priority, assignee, status machine: `OPEN -> IN_PROGRESS/CANCELLED`, `IN_PROGRESS -> CLOSED`.
- Giới hạn 20 issue active do một user tạo.

Interaction:
- Notification in-app theo Firestore snapshot.
- Direct message 1-1 với unread badge.
- Share idea gửi text gồm title, mô tả và deep link vào nhiều người nhận.

Contribution:
- `ContributionRepository` query `ideas`, `issues`, `comments` theo `authorId` và `createdAt`.
- `ContributionSummaryBuilder` gom sự kiện thành 84 ngày, tính level 0..4.
- Profile render graph 12 tuần và tổng số hoạt động.

## 5. Kiểm Thử

Chiến lược kiểm thử chi tiết nằm trong [PROCESS.md](PROCESS.md). Kết quả tự động gần nhất:

| Ngày | Lệnh | Kết quả |
|---|---|---|
| 30/05/2026 | `.\gradlew.bat compileDebugKotlin` | PASS |
| 30/05/2026 | `.\gradlew.bat testDebugUnitTest` | PASS |

Unit test hiện bao phủ các use case auth, profile, idea media, message, notification và các phần mới:

- Share idea: validate recipient, dedupe, bỏ current user, tiếp tục khi lỗi một phần.
- Contribution graph: bucket 12 tuần, ignore event ngoài range, level intensity ổn định.

Manual regression cần chạy trên thiết bị/emulator trước release cuối. Tài liệu không ghi nhận manual pass khi chưa có bằng chứng chạy thực tế.

## 6. Rủi Ro Và Cách Xử Lý

| ID | Rủi ro | Mức độ | Cách xử lý |
|---|---|---|---|
| R1 | Firestore rules sai gây lỗi quyền | Trung bình | Kiểm tra rules, chạy smoke test author/collaborator/participant |
| R2 | R2 config sai làm upload fail | Trung bình | Dùng `local.properties`, kiểm tra biến môi trường trước demo |
| R3 | Google Sign-In thiếu SHA-1 | Trung bình | Kiểm tra Firebase Console trước khi build demo |
| R4 | Query contribution thiếu index | Trung bình | Cập nhật `firestore.indexes.json` và deploy indexes |
| R5 | Manual regression chưa đủ | Cao | Dùng checklist trong PROCESS trước khi nộp/release |

## 7. Kết Luận

RedShark hoàn thiện một ứng dụng Android native có đủ luồng cộng tác cốt lõi: quản lý ý tưởng, công việc, trao đổi, thông báo, chia sẻ và thống kê đóng góp. Dự án thể hiện khả năng kết hợp Clean Architecture, MVVM, Firebase serverless, R2 object storage và Jetpack Compose để tạo một sản phẩm mobile-first có tính thực tiễn cho nhóm nhỏ.

Hướng phát triển tiếp theo:

- Thêm FCM push notification.
- Thêm group conversation và đính kèm media trong message.
- Thêm offline-first với Room.
- Mở rộng dashboard thống kê cho chủ idea.
- Tự động hóa UI test/instrumented test trên emulator.

## 8. Tài Liệu Tham Khảo

1. Android Developers Documentation: https://developer.android.com/
2. Kotlin Documentation: https://kotlinlang.org/docs/
3. Jetpack Compose: https://developer.android.com/jetpack/compose
4. Firebase Authentication: https://firebase.google.com/docs/auth
5. Cloud Firestore: https://firebase.google.com/docs/firestore
6. Cloudflare R2: https://developers.cloudflare.com/r2/
7. Material Design 3: https://m3.material.io/

## Phụ Lục - Phiên Bản 1.0.1

Bản 1.0.1 là bản bảo trì sau 1.0.0, tập trung vào chất lượng đọc hiểu và bàn giao mã nguồn. Thay đổi chính gồm bổ sung comment giải thích cho Kotlin/XML không tạo sinh, giữ logic nghiệp vụ ổn định, rút gọn helper tính upvote theo biểu thức delta rõ hơn và cập nhật tài liệu phát hành.

Baseline kiểm thử của bản này gồm `compileDebugKotlin`, `testDebugUnitTest` và `assembleDebug`. Nhánh local dùng cho bản này là `hotfix/v1.0.1`, tag local là `v1.0.1`.
