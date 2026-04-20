# PLAN-1-FOUNDATION.md — Giai đoạn 1: Nền tảng dự án

**Thời gian:** 16/03/2026 – 15/04/2026 (Giai đoạn nền tảng)
**Mục tiêu:** Thiết lập nền tảng Kotlin Android Native, sẵn sàng cho việc phát triển tính năng.
**WBS tham chiếu:** [WBS.md](WBS.md) — nhóm công việc `2.0`.

**Phân công theo WBS:**
- Phụ trách nhóm nền tảng: **Sỹ** (lập trình chính, chịu trách nhiệm commit chính).
- Thành viên phối hợp: **Nam**, **Hải**.

## 1. Sản phẩm bàn giao
- [x] Dự án Android Studio khởi tạo, biên dịch thành công (`./gradlew assembleDebug`) — commit `chore(init)` ngày 15/04
- [x] Cấu trúc thư mục tuân thủ [STRUCTURE.md](STRUCTURE.md) — scaffold đã được commit
- [x] Tài liệu quy ước mã nguồn được commit — toàn bộ `docs/` đã có trên `main`
- [x] Hoàn tất kiểm tra biên dịch cục bộ — Compose cơ bản build thành công
- [ ] Firebase Auth + Data Connect SDK kết nối thành công (ping `GetMe`) — **chuyển sang giai đoạn 2**
- [ ] Cloudflare R2 upload thử nghiệm thành công 1 file — **chuyển sang giai đoạn 2**

## 2. Công việc chi tiết

### 2.1 Khởi tạo dự án
- Android Studio Iguana+, Kotlin 2.3.20, AGP 9.1.1, Gradle 9.4.1
- `minSdk = 26`, `targetSdk = 36`, `compileSdk = 36`
- Gradle Kotlin DSL, version catalog `libs.versions.toml`
- Bật Compose, Coroutines

### 2.2 Phụ thuộc giai đoạn 1 (đã có trong `libs.versions.toml`)
```toml
[versions]
agp = "9.1.1"
kotlin = "2.3.20"
composeBom = "2026.03.01"
coreKtx = "1.18.0"
lifecycleRuntimeKtx = "2.10.0"
activityCompose = "1.13.0"
```

> Các phụ thuộc cho Hilt, Firebase, Credential Manager, Navigation-Compose và R2 (OkHttp) sẽ được bổ sung vào `libs.versions.toml` tại đầu giai đoạn 2. Xem chi tiết tại [PLAN-2-AUTH.md](PLAN-2-AUTH.md).

### 2.3 Mẫu kiến trúc chuẩn dự án

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

### 2.4 Kiểm tra biên dịch nền tảng (cục bộ)
Chuỗi lệnh đang dùng để xác nhận dự án sẵn sàng phát triển tính năng:
1. `./gradlew lint`
2. `./gradlew testDebugUnitTest`
3. `./gradlew assembleDebug`

> Kế hoạch CI/CD sẽ bổ sung ở giai đoạn sau khi hoàn tất kiểm tra biên dịch nền tảng.

### 2.5 Kết nối hạ tầng
- `google-services.json` đặt trong `app/` (gitignored)
- Chạy `firebase dataconnect:sdk:generate --output app/src/main/java/com/helios/redshark/data/remote/dataconnect/generated`
- Kiểm tra nhanh: `DataConnectSource.getMe()` trả 401 (chưa đăng nhập) được xem là đúng kỳ vọng

### 2.6 Quy chuẩn mã nguồn (bắt buộc toàn dự án)

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
- Mục tiêu: `ktlint` + `detekt` (fail build khi violation) — **chưa cấu hình, sẽ bổ sung ở giai đoạn 5**
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

## 3. Tiêu chí hoàn thành
- [x] `./gradlew assembleDebug` xanh
- [x] `docs/README.md` có mục lục tài liệu dự án
- [ ] Lint 0 warning nghiêm trọng — kiểm tra khi chạy `./gradlew lint`
- [x] Commit khởi tạo `chore(init)` đã có trên `main` (15/04/2026)
- [ ] Nhánh `develop` được tạo từ `main` và push lên remote — **việc đầu tiên của giai đoạn 2**
