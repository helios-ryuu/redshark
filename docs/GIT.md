# GIT.md - Quy ước Gitflow và commit cho RedShark

Tài liệu này quy định cách đặt thông điệp commit, cách tạo nhánh, và lịch commit theo tiến độ để cả nhóm làm việc đúng mốc.

## 1) Quy ước thông điệp commit

Dùng chuẩn **Conventional Commits**:

```text
type(scope): message ngắn gọn ở thì hiện tại
```

### Loại bắt buộc
- `feat`: thêm tính năng mới
- `fix`: sửa lỗi
- `refactor`: cải tổ code, không đổi hành vi
- `docs`: cập nhật tài liệu
- `test`: thêm/chỉnh test
- `chore`: việc bảo trì, cấu hình, biên dịch, khởi tạo khung
- `perf`: tối ưu hiệu năng

### Phạm vi gợi ý
- Theo nhóm chức năng: `auth`, `profile`, `idea`, `issue`, `comment`, `notification`, `message`, `lookup`
- Hoặc theo lớp: `ui`, `domain`, `data`, `core`, `build`, `docs`

### Ví dụ
```text
feat(auth): add Google Sign-In flow
feat(profile): enforce first profile completion with displayName 3..50
fix(issue): block invalid CLOSED -> OPEN transition
docs(timeline): align milestone with v0.1.0-auth
chore(init): bootstrap Android project and base docs
```

## 2) Mô hình nhánh (Gitflow rút gọn)

Chỉ dùng các nhánh/prefix sau:
- `main`: nhánh phát hành ổn định
- `develop`: nhánh tích hợp chính
- `feature/<name>`: phát triển tính năng
- `release/<version>`: ổn định trước phát hành
- `hotfix/<name>`: vá lỗi khẩn cấp từ `main`

> Không tạo nhánh ngoài 5 loại nêu trên.

### Luồng hợp nhất nhánh
1. `feature/*` tạo từ `develop`, hợp nhất về `develop` qua PR.
2. `release/*` tạo từ `develop` khi vào pha release, chỉ nhận fix ổn định.
3. `release/*` hợp nhất vào `main` (gắn nhãn phát hành) và hợp nhất ngược về `develop`.
4. `hotfix/*` tạo từ `main`, hợp nhất lại `main` và `develop`.

## 3) Quy tắc commit theo tiến độ

Dựa theo `docs/TIMELINE.md`, chỉ chốt commit mốc chính ở đúng cửa sổ thời gian:
Để truy vết theo kế hoạch, mô tả PR/commit nên nêu mã WBS liên quan (ví dụ: `WBS 3.2.1`), đối chiếu tại [WBS.md](WBS.md).

Quy ước phụ trách theo WBS:
- Nhóm `3.0` (Auth/Profile): **Sỹ** chịu trách nhiệm commit chính.
- Nhóm `4.0` (Content): **Hải** chịu trách nhiệm commit chính.
- Nhóm `5.0` (Interaction): **Nam** chịu trách nhiệm commit chính.
- Thành viên phối hợp vẫn có thể commit fix/docs/test khi được người phụ trách nhóm thống nhất trong PR.

### Tuần 1-4 (16/03 -> 12/04) - Khảo sát + chuẩn bị nền tảng
- Loại commit ưu tiên: `docs`, `chore(build)`, `chore(structure)`
- Tập trung tài liệu, cấu trúc, biên dịch nền tảng cục bộ; chưa đẩy tính năng nghiệp vụ lớn.

### Tuần 5 (13/04 -> 20/04) - Giai đoạn 2a lõi xác thực
- Mốc cứng 15/04: commit khởi tạo dự án (`chore(init)`) do Sỹ thực hiện.
- Loại commit ưu tiên: `feat(auth)`, `fix(auth)`, `test(auth)`
- Mốc cứng trước 21/04: có ít nhất 1 commit `feat(auth): ...`.

### Hạn cuối 20/04
- Bắt buộc: đã có commit tính năng xác thực trên nhánh tích hợp.
- Khuyến nghị: tạo nhãn `v0.1.0-auth` khi nhánh đã ổn định và hợp nhất vào `main`.

### Tuần 6 (21/04 -> 27/04) - Profile + Ideas
- Loại commit ưu tiên: `feat(profile)`, `feat(idea)`, `fix(profile|idea)`

### Tuần 7 (28/04 -> 04/05) - Issues + Comments
- Loại commit ưu tiên: `feat(issue|comment)`, `fix(issue|comment)`

### Tuần 8 (05/05 -> 11/05) - Notifications + Messages
- Loại commit ưu tiên: `feat(notification|message)`, `fix(notification|message)`

### Tuần 9 (12/05 -> 17/05) - Kiểm thử / dọn dẹp / phát hành
- Tạo `release/v1.0.0` từ `develop`
- Chỉ nhận `fix`, `docs`, `chore` phục vụ ổn định phát hành
- Sau kiểm thử: hợp nhất `release/v1.0.0` -> `main`, gắn nhãn `v1.0.0`, rồi hợp nhất ngược về `develop`

## 4) Quy tắc chất lượng commit

- Mỗi commit chỉ nên giải quyết 1 ý chính.
- Thông điệp ngắn gọn, mô tả đúng thay đổi thực tế.
- Không commit file sinh tự động, file build, hoặc thông tin nhạy cảm.
- PR phải bám đúng giai đoạn hiện tại trong tiến độ.

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

