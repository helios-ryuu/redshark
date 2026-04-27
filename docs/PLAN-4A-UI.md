# PLAN-4A-UI.md — Giai đoạn 4A: Thiết kế giao diện Fluent Minimal

**Thời gian:** 27/04/2026 (thực hiện ngoài lịch — bổ sung sau WBS 5.0)
**Mục tiêu:** Nâng cấp toàn bộ lớp UI theo phong cách Fluent Minimal: sạch sẽ, tập trung nội dung, không gradient. Chuẩn hóa hệ thống điều hướng và các thành phần dùng chung.
**WBS tham chiếu:** [WBS.md](WBS.md) — nhóm công việc `4A.0` (bổ sung ngoài kế hoạch gốc).
**Trạng thái:** ✅ Code hoàn tất 27/04/2026. Build `assembleDebug` và `testDebugUnitTest` xanh.

**Phân công:**
- Phụ trách chính: **Sỹ** (thiết kế + lập trình toàn bộ).
- Thành viên phối hợp: **Hải**, **Nam** (review, kiểm tra tích hợp màn hình phụ trách).

> **Tiền điều kiện:** WBS 5.0 (Interaction) đã hoàn thành — toàn bộ tính năng domain/data/UI đã implement xong, sẵn sàng cho lớp UI overhaul.

---

## 1. Nguyên tắc thiết kế (Design Rules)

| Nguyên tắc | Quy định cụ thể |
|---|---|
| **Style** | Fluent Minimal — sạch sẽ, hiện đại, tập trung vào nội dung |
| **Gradient** | Tuyệt đối KHÔNG sử dụng |
| **Token** | Luôn dùng `Dimens.*` thay literal `dp`; không hardcode màu |
| **Reuse** | Ưu tiên composable trong `ui/common/` trước khi tạo mới |
| **Strings** | Mọi text UI phải qua `stringResource(R.string.*)` — không hardcode |

---

## 2. Tính năng và thay đổi

### 2.1 Shell — Điều hướng toàn cục (`HomeScreen`)

**Top App Bar:**
- Tiêu đề luôn cố định là `"RedShark"` (thay vì đổi theo tab).
- Thêm nút hamburger (`Icons.Default.Menu`) ở góc trái — mở Drawer.
- Xoá nút `AccountCircle` và `Settings` khỏi thanh actions.
- Ẩn/hiện đồng bộ với Bottom Nav khi cuộn (xem mục Animation).

**Bottom Navigation Bar:**
- Mở rộng từ 4 lên **5 tab** theo thứ tự: `Home → My Ideas → Notifications → Messages → Profile`.
- Tab `Profile` điều hướng đến `ProfileViewScreen` của user hiện tại (không embed).
- Tab `Feed` đổi nhãn thành `Home`.

**Sidebar Drawer (`ModalNavigationDrawer`):**
- Chứa mục `Cài đặt` với icon `Settings`.
- Kéo từ cạnh trái hoặc nhấn nút hamburger để mở.
- Đóng tự động sau khi chọn mục.

**Scroll-aware Animation:**
- Dùng `NestedScrollConnection` để phát hiện hướng cuộn.
- Cuộn lên (xem nội dung bên dưới): Top Bar trượt lên + fade out; Bottom Nav trượt xuống + fade out — đồng thời.
- Cuộn xuống: cả hai trượt về vị trí ban đầu + fade in.
- Dùng `AnimatedVisibility` với `slideInVertically + fadeIn` / `slideOutVertically + fadeOut`.

### 2.2 Shared IdeaCard (`ui/common/IdeaCard.kt`)

Component dùng chung cho cả tab **Home** và **My Ideas**.

**Cấu trúc layout:**
```
Card (fillMaxWidth, flexible height, elevation = CardElevation)
 ├─ [Header] Row — Avatar(AvatarSm) + Column(authorId.take(8), idea.title) — căn trái
 ├─ [Body] Text description (bodyMedium, maxLines=5, onSurfaceVariant)
 │           AsyncImage nếu imageUrl != null (heightIn max=240dp, Crop)
 └─ [Footer] Row(SpaceBetween)
              ├─ TextButton: ThumbUp icon + upvoteCount (count=0 placeholder)
              ├─ IconButton: ThumbDown (không hiện count — theo spec)
              ├─ TextButton: ChatBubbleOutline icon + commentCount (count=0 placeholder)
              └─ IconButton: Share
```

**Tham số:**
```kotlin
fun IdeaCard(
    idea: Idea,
    onClick: () -> Unit,
    onUpvote: () -> Unit = {},
    onDownvote: () -> Unit = {},
    onComment: () -> Unit = {},
    onShare: () -> Unit = {},
    imageUrl: String? = null,   // mở rộng khi domain có imageUrl
    upvoteCount: Int = 0,
    commentCount: Int = 0,
    modifier: Modifier = Modifier,
)
```

**Lưu ý kỹ thuật:**
- `authorId.take(8)` là placeholder — thay bằng `displayName` khi có profile lookup.
- `upvoteCount`, `commentCount` = 0 placeholder cho đến khi domain `Idea` có trường này.
- `imageUrl` luôn là `null` hiện tại — slot đã có sẵn, wire sau khi backend hỗ trợ.

### 2.3 HomeFeedScreen — Tab Home hiển thị Ideas

- **Trước:** `HomeFeedScreen` hiển thị Issues dùng `IssueCard` (via `GetHomeFeedUseCase`).
- **Sau:** Hiển thị tất cả Ideas của mọi người dùng dùng shared `IdeaCard` (via `GetAllIdeasUseCase`).
- Callback đổi từ `onIssueClick` → `onIdeaClick`.

### 2.4 MyIdeasScreen — Dùng shared IdeaCard

- Xoá `IdeaCard` private cũ (trước: title + status pill + 2-line description).
- Import và dùng `com.helios.redshark.ui.common.IdeaCard`.
- Action callbacks (`onUpvote`, `onDownvote`, `onComment`, `onShare`) truyền `{}` — wire sau.

### 2.5 ConversationListScreen — Tile danh sách tin nhắn

**Trước:** Card có Row đơn giản, không có avatar, không phân biệt read/unread.

**Sau:** List tile theo chuẩn:
```
Row (clickable, padding horizontal=SpaceLg, vertical=SpaceMd)
 ├─ AvatarImage(AvatarMd, url=null, displayName=peerId) — hình tròn căn trái
 └─ Column(weight=1f)
     ├─ Text(peerId.take(8), titleSmall)
     └─ Text(preview, style = bodySmall)
         — Bold nếu tin nhắn từ peer và hasUnread=true
         — Prefix "Bạn: " nếu lastMessageSenderId == currentUserId
```
- Phân tách bằng `HorizontalDivider` (không dùng Card wrapper).
- Không còn hiện timestamp bên phải (tạm bỏ để tile gọn hơn).

---

## 3. Thay đổi domain và data layer

### 3.1 `Conversation` domain model

Thêm 2 trường mới:
```kotlin
val lastMessageSenderId: String?   // null nếu chưa có tin nhắn
val hasUnread: Boolean             // true nếu peer gửi tin cuối và chưa đọc
```

### 3.2 `ConversationDto` + `ConversationMapper`

- Thêm `lastMessageSenderId: String? = null` và `hasUnread: Boolean = false` vào DTO.
- DTO có `@IgnoreExtraProperties` → tương thích ngược nếu Firestore chưa có trường này.
- Mapper ánh xạ sang domain.

### 3.3 `IdeaRepository` + `IdeaRepositoryImpl`

Thêm phương thức:
```kotlin
fun getAllIdeas(): Flow<List<Idea>>
```
- Query Firestore `ideas` không lọc `authorId`, chỉ lọc `deletedAt == null`, sắp xếp `createdAt DESC`.
- Pattern giống `getMyIdeas()` nhưng bỏ điều kiện `whereEqualTo("authorId", uid)`.

### 3.4 `GetAllIdeasUseCase` (mới)

```kotlin
class GetAllIdeasUseCase @Inject constructor(private val repo: IdeaRepository) {
    operator fun invoke(): Flow<List<Idea>> = repo.getAllIdeas()
}
```

### 3.5 `HomeViewModel`

- Thay `GetHomeFeedUseCase` → `GetAllIdeasUseCase`.
- `HomeUiState.issues: List<Issue>` → `HomeUiState.ideas: List<Idea>`.

---

## 4. Màn hình và file thay đổi

| File | Loại thay đổi |
|---|---|
| `ui/home/HomeScreen.kt` | Overhaul toàn bộ: 5 tab, Drawer, scroll animation |
| `ui/home/HomeViewModel.kt` | Switch use case + đổi UiState |
| `ui/home/HomeFeedScreen.kt` | Dùng `IdeaCard`, đổi callback |
| `ui/myideas/MyIdeasScreen.kt` | Xoá IdeaCard private, import shared |
| `ui/message/ConversationListScreen.kt` | Redesign tile, thêm avatar/bold/prefix |
| `ui/common/IdeaCard.kt` | **Tạo mới** — shared component |
| `domain/model/Conversation.kt` | Thêm 2 trường |
| `domain/repository/IdeaRepository.kt` | Thêm `getAllIdeas()` |
| `domain/usecase/idea/GetAllIdeasUseCase.kt` | **Tạo mới** |
| `data/remote/firestore/dto/ConversationDto.kt` | Thêm 2 trường |
| `data/mapper/ConversationMapper.kt` | Map 2 trường mới |
| `data/repository/IdeaRepositoryImpl.kt` | Implement `getAllIdeas()` |
| `res/values/strings.xml` | Thêm ~12 key mới |

---

## 5. Strings mới (`res/values/strings.xml`)

```xml
<string name="home_title_app">RedShark</string>
<string name="home_tab_home">Home</string>
<string name="home_tab_profile">Hồ sơ</string>
<string name="home_title_profile">Hồ sơ</string>
<string name="home_drawer_settings">Cài đặt</string>
<string name="home_action_open_menu">Mở menu</string>
<string name="home_feed_empty">Chưa có ý tưởng nào.</string>
<string name="idea_action_upvote">Upvote</string>
<string name="idea_action_downvote">Downvote</string>
<string name="idea_action_comment">Bình luận</string>
<string name="idea_action_share">Chia sẻ</string>
<string name="message_preview_you_prefix">Bạn: %1$s</string>
```

---

## 6. Tiêu chí chấp nhận (Acceptance Criteria)

- [x] Bottom nav hiển thị đúng 5 tab theo thứ tự: Home → My Ideas → Notifications → Messages → Profile
- [x] TopAppBar luôn hiển thị chữ "RedShark"; nút hamburger mở Drawer chứa mục Cài đặt
- [x] Cuộn lên → cả TopAppBar và BottomNav ẩn đồng thời (slide + fade); cuộn xuống → hiện lại
- [x] Tab Home hiển thị Ideas của tất cả người dùng bằng `IdeaCard` mới
- [x] Tab My Ideas cũng dùng `IdeaCard` mới — không còn card design cũ
- [x] Footer IdeaCard: icon Upvote + số, icon Downvote (không số), icon Comment + số, icon Share
- [x] Conversation tile: avatar tròn bên trái, bold preview nếu unread từ peer, prefix "Bạn: " nếu mình gửi
- [x] `./gradlew assembleDebug` — BUILD SUCCESSFUL
- [x] `./gradlew testDebugUnitTest` — BUILD SUCCESSFUL, không regress

---

## 7. Hạng mục tạm hoãn (Deferred)

| Hạng mục | Lý do hoãn |
|---|---|
| Vote/comment count thực tế | Domain `Idea` chưa có trường `upvoteCount`/`commentCount` — hiện tại hiện `0` |
| Ảnh trong IdeaCard | Domain `Idea` chưa có `imageUrl` — slot `imageUrl: String?` đã có sẵn để wire sau |
| Tên thật trong tile tin nhắn | Chưa có profile lookup per-conversation — dùng `userId.take(8)` tạm |
| `hasUnread`/`lastMessageSenderId` trên Firestore | DTO có `@IgnoreExtraProperties`, mặc định `false`/`null` nếu backend chưa write |

---

## Phase 2 — Mission Objective: Full Fluent Overhaul

**Started:** 27/04/2026 | **Branch:** `feature/ui-fluent-overhaul`
**Goal:** Deep Fluent Minimalist polish — Lexend font, clean white palette, full English, beautiful empty states, strong chat bubbles, skill chips, polished forms.

### Checklist

- [ ] **Step 0** — Git setup: create `feature/ui-fluent-overhaul`, commit WBS 4A baseline
- [ ] **Step 1** — Lexend font: `build.gradle.kts` + `Type.kt`
- [ ] **Step 2** — Color palette & design tokens: `Color.kt`, `Dimens.kt`, `Shape.kt`
- [ ] **Step 3** — Full English strings: `strings.xml` (all Vietnamese → English)
- [ ] **Step 4** — Common components: `StatusPill.kt`, `IdeaCard.kt`, `IssueCard.kt`, `StateContent.kt`
- [ ] **Step 5** — Navigation: filled/outlined icon distinction for active/inactive tabs
- [ ] **Step 6** — Feed screens: `HomeFeedScreen.kt`, `MyIdeasScreen.kt`
- [ ] **Step 7** — Notifications: read/unread visual diff, empty state
- [ ] **Step 8** — Chat (CRITICAL): strong sent bubble color, asymmetric bubble shapes, flat input bar
- [ ] **Step 9** — Profile: skill chips (`AssistChip`), Fluent input fields
- [ ] **Step 10** — Idea/Issue detail & form screens polish
- [ ] **Step 11** — Auth screens: clean Fluent layout
- [ ] **Verify** — `assembleDebug` green, `testDebugUnitTest` green, no Vietnamese visible
