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

**RedShark Android Native** là ứng dụng cộng tác cho nhóm nhỏ, tập trung vào theo dõi **ý tưởng/công việc** và tương tác trong nhóm qua **bình luận, thông báo, nhắn tin**.

Định hướng hệ thống:
- 100% Kotlin Android Native + Jetpack Compose
- Kiến trúc Clean Architecture + MVVM + UDF
- Firebase Authentication cho xác thực
- Firebase Data Connect (Cloud SQL PostgreSQL) cho dữ liệu nghiệp vụ
- Cloudflare R2 (S3-compatible) cho media/avatar

Mục tiêu chất lượng chính:
- Khởi động nguội đến màn hình Home (đã đăng nhập) <= 3 giây
- Tỷ lệ phiên sử dụng không gặp sự cố >= 99%
- Tải ảnh đại diện 1MB <= 3 giây trên 4G
- Độ bao phủ kiểm thử đơn vị (domain + data) >= 60%

---

## Thông tin học phần

| Hạng mục        | Thông tin                                                        |
|-----------------|------------------------------------------------------------------|
| Môn học         | Phát triển ứng dụng trên thiết bị di động (NT118)                |
| Lớp             | NT118.Q22 - HK2 2025-2026                                        |
| Giảng viên      | ThS. Trần Hồng Nghi - nghith@uit.edu.vn                          |
| Đề tài          | RedShark Android Native (Kotlin)                                 |
| Thời gian dự án | 16/03/2026 -> 17/05/2026 (9 tuần)                                |
| Mốc khởi tạo    | Commit khởi tạo dự án ngày 15/04/2026 (Sỹ)                       |
| Mốc quan trọng  | Bắt buộc có ít nhất 1 commit tính năng xác thực trước 21/04/2026 |

---

## Thành viên nhóm

| STT |     MSSV | Họ và Tên      | Vai trò                                                   | GitHub                                        |
|-----|---------:|----------------|-----------------------------------------------------------|-----------------------------------------------|
| 1   | 23521367 | Ngô Tiến Sỹ    | Lập trình viên Android, quản lý tiến độ/kiểm thử/tài liệu | [helios-ryuu](https://github.com/helios-ryuu) |
| 2   | 24520442 | Phạm Tuấn Hải  | Lập trình viên Android                                    | [haiphamt](https://github.com/haiphamt)       |
| 3   | 23520982 | Nguyễn Văn Nam | Lập trình viên Android                                    | [Sinister-VN](https://github.com/Sinister-VN) |

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
- Tách rõ 3 tầng: `data`, `domain`, `ui`
- Tầng `domain` thuần Kotlin, không phụ thuộc Android SDK
- Giao diện kho dữ liệu đặt ở `domain`, phần cài đặt đặt ở `data`
- `ViewModel` cung cấp `StateFlow<UiState>`, giao diện đọc trạng thái theo luồng dữ liệu một chiều (UDF)
- Quy tắc bảo mật và quyền sở hữu dữ liệu được kiểm soát phía máy chủ qua Data Connect

---

## Phạm vi chức năng

| Nhóm chức năng | Chức năng chính                                                                                   |
|----------------|---------------------------------------------------------------------------------------------------|
| Xác thực       | Đăng nhập Google, hoàn thiện hồ sơ lần đầu (`displayName` 3..50), duy trì phiên, đăng xuất        |
| Hồ sơ          | Xem/sửa hồ sơ, chọn kỹ năng, tải ảnh đại diện lên R2, xem hồ sơ công khai                         |
| Ý tưởng        | Tạo/xem/sửa/xóa mềm ý tưởng, quản lý trạng thái ACTIVE/CLOSED/CANCELLED                           |
| Công việc      | Tạo/xem/sửa/xóa mềm công việc, kiểm soát máy trạng thái, giới hạn 20 công việc đang mở/người dùng |
| Bình luận      | Bình luận trên ý tưởng (1..1000 ký tự), sắp xếp theo thời gian                                    |
| Thông báo      | Danh sách thông báo, huy hiệu chưa đọc, đánh dấu đã đọc, xử lý yêu cầu cộng tác                   |
| Nhắn tin       | Hội thoại trực tiếp 1-1, gửi/nhận tin nhắn, tránh tạo trùng hội thoại                             |
| Tra cứu        | Danh sách thẻ/kỹ năng công khai, hỗ trợ lọc ý tưởng/công việc                                     |

Ngoài phạm vi:
- iOS/Web
- Thông báo đẩy FCM (hiện tại dùng cơ chế cập nhật theo chu kỳ)
- Hội thoại nhóm (> 2 thành viên)
- Thanh toán/gói thuê bao

---

## Công nghệ

| Tầng kỹ thuật    | Công nghệ                                    |
|------------------|----------------------------------------------|
| Ứng dụng di động | Kotlin + Android Native                      |
| Giao diện        | Jetpack Compose + Material 3                 |
| Kiến trúc        | Clean Architecture + MVVM + UDF              |
| Tiêm phụ thuộc   | Hilt                                         |
| Bất đồng bộ      | Kotlin Coroutines + Flow                     |
| Xác thực         | Firebase Authentication                      |
| Dữ liệu          | Firebase Data Connect + Cloud SQL PostgreSQL |
| Lưu trữ          | Cloudflare R2 (tương thích S3)               |
| Biên dịch        | Gradle Kotlin DSL                            |
| Chất lượng mã    | Ktlint + Detekt + kiểm thử đơn vị            |

---

## Thiết lập môi trường

### 1) Yêu cầu
- Android Studio Iguana+ (khuyến nghị JDK 17)
- Android SDK: `minSdk 26`
- Firebase CLI
- Tài khoản Firebase project và Cloudflare R2

### 2) Cấu hình dịch vụ
- Thiết lập Google Sign-In cho ứng dụng Android (SHA-1 debug/phát hành + OAuth Client ID)
- Thiết lập bộ kết nối Firebase Data Connect
- Thiết lập bucket và endpoint Cloudflare R2 cho ảnh đại diện/tệp phương tiện
- Danh sách biến cấu hình hiện dùng: xem `docs/SECRET.md`

### 3) Quy trình Data Connect
```powershell
firebase emulators:start --only dataconnect
firebase dataconnect:sdk:generate
firebase deploy --only dataconnect
```

### 4) Biên dịch và kiểm tra chất lượng
```powershell
./gradlew lint
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

---

## Quy trình phát triển

- Quy trình lặp tăng dần theo 5 giai đoạn:
  - Giai đoạn 1: Nền tảng
  - Giai đoạn 2: Xác thực + Hồ sơ
  - Giai đoạn 3: Ý tưởng + Công việc + Bình luận
  - Giai đoạn 4: Thông báo + Nhắn tin
  - Giai đoạn 5: Kiểm thử, dọn dẹp, phát hành
- Mô hình nhánh: tạo nhánh tính năng và gửi pull request để hợp nhất theo quy ước tại `docs/GIT.md`
- Quy ước thông điệp commit theo Conventional Commits:

```text
feat(auth): add Google Sign-In flow
fix(issue): prevent invalid CLOSED -> OPEN transition
refactor(data): extract FDC error mapper
docs: update timeline and test checklist
```

Danh sách kiểm tra bắt buộc khi rà soát:
- Không ghi cứng thông tin nhạy cảm trong mã nguồn
- Có xử lý trạng thái lỗi và cơ chế thử lại
- Không vi phạm máy trạng thái nghiệp vụ
- `lint` và `test` đạt yêu cầu trước khi hợp nhất

Tài liệu chi tiết:
- Tổng quan tài liệu: `docs/README.md`
- WBS tổng thể: `docs/WBS.md`
- Tuyên ngôn dự án: `docs/PROJECT_CHARTER.md`
- Kiến trúc mã nguồn: `docs/STRUCTURE.md`
- Yêu cầu: `docs/REQUIREMENT-1-FUNCTIONAL.md`, `docs/REQUIREMENT-2-NONFUNCTIONAL.md`
- Quy trình nghiệp vụ: `docs/PROCESS-1-AUTH.md`, `docs/PROCESS-2-CONTENT.md`, `docs/PROCESS-3-INTERACTION.md`
- Danh sách kiểm thử: `docs/CHECK-1-AUTH.md`, `docs/CHECK-2-CONTENT.md`, `docs/CHECK-3-INTERACTION.md`
- Báo cáo: `docs/REPORT.md`
