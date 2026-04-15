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
        │       │   │   ├── AppModule.kt
        │       │   │   ├── FirebaseModule.kt
        │       │   │   ├── R2Module.kt
        │       │   │   └── NetworkModule.kt
        │       │   ├── util/
        │       │   │   ├── Result.kt              # Sealed class Success/Error/Loading
        │       │   │   ├── DateUtils.kt
        │       │   │   └── ImageCompressor.kt
        │       │   ├── error/
        │       │   │   ├── AppException.kt
        │       │   │   └── ErrorMapper.kt
        │       │   └── constants/
        │       │       └── Constants.kt
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
        │       │   │   │   └── DataConnectSource.kt
        │       │   │   ├── dataconnect/
        │       │   │   │   ├── generated/         # SDK gen từ firebase dataconnect:sdk:generate
        │       │   │   │   └── Connector.kt
        │       │   │   └── r2/
        │       │   │       ├── R2Client.kt        # OkHttp + AWS SigV4
        │       │   │       └── R2UploadService.kt
        │       │   ├── repository/                # Implement interface từ domain
        │       │   │   ├── AuthRepositoryImpl.kt
        │       │   │   ├── IdeaRepositoryImpl.kt
        │       │   │   ├── IssueRepositoryImpl.kt
        │       │   │   ├── CommentRepositoryImpl.kt
        │       │   │   ├── NotificationRepositoryImpl.kt
        │       │   │   ├── MessageRepositoryImpl.kt
        │       │   │   ├── ProfileRepositoryImpl.kt
        │       │   │   └── MediaRepositoryImpl.kt
        │       │   └── mapper/                    # DTO ↔ Domain model
        │       │       ├── UserMapper.kt
        │       │       ├── IdeaMapper.kt
        │       │       └── IssueMapper.kt
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
        │       │   │   ├── AuthRepository.kt
        │       │   │   ├── IdeaRepository.kt
        │       │   │   └── ...
        │       │   └── usecase/
        │       │       ├── auth/
        │       │       │   ├── SignInGoogleUseCase.kt
        │       │       │   ├── CompleteFirstProfileUseCase.kt
        │       │       │   ├── SignOutUseCase.kt
        │       │       │   └── ObserveAuthStateUseCase.kt
        │       │       ├── idea/
        │       │       │   ├── GetIdeasUseCase.kt
        │       │       │   ├── CreateIdeaUseCase.kt
        │       │       │   └── ...
        │       │       ├── issue/
        │       │       ├── comment/
        │       │       ├── notification/
        │       │       ├── message/
        │       │       └── profile/
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
        │               │   ├── google/
        │               │   │   ├── GoogleSignInScreen.kt
        │               │   │   └── GoogleSignInViewModel.kt
        │               │   ├── profile_setup/      # Luồng hoàn thiện hồ sơ lần đầu
        │               │   │   ├── ProfileSetupScreen.kt
        │               │   │   └── ProfileSetupViewModel.kt
        │               │   └── AuthState.kt
        │               ├── home/
        │               │   ├── HomeScreen.kt
        │               │   └── HomeViewModel.kt
        │               ├── idea/
        │               │   ├── list/
        │               │   ├── detail/
        │               │   ├── create/
        │               │   └── edit/
        │               ├── issue/
        │               ├── comment/
        │               ├── message/
        │               │   ├── list/
        │               │   └── conversation/
        │               ├── notification/
        │               ├── profile/
        │               │   ├── view/
        │               │   └── edit/
        │               └── settings/
        │
        ├── test/                                  # Unit tests (JVM)
        │   └── java/com/helios/redshark/
        │       ├── domain/usecase/
        │       └── data/repository/
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

### Domain Layer
- **Responsibility:** Business logic thuần, use cases single-responsibility.
- **Pure Kotlin**, không import `android.*` hay `firebase.*`.
- Chỉ khai báo **interface** `Repository`; impl thuộc về Data layer.

### UI Layer
- **Responsibility:** Jetpack Compose UI + `ViewModel` (MVVM).
- State flow: `ViewModel` expose `StateFlow<UiState>`; Composable `collectAsStateWithLifecycle()`.
- Navigation: single-activity, Compose Navigation, route strings trong `Routes.kt`.

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
