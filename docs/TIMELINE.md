# TIMELINE.md — Tiến độ dự án

**Thời gian:** 05/04/2026 → 17/05/2026 (6 tuần, 43 ngày).
**Nhóm:** 3 thành viên — **Sỹ (PM)**, **Nam**, **Hải**.
**Milestone quan trọng:** Trước **20/04/2026** có commit hoàn thiện module **Authentication**.

## 1. Sơ đồ giai đoạn

```
Tuần 1 ─ 05/04 → 11/04 │ Phase 1: Foundation
Tuần 2 ─ 12/04 → 18/04 │ Phase 2a: Auth core          ◄── Milestone 20/04
Tuần 3 ─ 19/04 → 25/04 │ Phase 2b Profile + Phase 3a Ideas
Tuần 4 ─ 26/04 → 02/05 │ Phase 3b: Issues + Comments
Tuần 5 ─ 03/05 → 09/05 │ Phase 4: Notifications + Messages
Tuần 6 ─ 10/05 → 17/05 │ Phase 5: QA, cleanup, release
```

## 2. Bảng phân công theo tuần

### Tuần 1 — 05/04 → 11/04 — **Phase 1: Foundation**

| Thành viên  | Công việc                                                                                                                                      |
|-------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| **Sỹ (PM)** | Lập PROJECT_CHARTER, TIMELINE, PLAN-1; tạo repo GitHub + rule bảo vệ branch; thiết lập Firebase project + R2 bucket; phân quyền team           |
| **Nam**     | Init Android Studio project (Kotlin + Compose + Hilt); cấu hình `libs.versions.toml`; xây theme (Color, Type, Shape); scaffold navigation      |
| **Hải**     | Thiết lập baseline build local; tích hợp Firebase SDK + Data Connect SDK generate; smoke test R2 putObject; viết `Result.kt`, `ErrorMapper.kt` |

### Tuần 2 — 12/04 → 18/04 — **Phase 2a: Authentication** *(milestone 20/04)*

| Thành viên | Công việc                                                                                                                                                            |
|------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Sỹ**     | Review PR Auth; viết CHECK-1-AUTH; test thủ công TC-A01..TC-A10; tổng hợp báo cáo tuần                                                                               |
| **Nam**    | UI: `GoogleSignInScreen`, `ProfileSetupScreen`, `AuthViewModel`; navigation auth graph; error dialog                                                                 |
| **Hải**    | `FirebaseAuthSource`, `AuthRepositoryImpl`; UseCases `SignInGoogleUseCase`, `CompleteFirstProfileUseCase`, `SignOutUseCase`; `GoogleSignInHelper` Credential Manager |

**🎯 Deadline 20/04/2026:** Commit tag `v0.1.0-auth` — Auth Google-only + hoàn thiện hồ sơ lần đầu + persist session.

### Tuần 3 — 19/04 → 25/04 — **Phase 2b Profile + Phase 3a Ideas**

| Thành viên | Công việc                                                                                                                              |
|------------|----------------------------------------------------------------------------------------------------------------------------------------|
| **Sỹ**     | Review + test TC-A11..TC-A20 (Profile/Avatar); cập nhật PROCESS, CHECK; demo tuần                                                      |
| **Nam**    | UI: `ProfileViewScreen`, `ProfileEditScreen`, `SkillSelect`, `Avatar` composable; `MyIdeasScreen`, `CreateIdeaScreen`, FAB             |
| **Hải**    | `R2Client` SigV4, `MediaRepositoryImpl`, `UploadAvatarUseCase`, image compressor; `IdeaRepositoryImpl`, CreateIdea/UpdateIdea UseCases |

### Tuần 4 — 26/04 → 02/05 — **Phase 3b: Issues + Comments**

| Thành viên | Công việc                                                                                                                |
|------------|--------------------------------------------------------------------------------------------------------------------------|
| **Sỹ**     | Test toàn bộ CHECK-2-CONTENT; viết PROCESS-2; điều phối; chuẩn bị mid-project demo                                       |
| **Nam**    | UI: `IdeaDetailScreen`, `IssueDetailScreen`, `CreateIssueScreen`, `CommentSection`, status dropdown, state machine       |
| **Hải**    | `IssueRepositoryImpl` với ràng buộc 20 active; `CommentRepositoryImpl`; validation state machine server-side/client-side |

### Tuần 5 — 03/05 → 09/05 — **Phase 4: Notifications + Messages**

| Thành viên | Công việc |
|-----------|-----------|
| **Sỹ** | Test CHECK-3-INTERACTION; kiểm tra business rule Collab Request/Accept; cập nhật báo cáo |
| **Nam** | UI: `NotificationListScreen` + badge; `ConversationListScreen`, `ConversationScreen`, `ConversationNewScreen`; polling hook |
| **Hải** | `NotificationRepositoryImpl`, `MessageRepositoryImpl`; `FindOrCreateDirectConversationUseCase`; accept/reject collab logic |

### Tuần 6 — 10/05 → 17/05 — **Phase 5: QA, Cleanup, Release**

| Ngày | Thành viên | Công việc |
|------|-----------|-----------|
| 10–12/05 | Cả nhóm | Regression test full CHECK-1/2/3 trên 3 thiết bị; fix bug P0/P1 |
| 13/05 | Hải | Dọn log, xóa file rác, chạy `gitleaks`, rà R8/ProGuard |
| 14/05 | Nam | Polish UI: dark mode, empty/error state, accessibility |
| 15/05 | Sỹ | Generate signed APK; viết REPORT.md phần kết quả; slide demo |
| 16/05 | Cả nhóm | Smoke test APK release; tag `v1.0.0`; upload GitHub Release |
| 17/05 | Sỹ | Nộp bài; dự phòng hotfix |

## 3. Deliverables từng tuần

| Tuần | Deliverable |
|------|-------------|
| 1 | Repo có scaffold, baseline build local, Firebase/R2 kết nối |
| 2 | APK debug có Auth Google-only + hoàn thiện hồ sơ lần đầu, tag `v0.1.0-auth` |
| 3 | Profile + Ideas CRUD |
| 4 | Issues + Comments CRUD, state machine |
| 5 | Notifications + Messages |
| 6 | APK release signed + báo cáo + slide |

## 4. Risk buffer

- Mỗi Phase có **1 ngày buffer** cuối tuần cho fix bug.
- Nếu Milestone 20/04 trượt: giữ Google Sign-In + persist session, dời avatar polish sang Phase 3.
