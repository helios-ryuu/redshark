# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Common Commands

```bash
# Build
./gradlew assembleDebug

# Lint
./gradlew lint

# Unit tests (JVM, no device required)
./gradlew testDebugUnitTest

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew testDebugUnitTest --tests "com.helios.redshark.domain.usecase.auth.SignInGoogleUseCaseTest"
```

## Architecture

Clean Architecture with vertical feature slicing. Three strict layers — each layer only depends inward:

- **`domain/`** — Pure Kotlin only. No `android.*` or `firebase.*` imports. Contains interfaces (`AuthRepository`, etc.), domain models, and single-responsibility use cases (`<Verb><Noun>UseCase`). This is the source of truth for business rules.
- **`data/`** — Implements domain interfaces. Contains `repository/*Impl.kt`, DTO mappers (`mapper/`), Cloud Firestore source, Cloudflare R2 client (OkHttp + AWS SigV4), and DataStore for local caching.
- **`ui/`** — Jetpack Compose + MVVM. ViewModels expose `StateFlow<UiState>` and Composables consume it via `collectAsStateWithLifecycle()`. Single-activity architecture with Compose Navigation; all routes are string constants in `ui/navigation/Routes.kt`.

**Cross-cutting concerns** live in `core/`: `Result.kt` sealed class (Success/Error/Loading), `AppException`, `ErrorMapper`, and Hilt DI modules.

**DI:** Hilt throughout. `RedSharkApp.kt` is `@HiltAndroidApp`. Modules in `core/di/`: `AppModule`, `FirebaseModule` (FirebaseAuth + FirebaseFirestore), `R2Module`, `NetworkModule`.

## Tech Stack

| Concern | Choice |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Auth | Firebase Authentication (Google Sign-In) |
| Database | Cloud Firestore (NoSQL document DB) |
| Storage | Cloudflare R2 (S3-compatible) |
| Local cache | DataStore (tokens, preferences); Room optional for offline |
| Async | Kotlin Coroutines + Flow |
| Min/Target SDK | 26 / 36 |

## Local Configuration

`local.properties` (gitignored) must contain:

```
sdk.dir=<path>
GOOGLE_WEB_CLIENT_ID=...
GOOGLE_ANDROID_CLIENT_ID=...
CLOUDFLARE_R2_ACCOUNT_ID=...
CLOUDFLARE_R2_ACCESS_KEY_ID=...
CLOUDFLARE_R2_SECRET_ACCESS_KEY=...
CLOUDFLARE_R2_BUCKET=...
CLOUDFLARE_R2_ENDPOINT=...
CLOUDFLARE_R2_PUBLIC_BASE_URL=...
```

`google-services.json` is also gitignored and must be placed in `app/`.

## Git Workflow

Gitflow model. Branch types: `main`, `develop`, `feature/<name>`, `release/<version>`, `hotfix/<name>`.

- All feature work branches from `develop` and merges back via PR.
- `main` only accepts merges from `release/*` or `hotfix/*`.
- Prefer `git switch` over `git checkout`; use `git pull --ff-only` to avoid accidental merge commits when updating.
- Use `--no-ff` when merging feature branches into `develop` to preserve merge history.

**Commit format:** Conventional Commits — `type(scope): message in present tense`

Types: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `perf`

Scopes by feature: `auth`, `profile`, `idea`, `issue`, `comment`, `notification`, `message`, `lookup`
Scopes by layer: `ui`, `domain`, `data`, `core`, `build`, `docs`

## Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Package | lowercase, no underscores | `com.helios.redshark.data.repository` |
| Interface | PascalCase, no `I` prefix | `IdeaRepository` |
| Implementation | `<Interface>Impl` | `IdeaRepositoryImpl` |
| Use case | `<Verb><Noun>UseCase` | `CreateIdeaUseCase` |
| Composable / file | PascalCase, file = class name | `IdeaDetailScreen.kt` |
| ViewModel | `<Feature>ViewModel` | `IdeaDetailViewModel` |

## Project Timeline Context

9-week project (2026-03-16 → 2026-05-17), 3-person team:
- **Sỹ** — Lead, foundation, auth (WBS 3.x)
- **Hải** — Content features: ideas, issues (WBS 4.x)
- **Nam** — Interaction features: notifications, messages (WBS 5.x)

Hard deadline: at least one `feat(auth)` commit merged to `develop` before 2026-04-21. Target coverage ≥ 60% for `domain/` and `data/` layers.
