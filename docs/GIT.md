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

### Thiết lập nhánh `develop` ban đầu (làm một lần duy nhất)

Kho mã nguồn được khởi tạo với commit `chore(init)` trực tiếp trên `main`. Trước khi bắt đầu bất kỳ công việc tính năng nào, cần tạo nhánh `develop` từ `main`:

```bash
git switch main
git pull --ff-only origin main
git switch -c develop
git push -u origin develop
```

Sau bước này, mọi nhánh `feature/*` đều tạo từ `develop`, không tạo từ `main`.

### Luồng hợp nhất nhánh
1. `feature/*` tạo từ `develop`, hợp nhất về `develop` qua PR.
2. `release/*` tạo từ `develop` khi vào pha release, chỉ nhận fix ổn định.
3. `release/*` hợp nhất vào `main` (gắn nhãn phát hành) và hợp nhất ngược về `develop`.
4. `hotfix/*` tạo từ `main`, hợp nhất lại `main` và `develop`.

## 3) Ma trận commit message và vị trí nhánh

Bảng dưới đây chốt rõ mọi trường hợp thường gặp: commit theo mẫu nào, làm việc trên nhánh nào, và hợp nhất về đâu.

| Trường hợp | Mẫu commit message | Ví dụ | Tạo từ nhánh | Commit tại nhánh | Hợp nhất đến | Ghi chú |
|---|---|---|---|---|---|---|
| Chỉnh sửa tài liệu nhỏ, cần hiệu lực ngay (không cần review) | `docs(<scope>): <message>` | `docs(testing): update regression checklist` | Không cần tạo nhánh mới | `develop` | Không bắt buộc PR | Không push trực tiếp vào `main` |
| Chỉnh sửa tài liệu đi kèm tính năng | `docs(<scope>): <message>` | `docs(auth): document Google Sign-In flow` | `develop` | `feature/<name>` | `develop` | Đi cùng PR của tính năng |
| Khởi tạo/cấu hình nền tảng, build, tooling | `chore(<scope>): <message>` | `chore(build): align gradle plugin versions` | `develop` | `feature/<name>` | `develop` | Dùng cho thay đổi nền tảng/build |
| Thêm tính năng mới | `feat(<scope>): <message>` | `feat(auth): add Google Sign-In flow` | `develop` | `feature/<name>` | `develop` | Nhóm scope: `auth`, `profile`, `idea`, `issue`, `comment`, `notification`, `message` |
| Sửa lỗi trong quá trình phát triển thường | `fix(<scope>): <message>` | `fix(profile): trim displayName before validation` | `develop` | `feature/<name>` | `develop` | Nếu lỗi nằm trong nhánh feature đang làm, sửa trực tiếp trên nhánh đó |
| Cải tổ code, không đổi hành vi | `refactor(<scope>): <message>` | `refactor(data): extract auth mapper` | `develop` | `feature/<name>` | `develop` | Khuyến nghị đi kèm test |
| Bổ sung/chỉnh test | `test(<scope>): <message>` | `test(auth): add sign-in failure unit tests` | `develop` | `feature/<name>` | `develop` | Ưu tiên gắn module liên quan trong mô tả PR |
| Tối ưu hiệu năng | `perf(<scope>): <message>` | `perf(message): reduce conversation polling allocations` | `develop` | `feature/<name>` | `develop` | Nếu chưa ở pha release |
| Sửa lỗi ở pha ổn định phát hành | `fix(<scope>): <message>` | `fix(release): prevent crash on empty notification list` | `develop` (tạo `release/*`) | `release/<version>` | `main` và merge ngược `develop` | Chỉ nhận `fix`, `docs`, `chore` trong nhánh `release/*` |
| Chỉnh docs/chore phục vụ phát hành | `docs(release): <message>` / `chore(release): <message>` | `docs(release): add known issues for v1.0.0` | `develop` (tạo `release/*`) | `release/<version>` | `main` và merge ngược `develop` | Không thêm `feat` mới trên `release/*` |
| Hotfix khẩn cấp trên bản phát hành | `fix(hotfix): <message>` | `fix(hotfix): handle token refresh null on startup` | `main` | `hotfix/<name>` | `main` và `develop` | Tạo tag bản và phát hành lại sau khi merge |

### Quy tắc nhánh cần nhớ nhanh
- Mặc định mọi thay đổi code/tính năng/sửa lỗi đi theo `feature/*` và hợp nhất về `develop`.
- `develop` là nhánh tích hợp chính, cho phép commit trực tiếp cho thay đổi `docs` nhỏ cần cập nhật ngay.
- `main` chỉ nhận merge từ `release/*` hoặc `hotfix/*`.
- `release/*` chỉ dùng để ổn định phát hành, không mở rộng phạm vi tính năng.
- `hotfix/*` chỉ dùng khi lỗi nghiêm trọng đã nằm trên bản phát hành (`main`).

## 4) Quy tắc commit theo tiến độ

Dựa theo `docs/TIMELINE.md`, chỉ chốt commit mốc chính ở đúng cửa sổ thời gian:
Để truy vết theo kế hoạch, mô tả PR/commit nên nêu module liên quan (`auth`, `profile`, `idea`, `media`, `issue`, `comment`, `notification`, `message`, `docs`) và checklist kiểm thử trong [TESTING.md](TESTING.md).

Quy ước phụ trách theo module:
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

## 5) Quy tắc chất lượng commit

- Mỗi commit chỉ nên giải quyết 1 ý chính.
- Thông điệp ngắn gọn, mô tả đúng thay đổi thực tế.
- Không commit file sinh tự động, file build, hoặc thông tin nhạy cảm.
- PR phải bám đúng giai đoạn hiện tại trong tiến độ.

## 6) Gợi ý lệnh nhanh (Git Flow)

**Nguyên tắc chung:** Ưu tiên sử dụng `git switch` (để chuyển/tạo nhánh) và `git restore` (để khôi phục thay đổi) thay vì lệnh `git checkout` đa năng của các phiên bản Git cũ. Điều này giúp thao tác rõ ràng, an toàn và tránh nhầm lẫn giữa việc thao tác với nhánh (branch) và tệp (file).

### 6.1 Bắt đầu nhánh tính năng (Feature Branch) từ `develop`

Quy trình này đảm bảo luôn bắt đầu công việc trên một nền tảng mã nguồn mới nhất và tách biệt hoàn toàn tính năng mới khỏi mã nguồn chính.

```bash
git switch develop
git pull --ff-only origin develop
git switch -c feature/auth-google-signin

git add .
git commit -m "feat(auth): add Google Sign-In flow"
git push -u origin feature/auth-google-signin
```

**Phân tích chi tiết:**

| Lệnh / Tham số    | Chức năng & Ý nghĩa                                                                                                                           | Ảnh hưởng thực tế                                                                                                                                       |
|:------------------|:----------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------|
| `switch develop`  | Chuyển không gian làm việc (Working Tree) sang nhánh `develop`.                                                                               | Đảm bảo các lệnh tiếp theo tác động đúng lên nhánh nền tảng.                                                                                            |
| `pull --ff-only`  | Kéo code mới nhất từ remote (`origin/develop`) về máy. Tham số `--ff-only` (Fast-forward only) ép Git chỉ cập nhật nếu lịch sử là tuyến tính. | **An toàn cao:** Nếu nhánh local bị phân nhánh (diverged) so với remote, lệnh sẽ thất bại thay vì tự động tạo ra một merge commit rác ngoài ý muốn.     |
| `switch -c <tên>` | `-c` (create) vừa tạo nhánh mới tên `feature/...` vừa lập tức chuyển sang nhánh đó.                                                           | Tạo ra một môi trường cô lập để phát triển tính năng.                                                                                                   |
| `add .`           | Đưa toàn bộ các file có thay đổi (thêm mới, sửa, xóa) vào Staging Area.                                                                       | Chuẩn bị dữ liệu để đóng gói thành commit.                                                                                                              |
| `commit -m "..."` | Đóng gói các thay đổi với thông điệp. Sử dụng chuẩn *Conventional Commits* (`feat(...)`) giúp lịch sử rõ ràng.                                | Lưu lại một mốc lịch sử (snapshot) trên máy cục bộ.                                                                                                     |
| `push -u origin`  | Đẩy nhánh lên server. `-u` (set-upstream) liên kết nhánh local với nhánh remote.                                                              | Các lần sau chỉ cần gõ `git push` hoặc `git pull` mà không cần chỉ định rõ tên nhánh.                                                                   |

### 6.2 Cập nhật nhánh Feature theo `develop` mới nhất

Khi tính năng mất nhiều ngày để làm, nhánh `develop` có thể đã được người khác cập nhật. Cần đồng bộ nhánh của mình để tránh xung đột (conflict) quá lớn lúc gom code.

```bash
git switch develop
git pull --ff-only origin develop
git switch feature/auth-google-signin
git merge --no-ff develop
```

**Phân tích chi tiết:**

| Lệnh / Tham số  | Chức năng & Ý nghĩa                                                                                                                                    | Ảnh hưởng thực tế                                                                                                                            |
|:----------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------|
| `merge --no-ff` | Trộn code từ `develop` vào nhánh hiện tại. `--no-ff` (No Fast-forward) bắt buộc Git tạo ra một "Merge Commit" mới, ngay cả khi có thể trộn tuyến tính. | Giữ lại dấu vết rõ ràng rằng đã chủ động cập nhật nền tảng vào thời điểm nào, giúp dễ dàng rà soát lỗi nếu việc cập nhật làm hỏng tính năng. |

> **Góc nhìn của anh:** Dù `--no-ff` giúp bảo vệ lịch sử, nhưng nếu làm việc độc lập và muốn lịch sử nhánh feature thật sạch sẽ, thẳng tắp, anh khuyến nghị tìm hiểu thêm lệnh `git rebase develop` thay cho `git merge`. Rebase sẽ "bứng" các commit và đặt lên đỉnh mới nhất của `develop`.

### 6.3 Hợp nhất Feature vào `develop` (Sau khi PR/MR được duyệt)

Đây là bước đưa tính năng đã hoàn thiện và kiểm thử vào nhánh tích hợp chung.

```bash
git switch develop
git pull --ff-only origin develop
git merge --no-ff feature/auth-google-signin
git push origin develop
```

**Phân tích chi tiết:**

| Lệnh / Tham số  | Chức năng & Ý nghĩa                                                  | Ảnh hưởng thực tế                                                                                                                                                                                       |
|:----------------|:---------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `merge --no-ff` | Trộn nhánh tính năng vào `develop` và **bắt buộc tạo Merge Commit**. | **Bắt buộc trong DevOps:** Nó gói gọn toàn bộ các commit lắt nhắt của nhánh feature vào một "nút" (node) duy nhất. Nếu tính năng này gây sập server, chỉ cần `revert` đúng cái merge commit đó là xong. |

### 6.4 Tạo nhánh Release và chốt phát hành

Nhánh release dùng để đóng băng mã nguồn, chuẩn bị deploy lên production. Giai đoạn này chỉ cho phép sửa lỗi (bug fix), không thêm tính năng mới.

```bash
git switch develop
git pull --ff-only origin develop
git switch -c release/v1.0.0

# ... Thực hiện các commit fix/docs/chore để ổn định

git switch main
git pull --ff-only origin main
git merge --no-ff release/v1.0.0
git tag v1.0.0
git push origin main
git push origin v1.0.0

git switch develop
git merge --no-ff release/v1.0.0
git push origin develop
```

**Phân tích chi tiết:**

| Lệnh / Tham số       | Chức năng & Ý nghĩa                                                                                               | Ảnh hưởng thực tế                                                                                |
|:---------------------|:------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------|
| `tag v1.0.0`         | Đánh dấu (bookmark) vĩnh viễn trạng thái mã nguồn tại thời điểm hiện tại bằng một tên phiên bản.                  | Điểm neo quan trọng cho các hệ thống CI/CD để tự động build ứng dụng hoặc rollback khi có sự cố. |
| `push origin v1.0.0` | Lệnh `push` thông thường không đẩy tag. Phải chỉ định rõ tên tag để đồng bộ lên server.                           | Kích hoạt webhook hoặc các pipeline deploy tương ứng với phiên bản.                              |
| `merge ... release`  | Trộn ngược từ nhánh release về `main` (để live) và về `develop` (để các tính năng sau thừa hưởng các bản vá lỗi). | Đảm bảo tính nhất quán của mã nguồn trên toàn bộ hệ thống.                                       |

### 6.5 Tạo nhánh Hotfix xử lý sự cố khẩn cấp

Dành cho các lỗi nghiêm trọng (Critical Bug) trên môi trường Production cần vá ngay lập tức. Phải rẻ nhánh từ `main`.

```bash
git switch main
git pull --ff-only origin main
git switch -c hotfix/auth-token-null

git add .
git commit -m "fix(hotfix): handle token refresh null on startup"
git push -u origin hotfix/auth-token-null

# Sau khi merge hotfix vào main và có tag mới, nhớ merge ngược về develop
git switch develop
git merge --no-ff hotfix/auth-token-null
git push origin develop
```

**Lưu ý quan trọng:** Phải **merge ngược về develop**. Nếu quên bước này, lỗi sẽ xuất hiện lại ở bản Release tiếp theo.

### 6.6 Khôi phục thay đổi an toàn với `git restore`

`git restore` là công cụ chuyên dụng để hoàn tác mã nguồn, giúp tách biệt khái niệm "chuyển nhánh" khỏi "khôi phục tệp".

| Cú pháp lệnh                  | Chức năng                                                                | Trạng thái File sau lệnh                                                                                         |
|:------------------------------|:-------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------|
| `git restore <file>`          | Bỏ qua các thay đổi chưa được add (chưa đưa vào Staging Area).           | Trở về trạng thái giống y hệt commit gần nhất. Mọi code mới gõ sẽ bị xóa sạch.                                   |
| `git restore .`               | Áp dụng khôi phục cho toàn bộ file trong thư mục hiện tại.               | Reset sạch sẽ toàn bộ Working Tree chưa add.                                                                     |
| `git restore --staged <file>` | Kéo file từ Staging Area ra ngoài (tương đương `git reset HEAD <file>`). | File quay lại trạng thái "Modified" nhưng chưa được chuẩn bị để commit. Mã nguồn mới gõ **vẫn được giữ nguyên**. |

**Ví dụ thực tế:** Khi vô tình gõ `git add .` dính cả những file config cá nhân, dùng `git restore --staged <file>` để rút nó ra khỏi mẻ commit sắp tới mà không làm mất những dòng cấu hình vừa viết.

### 6.7 Dọn dẹp nhánh cục bộ

```bash
git switch develop
git branch -d feature/auth-google-signin
git branch -d release/v1.0.0
```

**Phân tích chi tiết:**

| Lệnh / Tham số    | Chức năng & Ý nghĩa                                                                | Ảnh hưởng thực tế                                                                                                                           |
|:------------------|:-----------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------|
| `branch -d <tên>` | Xóa nhánh ở môi trường cục bộ (Local). Chữ `d` viết thường biểu thị "Safe Delete". | Git sẽ kiểm tra, nếu nhánh này **chưa được merge** hoàn toàn vào `develop` hoặc `main`, nó sẽ báo lỗi và ngăn không cho xóa để bảo vệ code. |
| *(Mở rộng)* `-D`  | Bắt buộc xóa (Force Delete).                                                       | Dùng khi code thử nghiệm một tính năng, thấy sai hướng và muốn vứt bỏ hoàn toàn nhánh đó.                                                   |
