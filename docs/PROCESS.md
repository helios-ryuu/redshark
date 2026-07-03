# PROCESS.md - Quy Trình Nghiệp Vụ Và Kiểm Thử RedShark

Tài liệu này là nguồn chính mô tả luồng nghiệp vụ, quy tắc kiểm thử và checklist nghiệm thu của RedShark.

## 1. Xác Thực Và Hồ Sơ

### 1.1 Đăng nhập Google

1. UI gọi Credential Manager để lấy Google ID token.
2. `FirebaseAuthSource.signInWithGoogle` đăng nhập Firebase Authentication.
3. `AuthRepositoryImpl` tạo/cập nhật `users/{uid}` trong Firestore.
4. App lưu `uid` và display name vào DataStore.
5. Nếu display name chưa hợp lệ, điều hướng sang Profile Setup; nếu hợp lệ, vào Home.

### 1.2 Đăng ký Email/Password

1. UI kiểm tra dữ liệu bắt buộc, confirm password và ngày sinh.
2. `SignUpEmailPasswordUseCase` validate display name, username, email, ngày sinh từ 13 tuổi trở lên và password tối thiểu 8 ký tự có chữ hoa + chữ số.
3. `CheckUsernameAvailabilityUseCase` chuẩn hóa username và query Firestore.
4. Firebase Auth tạo user bằng email/password.
5. Firestore ghi `users/{uid}` với email, displayName, username, dateOfBirth và authProvider.
6. Lỗi username/email/password/DOB hiển thị đúng field.

### 1.3 Đăng nhập Email/Password

1. UI gửi email/password.
2. Firebase Auth xác thực credential.
3. Repository đọc `users/{uid}` để lấy hồ sơ đầy đủ.
4. DataStore lưu session local, UI vào Home.

### 1.4 Hồ sơ

1. Profile đọc `users/{userId}`.
2. Chủ hồ sơ có thể sửa display name, bio, skills và avatar.
3. Avatar picker đọc bytes từ `ContentResolver`, use case kiểm tra mime/size và upload lên R2.
4. Firestore cập nhật `avatarUrl`.
5. Profile hiển thị contribution graph 12 tuần gần nhất từ idea/issue/comment của user.

## 2. Idea, Media, Issue, Comment

### 2.1 Tạo idea

1. Người dùng nhập title và description.
2. `CreateIdeaUseCase` kiểm tra title 3..120 ký tự, description tối đa 5000 ký tự.
3. Repository ghi `ideas/{uuid}` với authorId, status ACTIVE, collaboratorIds rỗng, mediaAttachments rỗng và timestamp server.
4. Home/My Ideas nhận snapshot và render `IdeaCard`.

### 2.2 Hiển thị tác giả

1. Idea/comment/issue chỉ lưu `authorId`.
2. ViewModel tải users qua `GetUsersUseCase`.
3. UI dùng `usersById[authorId]` để hiển thị displayName/avatar.
4. Nếu thiếu user, fallback sang UID rút gọn.

### 2.3 Media idea

1. Author/collaborator mở Idea Detail và chọn ảnh/video.
2. UI đọc bytes, mime type và tên file.
3. `UploadIdeaMediaUseCase` chấp nhận `image/jpeg`, `image/png`, `image/webp`, `video/mp4`, `video/webm`; ảnh tối đa 8MB, video tối đa 50MB.
4. `MediaRepository` upload lên R2 key `ideas/{ideaId}/{userId}-{uuid}.{ext}`.
5. `UpdateIdeaMediaUseCase` cập nhật `ideas/{ideaId}.mediaAttachments`.
6. Firestore Rules cho phép author sửa idea; collaborator chỉ cập nhật mediaAttachments và updatedAt.

### 2.4 Sửa, đổi trạng thái và xóa idea

1. Chỉ author thấy action sửa, xóa và đổi trạng thái.
2. Sửa title/description/tag không xóa media hiện có.
3. Đóng/hủy idea cập nhật status và updatedAt.
4. Xóa mềm đặt `deletedAt`; list/detail xử lý soft-deleted như không tồn tại.

### 2.5 Issue

1. Người dùng tạo issue trong idea ACTIVE.
2. Use case kiểm tra idea tồn tại, chưa soft-delete, status ACTIVE và user chưa vượt 20 issue active.
3. Repository ghi issue với status OPEN và priority đã chọn.
4. Nếu người tạo khác author idea, tạo notification `ISSUE_CREATED`.
5. State machine hợp lệ: `OPEN -> IN_PROGRESS/CANCELLED`, `IN_PROGRESS -> CLOSED`; CLOSED/CANCELLED là terminal.

### 2.6 Comment

1. UI thêm optimistic comment bằng local UUID.
2. `CreateCommentUseCase` kiểm tra nội dung 1..1000 ký tự.
3. Firestore ghi comment với authorId và createdAt.
4. Snapshot server thay optimistic item.
5. Nếu lỗi, UI rollback và hiển thị lỗi.
6. Nếu người bình luận khác author idea, tạo notification `COMMENT`.

## 3. Thông Báo Và Cộng Tác

### 3.1 Gửi yêu cầu cộng tác

1. Non-author trên idea ACTIVE nhấn “Xin tham gia”.
2. Use case chặn author và user đã là collaborator.
3. Repository tạo notification `COLLAB_REQUEST` cho author idea.

### 3.2 Chấp nhận/từ chối

1. Author mở notification sheet.
2. Accept thêm actorId vào collaboratorIds, đánh dấu notification đã đọc, tạo direct conversation nếu cần và tạo notification accepted.
3. Reject đánh dấu request và tạo notification rejected, không đổi collaboratorIds.

## 4. Tin Nhắn Và Chia Sẻ Idea

### 4.1 Direct conversation

1. Người dùng tạo hội thoại bằng email hoặc từ profile/user target.
2. ViewModel tìm user trong users list.
3. `FindOrCreateDirectConversationUseCase` tìm conversation cũ trước.
4. Conversation mới dùng deterministic UUID từ cặp UID đã sort để hạn chế tạo trùng.
5. Conversation cũ có UUID random vẫn đọc được để không cần migration.

### 4.2 Gửi message

1. UI gửi content 1..2000 ký tự.
2. Repository ghi `messages/{uuid}`.
3. Repository cập nhật `conversations/{conversationId}` gồm lastMessageAt, preview, lastMessageSenderId, hasUnread.
4. Conversation list sort theo lastMessageAt và hiển thị unread badge cho người nhận.
5. Mở thread sẽ mark conversation read.

### 4.3 Chia sẻ idea nhiều người nhận

1. Người dùng nhấn share trên `IdeaCard`.
2. App tạo nội dung gồm title, mô tả nếu có và deep link `redshark://idea/{id}`.
3. Bottom sheet share hiển thị conversation hiện có và toàn bộ user searchable.
4. User đã có conversation được dedupe thành một target.
5. Người dùng chọn nhiều target rồi nhấn gửi.
6. `ShareMessageToRecipientsUseCase` bỏ current user, dedupe recipient, tìm/tạo direct conversation và gửi message.
7. Nếu gửi thành công toàn bộ, sheet đóng. Nếu lỗi một phần, các target lỗi vẫn được chọn để retry.
8. Nhấn deep link trong message mở đúng Idea Detail.

## 5. Contribution Graph

1. Profile gọi `GetUserContributionGraphUseCase(userId, weeks = 12)`.
2. `ContributionRepositoryImpl` query `ideas`, `issues`, `comments` theo authorId và createdAt.
3. Idea/issue soft-deleted không được tính.
4. `ContributionSummaryBuilder` gom event theo local date trong 84 ngày gần nhất.
5. Mỗi ngày có count và level 0..4.
6. UI render graph 12 cột tuần, 7 hàng ngày, kèm tổng số hoạt động và số ngày có đóng góp.

## 6. Deploy Và Reset Dữ Liệu

1. Chạy build/test trước deploy.
2. Deploy Firestore Rules và Indexes:

```powershell
firebase deploy --only firestore:rules,firestore:indexes
```

3. Kiểm tra reset production bằng dry-run:

```powershell
python scripts/reset_production.py dry-run --project redshark-application
```

4. Reset có xác nhận:

```powershell
python scripts/reset_production.py reset --project redshark-application --confirm REDSHARK_PRODUCTION_RESET
```

5. Verify sau reset:

```powershell
python scripts/reset_production.py verify --project redshark-application
```

Không chạy seed script trên production.

## 7. Lệnh Kiểm Thử Bắt Buộc

Chạy từ thư mục gốc:

```powershell
.\gradlew.bat compileDebugKotlin
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

Nếu thay đổi Firestore Rules/Indexes:

```powershell
firebase emulators:start --only firestore
firebase deploy --only firestore:rules,firestore:indexes
```

## 8. Checklist Regression

### Auth/Profile

| ID | Kịch bản | Kết quả mong đợi |
|---|---|---|
| AUTH-01 | Mở app khi chưa đăng nhập | Vào auth flow, không vào Home |
| AUTH-02 | Đăng nhập Google lần đầu | Tạo/upsert user, nếu thiếu name thì vào setup |
| AUTH-03 | Đăng ký email hợp lệ | Tạo Auth user, ghi Firestore, vào Home |
| AUTH-04 | Email/username/password/DOB sai | Hiển thị lỗi đúng field |
| AUTH-05 | Đăng xuất | Xóa session local, quay về auth |
| PROFILE-01 | Xem profile | Hiển thị name/email/avatar/bio/skills |
| PROFILE-02 | Sửa profile/avatar | Lưu thành công và refresh UI |
| PROFILE-03 | Contribution graph | Hiển thị 12 tuần, tổng activity và active days |

### Content

| ID | Kịch bản | Kết quả mong đợi |
|---|---|---|
| CONTENT-01 | Tạo idea hợp lệ | Idea xuất hiện trên Home/My Ideas |
| CONTENT-02 | Tạo idea invalid | Hiển thị validation, không ghi Firestore |
| CONTENT-03 | Sửa/xóa mềm idea | Update thành công, item xóa không còn trên list |
| CONTENT-04 | Upload media hợp lệ | R2 upload, detail render attachment |
| CONTENT-05 | Upload media sai mime/size | Bị chặn, không ghi metadata |
| CONTENT-06 | Tạo issue trên ACTIVE idea | Issue xuất hiện trong detail |
| CONTENT-07 | Tạo issue trên CLOSED/CANCELLED idea | Bị chặn |
| CONTENT-08 | Quá 20 issue active/user | Bị chặn |
| CONTENT-09 | Đổi trạng thái issue hợp lệ | Cập nhật đúng state machine |
| CONTENT-10 | Gửi comment hợp lệ | Optimistic item xuất hiện, server snapshot thay thế |

### Interaction

| ID | Kịch bản | Kết quả mong đợi |
|---|---|---|
| INT-01 | Có notification mới | Badge unread tăng |
| INT-02 | Accept/reject collab | Collaborator/notification cập nhật đúng |
| INT-03 | Tạo direct conversation | Không tạo trùng cho cùng cặp UID |
| INT-04 | Gửi/nhận message | Thread và preview cập nhật |
| INT-05 | Share idea nhiều người | Gửi tới nhiều target, tự tạo conversation khi cần |
| INT-06 | Share lỗi một phần | Target lỗi vẫn được chọn để retry |
| INT-07 | Mở deep link idea | Điều hướng tới đúng Idea Detail |

### UI/UX và NFR

| ID | Kịch bản | Kết quả mong đợi |
|---|---|---|
| UX-01 | Home/My Ideas/Messages/Profile | Loading/error/empty/content rõ ràng |
| UX-02 | FAB/input bar/bottom sheet | Không che nội dung cuối, không overflow trên màn nhỏ |
| UX-03 | Search/filter | Không tràn layout, empty state đúng |
| UX-04 | Offline/network error | Không crash, có retry hoặc thông báo lỗi |
| UX-05 | Recompose/orientation | State quan trọng không mất bất thường |
| NFR-01 | Cold start đã login | Mục tiêu không quá 3 giây trên thiết bị test |
| NFR-02 | Scroll feed 50 item | Không query user per item, không giật rõ |
| NFR-03 | Debug/release build | Build thành công, không hardcode secret |

## 9. Tiêu Chí Hoàn Thành

- `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug` pass.
- Không còn link tới tài liệu đã xóa.
- Không còn tính năng UI giả hoặc chưa nối logic.
- Firestore rules/indexes đồng bộ với query đang dùng.
- Manual regression các mục liên quan thay đổi mới được chạy trước khi demo/release.

## 10. Ghi Chú Phiên Bản 1.0.0

- Đăng ký email/password phải phân biệt rõ username đã tồn tại với lỗi không kiểm tra được username.
- Contribution graph phải hiển thị được trạng thái rỗng hợp lệ, không quy mọi lỗi query thành lỗi biểu đồ thường trực.
- Notification đã đọc được cập nhật `isRead = true`; chỉ thao tác `deleteAll` mới xóa document.
- My Ideas phải phản ánh snapshot mới nhất, bao gồm trường hợp idea bị soft-delete hoặc không còn thuộc author/collaborator query.
- Profile update phải trả lỗi thật khi Firestore update thất bại và phải cho phép xóa bio bằng giá trị rỗng.
- Baseline 1.0.0 yêu cầu `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug` pass trước khi tag.