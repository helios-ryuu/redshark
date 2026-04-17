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
├── docs/                              # Tài liệu dự án (charter, requirement, plan, process, check, report, git)
└── app/
    ├── build.gradle.kts               # App module config (Compose, Hilt, Firebase, ...)
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
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
        │       │   │   └── RepositoryModule.kt    # ✅ bind Auth/Profile/Media/Firestore
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
        │       │   │   │   ├── FirestoreSource.kt         # ✅ Interface (Firestore operations)
        │       │   │   │   ├── FirestoreSourceImpl.kt     # ✅ Firestore SDK calls (upsertUser, getUser, updateProfile)
        │       │   │   │   └── dto/
        │       │   │   │       └── UserDto.kt             # ✅
        │       │   │   │
        │       │   │   └── r2/
        │       │   │       └── R2Client.kt            # OkHttp + AWS SigV4
        │       │   ├── repository/                # Implement interface từ domain
        │       │   │   ├── AuthRepositoryImpl.kt      # ✅ sign-in + upsertUser Firestore
        │       │   │   ├── ProfileRepositoryImpl.kt   # ✅ FirestoreSource wired
        │       │   │   ├── MediaRepositoryImpl.kt     # ✅ R2Client upload
        │       │   │   ├── IdeaRepositoryImpl.kt      # (giai đoạn 3)
        │       │   │   ├── IssueRepositoryImpl.kt     # (giai đoạn 3)
        │       │   │   ├── CommentRepositoryImpl.kt   # (giai đoạn 3)
        │       │   │   ├── NotificationRepositoryImpl.kt # (giai đoạn 4)
        │       │   │   └── MessageRepositoryImpl.kt   # (giai đoạn 4)
        │       │   └── mapper/                    # DTO ↔ Domain model
        │       │       ├── UserMapper.kt              # ✅ FirebaseUser + UserDto → User
        │       │       ├── IdeaMapper.kt              # (giai đoạn 3)
        │       │       └── IssueMapper.kt             # (giai đoạn 3)
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
        │       │   │   ├── ProfileRepository.kt       # ✅
        │       │   │   ├── MediaRepository.kt         # ✅
        │       │   │   ├── IdeaRepository.kt          # (giai đoạn 3)
        │       │   │   └── ...
        │       │   └── usecase/
        │       │       ├── auth/
        │       │       │   ├── SignInGoogleUseCase.kt         # ✅
        │       │       │   ├── CompleteFirstProfileUseCase.kt # ✅
        │       │       │   ├── SignOutUseCase.kt              # ✅
        │       │       │   ├── ObserveAuthStateUseCase.kt     # ✅
        │       │       │   ├── UpdateProfileUseCase.kt        # ✅
        │       │       │   └── UploadAvatarUseCase.kt         # ✅
        │       │       ├── idea/
        │       │       │   ├── GetIdeasUseCase.kt     # (giai đoạn 3)
        │       │       │   ├── CreateIdeaUseCase.kt   # (giai đoạn 3)
        │       │       │   └── ...
        │       │       ├── issue/
        │       │       ├── comment/
        │       │       ├── notification/
        │       │       └── message/
        │       │
        │       └── ui/                            # Presentation layer
        │           ├── theme/
        │           │   ├── Color.kt
        │           │   ├── Theme.kt
        │           │   ├── Type.kt
        │           │   └── Shape.kt
        │           ├── navigation/
        │           │   ├── NavGraph.kt
        │           │   ├── Routes.kt
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
        │           └── feature/
        │               ├── auth/
        │               │   ├── GoogleSignInScreen.kt  # ✅
        │               │   ├── ProfileSetupScreen.kt  # ✅
        │               │   └── AuthViewModel.kt       # ✅ (sign-in, setup, sign-out)
        │               ├── home/
        │               │   └── HomeScreen.kt          # ✅ TopAppBar + profile/settings nav
        │               ├── profile/
        │               │   ├── ProfileViewModel.kt    # ✅
        │               │   ├── ProfileViewScreen.kt   # ✅
        │               │   └── ProfileEditScreen.kt   # ✅ (name, bio, skills, avatar picker)
        │               ├── settings/
        │               │   └── SettingsScreen.kt      # ✅ (sign out + delete placeholder)
        │               ├── idea/                      # (giai đoạn 3)
        │               ├── issue/                     # (giai đoạn 3)
        │               ├── comment/                   # (giai đoạn 3)
        │               ├── message/                   # (giai đoạn 4)
        │               └── notification/              # (giai đoạn 4)
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
- Navigation: single-activity, Compose Navigation, route strings trong `Routes.kt`.
- Polling dùng `viewModelScope.launch { while (isActive) { delay(...); refresh() } }`, không dùng `rememberCoroutineScope` cho tác vụ nền.

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
