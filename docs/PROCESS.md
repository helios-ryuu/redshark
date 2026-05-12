# PROCESS.md - Quy trình tính năng RedShark

Tài liệu này là nguồn chính mô tả các luồng nghiệp vụ hiện tại của RedShark.

## 1. Xác thực và hồ sơ

### Đăng nhập Google
1. Giao diện gọi Credential Manager để lấy Google ID token.
2. `FirebaseAuthSource.signInWithGoogle` đăng nhập Firebase Authentication.
3. `AuthRepositoryImpl` tạo/cập nhật `users/{uid}` với email và tên hiển thị.
4. Ứng dụng lưu `uid` và tên hiển thị vào DataStore.
5. Nếu tên hiển thị chưa hợp lệ, điều hướng sang màn hoàn thiện hồ sơ; nếu hợp lệ, vào Home.

### Đăng ký Email/Password
1. Giao diện kiểm tra dữ liệu bắt buộc, mật khẩu xác nhận và ngày sinh.
2. `SignUpEmailPasswordUseCase` kiểm tra displayName, username, email, ngày sinh từ 13 tuổi trở lên và mật khẩu tối thiểu 8 ký tự có chữ hoa + chữ số.
3. `CheckUsernameAvailabilityUseCase` chuẩn hóa username, kiểm tra regex `[a-z0-9._-]` và query Firestore khi hợp lệ.
4. Firebase Authentication tạo user bằng email/password.
5. Firestore ghi `users/{uid}` với `email`, `displayName`, `username`, `dateOfBirth`, `authProvider = EMAIL`.
6. Username trùng hiển thị lỗi ở field username; email trùng hiển thị lỗi ở field email.

### Đăng nhập Email/Password
1. Giao diện gửi email/password.
2. Firebase Authentication xác thực credential.
3. Repository đọc `users/{uid}` để lấy hồ sơ đầy đủ.
4. DataStore lưu session local, giao diện vào Home.

### Hồ sơ và ảnh đại diện
1. Màn Profile đọc `users/{userId}`.
2. Chủ hồ sơ được sửa tên hiển thị, giới thiệu và kỹ năng.
3. Avatar picker đọc file từ `ContentResolver`, kiểm tra image, tải lên R2 tại `avatars/{uid}.{ext}`.
4. Firestore cập nhật `avatarUrl`.

## 2. Ý tưởng và media

### Tạo ý tưởng
1. Người dùng nhập tiêu đề và mô tả.
2. `CreateIdeaUseCase` kiểm tra tiêu đề 3..120 ký tự, mô tả tối đa 5000 ký tự.
3. Repository ghi `ideas/{uuid}` với `authorId = auth.uid`, `status = ACTIVE`, `collaboratorIds = []`, `mediaAttachments = []` và timestamp server.
4. Home/My Ideas nhận snapshot và render card.

### Hiển thị tác giả
1. Idea/Comment chỉ lưu `authorId`.
2. ViewModel tải danh sách users qua `GetUsersUseCase`, tạo `usersById`.
3. UI dùng `usersById[authorId]` để hiển thị displayName/avatar; fallback là 8 ký tự đầu UID.
4. Không denormalize display name vào idea/comment để tránh dữ liệu cũ.

### Tải media cho ý tưởng
1. Tác giả hoặc cộng tác viên mở Idea Detail và chọn ảnh/video.
2. UI đọc bytes và mime type từ `ContentResolver`.
3. `UploadIdeaMediaUseCase` chấp nhận `image/jpeg`, `image/png`, `image/webp`, `video/mp4`, `video/webm`; giới hạn ảnh 8MB, video 50MB.
4. `MediaRepository` tải lên R2 key `ideas/{ideaId}/{userId}-{uuid}.{ext}`.
5. `UpdateIdeaMediaUseCase` cập nhật `ideas/{ideaId}.mediaAttachments`.
6. Firestore Rules cho phép author sửa toàn bộ idea; collaborator chỉ được đổi `mediaAttachments` và `updatedAt`.

### Sửa/xóa ý tưởng
1. Chỉ author thấy nút sửa, xóa và đổi trạng thái.
2. Sửa tiêu đề/mô tả/tag không xóa media hiện có.
3. Xóa mềm đặt `deletedAt`; list query/mapper loại document đã xóa.

## 3. Công việc và bình luận

### Tạo công việc
1. Người dùng tạo issue trong idea đang ACTIVE.
2. Use case kiểm tra idea tồn tại, đang ACTIVE và user chưa vượt 20 issue active.
3. Repository ghi issue với `authorId = auth.uid`, assignee tùy chọn, status mặc định OPEN.
4. Nếu người tạo khác author idea, tạo notification `ISSUE_CREATED`.

### Chuyển trạng thái công việc
1. UI gửi status mới.
2. Use case kiểm tra state machine.
3. Repository cập nhật status và `updatedAt`.
4. Author hoặc assignee được sửa theo Firestore Rules.

### Bình luận
1. UI thêm optimistic comment bằng local UUID.
2. `CreateCommentUseCase` kiểm tra nội dung 1..1000 ký tự và ghi Firestore.
3. Snapshot comments thay optimistic item bằng dữ liệu server.
4. Nếu lỗi, UI rollback snapshot cũ và hiển thị lỗi.
5. Nếu người bình luận khác author idea, tạo notification `COMMENT`.

## 4. Thông báo và cộng tác

### Gửi yêu cầu cộng tác
1. Non-author trên idea ACTIVE nhấn “Xin tham gia”.
2. Use case chặn nếu user đã là collaborator hoặc là author.
3. Repository tạo notification `COLLAB_REQUEST` cho author idea.

### Chấp nhận/từ chối yêu cầu
1. Author đọc notification trong sheet.
2. Accept thêm `actorId` vào `idea.collaboratorIds` bằng `arrayUnion`, đánh dấu notification đã đọc và tạo notification accepted.
3. Reject tạo notification rejected, không đổi collaborators.

## 5. Tin nhắn và chia sẻ

### Tạo hội thoại direct
1. Người dùng nhập email người nhận.
2. ViewModel tìm user qua users list.
3. Repository tạo conversation deterministic cho cặp UID nếu chưa có.
4. Nếu conversation đã tồn tại, mở conversation cũ.

### Gửi tin nhắn
1. UI gửi content 1..2000 ký tự.
2. Repository ghi `messages` và cập nhật preview/time của `conversations`.
3. Conversation list sắp xếp theo `lastMessageAt`.
4. Unread badge tính theo `hasUnread` và `lastMessageSenderId != currentUserId`.

### Chia sẻ ý tưởng
1. Người dùng nhấn share trên card/detail.
2. App tạo text gồm title, description nếu có và deep link `redshark://idea/{id}`.
3. Share sheet trong app gửi text vào direct conversation.
4. Deep link mở Idea Detail đúng ID.

## 6. Deploy và reset production

1. Chạy build/test trước khi deploy.
2. Deploy Firestore Rules và Indexes bằng Firebase CLI.
3. Chạy `scripts/reset_production.py dry-run` để kiểm tra số document/user/object sẽ xóa.
4. Chỉ chạy reset với `--confirm REDSHARK_PRODUCTION_RESET`.
5. Sau reset, chạy verify để đảm bảo Firestore, Firebase Auth và R2 đều trắng dữ liệu.
