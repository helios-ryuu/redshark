# GIT.md - Quy ước Gitflow và Commit cho RedShark

Tài liệu này quy định cách đặt commit message, cách tạo nhánh, và lịch commit theo timeline để cả nhóm làm việc đúng mốc.

## 1) Commit Message Convention

Dùng chuẩn **Conventional Commits**:

```text
type(scope): message ngắn gọn ở thì hiện tại
```

### Type bắt buộc
- `feat`: thêm tính năng mới
- `fix`: sửa lỗi
- `refactor`: cải tổ code, không đổi hành vi
- `docs`: cập nhật tài liệu
- `test`: thêm/chỉnh test
- `chore`: việc bảo trì, cấu hình, build, scaffold
- `perf`: tối ưu hiệu năng

### Scope gợi ý
- Theo module: `auth`, `profile`, `idea`, `issue`, `comment`, `notification`, `message`, `lookup`
- Hoặc theo lớp: `ui`, `domain`, `data`, `core`, `build`, `docs`

### Ví dụ
```text
feat(auth): add Google Sign-In flow
feat(profile): enforce first profile completion with displayName 3..50
fix(issue): block invalid CLOSED -> OPEN transition
docs(timeline): align milestone with v0.1.0-auth
chore(init): bootstrap Android project and base docs
```

## 2) Branch Model (Gitflow rút gọn)

Chỉ dùng các nhánh/prefix sau:
- `main`: nhánh phát hành ổn định
- `develop`: nhánh tích hợp chính
- `feature/<name>`: phát triển tính năng
- `release/<version>`: ổn định trước phát hành
- `hotfix/<name>`: vá lỗi khẩn cấp từ `main`

> Khong tao cac nhanh ngoai 5 loai tren.

### Luồng merge
1. `feature/*` tạo từ `develop`, merge về `develop` qua PR.
2. `release/*` tạo từ `develop` khi vào pha release, chỉ nhận fix ổn định.
3. `release/*` merge vào `main` (tag release) và merge ngược về `develop`.
4. `hotfix/*` tạo từ `main`, merge lại `main` va `develop`.

## 3) Quy tắc commit theo timeline

Dựa theo `docs/TIMELINE.md`, chỉ chốt commit milestone ở đúng cửa sổ thời gian:

### Tuần 1 (05/04 -> 11/04) - Phase 1 Foundation
- Loại commit ưu tiên: `chore`, `build`, `docs`
- Không gộp logic feature nghiệp vụ lớn.

### Tuần 2 (12/04 -> 18/04) - Phase 2a Auth core
- Loại commit ưu tiên: `feat(auth)`, `fix(auth)`, `test(auth)`
- Mốc cứng 20/04: hoàn thành Google Auth + persist session.

### Deadline 20/04
- Tạo tag: `v0.1.0-auth`
- Chỉ tạo tag khi nhánh đã ổn định và merge vào `main`.

### Tuần 3 (19/04 -> 25/04) - Profile + Ideas
- Loại commit ưu tiên: `feat(profile)`, `feat(idea)`, `fix(profile|idea)`

### Tuần 4 (26/04 -> 02/05) - Issues + Comments
- Loại commit ưu tiên: `feat(issue|comment)`, `fix(issue|comment)`

### Tuần 5 (03/05 -> 09/05) - Notifications + Messages
- Loại commit ưu tiên: `feat(notification|message)`, `fix(notification|message)`

### Tuần 6 (10/05 -> 17/05) - QA / Cleanup / Release
- Tạo `release/v1.0.0` từ `develop`
- Chỉ nhận `fix`, `docs`, `chore` ổn định phát hành
- Sau kiểm thử: merge `release/v1.0.0` -> `main`, tag `v1.0.0`, rồi merge ngược về `develop`

## 4) Quy tắc chất lượng commit

- Mỗi commit chỉ nên giải quyết 1 ý chính.
- Message ngắn gọn, mô tả đúng thay đổi thực tế.
- Không commit file sinh tự động, file build, hoặc thông tin nhạy cảm.
- PR phải bám đúng phase hiện tại trong timeline.

## 5) Gợi ý lệnh nhanh

```powershell
git checkout develop
git checkout -b feature/auth-google-signin

git add .
git commit -m "feat(auth): add Google Sign-In flow"

git checkout develop
git merge --no-ff feature/auth-google-signin

# Milestone
git checkout main
git tag v0.1.0-auth
```

