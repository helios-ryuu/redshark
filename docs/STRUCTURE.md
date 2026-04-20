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
        │       │   │   └── RepositoryModule.kt    # ✅ Auth/Profile/Media + Idea/Issue/Comment/Notification
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
        │       │   │   │       └── CommentDto.kt          # ✅
        │       │   │   │
        │       │   │   └── r2/
        │       │   │       └── R2Client.kt            # OkHttp + AWS SigV4
        │       │   ├── repository/                # Implement interface từ domain
        │       │   │   ├── AuthRepositoryImpl.kt          # ✅ sign-in + upsertUser Firestore
        │       │   │   ├── ProfileRepositoryImpl.kt       # ✅ + getUsers()
        │       │   │   ├── MediaRepositoryImpl.kt         # ✅ R2Client upload
        │       │   │   ├── IdeaRepositoryImpl.kt          # ✅ + NetworkChecker guard
        │       │   │   ├── IssueRepositoryImpl.kt         # ✅
        │       │   │   ├── CommentRepositoryImpl.kt       # ✅
        │       │   │   ├── NotificationRepositoryImpl.kt  # ✅
        │       │   │   └── MessageRepositoryImpl.kt       # (giai đoạn 5)
        │       │   └── mapper/                    # DTO ↔ Domain model
        │       │       ├── UserMapper.kt              # ✅ FirebaseUser + UserDto → User
        │       │       ├── IdeaMapper.kt              # ✅
        │       │       ├── IssueMapper.kt             # ✅
        │       │       └── CommentMapper.kt           # ✅
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
        │       │   │   ├── IdeaRepository.kt          # ✅
        │       │   │   ├── IssueRepository.kt         # ✅
        │       │   │   ├── CommentRepository.kt       # ✅
        │       │   │   └── NotificationRepository.kt  # ✅
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
        │       │       ├── notification/                  # (giai đoạn 5)
        │       │       └── message/                       # (giai đoạn 5)
        │       │
        │       └── ui/                            # Presentation layer
        │           ├── theme/
        │           │   ├── Color.kt
        │           │   ├── Theme.kt
        │           │   ├── Type.kt
        │           │   └── Shape.kt
        │           ├── navigation/
        │           │   ├── NavGraph.kt            # ✅ + deep link redshark://idea/{id}
        │           │   ├── Routes.kt              # ✅
        │           │   └── BottomNavBar.kt
        │           ├── common/                    # Reusable composables
        │           │   ├── AppButton.kt
        │           │   ├── AppTextField.kt
        │           │   ├── Avatar.kt
        │           │   ├── FAB.kt
        │           │   ├── SkillChip.kt
        │           │   ├── TagChip.kt
        │           │   ├── EmptyState.kt
        │           │   ├── ErrorState.kt
        │           │   └── LoadingIndicator.kt
        │           ├── home/                          # ✅ Shared content components (IssueCard)
        │           ├── createidea/                    # ✅ CreateIdeaScreen, CreateIdeaViewModel
        │           ├── editidea/                      # ✅ EditIdeaScreen, EditIdeaViewModel
        │           ├── myideas/                       # ✅ MyIdeasScreen, MyIdeasViewModel (tag filter, offline check)
        │           ├── ideadetail/                    # ✅ IdeaDetailScreen, IdeaDetailViewModel, CommentItem, CommentInput
        │           ├── createissue/                   # ✅ CreateIssueScreen, CreateIssueViewModel
        │           ├── editissue/                     # ✅ EditIssueScreen, EditIssueViewModel (assignee dropdown)
        │           ├── issuedetail/                   # ✅ IssueDetailScreen, IssueDetailViewModel (AssigneeRow)
        │           └── feature/
        │               ├── auth/
        │               │   ├── GoogleSignInScreen.kt  # ✅
        │               │   ├── ProfileSetupScreen.kt  # ✅
        │               │   └── AuthViewModel.kt       # ✅ (sign-in, setup, sign-out)
        │               ├── home/
        │               │   └── HomeScreen.kt          # ✅ TopAppBar + BottomNav + feed
        │               ├── profile/
        │               │   ├── ProfileViewModel.kt    # ✅
        │               │   ├── ProfileViewScreen.kt   # ✅
        │               │   └── ProfileEditScreen.kt   # ✅ (name, bio, skills, avatar picker)
        │               ├── settings/
        │               │   └── SettingsScreen.kt      # ✅ (sign out + "Mở Idea đã xóa" dialog)
        │               ├── message/                   # (giai đoạn 5)
        │               └── notification/              # (giai đoạn 5)
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
- Polling dùng `viewModelScope.launch { while (isActive) { delay(...); refresh() } }`, không dùng `rememberCoroutineScope` cho tác vụ nền.
- **Quy ước thư mục:** Các màn hình Content (Ideas/Issues) dùng vertical slicing by screen dưới `ui/<screenname>/` (vd: `ui/createidea/`, `ui/ideadetail/`). Pattern `ui/feature/<domain>/` giữ nguyên cho Auth, Home, Profile, Settings.

### Logging
- Dùng **Timber** thay `Log.*` trực tiếp. Debug: `Timber.d(...)`, Warning+: `Timber.w(...)`.
- Release build chỉ plant `ReleaseTree` (log WARN+, không log token/email).

### `core/util/Result.kt`
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: AppException) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```
Đây là kiểu trả về chuẩn cho toàn bộ repository và use case trong dự án.

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
