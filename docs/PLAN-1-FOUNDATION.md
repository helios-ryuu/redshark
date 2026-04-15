# PLAN-1-FOUNDATION.md — Phase 1: Nền tảng dự án

**Thời gian:** 05/04/2026 – 11/04/2026 (Tuần 1)
**Mục tiêu:** Thiết lập nền tảng Kotlin Android Native, sẵn sàng cho việc code feature.

## 1. Deliverables
- [ ] Dự án Android Studio khởi tạo, build thành công (`./gradlew assembleDebug`)
- [ ] Cấu trúc thư mục tuân thủ [STRUCTURE.md](STRUCTURE.md)
- [ ] Firebase Auth + Data Connect SDK kết nối thành công (ping `GetMe`)
- [ ] Cloudflare R2 upload thử nghiệm thành công 1 file
- [ ] Tài liệu coding convention được commit
- [ ] Hoàn tất baseline local build, kế hoạch CI sẽ tách riêng sau

## 2. Công việc chi tiết

### 2.1 Khởi tạo dự án
- Android Studio Iguana+, Kotlin 2.3.20, AGP 9.1.1, Gradle 9.4.1
- `minSdk = 26`, `targetSdk = 36`, `compileSdk = 36`
- Gradle Kotlin DSL, version catalog `libs.versions.toml`
- Bật Compose, Coroutines

### 2.2 Dependencies chính
```toml
[versions]
agp = "9.1.1"
kotlin = "2.3.20"
compose-bom = "2026.03.01"
core-ktx = "1.18.0"
lifecycle-runtime-ktx = "2.10.0"
activity-compose = "1.13.0"
```

### 2.3 Design Pattern chuẩn dự án

#### MVVM + Clean Architecture
- **View (Composable):** stateless, nhận `UiState` + `onEvent`.
- **ViewModel:** giữ `StateFlow<UiState>`, expose intent functions.
- **UseCase:** `operator fun invoke()` duy nhất, SRP.
- **Repository interface (domain):** định nghĩa contract.
- **RepositoryImpl (data):** gọi FDC / R2 / local, map DTO.

#### Unidirectional Data Flow (UDF)
```
UI Event ──► ViewModel.onEvent() ──► UseCase ──► Repository
                │                                     │
                ▼                                     ▼
          StateFlow<UiState> ◄────── map(Result<T>) ──┘
```

### 2.4 Build baseline (local)
Chuỗi lệnh baseline đang dùng để xác nhận dự án sẵn sàng code feature:
1. `./gradlew lint`
2. `./gradlew testDebugUnitTest`
3. `./gradlew assembleDebug`

> Kế hoạch CI/CD sẽ bổ sung ở phase sau khi hoàn tất baseline build.

### 2.5 Kết nối hạ tầng
- `google-services.json` đặt trong `app/` (gitignored)
- Chạy `firebase dataconnect:sdk:generate --output app/src/main/java/com/helios/redshark/data/remote/dataconnect/generated`
- Smoke test: `DataConnectSource.getMe()` trả 401 (chưa login) là OK

### 2.6 Code Style (bắt buộc toàn dự án)

> **Nguyên tắc:** Ưu tiên **đơn giản, tường minh, dễ hiểu**. Không tối ưu sớm, không abstract khi chưa có 3 use case thực tế.

**Kotlin Naming Convention (Google + Kotlinlang official):**

| Mục               | Quy tắc                | Ví dụ                                 |
|-------------------|------------------------|---------------------------------------|
| Package           | lowercase, không `_`   | `com.helios.redshark.ui.feature.idea` |
| Class / Interface | PascalCase, danh từ    | `IdeaDetailScreen`, `AuthRepository`  |
| Hàm               | camelCase, động từ     | `fetchIdeas()`, `onSubmitClicked()`   |
| Biến              | camelCase              | `currentUser`, `isLoading`            |
| Constant          | UPPER_SNAKE_CASE       | `MAX_ACTIVE_ISSUES = 20`              |
| Composable        | PascalCase (như class) | `IdeaCard()`, `PrimaryButton()`       |
| File              | tên class chính        | `IdeaDetailViewModel.kt`              |
| Enum values       | UPPER_SNAKE_CASE       | `OPEN`, `IN_PROGRESS`                 |
| Resource (XML)    | lowercase_snake        | `ic_arrow_back.xml`, `color_primary`  |

**Formatting:**
- Dùng `ktlint` + `detekt` (fail build khi violation)
- Line length ≤ 120
- 4 space indent, không tab
- Trailing comma bật
- Import sắp xếp alphabet

**API Convention:**
- **GraphQL (FDC):** `PascalCase` cho Query/Mutation name (đã áp dụng), field `camelCase`.
- **REST (R2):** tuân thủ chuẩn S3 API, URL dùng `kebab-case`.

**Commit message (Conventional Commits):**
```
feat(auth): add Google Sign-In flow
fix(idea): prevent crash when tagIds is null
refactor(data): extract FDC error mapper
docs: update SCHEMA with notifications table
```

## 3. DoD (Definition of Done)
- [ ] `./gradlew assembleDebug` xanh
- [ ] README.md có hướng dẫn setup < 10 bước
- [ ] Lint 0 warning nghiêm trọng
- [ ] Đã merge PR `chore: scaffold project`
