# RedShark Android Native

RedShark là ứng dụng Android gốc hỗ trợ nhóm nhỏ quản lý ý tưởng, công việc, bình luận, thông báo, nhắn tin 1-1 và tệp media. Ứng dụng dùng Kotlin, Jetpack Compose, Firebase và Cloudflare R2.

## Tổng quan kỹ thuật

- Ngôn ngữ và giao diện: Kotlin, Jetpack Compose, Material 3.
- Kiến trúc: Clean Architecture, MVVM, luồng dữ liệu một chiều.
- Xác thực: Firebase Authentication với Google Sign-In và Email/Password.
- Dữ liệu nghiệp vụ: Cloud Firestore, phân quyền bằng Security Rules.
- Lưu trữ media: Cloudflare R2 tương thích S3 cho avatar và ảnh/video của ý tưởng.
- Thư viện chính: Hilt, Coroutines, Flow, DataStore, Coil, OkHttp.

## Chức năng chính

| Nhóm | Chức năng |
|---|---|
| Xác thực | Đăng nhập Google, đăng ký/đăng nhập email, kiểm tra username/email/mật khẩu/ngày sinh, đăng xuất |
| Hồ sơ | Xem/sửa hồ sơ, giới thiệu, kỹ năng, ảnh đại diện R2 |
| Ý tưởng | Tạo/xem/sửa/xóa mềm, trạng thái ACTIVE/CLOSED/CANCELLED, bảng tin, ý tưởng của tôi |
| Media | Tác giả/cộng tác viên tải ảnh hoặc video vào ý tưởng |
| Công việc | CRUD issue, state machine, giới hạn 20 công việc đang hoạt động mỗi người dùng |
| Bình luận | Bình luận realtime với cập nhật lạc quan |
| Thông báo | Badge chưa đọc, yêu cầu cộng tác, chấp nhận/từ chối, thông báo comment/issue |
| Tin nhắn | Hội thoại 1-1, tìm cuộc trò chuyện, badge chưa đọc, chia sẻ deep link ý tưởng |

## Cấu trúc dự án

```text
app/src/main/java/com/helios/redshark
  core/        kết quả, lỗi, DI, kiểm tra mạng
  data/        Firebase, Firestore, R2, repository implementation, mapper, DTO
  domain/      model, repository interface, use case
  ui/          Compose screen, ViewModel, navigation, theme, component dùng chung
docs/
  PROCESS.md          quy trình nghiệp vụ
  TESTING.md          kế hoạch kiểm thử
  SCHEMA.md           lược đồ Firestore, rules, indexes, deploy/reset
  REPORT.md           báo cáo đồ án
  PROJECT_CHARTER.md  tuyên ngôn dự án
  GIT.md              quy ước Git/commit
scripts/
  reset_production.py reset/verify Firestore, Firebase Auth và Cloudflare R2
```

## Thiết lập môi trường

Yêu cầu:

- Android Studio mới, JDK 17.
- Android SDK với `minSdk 26`, `compileSdk 36`.
- Firebase project đã bật Authentication và Firestore.
- Cloudflare R2 bucket và public base URL.

`local.properties` cần có các biến sau và không được commit:

```properties
GOOGLE_WEB_CLIENT_ID=
CLOUDFLARE_R2_ACCOUNT_ID=
CLOUDFLARE_R2_ACCESS_KEY_ID=
CLOUDFLARE_R2_SECRET_ACCESS_KEY=
CLOUDFLARE_R2_BUCKET=
CLOUDFLARE_R2_ENDPOINT=
CLOUDFLARE_R2_PUBLIC_BASE_URL=
```

## Build và kiểm thử

```powershell
.\gradlew.bat compileDebugKotlin
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

## Deploy và chuẩn bị production trắng dữ liệu

Deploy Firestore Rules và Indexes:

```powershell
firebase use redshark-application
firebase deploy --only firestore:rules,firestore:indexes
```

Reset dữ liệu production chỉ chạy khi đã xác nhận đúng project/bucket:

```powershell
python scripts/reset_production.py dry-run --project redshark-application
python scripts/reset_production.py reset --project redshark-application --confirm REDSHARK_PRODUCTION_RESET
python scripts/reset_production.py verify --project redshark-application
```

Sau reset, Firestore không còn document nghiệp vụ, Firebase Authentication không còn user, Cloudflare R2 không còn object. Không seed dữ liệu mẫu vào production.

## Tài liệu

- [Quy trình nghiệp vụ](docs/PROCESS.md)
- [Kế hoạch kiểm thử](docs/TESTING.md)
- [Lược đồ Firestore và vận hành dữ liệu](docs/SCHEMA.md)
- [Báo cáo đồ án](docs/REPORT.md)
- [Quy ước Git](docs/GIT.md)

## Nhóm thực hiện

| MSSV | Họ và tên | Vai trò |
|---:|---|---|
| 23521367 | Ngô Tiến Sỹ | Android, quản lý tiến độ, kiểm thử, tài liệu |
| 24520442 | Phạm Tuấn Hải | Android |
| 23520982 | Nguyễn Văn Nam | Android |
