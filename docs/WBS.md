# WBS.md — Cấu trúc phân rã công việc dự án RedShark Android Native

## 1. Mục đích và phạm vi

Tài liệu này tổng hợp toàn bộ kế hoạch triển khai từ các tài liệu trong thư mục `docs/`, nhằm:
- Phân rã công việc theo cấu trúc phân cấp (1.0, 1.1, 1.1.1...).
- Xác định rõ sản phẩm bàn giao, người phụ trách, phụ thuộc và mốc thời gian.
- Đồng bộ với quyết định hiện tại của dự án:
  - Xác thực hỗ trợ Google Sign-In và Email/Password (Firebase Auth).
  - Luồng **hoàn thiện hồ sơ lần đầu** bắt buộc `displayName` hợp lệ (3..50 ký tự).
  - Chưa triển khai CI bắt buộc ở giai đoạn hiện tại (duy trì kiểm tra biên dịch cục bộ).
  - Không sử dụng thư mục `.github/` trong cấu trúc hiện tại.
  - Biến cấu hình nhạy cảm được tối giản theo `SECRET.md`.

## 2. Cơ sở lập WBS

WBS được xây dựng dựa trên các nhóm tài liệu sau:
- Định hướng dự án: `PROJECT_CHARTER.md`.
- Kế hoạch thực thi cuối: `PLAN-1-AUTH.md`, `PLAN-2-CONTENT.md`, `PLAN-3-INTERACTION.md`, `PLAN-4-FINALIZE.md`.
- Quy trình nghiệp vụ: `PROCESS-1-AUTH.md` đến `PROCESS-3-INTERACTION.md`.
- Tiêu chuẩn nghiệm thu: `CHECK-1-AUTH.md` đến `CHECK-3-INTERACTION.md`.
- Ràng buộc kỹ thuật: `SCHEMA.md`, `SECRET.md`, `GIT.md`, `REPORT.md`, `CLAUDE.md`.

## 3. Mốc hiện tại (hiện trạng)

- **Ngày cập nhật:** 30/04/2026.
- **Trạng thái hiện tại:** WBS 3.0 (Auth) ✅, WBS 4.0 (Content) ✅, WBS 5.0 (Tương tác) ✅, WBS 4A.0 (UI Overhaul) ✅ hoàn thành. Đang trong giai đoạn dọn dẹp docs + chuẩn bị thực thi PLAN-1..4 để vào WBS 6.0 (Kiểm thử/NFR) và WBS 7.0 (Phát hành).
- **Người thực hiện commit init:** **Sỹ**.
- **Commit init chuẩn:** `chore(init): bootstrap Android project and project documents`.

### Lịch thực thi còn lại

```
Tuần 7 ─ 28/04 → 04/05 │ PLAN-1-AUTH (Sỹ) + PLAN-2-CONTENT (Hải) song song
                        │ PLAN-3-INTERACTION (Nam) — hard deadline 18:00 ngày 03/05
Tuần 8 ─ 05/05 → 11/05 │ PLAN-4-FINALIZE (Sỹ): email/password auth + NFR + release prep
Tuần 9 ─ 12/05 → 17/05 │ Kiểm thử hồi quy + phát hành v1.0.0
```

**Phân công Tuần 7 (28/04–04/05):**

| Thành viên | Công việc |
|---|---|
| **Sỹ** | PLAN-1-AUTH: rà soát + commit UI auth/profile; manual test CHECK-1-AUTH |
| **Hải** | PLAN-2-CONTENT: rà soát + commit UI content; manual test CHECK-2-CONTENT |
| **Nam** | PLAN-3-INTERACTION: commit UI interaction; manual test CHECK-3-INTERACTION TC-N/M (xong trước 18:00 03/05) |

**Phân công Tuần 8 (05/05–11/05):**

| Thành viên | Công việc |
|---|---|
| **Sỹ** | PLAN-4-FINALIZE Phần 1: implement email/password auth (domain + data + UI) |
| **Hải** | Phối hợp review + test tính năng email-register mới |
| **Nam** | Phối hợp NFR: dark mode, rotation, back nav |

**Phân công Tuần 9 (12/05–17/05):**

| Ngày | Thành viên | Công việc |
|---|---|---|
| 12–14/05 | Cả nhóm | Kiểm thử hồi quy CHECK-1/2/3 trên 3 thiết bị; sửa lỗi P0/P1 |
| 15/05 | Sỹ + Nam | PLAN-4-FINALIZE Phần 2: NFR TC-X01..X07 + dọn kỹ thuật |
| 16/05 | Sỹ + Hải | Release hardening, signed APK, đo hiệu năng, cập nhật REPORT |
| 17/05 | Cả nhóm | Smoke test bản phát hành, git tag v1.0.0, nộp bài |

**Vùng đệm rủi ro:**
- Giai đoạn Content hoàn thành sớm 20/04; WBS 5.0 implement xong 25/04 — tận dụng buffer cho NFR và email-auth.
- Mỗi giai đoạn có tối thiểu 1 ngày dự phòng để sửa lỗi.
- Nếu PLAN-3 trễ deadline 03/05: PLAN-4 vẫn có thể bắt đầu Phần 1 (email-auth) độc lập; chỉ cần PLAN-3 merge trước khi vào Phần 2 (NFR full regression).

## 4. Cấu trúc phân rã công việc (WBS)

## 1.0 Quản trị dự án và kiểm soát tiến độ

### 1.1 Khởi tạo dự án
#### 1.1.1 Xác lập mục tiêu, phạm vi, rủi ro
- Sản phẩm bàn giao: `PROJECT_CHARTER.md`.
- Phụ trách chính: Sỹ (PM).
- Phụ thuộc: Không.
- Thời gian: 16/03 - 20/03.

#### 1.1.2 Thiết lập tiến độ và mốc chính
- Sản phẩm bàn giao: `TIMELINE.md` (mốc bắt buộc có commit tính năng xác thực trước 21/04).
- Phụ trách chính: Sỹ.
- Phụ thuộc: 1.1.1.
- Thời gian: 21/03 - 24/03.

#### 1.1.3 Khởi tạo kho mã nguồn và quy ước Git
- Sản phẩm bàn giao: `GIT.md`, nhánh `main/develop/feature/*/release/*/hotfix/*`.
- Phụ trách chính: Sỹ.
- Phụ thuộc: 1.1.2.
- Thời gian: 25/03 - 29/03.
- Ghi chú: Không dùng `.github/` trong giai đoạn hiện tại.

### 1.2 Theo dõi tiến độ và báo cáo
#### 1.2.1 Rà soát tiến độ theo tuần
- Sản phẩm bàn giao: nhật ký tiến độ nội bộ, cập nhật timeline khi cần.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam, Hải.
- Phụ thuộc: 1.1.*.
- Thời gian: 16/03 - 17/05 (xuyên suốt).

#### 1.2.2 Tổng hợp báo cáo cuối kỳ
- Sản phẩm bàn giao: `REPORT.md`, slide demo.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam, Hải.
- Phụ thuộc: 2.0 - 7.0.
- Thời gian: 12/05 - 17/05.

## 2.0 Nền tảng kỹ thuật (Foundation)

### 2.1 Thiết lập dự án Android Kotlin
#### 2.1.1 Khởi tạo khung dự án Android Native
- Sản phẩm bàn giao: module `app/`, Gradle Kotlin DSL, Compose, Hilt cơ bản.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 1.1.*.
- Thời gian: 30/03 - 07/04.

#### 2.1.2 Cấu hình kiểm tra biên dịch cục bộ
- Sản phẩm bàn giao: quy trình chạy `lint`, `testDebugUnitTest`, `assembleDebug` tại máy cục bộ.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 2.1.1.
- Thời gian: 08/04 - 12/04.
- Ghi chú: Chưa bắt buộc CI ở giai đoạn này.

### 2.2 Tích hợp hạ tầng backend và storage
#### 2.2.1 Tích hợp Firebase Auth và Firestore
- Sản phẩm bàn giao: kết nối Firestore, kiểm tra nhanh truy vấn.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 2.1.1.
- Thời gian: 08/04 - 13/04.

#### 2.2.2 Tích hợp Cloudflare R2 cho media
- Sản phẩm bàn giao: `R2Client` + upload thử nghiệm thành công.
- Phụ trách chính: Sỹ.
- Phụ thuộc: 2.1.1.
- Thời gian: 09/04 - 13/04.

### 2.3 Chuẩn hóa kiến trúc và tài liệu nền
#### 2.3.1 Chuẩn hóa cấu trúc thư mục, layer kiến trúc
- Sản phẩm bàn giao: `STRUCTURE.md` nhất quán với mã nguồn.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 2.1.1.
- Thời gian: 10/04 - 14/04.

#### 2.3.2 Chuẩn hóa quản lý cấu hình nhạy cảm
- Sản phẩm bàn giao: `SECRET.md` tối giản biến còn sử dụng.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 2.2.*.
- Thời gian: 11/04 - 15/04.

## 3.0 Nhóm chức năng Xác thực và Hồ sơ

- Phụ trách nhóm chức năng: **Sỹ** (code chính và chịu trách nhiệm commit chính).

### 3.1 Luồng xác thực Google-only
#### 3.1.1 Xây dựng giao diện và điều hướng Auth
- Sản phẩm bàn giao: `GoogleSignInScreen`, trạng thái Auth, điều hướng vào app.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 2.1.*, 2.2.1.
- Thời gian: 16/04 - 20/04.

#### 3.1.2 Cài đặt nguồn dữ liệu và repository Auth
- Sản phẩm bàn giao: `FirebaseAuthSource`, `AuthRepositoryImpl`, use case đăng nhập/đăng xuất/persist.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 2.2.1.
- Thời gian: 16/04 - 20/04.

### 3.2 Hoàn thiện hồ sơ lần đầu
#### 3.2.1 Thiết kế luồng `profile/setup`
- Sản phẩm bàn giao: `ProfileSetupScreen`.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 3.1.1.
- Thời gian: 17/04 - 20/04.

#### 3.2.2 Ràng buộc nghiệp vụ `displayName` 3..50
- Sản phẩm bàn giao: validate client + use case `CompleteFirstProfileUseCase`.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 3.1.2.
- Thời gian: 17/04 - 20/04.

### 3.3 Hồ sơ người dùng và avatar
#### 3.3.1 Xem/chỉnh sửa hồ sơ
- Sản phẩm bàn giao: `ProfileViewScreen`, `ProfileEditScreen`, cập nhật profile.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 3.2.*.
- Thời gian: 21/04 - 27/04.

#### 3.3.2 Upload avatar lên R2
- Sản phẩm bàn giao: nén ảnh <= 1MB, upload R2, cập nhật `avatarUrl`.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 2.2.2, 3.3.1.
- Thời gian: 21/04 - 27/04.

#### 3.3.3 Mốc bắt buộc xác thực
- Sản phẩm bàn giao: ít nhất 1 commit `feat(auth): ...` (khuyến nghị gắn nhãn `v0.1.0-auth`).
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam, Hải.
- Phụ thuộc: 3.1.*, 3.2.*, 3.3 cơ bản.
- Hạn cuối: trước 21/04 (chậm nhất 20/04).

## 4.0 Nhóm chức năng Nội dung (Ý tưởng, Công việc, Bình luận)

- Phụ trách nhóm chức năng: **Hải** (code chính và chịu trách nhiệm commit chính).

### 4.1 Idea
#### 4.1.1 Danh sách và chi tiết Idea
- Sản phẩm bàn giao: `MyIdeasScreen`, `IdeaDetailScreen`.
- Phụ trách chính: Hải.
- Phụ thuộc: 3.0.
- Thời gian: 22/04 - 29/04.
- Ghi chú: ✅ Hoàn thành 20/04/2026. Sản phẩm: MyIdeasScreen, IdeaDetailScreen, tag filter, deep link `redshark://idea/{id}`.

#### 4.1.2 CRUD Idea và trạng thái Idea
- Sản phẩm bàn giao: tạo/cập nhật/xóa mềm, chuyển trạng thái ACTIVE -> CLOSED/CANCELLED.
- Phụ trách chính: Hải.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 4.1.1.
- Thời gian: 23/04 - 30/04.
- Ghi chú: ✅ Hoàn thành 20/04/2026. Sản phẩm: CreateIdeaScreen, EditIdeaScreen, soft delete (`deletedAt`), ACTIVE→CLOSED/CANCELLED.

### 4.2 Issue
#### 4.2.1 Tạo/sửa/xóa Issue theo quyền sở hữu
- Sản phẩm bàn giao: `CreateIssueScreen`, `IssueDetailScreen`, xóa mềm công việc.
- Phụ trách chính: Hải.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 4.1.*.
- Thời gian: 28/04 - 04/05.
- Ghi chú: ✅ Hoàn thành 20/04/2026. Sản phẩm: CreateIssueScreen, EditIssueScreen, IssueDetailScreen, assignee picker (dropdown + avatar), soft delete.

#### 4.2.2 Áp dụng ràng buộc tối đa 20 Issue active/user
- Sản phẩm bàn giao: kiểm tra nghiệp vụ tại use case + server-side validation.
- Phụ trách chính: Hải.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 4.2.1.
- Thời gian: 29/04 - 04/05.
- Ghi chú: ✅ Hoàn thành 20/04/2026. Sản phẩm: CountMyActiveIssuesUseCase, Firestore rules chặn CLOSED→OPEN.

### 4.3 Comment và Collab Request
#### 4.3.1 Bình luận trên Idea
- Sản phẩm bàn giao: `CommentSection`, tạo bình luận, cập nhật lạc quan.
- Phụ trách chính: Hải.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 4.1.1.
- Thời gian: 30/04 - 04/05.
- Ghi chú: ✅ Hoàn thành 20/04/2026. Sản phẩm: CommentSection trong IdeaDetailScreen, CreateCommentUseCase (+ COMMENT_ADDED notification), optimistic update.

#### 4.3.2 Gửi yêu cầu cộng tác từ Idea
- Sản phẩm bàn giao: tạo thông báo `COLLAB_REQUEST`.
- Phụ trách chính: Hải.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 4.1.1.
- Thời gian: 30/04 - 04/05.
- Ghi chú: ⚠️ Đã có `RequestCollabUseCase` trong nhánh Interaction; còn thiếu entrypoint UI gọi từ `IdeaDetailScreen` để đóng hạng mục hoàn toàn.

## 5.0 Nhóm chức năng Tương tác (Thông báo, Nhắn tin)

- Phụ trách nhóm chức năng: **Nam** (code chính và chịu trách nhiệm commit chính).

### 5.1 Notifications
#### 5.1.1 Danh sách thông báo và badge chưa đọc
- Sản phẩm bàn giao: `NotificationListScreen`, unread badge ở BottomNav.
- Phụ trách chính: Nam.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 4.0.
- Thời gian: 05/05 - 09/05.
- Ghi chú: ✅ Hoàn thành 25/04/2026. Sản phẩm: `NotificationListScreen`, `NotificationViewModel`, `GetNotificationsUseCase`, `GetUnreadCountUseCase`, `MarkNotificationReadUseCase`; badge hiển thị trên tab Notifications trong HomeScreen.

#### 5.1.2 Đọc thông báo và xử lý Collab accept/reject
- Sản phẩm bàn giao: đánh dấu đã đọc, chấp nhận/từ chối cộng tác, tạo thông báo phản hồi.
- Phụ trách chính: Nam.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 5.1.1.
- Thời gian: 05/05 - 10/05.
- Ghi chú: ✅ Hoàn thành 25/04/2026. Sản phẩm: `AcceptCollabUseCase` (thêm collaboratorIds + COLLAB_ACCEPTED notification), `RejectCollabUseCase` (COLLAB_REJECTED notification); nút Chấp nhận/Từ chối hiện trên COLLAB_REQUEST notification chưa đọc.

#### 5.1.3 Gửi yêu cầu cộng tác (chuyển từ 4.3.2)
- Sản phẩm bàn giao: `RequestCollabUseCase` tạo notification `COLLAB_REQUEST`; UI nút "Xin tham gia" trên `IdeaDetailScreen` (FR-IDEA-07 SHOULD).
- Phụ trách chính: Nam.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 5.1.1, 4.1.1.
- Thời gian: 05/05 - 10/05.
- Ghi chú: ✅ Hoàn thành 25/04/2026. Sản phẩm: `RequestCollabUseCase`; nút "Xin tham gia" hiện với non-author trên idea ACTIVE, ẩn khi đã là collaborator hoặc là tác giả.

### 5.2 Messages (DIRECT 1-1)
#### 5.2.1 Danh sách hội thoại và luồng tạo/đi tới hội thoại
- Sản phẩm bàn giao: `ConversationListScreen`, `FindOrCreateDirectConversationUseCase`.
- Phụ trách chính: Nam.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 5.1.*.
- Thời gian: 06/05 - 10/05.
- Ghi chú: ✅ Hoàn thành 25/04/2026. Sản phẩm: `ConversationListScreen`, `FindOrCreateDirectConversationUseCase`, `MessageViewModel`; route `conversation/new?peerId=` xử lý redirect.

#### 5.2.2 Gửi/nhận tin nhắn với cập nhật lạc quan và cập nhật theo chu kỳ
- Sản phẩm bàn giao: `ConversationScreen`, gửi tin, cập nhật real-time (Firestore listener thay poll), chống trùng hội thoại.
- Phụ trách chính: Nam.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 5.2.1.
- Thời gian: 07/05 - 11/05.
- Ghi chú: ✅ Hoàn thành 25/04/2026. Sản phẩm: `ConversationScretaen`, `SendMessageUseCase`, `GetMessagesUseCase`; optimistic send + real-time Firestore listener; chống trùng qua `findDirectConversation` trước `createDirectConversation`.

## 4A.0 Nâng cấp giao diện người dùng (UI Overhaul — Fluent Minimal)

**Thực hiện ngoài lịch:** 27/04/2026 — bổ sung sau WBS 5.0, trước WBS 6.0.
**Tài liệu tham chiếu:** [PLAN-4A-UI.md](PLAN-4A-UI.md).
**Phụ trách chính:** Sỹ. Phối hợp: Nam, Hải.

### 4A.1 Hệ thống điều hướng toàn cục
#### 4A.1.1 Mở rộng Bottom Navigation lên 5 tab + Sidebar Drawer
- Sản phẩm bàn giao: `ui/home/HomeScreen.kt` — 5 tab (Home/Ideas/Notifications/Messages/Profile), `ModalNavigationDrawer` chứa Settings.
- Phụ trách chính: Sỹ.
- Phụ thuộc: 5.0.
- Thời gian: 27/04/2026.
- Ghi chú: ✅ Hoàn thành. TopAppBar cố định "RedShark"; hamburger mở Drawer; Profile tab điều hướng đến ProfileViewScreen.

#### 4A.1.2 Scroll-aware animation cho Top Bar và Bottom Nav
- Sản phẩm bàn giao: `NestedScrollConnection` + `AnimatedVisibility` — ẩn/hiện đồng thời cả hai bar khi cuộn.
- Phụ trách chính: Sỹ.
- Phụ thuộc: 4A.1.1.
- Thời gian: 27/04/2026.
- Ghi chú: ✅ Hoàn thành.

### 4A.2 Shared IdeaCard
#### 4A.2.1 Tạo `IdeaCard` dùng chung
- Sản phẩm bàn giao: `ui/common/IdeaCard.kt` — header (avatar + author + title), body (description + image slot), footer (upvote/downvote/comment/share).
- Phụ trách chính: Sỹ.
- Phụ thuộc: 4.1.
- Thời gian: 27/04/2026.
- Ghi chú: ✅ Hoàn thành. Vote count = 0 placeholder; imageUrl slot sẵn sàng cho extension sau.

#### 4A.2.2 Áp dụng IdeaCard vào HomeFeed và MyIdeas
- Sản phẩm bàn giao: `HomeFeedScreen` + `HomeViewModel` dùng `GetAllIdeasUseCase`; `MyIdeasScreen` dùng shared `IdeaCard`.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 4A.2.1.
- Thời gian: 27/04/2026.
- Ghi chú: ✅ Hoàn thành. `GetAllIdeasUseCase` + `getAllIdeas()` thêm vào domain/data layer.

### 4A.3 Conversation List Tile
#### 4A.3.1 Redesign ConversationItem
- Sản phẩm bàn giao: `ConversationListScreen.kt` — tile dạng Row(avatar + Column(tên + preview)); bold nếu unread; prefix "Bạn: " nếu mình gửi.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 5.2.
- Thời gian: 27/04/2026.
- Ghi chú: ✅ Hoàn thành. Mở rộng `Conversation` model + DTO với `lastMessageSenderId` và `hasUnread`; DTO `@IgnoreExtraProperties` đảm bảo tương thích ngược.

## 6.0 Chất lượng, kiểm thử và tuân thủ NFR

### 6.1 Kế hoạch kiểm thử
#### 6.1.1 Chuẩn bị và duy trì danh sách kiểm thử thủ công
- Sản phẩm bàn giao: `CHECK-1-AUTH.md`, `CHECK-2-CONTENT.md`, `CHECK-3-INTERACTION.md`.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam, Hải.
- Phụ thuộc: 3.0 - 5.0.
- Thời gian: xuyên suốt theo từng giai đoạn.

#### 6.1.2 Thực thi kiểm thử hồi quy toàn bộ
- Sản phẩm bàn giao: kết quả kiểm thử tổng hợp 61 trường hợp kiểm thử, tỷ lệ đạt >= 95%.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: cả nhóm.
- Phụ thuộc: 3.0 - 5.0 hoàn thành.
- Thời gian: 12/05 - 16/05.

### 6.2 Đảm bảo yêu cầu phi chức năng
#### 6.2.1 Hiệu năng và độ ổn định
- Sản phẩm bàn giao: biên bản đo khởi động nguội, tỷ lệ phiên không sự cố, RAM, kích thước APK.
- Phụ trách chính: Hải.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 6.1.2.
- Thời gian: 13/05 - 16/05.

#### 6.2.2 Bảo mật và quản lý cấu hình
- Sản phẩm bàn giao: rà soát biến nhạy cảm, chống ghi token vào nhật ký, kiểm tra cấu hình phát hành.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 6.1.2.
- Thời gian: 13/05 - 16/05.

## 7.0 Hoàn thiện, phát hành và bàn giao

### 7.1 Dọn dẹp kỹ thuật
#### 7.1.1 Dọn tài nguyên và mã thừa
- Sản phẩm bàn giao: loại bỏ tệp biên dịch/tệp rác, dọn phụ thuộc trùng, chuẩn hóa nhật ký bản phát hành.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Nam.
- Phụ thuộc: 5.0.
- Thời gian: 12/05 - 15/05.

#### 7.1.2 Rà soát giao diện và khả dụng
- Sản phẩm bàn giao: dark mode, empty/error state, accessibility cơ bản.
- Phụ trách chính: Nam.
- Phụ thuộc: 5.0.
- Thời gian: 13/05 - 16/05.

### 7.2 Đóng gói phát hành
#### 7.2.1 Build APK phát hành
- Sản phẩm bàn giao: APK phát hành đã ký số.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: Hải.
- Phụ thuộc: 7.1.*.
- Thời gian: 16/05 - 17/05.

#### 7.2.2 Tag và bàn giao cuối kỳ
- Sản phẩm bàn giao: tag `v1.0.0`, mã nguồn, APK, báo cáo, slide.
- Phụ trách chính: Sỹ.
- Thành viên phối hợp: cả nhóm.
- Phụ thuộc: 7.2.1, 6.0.
- Thời gian: 17/05.

## 5. Ma trận phụ thuộc chính (rút gọn)

| Từ công việc                   | Phụ thuộc trực tiếp | Ý nghĩa                                                          |
|--------------------------------|---------------------|------------------------------------------------------------------|
| 3.1 (Google Auth)              | 2.1, 2.2.1          | Chỉ triển khai Auth khi nền tảng và Firebase đã sẵn sàng         |
| 3.2 (Hoàn thiện hồ sơ lần đầu) | 3.1                 | Luồng `profile/setup` kích hoạt sau đăng nhập thành công         |
| 4.0 (Nội dung)                 | 3.0                 | Nghiệp vụ nội dung cần ngữ cảnh người dùng đã xác thực           |
| 5.0 (Tương tác)                | 4.0                 | Thông báo/nhắn tin phụ thuộc dữ liệu ý tưởng/công việc/bình luận |
| 6.0 (Kiểm thử/NFR)             | 3.0-5.0             | Kiểm thử tổng hợp sau khi nhóm chức năng chính hoàn thiện        |
| 7.0 (Phát hành)                | 6.0                 | Chỉ phát hành sau khi đạt ngưỡng chất lượng                      |

## 6. Mapping WBS theo mốc thời gian

| Giai đoạn                         | Mốc thời gian | WBS trọng tâm | Sản phẩm bàn giao chính                                                               |
|-----------------------------------|---------------|---------------|---------------------------------------------------------------------------------------|
| Giai đoạn 0-1 Khảo sát + Nền tảng | 16/03 - 15/04 | 1.0, 2.0      | Tài liệu nền, kiểm tra biên dịch cục bộ, chuẩn bị Firebase/R2, commit khởi tạo        |
| Giai đoạn 2 Xác thực              | 16/04 - 20/04 | 3.0           | Xác thực Google-only, hoàn thiện hồ sơ lần đầu, commit tính năng xác thực trước 21/04 |
| Giai đoạn 3 Nội dung              | 21/04 - 04/05 | 4.0           | Ý tưởng/Công việc/Bình luận + ràng buộc nghiệp vụ                                     |
| Giai đoạn 4 Tương tác             | 05/05 - 11/05 | 5.0           | Thông báo/Nhắn tin trực tiếp 1-1                                                      |
| Giai đoạn 4A UI Overhaul (bổ sung) | 27/04/2026   | 4A.0          | Fluent Minimal UI: 5-tab nav, Drawer, scroll animation, IdeaCard, Conversation tile   |
| Giai đoạn 5 Hoàn thiện            | 12/05 - 17/05 | 6.0, 7.0      | Kiểm thử, dọn dẹp, phát hành APK, bàn giao                                            |

## 7. Tiêu chí hoàn thành WBS

- Hoàn thành 100% các sản phẩm bàn giao mức 2.0 đến 7.0 theo phạm vi trong Tuyên ngôn dự án.
- Bảo đảm commit khởi tạo ngày 15/04, mốc commit tính năng xác thực trước 21/04 và mốc bàn giao cuối kỳ 17/05.
- Tỷ lệ đạt kiểm thử thủ công >= 95%.
- Tuân thủ yêu cầu bảo mật: không lộ biến cấu hình nhạy cảm trên Git/log.
- Tài liệu trong `docs/` được duy trì nhất quán với mã nguồn tại từng giai đoạn.

