# STRUCTURE.md — Cấu trúc thư mục dự án Kotlin/Android

## Kiến trúc tổng quan

Dự án áp dụng **Clean Architecture** + **MVVM** với 3 layer tách biệt: `data`, `domain`, `ui`. Mỗi tính năng (feature) được module hóa theo chiều dọc (vertical slicing).

```
redshark/
├── .gitignore
├── build.gradle.kts                   # Root Gradle (Kotlin DSL)
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties                   # (gitignored) secrets, SDK path
├── firestore.indexes.json             # ✅ Composite indexes cho queries
├── firestore.rules                    # ✅ Security rules (CLOSED→OPEN block)
├── .firebaserc                        # ✅ Firebase project config
├── docs/                              # Tài liệu dự án (charter, requirement, plan, process, check, report, git)
└── app/
    ├── build.gradle.kts               # App module config (Compose, Hilt, Firebase, ...)
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml    # ✅ + deep link intent-filter redshark://idea
        │   ├── res/
        │   │   ├── drawable/          # PNG logo assets (không dùng WebP)
        │   │   ├── values/
        │   │   │   ├── strings.xml
        │   │   │   ├── colors.xml
        │   │   │   └── themes.xml
        │   │   └── mipmap-*/
        │   └── java/com/helios/redshark/
        │       │
        │       ├── RedSharkApp.kt                 # @HiltAndroidApp
        │       ├── MainActivity.kt                # Single-activity host
        │       │
        │       ├── core/                          # Cross-cutting
        │       │   ├── di/
        │       │   │   ├── AppModule.kt           # ✅ DataStore
        │       │   │   ├── FirebaseModule.kt      # ✅ FirebaseAuth + FirebaseFirestore
        │       │   │   ├── R2Module.kt            # ✅ OkHttpClient
        │       │   │   └── RepositoryModule.kt    # ✅ Auth/Profile/Media + Idea/Issue/Comment/Notification/Message
        │       │   ├── NetworkChecker.kt          # ✅ ConnectivityManager online check
        │       │   ├── util/
        │       │   │   └── Result.kt              # ✅ Sealed class Success/Error/Loading
        │       │   └── error/
        │       │       ├── AppException.kt        # ✅
        │       │       └── ErrorMapper.kt         # ✅
        │       │
        │       ├── data/                          # Data layer
        │       │   ├── local/
        │       │   │   ├── datastore/
        │       │   │   │   └── UserPreferences.kt # DataStore (token cache, theme)
        │       │   │   └── db/                    # (optional) Room offline cache
        │       │   │       ├── RedSharkDatabase.kt
        │       │   │       └── dao/
        │       │   ├── remote/
        │       │   │   ├── firebase/
        │       │   │   │   ├── FirebaseAuthSource.kt
        │       │   │   │   └── GoogleSignInHelper.kt  # Credential Manager (One Tap + fallback)
        │       │   │   ├── firestore/
        │       │   │   │   ├── FirestoreSource.kt         # ✅ Interface (+ idea/issue/comment/user ops)
        │       │   │   │   ├── FirestoreSourceImpl.kt     # ✅ Firestore SDK calls
        │       │   │   │   └── dto/
        │       │   │   │       ├── UserDto.kt             # ✅
        │       │   │   │       ├── IdeaDto.kt             # ✅
        │       │   │   │       ├── IssueDto.kt            # ✅
        │       │   │   │       ├── CommentDto.kt          # ✅
        │       │   │   │       ├── NotificationDto.kt     # ✅
        │       │   │   │       ├── ConversationDto.kt     # ✅
        │       │   │   │       └── MessageDto.kt          # ✅
        │       │   │   │
        │       │   │   └── r2/
        │       │   │       └── R2Client.kt            # OkHttp + AWS SigV4
        │       │   ├── repository/                # Implement interface từ domain
        │       │   │   ├── AuthRepositoryImpl.kt          # ✅ sign-in + upsertUser Firestore
        │       │   │   ├── ProfileRepositoryImpl.kt       # ✅ + getUsers()
        │       │   │   ├── MediaRepositoryImpl.kt         # ✅ R2Client upload
        │       │   │   ├── IdeaRepositoryImpl.kt          # ✅ + NetworkChecker guard + addCollaborator
        │       │   │   ├── IssueRepositoryImpl.kt         # ✅
        │       │   │   ├── CommentRepositoryImpl.kt       # ✅
        │       │   │   ├── NotificationRepositoryImpl.kt  # ✅
        │       │   │   └── MessageRepositoryImpl.kt       # ✅ Firestore real-time listeners
        │       │   └── mapper/                    # DTO ↔ Domain model
        │       │       ├── UserMapper.kt              # ✅ FirebaseUser + UserDto → User
        │       │       ├── IdeaMapper.kt              # ✅
        │       │       ├── IssueMapper.kt             # ✅
        │       │       ├── CommentMapper.kt           # ✅
        │       │       ├── NotificationMapper.kt      # ✅
        │       │       ├── ConversationMapper.kt      # ✅
        │       │       └── MessageMapper.kt           # ✅
        │       │
        │       ├── domain/                        # Domain layer (pure Kotlin, no Android)
        │       │   ├── model/
        │       │   │   ├── User.kt
        │       │   │   ├── Idea.kt
        │       │   │   ├── Issue.kt
        │       │   │   ├── Comment.kt
        │       │   │   ├── Notification.kt
        │       │   │   ├── Conversation.kt
        │       │   │   ├── Message.kt
        │       │   │   ├── Tag.kt
        │       │   │   └── Skill.kt
        │       │   ├── repository/                # Interfaces only
        │       │   │   ├── AuthRepository.kt          # ✅
        │       │   │   ├── ProfileRepository.kt       # ✅ + getUsers()
        │       │   │   ├── MediaRepository.kt         # ✅
        │       │   │   ├── IdeaRepository.kt          # ✅ + addCollaborator()
        │       │   │   ├── IssueRepository.kt         # ✅
        │       │   │   ├── CommentRepository.kt       # ✅
        │       │   │   ├── NotificationRepository.kt  # ✅
        │       │   │   └── MessageRepository.kt       # ✅
        │       │   └── usecase/
        │       │       ├── auth/
        │       │       │   ├── SignInGoogleUseCase.kt         # ✅
        │       │       │   ├── CompleteFirstProfileUseCase.kt # ✅
        │       │       │   ├── SignOutUseCase.kt              # ✅
        │       │       │   ├── ObserveAuthStateUseCase.kt     # ✅
        │       │       │   ├── UpdateProfileUseCase.kt        # ✅
        │       │       │   └── UploadAvatarUseCase.kt         # ✅
        │       │       ├── idea/                          # ✅ GetMyIdeas, GetIdeaDetail, Create, Update, Delete, ChangeStatus
        │       │       ├── issue/                         # ✅ GetIssues, Create (w/ limit check), Update, Delete, ChangeStatus, GetHomeFeed
        │       │       ├── comment/                       # ✅ GetComments, CreateComment (+ notification)
        │       │       ├── user/                          # ✅ GetUsersUseCase
        │       │       ├── notification/                  # ✅ Get, MarkRead, GetUnreadCount, RequestCollab, AcceptCollab, RejectCollab
        │       │       └── message/                       # ✅ GetConversations, GetMessages, SendMessage, FindOrCreateDirectConversation
        │       │
        │       └── ui/                            # Presentation layer (cấu trúc phẳng — mỗi feature một thư mục)
        │           ├── theme/                          # ✅ Design system tokens
        │           │   ├── Color.kt                    # Brand palette (Red/Teal/Amber + neutrals)
        │           │   ├── Type.kt                     # Material3 typography scale đầy đủ
        │           │   ├── Dimens.kt                   # Spacing/Elevation/Icon/Avatar tokens
        │           │   ├── Shape.kt                    # Material3 Shapes
        │           │   └── Theme.kt                    # RedSharkTheme (dynamicColor=false default)
        │           ├── navigation/
        │           │   ├── NavGraph.kt                 # ✅ + deep link redshark://idea/{id}
        │           │   └── Routes.kt                   # ✅ (orphan NOTIFICATIONS/MESSAGES đã loại bỏ — render dưới dạng tab)
        │           ├── common/                         # ✅ Reusable composables
        │           │   ├── AvatarImage.kt              # Avatar w/ Coil + initial-letter fallback
        │           │   ├── StateContent.kt             # LoadingContent / ErrorContent / EmptyContent / InlineErrorText
        │           │   ├── StatusPill.kt               # Generic StatusPill + IdeaStatusPill / IssueStatusPill / IssuePriorityPill
        │           │   └── IssueCard.kt                # IssueCard (dùng ở Feed + IdeaDetail)
        │           ├── auth/                           # ✅ GoogleSignInScreen, ProfileSetupScreen, AuthViewModel
        │           ├── home/                           # ✅ HomeScreen (shell 4-tab) + HomeFeedScreen (Feed tab) + HomeViewModel
        │           ├── myideas/                        # ✅ MyIdeasScreen, MyIdeasViewModel (tag filter, offline check)
        │           ├── createidea/                     # ✅ CreateIdeaScreen, CreateIdeaViewModel
        │           ├── editidea/                       # ✅ EditIdeaScreen, EditIdeaViewModel
        │           ├── ideadetail/                     # ✅ IdeaDetailScreen, IdeaDetailViewModel, CommentItem, CommentInput; nút "Xin tham gia" (FR-IDEA-07)
        │           ├── createissue/                    # ✅ CreateIssueScreen, CreateIssueViewModel
        │           ├── editissue/                      # ✅ EditIssueScreen, EditIssueViewModel (assignee dropdown)
        │           ├── issuedetail/                    # ✅ IssueDetailScreen, IssueDetailViewModel (AssigneeRow)
        │           ├── notification/                   # ✅ NotificationListScreen, NotificationViewModel
        │           ├── message/                        # ✅ ConversationListScreen, ConversationScreen, MessageViewModel
        │           ├── profile/                        # ✅ ProfileViewModel, ProfileViewScreen, ProfileEditScreen
        │           └── settings/                       # ✅ SettingsScreen (sign out + "Mở Idea đã xóa" dialog)
        │
        ├── test/                                  # Unit tests (JVM)
        │   └── java/com/helios/redshark/
        │       └── domain/usecase/auth/
        │           ├── SignInGoogleUseCaseTest.kt         # ✅
        │           ├── ObserveAuthStateUseCaseTest.kt     # ✅
        │           ├── CompleteFirstProfileUseCaseTest.kt # ✅
        │           ├── SignOutUseCaseTest.kt              # ✅
        │           ├── UpdateProfileUseCaseTest.kt        # ✅
        │           └── UploadAvatarUseCaseTest.kt         # ✅
        │
        └── androidTest/                           # Instrumented UI tests
            └── java/com/helios/redshark/
                └── ui/
```

## Phân layer chi tiết

### Data Layer
- **Responsibility:** Gọi API, cache, parse DTO → Domain model.
- **Không phụ thuộc** lên domain/ui.
- Gồm: `remote/firebase/*`, `remote/r2/*`, `local/*`, `repository/*Impl.kt`, `mapper/*`.
- Mọi hàm public trong `*RepositoryImpl` trả về `Flow<Result<T>>` hoặc `Result<T>` (từ `core/util/Result.kt`).

### Domain Layer
- **Responsibility:** Business logic thuần, use cases single-responsibility.
- **Pure Kotlin**, không import `android.*` hay `firebase.*`.
- Chỉ khai báo **interface** `Repository`; impl thuộc về Data layer.
- Use case expose `Flow<Result<T>>` hoặc `suspend fun invoke(): Result<T>`.

### UI Layer
- **Responsibility:** Jetpack Compose UI + `ViewModel` (MVVM).
- State flow: `ViewModel` expose `StateFlow<UiState>`; Composable `collectAsStateWithLifecycle()`.
- Navigation: single-activity, Compose Navigation, route strings trong `Routes.kt`. Deep link `redshark://idea/{ideaId}` được khai báo tại `NavGraph.kt` và `AndroidManifest.xml`.
- **Home split:** `ui/home/HomeScreen.kt` là shell (TopAppBar + BottomNav **4 tab**: Feed / Ideas / Notifications-badge / Messages); `ui/home/HomeFeedScreen.kt` là nội dung tab Feed; `ui/myideas/MyIdeasScreen.kt` là tab Ideas (kèm tag chip filter — TC-C24); `ui/notification/NotificationListScreen.kt` là tab Notifications; `ui/message/ConversationListScreen.kt` là tab Messages. Tab Notifications và Messages **không có route riêng** trong NavGraph — chỉ render dưới dạng tab.
- Real-time update dùng **Firestore snapshot listener** (`callbackFlow` + `addSnapshotListener`) cho Notifications và Messages; không cần polling thủ công.
- **Quy ước thư mục:** Mọi feature đặt phẳng trực tiếp dưới `ui/<feature>/`. Đã loại bỏ namespace trung gian `ui/feature/*` (giai đoạn UI consolidation 2026-04-26).
- **Common composables** (`ui/common/`): mọi list screen dùng `LoadingContent` / `ErrorContent` / `EmptyContent` thay cho `CircularProgressIndicator + Text` inline. Status badge thống nhất qua `StatusPill` + 3 wrapper (`IdeaStatusPill`, `IssueStatusPill`, `IssuePriorityPill`). `IssueCard` được tái sử dụng giữa Feed và IdeaDetail.
- **Theme tokens** (`ui/theme/Dimens.kt`): mọi screen dùng `Dimens.SpaceLg/SpaceMd/SpaceSm/...` thay vì literal `16.dp`. `dynamicColor` mặc định = `false` để giữ brand identity.
- **Strings**: 100% chuỗi UI shell (titles, labels, buttons, empty/error states, dialogs) đã chuyển vào `res/values/strings.xml` với namespace prefix (`home_*`, `idea_*`, `issue_*`, `notification_*`, `message_*`, `profile_*`, `settings_*`, `auth_*`, `action_*`).
- **UiState patterns** (cố ý không thống nhất 1 pattern duy nhất, mỗi pattern phù hợp use case):
  - **Form ViewModels** (Create/Edit Idea/Issue) dùng `sealed interface` với state machine `Idle/Loading/Loaded?/Success/ValidationError/NetworkError/Failure.*` — phù hợp cho action 1 lần.
  - **List/Detail ViewModels** (Home, MyIdeas, IdeaDetail, IssueDetail, Notification, Message, Auth, Profile) dùng `data class` với `isLoading/errorMessage/data` — phù hợp cho luồng dữ liệu liên tục.

### Logging
- Dùng **Timber** thay `Log.*` trực tiếp. Debug: `Timber.d(...)`, Warning+: `Timber.w(...)`.
- Release build chỉ plant `ReleaseTree` (log WARN+, không log token/email).

### Core types (`core/util/Result.kt` + `core/error/AppException.kt`)

`AppException` là sealed class duy nhất cho mọi lỗi nghiệp vụ; `Result<T>` (Success/Error/Loading) là kiểu trả về chuẩn cho repository và use case. **Không** dùng `core/AppException.kt` hoặc `core/Result.kt` — các path đó đã loại bỏ.


```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: AppException) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
```

## Quy ước đặt tên

| Thành phần | Convention | Ví dụ |
|-----------|-----------|-------|
| Package | lowercase, không dấu gạch | `com.helios.redshark.data.repository` |
| Class | PascalCase | `IdeaRepositoryImpl` |
| Interface | PascalCase (không prefix `I`) | `IdeaRepository` |
| UseCase | `<Verb><Noun>UseCase` | `CreateIdeaUseCase` |
| Composable | PascalCase | `IdeaDetailScreen` |
| ViewModel | `<Feature>ViewModel` | `IdeaDetailViewModel` |
| File một Composable | = tên Composable | `IdeaDetailScreen.kt` |
