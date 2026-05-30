# DELIVERABLE.md - Nội Dung Bàn Giao RedShark

## 1. Slide

Tổng số slide đề xuất: 22.

### Slide 1 - Bìa

- RedShark Android Native
- Ứng dụng quản lý ý tưởng, công việc và cộng tác nhóm nhỏ
- Môn học NT118.Q22 - Lập trình ứng dụng di động
- Nhóm: Sỹ, Nam, Hải

### Slide 2 - Thành viên

- Ngô Tiến Sỹ: PM, Android, auth/profile, kiểm thử, tài liệu
- Phạm Tuấn Hải: Android, idea/media/issue/comment/collab
- Nguyễn Văn Nam: Android, notification/message/UI phối hợp

### Slide 3 - Bối cảnh

- Nhóm sinh viên cần công cụ nhẹ để ghi nhận ý tưởng và chia việc.
- Nhiều công cụ hiện có thiên về web hoặc quy trình phức tạp.
- Mobile-first giúp cập nhật nhanh khi đang làm việc nhóm.

### Slide 4 - Vấn đề cần giải quyết

- Ý tưởng, công việc, bình luận và trao đổi thường nằm rời rạc.
- Khó biết ai đang đóng góp gì trong thời gian gần đây.
- Chia sẻ idea vào cuộc trò chuyện cần nhanh và đúng người nhận.

### Slide 5 - Mục tiêu

- Xây dựng app Android native hoàn chỉnh.
- Tích hợp Firebase Auth, Firestore, Cloudflare R2.
- Có luồng cộng tác thật: idea, issue, comment, notification, message.
- Có share idea nhiều người và contribution graph.

### Slide 6 - Người dùng mục tiêu

- Sinh viên làm đồ án nhóm.
- Nhóm dự án nhỏ cần quản lý task nhẹ.
- Người muốn theo dõi lịch sử đóng góp ngay trên điện thoại.

### Slide 7 - Kiến trúc tổng quan

- UI: Jetpack Compose + Material 3.
- Domain: model, repository interface, use case.
- Data: Firebase/Firestore/R2 repository implementation.
- Core: DI, error, network, result.

### Slide 8 - Công nghệ chính

- Kotlin, Jetpack Compose, Hilt.
- Coroutines, Flow, DataStore.
- Firebase Authentication, Cloud Firestore.
- Cloudflare R2, OkHttp, Coil.
- JUnit, MockK, Gradle.

### Slide 9 - Schema dữ liệu

- `users`: hồ sơ người dùng.
- `ideas`: ý tưởng, trạng thái, media, reaction.
- `issues`: công việc, priority, state machine.
- `comments`: thảo luận theo idea.
- `notifications`: thông báo cộng tác/comment/issue.
- `conversations`, `messages`: nhắn tin 1-1.

### Slide 10 - Xác thực và hồ sơ

- Google Sign-In.
- Email/Password với validate username, email, password, ngày sinh.
- Hoàn thiện hồ sơ lần đầu.
- Avatar upload lên R2.
- Bio, skills, contribution graph.

### Slide 11 - Idea

- Tạo, xem, sửa, xóa mềm idea.
- Status ACTIVE/CLOSED/CANCELLED.
- Upvote/downvote.
- Author/collaborator hiển thị bằng profile thật.

### Slide 12 - Media

- Upload ảnh/video cho idea.
- Kiểm tra mime type và size.
- File binary lưu trên Cloudflare R2.
- Firestore chỉ lưu metadata attachment.

### Slide 13 - Issue

- Tạo/sửa/xóa mềm issue trong idea.
- Priority LOW/MEDIUM/HIGH.
- Assignee tùy chọn.
- State machine kiểm soát trạng thái.
- Giới hạn 20 issue active mỗi user.

### Slide 14 - Comment và Notification

- Comment realtime theo idea.
- Optimistic update khi gửi comment.
- Notification unread badge.
- Collab request, accept, reject.
- Notification comment và issue created.

### Slide 15 - Message

- Direct conversation 1-1.
- Tìm cuộc trò chuyện.
- Preview tin nhắn cuối.
- Unread badge.
- Deep link mở Idea Detail từ message.

### Slide 16 - Tính năng mới: Share Idea

- Nút share trên idea.
- Bottom sheet hiển thị conversation hiện có và user có thể tìm.
- Chọn nhiều người nhận.
- Tự tạo direct conversation khi chưa có.
- Gửi text gồm title, mô tả và deep link.
- Retry riêng các người nhận lỗi.

### Slide 17 - Tính năng mới: Contribution Graph

- Đặt trên Profile.
- Hiển thị 12 tuần gần nhất.
- Tính từ idea, issue và comment do user tạo.
- Bỏ idea/issue đã soft-delete.
- Màu theo intensity giống contribution calendar.

### Slide 18 - UI/UX hoàn thiện

- Thống nhất Material 3 và RedShark theme.
- Loading/error/empty/content state rõ ràng.
- Bottom sheet cho notification, comment, share.
- Token hóa spacing, size, elevation.
- Dọn các phần UI placeholder/decorative không phục vụ workflow.

### Slide 19 - Bảo mật và phân quyền

- Firebase Auth làm nguồn định danh.
- Firestore Rules kiểm soát owner/participant/collaborator.
- Secret nằm trong `local.properties`, không commit.
- R2 credential đi qua BuildConfig.
- Soft delete tránh mất dữ liệu đột ngột.

### Slide 20 - Kiểm thử

- `compileDebugKotlin`: PASS.
- `testDebugUnitTest`: PASS.
- Unit test cho auth, profile, idea media, message, notification.
- Unit test mới cho share multi-recipient và contribution bucketing.
- Manual regression checklist trong `PROCESS.md`.

### Slide 21 - Kết luận

- RedShark đáp ứng bài toán cộng tác nhóm nhỏ trên Android.
- Kiến trúc rõ ràng, có thể mở rộng.
- Tính năng share và graph làm app thực tế hơn cho demo và sử dụng thật.

### Slide 22 - Kết thúc

- Cảm ơn thầy/cô và các bạn đã lắng nghe.
- Q&A.

## 2. Demo

Trình tự demo toàn bộ dự án:

1. Mở app ở trạng thái chưa đăng nhập.
2. Đăng ký Email/Password với dữ liệu hợp lệ hoặc đăng nhập Google.
3. Vào Home, giới thiệu bottom navigation và drawer.
4. Mở Profile, sửa bio/skills, xem contribution graph.
5. Tạo một idea mới với title/description.
6. Vào Idea Detail, upvote/downvote, thêm comment.
7. Upload một ảnh hoặc video hợp lệ vào idea.
8. Tạo issue trong idea, chọn priority và assignee nếu có.
9. Mở Issue Detail, đổi trạng thái từ OPEN sang IN_PROGRESS, sau đó CLOSED.
10. Đăng nhập hoặc dùng tài khoản thứ hai để gửi request cộng tác.
11. Ở tài khoản author, mở notification sheet và accept/reject request.
12. Mở Messages, tạo direct conversation bằng email.
13. Gửi một tin nhắn thường.
14. Quay lại Home/My Ideas, nhấn share trên idea.
15. Search người nhận, chọn nhiều user/conversation, nhấn gửi.
16. Mở conversation nhận share, nhấn deep link idea.
17. Mở Settings, thử nhập UUID/deep link idea nếu cần.
18. Đăng xuất.
19. Nếu có thời gian, chạy nhanh lệnh test/build hoặc trình bày kết quả pass.

## 3. Tính Năng

- Đăng nhập Google.
- Đăng ký và đăng nhập Email/Password.
- Kiểm tra username, email, password, ngày sinh.
- Hoàn thiện hồ sơ lần đầu.
- Xem/sửa hồ sơ.
- Upload avatar lên Cloudflare R2.
- Hiển thị contribution graph trên Profile.
- Tạo/xem/sửa/xóa mềm idea.
- Đổi trạng thái idea.
- Upvote/downvote idea.
- Hiển thị tác giả bằng dữ liệu user thật.
- Upload ảnh/video cho idea.
- Tạo/xem/sửa/xóa mềm issue.
- Gán priority và assignee cho issue.
- Đổi trạng thái issue theo state machine.
- Giới hạn 20 issue active mỗi user.
- Gửi/xem comment realtime.
- Optimistic comment update.
- Tạo notification khi có comment/issue/collab.
- Badge unread notification.
- Accept/reject collab request.
- Tạo direct conversation.
- Xem danh sách conversation và unread badge.
- Gửi/nhận message.
- Share idea qua deep link.
- Share idea cho nhiều người nhận trong một lần gửi.
- Tự tạo conversation khi share tới user chưa có chat.
- Deep link `redshark://idea/{id}` mở Idea Detail.
- Reset/verify production trắng dữ liệu bằng script.

## 4. Công Nghệ

- Kotlin.
- Android Native.
- Jetpack Compose.
- Material 3.
- Android Navigation Compose.
- Hilt Dependency Injection.
- Kotlin Coroutines.
- Flow/StateFlow.
- DataStore Preferences.
- Firebase Authentication.
- Cloud Firestore.
- Firestore Security Rules.
- Firestore Composite Indexes.
- Cloudflare R2.
- OkHttp.
- Coil.
- Gradle Kotlin DSL.
- JUnit.
- MockK.
- kotlinx-coroutines-test.
- Firebase CLI.
- Python script cho reset/verify dữ liệu.

## 5. Tính Chất

### 5.1 Tính phức tạp

RedShark không chỉ là app CRUD đơn giản. Ứng dụng có nhiều nhóm nghiệp vụ liên kết với nhau: idea tạo ra issue/comment/notification, collab ảnh hưởng quyền upload media, message nhận deep link idea, contribution graph tổng hợp dữ liệu từ nhiều collection. Các luồng này đòi hỏi xử lý state, quyền truy cập và lỗi bất đồng bộ nhất quán.

### 5.2 Độ khó kỹ thuật

- Tích hợp nhiều dịch vụ backend serverless trong một app Android native.
- Xử lý realtime snapshot của Firestore bằng Flow.
- Thiết kế rules cho owner, collaborator và conversation participant.
- Upload file lên R2 bằng S3-compatible API.
- Xây dựng UI Compose có loading/error/empty/content state rõ ràng.
- Tạo share multi-recipient có dedupe, partial failure và retry.
- Tính contribution graph từ nhiều nguồn dữ liệu không có bảng analytics riêng.
- Giữ kiến trúc Clean Architecture để use case có thể unit test.

### 5.3 Tính thực tiễn

Ứng dụng phù hợp với nhóm sinh viên hoặc nhóm dự án nhỏ cần một công cụ nhẹ để làm việc hằng ngày. Các tính năng như direct message, share idea và contribution graph giúp app gần với nhu cầu sử dụng thật hơn: trao đổi nhanh, dẫn người khác tới đúng nội dung, và nhìn được mức độ tham gia của từng thành viên.

### 5.4 Khả năng mở rộng

Kiến trúc hiện tại có thể mở rộng thêm FCM push notification, group conversation, media message, dashboard thống kê nâng cao, offline-first với Room hoặc bản iOS/Kotlin Multiplatform mà không phá vỡ domain model chính.

## 6. Kết Luận

RedShark hoàn thiện mục tiêu xây dựng một ứng dụng Android native có tính thực tiễn, kiến trúc rõ ràng và đủ các luồng cộng tác quan trọng. Dự án chứng minh khả năng áp dụng Kotlin, Jetpack Compose, Firebase và Cloudflare R2 để tạo sản phẩm mobile-first cho nhóm nhỏ.

Điểm nổi bật của phiên bản cuối là UX được thống nhất hơn, không còn tính năng dạng placeholder, có share idea nhiều người nhận và contribution graph cá nhân. Đây là nền tảng tốt để tiếp tục phát triển thành công cụ cộng tác hoàn chỉnh hơn trong tương lai.
