<p align="center">
  <a href="https://www.uit.edu.vn/" title="Trường Đại học Công nghệ Thông tin">
    <img src="https://i.imgur.com/WmMnSRt.png" alt="Trường Đại học Công nghệ Thông tin | University of Information Technology">
  </a>
</p>

<h1 align="center"><b>NT118.Q22 - RedShark Android Native (Kotlin)</b></h1>

## Mục lục
- [Tổng quan](#tổng-quan)
- [Thông tin học phần](#thông-tin-học-phần)
- [Thành viên nhóm](#thành-viên-nhóm)
- [Kiến trúc](#kiến-trúc)
- [Phạm vi chức năng](#phạm-vi-chức-năng)
- [Công nghệ](#công-nghệ)
- [Thiết lập môi trường](#thiết-lập-môi-trường)
- [Quy trình phát triển](#quy-trình-phát-triển)

---

## Tổng quan

**RedShark Android Native** là ứng dụng cộng tác cho nhóm nhỏ, tập trung vào theo dõi **Idea/Issue** và tương tác trong nhóm qua **Comment, Notification, Message**.

Định hướng hệ thống:
- 100% Kotlin Android Native + Jetpack Compose
- Kiến trúc Clean Architecture + MVVM + UDF
- Firebase Authentication cho xác thực
- Firebase Data Connect (Cloud SQL PostgreSQL) cho dữ liệu nghiệp vụ
- Cloudflare R2 (S3-compatible) cho media/avatar

Mục tiêu chất lượng chính:
- Cold start đến Home (đã login) <= 3s
- Crash-free session rate >= 99%
- Upload avatar 1MB <= 3s trên 4G
- Unit test coverage (domain + data) >= 60%

---

## Thông tin học phần

| Hạng mục             | Thông tin                                         |
|----------------------|---------------------------------------------------|
| Môn học              | Phát triển ứng dụng trên thiết bị di động (NT118) |
| Lớp                  | NT118.Q22 - HK2 2025-2026                         |
| Giảng viên           | ThS. Trần Hồng Nghi - nghith@uit.edu.vn           |
| Đề tài               | RedShark Android Native (Kotlin)                  |
| Thời gian dự án      | 05/04/2026 -> 17/05/2026 (6 tuần)                 |
| Milestone quan trọng | Hoàn thiện module Authentication trước 20/04/2026 |

---

## Thành viên nhóm

| STT |     MSSV | Họ và Tên      | Vai trò                                  | GitHub                                        |
|-----|---------:|----------------|------------------------------------------|-----------------------------------------------|
| 1   | 23521367 | Ngô Tiến Sỹ    | Android Developer, PM / QA / Docs        | [helios-ryuu](https://github.com/helios-ryuu) |
| 2   | 24520442 | Phạm Tuấn Hải  | Android Developer                        | [haiphamt](https://github.com/haiphamt)       |
| 3   | 23520982 | Nguyễn Văn Nam | Android Developer                        | [Sinister-VN](https://github.com/Sinister-VN) |

---

## Kiến trúc

```
┌───────────────────────────────────────────────┐
│ Android Native App (Kotlin + Compose)         │
│ MVVM + Clean Architecture + Hilt              │
│ UDF state flow + Coroutines + Flow            │
└───────────────┬───────────────────────┬───────┘
                │ Firebase Auth         │ Data Connect SDK
                ▼                       ▼
┌──────────────────────────┐   ┌─────────────────────────────┐
│ Firebase Authentication  │   │ Firebase Data Connect       │
│ - Google Sign-In         │   │ - GraphQL connectors        │
│ - Firebase session       │   │ - Auth directives           │
└──────────────────────────┘   └──────────────┬──────────────┘
                                              ▼
                                   ┌─────────────────────────┐
                                   │ Cloud SQL PostgreSQL    │
                                   │ (schema theo docs)      │
                                   └─────────────────────────┘
                                              │
                                              ▼
                                   ┌─────────────────────────┐
                                   │ Cloudflare R2           │
                                   │ Avatar/Media storage    │
                                   └─────────────────────────┘
```

Nguyên tắc kiến trúc:
- Tách rõ 3 layer: `data`, `domain`, `ui`
- Domain layer thuần Kotlin, không phụ thuộc Android SDK
- Repository interface ở domain, implementation ở data
- ViewModel expose `StateFlow<UiState>`, UI consume theo UDF
- Security/ownership enforce server-side qua Data Connect auth rule

---

## Phạm vi chức năng

| Module         | Chức năng chính                                                                           |
|----------------|-------------------------------------------------------------------------------------------|
| Authentication | Đăng nhập Google, hoàn thiện hồ sơ lần đầu (`displayName` 3..50), persist session, logout |
| Profile        | Xem/sửa profile, chọn skills, upload avatar R2, xem profile công khai                     |
| Ideas          | CRUD idea, soft delete, trạng thái ACTIVE/CLOSED/CANCELLED                                |
| Issues         | CRUD issue, soft delete, state machine, giới hạn 20 issue active/user                     |
| Comments       | Bình luận trên idea (1..1000 ký tự), sắp xếp theo thời gian                               |
| Notifications  | Danh sách thông báo, badge chưa đọc, mark read, xử lý collab request                      |
| Messages       | Hội thoại DIRECT 1-1, gửi/nhận tin nhắn, tránh trùng conversation                         |
| Lookup         | Danh sách tags/skills PUBLIC, hỗ trợ filter ideas/issues                                  |

Out-of-scope:
- iOS/Web
- Push notification FCM (hiện tại dùng polling)
- Group conversation (> 2 members)
- Payment/subscription

---

## Công nghệ

| Layer        | Công nghệ                                    |
|--------------|----------------------------------------------|
| Mobile       | Kotlin + Android Native                      |
| UI           | Jetpack Compose + Material 3                 |
| Architecture | Clean Architecture + MVVM + UDF              |
| DI           | Hilt                                         |
| Async        | Kotlin Coroutines + Flow                     |
| Auth         | Firebase Authentication                      |
| Data         | Firebase Data Connect + Cloud SQL PostgreSQL |
| Storage      | Cloudflare R2 (S3-compatible)                |
| Build        | Gradle Kotlin DSL                            |
| Quality      | Ktlint + Detekt + Unit tests                 |

---

## Thiết lập môi trường

### 1) Yêu cầu
- Android Studio Iguana+ (khuyến nghị JDK 17)
- Android SDK: `minSdk 26`
- Firebase CLI
- Tài khoản Firebase project và Cloudflare R2

### 2) Cấu hình dịch vụ
- Thiết lập Google Sign-In cho Android app (SHA-1 debug/release + OAuth Client ID)
- Thiết lập Firebase Data Connect connector
- Thiết lập Cloudflare R2 bucket và endpoint cho avatar/media
- Danh sách biến cấu hình hiện dùng: xem `docs/SECRET.md`

### 3) Data Connect workflow
```powershell
firebase emulators:start --only dataconnect
firebase dataconnect:sdk:generate
firebase deploy --only dataconnect
```

### 4) Build và quality gates
```powershell
./gradlew lint
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

---

## Quy trình phát triển

- Quy trình iterative theo 5 phase:
  - Phase 1: Foundation
  - Phase 2: Authentication + Profile
  - Phase 3: Ideas + Issues + Comments
  - Phase 4: Notifications + Messages
  - Phase 5: QA, Cleanup, Release
- Branching: feature branch + pull request vào `main`
- Commit message theo Conventional Commits:

```text
feat(auth): add Google Sign-In flow
fix(issue): prevent invalid CLOSED -> OPEN transition
refactor(data): extract FDC error mapper
docs: update timeline and test checklist
```

Review checklist bắt buộc:
- Không hardcode thông tin nhạy cảm
- Có xử lý error state + retry
- Không vi phạm state machine nghiệp vụ
- Lint/test xanh trước khi merge

Tài liệu chi tiết:
- Tổng quan tài liệu: `docs/README.md`
- Charter: `docs/PROJECT_CHARTER.md`
- Kiến trúc mã nguồn: `docs/STRUCTURE.md`
- Requirement: `docs/REQUIREMENT-1-FUNCTIONAL.md`, `docs/REQUIREMENT-2-NONFUNCTIONAL.md`
- Quy trình nghiệp vụ: `docs/PROCESS-1-AUTH.md`, `docs/PROCESS-2-CONTENT.md`, `docs/PROCESS-3-INTERACTION.md`
- Test checklist: `docs/CHECK-1-AUTH.md`, `docs/CHECK-2-CONTENT.md`, `docs/CHECK-3-INTERACTION.md`
- Báo cáo: `docs/REPORT.md`
