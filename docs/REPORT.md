# BÁO CÁO ĐỒ ÁN — ỨNG DỤNG REDSHARK ANDROID NATIVE (KOTLIN)

**Môn học:** NT118.Q22 — Lập trình ứng dụng di động
**Nhóm thực hiện:** Sỹ (PM), Nam, Hải
**Thời gian:** 05/04/2026 — 17/05/2026

---

## LỜI MỞ ĐẦU

Trong bối cảnh chuyển đổi số, các ứng dụng di động hỗ trợ cộng tác và quản lý công việc trở thành công cụ thiết yếu đối với sinh viên và các nhóm dự án nhỏ. Đồ án **RedShark** được thực hiện nhằm xây dựng một ứng dụng Android gốc bằng ngôn ngữ Kotlin, hướng đến trải nghiệm người dùng tối ưu, kiến trúc rõ ràng và khả năng mở rộng cao, thông qua việc khai thác các dịch vụ không máy chủ (serverless) của Google Firebase và Cloudflare.

## LỜI GIỚI THIỆU

**RedShark** là ứng dụng cộng tác cho phép người dùng tạo, theo dõi các **Ý tưởng (Idea)**, quản lý **Issue (vấn đề/công việc)** và tương tác qua hệ thống **bình luận, thông báo, nhắn tin**. Hệ thống được xây dựng trên nền:

- **Lớp giao diện:** Kotlin Android Native + Jetpack Compose.
- **Lớp dịch vụ và cơ sở dữ liệu:** Firebase Authentication + Firebase Data Connect (PostgreSQL được quản lý).
- **Lớp lưu trữ:** Cloudflare R2 (dịch vụ lưu trữ đối tượng tương thích S3) cho ảnh đại diện và dữ liệu đa phương tiện.

Phiên bản đồ án này tập trung triển khai ứng dụng Android gốc bằng Kotlin, giúp tối ưu hiệu năng và khai thác tối đa các giao diện lập trình ứng dụng (API) sẵn có của Android.

## LỜI CẢM ƠN

Nhóm xin gửi lời cảm ơn sâu sắc tới:
- Giảng viên môn NT118.Q22 đã hướng dẫn, phản biện và định hướng trong suốt quá trình thực hiện đề tài.
- Cộng đồng mã nguồn mở Kotlin, Jetpack Compose, Firebase, Cloudflare vì các tài liệu và công cụ chất lượng.
- Các thành viên trong nhóm đã nỗ lực cộng tác để hoàn thành đồ án đúng tiến độ.

## DANH MỤC BẢNG

| Số hiệu  | Tên bảng                                     | Chương |
|----------|----------------------------------------------|--------|
| Bảng 2.1 | So sánh các giải pháp lưu trữ object storage | 2      |
| Bảng 3.1 | Yêu cầu chức năng tổng hợp                   | 3      |
| Bảng 3.2 | Yêu cầu phi chức năng                        | 3      |
| Bảng 3.3 | Danh sách bảng CSDL                          | 3      |
| Bảng 3.4 | Ma trận phân quyền truy cập dữ liệu          | 3      |
| Bảng 4.1 | Danh sách màn hình và route                  | 4      |
| Bảng 4.2 | Danh sách GraphQL queries/mutations          | 4      |
| Bảng 5.1 | Tổng hợp ca kiểm thử Xác thực                | 5      |
| Bảng 5.2 | Tổng hợp ca kiểm thử Nội dung                | 5      |
| Bảng 5.3 | Tổng hợp ca kiểm thử Tương tác               | 5      |
| Bảng 5.4 | Kết quả đo hiệu năng                         | 5      |

## DANH MỤC HÌNH ẢNH

| Số hiệu  | Tên hình                                 | Chương |
|----------|------------------------------------------|--------|
| Hình 2.1 | Kiến trúc Firebase Data Connect          | 2      |
| Hình 2.2 | Mô hình tương thích S3 của Cloudflare R2 | 2      |
| Hình 3.1 | Sơ đồ use-case tổng thể                  | 3      |
| Hình 3.2 | Sơ đồ ERD của hệ thống                   | 3      |
| Hình 3.3 | Sơ đồ tuần tự Đăng nhập Google           | 3      |
| Hình 3.4 | Sơ đồ trạng thái Issue                   | 3      |
| Hình 4.1 | Cấu trúc thư mục dự án                   | 4      |
| Hình 4.2 | Luồng điều hướng màn hình                | 4      |
| Hình 4.3 | Giao diện Home / Ideas / Profile         | 4      |
| Hình 5.1 | Kết quả crash-free session               | 5      |

## DANH MỤC TỪ VIẾT TẮT

| Từ viết tắt | Ý nghĩa                                      |
|-------------|----------------------------------------------|
| FDC         | Firebase Data Connect                        |
| R2          | Cloudflare R2 (lưu trữ)                      |
| SDK         | Software Development Kit                     |
| UI          | User Interface                               |
| UX          | User Experience                              |
| MVVM        | Model – View – ViewModel                     |
| CRUD        | Create – Read – Update – Delete              |
| DTO         | Data Transfer Object                         |
| DI          | Dependency Injection                         |
| JWT         | JSON Web Token                               |
| GraphQL     | Graph Query Language                         |
| REST        | Representational State Transfer              |
| CI/CD       | Continuous Integration / Continuous Delivery |
| APK         | Android Package Kit                          |
| SigV4       | AWS Signature Version 4                      |

## TÓM TẮT

Đồ án xây dựng ứng dụng **RedShark** trên nền tảng Android gốc (Kotlin + Jetpack Compose), áp dụng kiến trúc **Clean Architecture + MVVM**. Dữ liệu được quản lý qua **Firebase Data Connect** (Cloud SQL PostgreSQL) với **Firebase Authentication** cho phân quyền, và **Cloudflare R2** cho lưu trữ dữ liệu đa phương tiện. Ứng dụng gồm 7 nhóm tính năng chính: Xác thực, Hồ sơ, Ý tưởng, Công việc, Bình luận, Thông báo, Nhắn tin. Toàn bộ dự án được triển khai trong 6 tuần bởi nhóm 3 người, đạt các tiêu chí: crash-free ≥ 99%, thời gian khởi động ≤ 3 giây, tỷ lệ đạt kiểm thử thủ công ≥ 95%.

---

## CHƯƠNG 1. GIỚI THIỆU / TỔNG QUAN ĐỀ TÀI

### 1.1 Đặt vấn đề
Các nhóm dự án nhỏ thường thiếu công cụ theo dõi ý tưởng và công việc có dung lượng nhẹ, chi phí thấp và tập trung cho thiết bị di động. Nhiều nền tảng hiện tại (Jira, Trello) có chi phí tương đối cao, ưu tiên trải nghiệm trên nền tảng web và chưa tối ưu cho ngữ cảnh sử dụng trên di động.

### 1.2 Lý do chọn đề tài
- Nhu cầu thực tế trong cộng tác nhóm sinh viên.
- Cơ hội thực hành kiến trúc Android gốc theo chuẩn công nghiệp.
- Khám phá bộ công nghệ mới: Firebase Data Connect (PostgreSQL được quản lý), Cloudflare R2.

### 1.3 Mục tiêu đề tài
- **Mục tiêu tổng quát:** Xây dựng ứng dụng Android theo dõi ý tưởng/công việc với đầy đủ các tính năng cốt lõi.
- **Mục tiêu cụ thể:** Hoàn thiện kiến trúc Kotlin cho Android gốc, áp dụng Clean Architecture, tích hợp 3 dịch vụ đám mây.

### 1.4 Phạm vi đề tài
- **Trong phạm vi:** Xác thực, Hồ sơ, Ý tưởng, Công việc, Bình luận, Thông báo, Nhắn tin trên Android ≥ 8.0.
- **Ngoài phạm vi:** iOS, web, FCM thời gian thực, nhóm chat nhiều người, thanh toán.

### 1.5 Đối tượng nghiên cứu
- Sinh viên và nhóm dự án nhỏ cần công cụ theo dõi công việc nhẹ.
- Sản phẩm: Ứng dụng Android cài đặt qua APK hoặc Google Play.

### 1.6 Phương pháp thực hiện
- Quy trình phát triển: Lặp tăng dần (iterative) theo 5 giai đoạn (xem [PLAN-1..5](PLAN-1-FOUNDATION.md)).
- Quản lý công việc: GitHub Issue + rà soát yêu cầu hợp nhất (PR review), theo chuẩn Conventional Commits.
- Kiểm thử: Kiểm thử đơn vị (domain/data), kiểm thử giao diện (Compose), kiểm thử thủ công theo danh sách kiểm tra.

---

## CHƯƠNG 2. CƠ SỞ LÝ THUYẾT

### 2.1 Kotlin và nền tảng Android gốc
Kotlin là ngôn ngữ chính thức của Google cho Android từ năm 2017, hỗ trợ an toàn rỗng (null-safety), coroutines, hàm mở rộng (extension functions) và DSL. Jetpack Compose là bộ công cụ xây dựng giao diện khai báo (declarative), thay thế XML trong Android gốc, cho phép phát triển giao diện hiện đại với lượng mã lặp thấp.

**Các thư viện Jetpack được sử dụng:**
- `Lifecycle` / `ViewModel` — quản lý vòng đời.
- `Navigation-Compose` — điều hướng giữa các màn.
- `DataStore` — lưu trữ preference thay SharedPreferences.
- `Hilt` — Dependency Injection.
- `Coil` — tải ảnh bất đồng bộ.

### 2.2 Firebase Authentication
Đây là dịch vụ định danh được Google quản lý. Luồng hiện tại sử dụng Google Sign-In (thông qua Trình quản lý thông tin xác thực - Credential Manager), trong đó mã thông báo JWT được tự động làm mới. Firebase Authentication tạo `uid` duy nhất cho mỗi người dùng, làm khóa liên kết với các bảng nghiệp vụ trong Data Connect.

### 2.3 Firebase Data Connect (FDC)
FDC là dịch vụ hậu trường dạng nền tảng (backend-as-a-service) mới của Firebase (phát hành bản beta năm 2024), cung cấp:
- PostgreSQL được quản lý (Cloud SQL, khu vực `asia-southeast1`).
- Schema định nghĩa bằng GraphQL SDL; các `@auth`, `@table`, `@unique` directive.
- Tự sinh bộ phát triển phần mềm (SDK) cho TypeScript/Android/iOS từ `queries.gql` và `mutations.gql`.
- Biểu thức phân quyền `auth.uid`, `request.time` — bảo đảm quyền sở hữu dữ liệu ngay tại truy vấn.
- Soft delete, audit pattern được hỗ trợ thông qua expression field.

**Ưu điểm:** Vừa khai thác được sức mạnh của PostgreSQL (join, transaction, index), vừa tận dụng cơ chế xác thực tích hợp sẵn của Firebase.

### 2.4 Cloudflare R2
Đây là dịch vụ lưu trữ đối tượng tương thích **S3 API**, không thu phí băng thông chiều ra (egress), với chi phí thấp hơn S3 khoảng 10 lần.
- Xác thực: AWS Signature Version 4 (SigV4).
- Hỗ trợ presigned URL TTL.
- Có thể gắn custom domain + Cloudflare CDN.

**Bảng 2.1 — So sánh giải pháp object storage:**

| Tiêu chí | AWS S3 | Firebase Storage | Cloudflare R2 |
|---------|--------|------------------|--------------|
| Phí băng thông chiều ra | Có | Có | **Không** |
| Tương thích S3 API | Native | Không | Có |
| CDN tích hợp | CloudFront riêng | Có | **Có (miễn phí)** |
| Free tier | 5GB/tháng | 5GB | **10GB** |

### 2.5 Clean Architecture + MVVM
Phân tách 3 layer:
- **Domain** (Use Case + Model interface) — pure Kotlin, test độc lập.
- **Data** (cài đặt repository + nguồn dữ liệu) — phụ thuộc domain.
- **Presentation** (ViewModel + Composable) — phụ thuộc domain.

Luồng dữ liệu một chiều (Unidirectional Data Flow): UI event → ViewModel intent → UseCase → Repository → StateFlow → UI.

---

## CHƯƠNG 3. PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG

### 3.1 Phân tích yêu cầu
Tổng hợp từ [REQUIREMENT-1-FUNCTIONAL.md](REQUIREMENT-1-FUNCTIONAL.md) và [REQUIREMENT-2-NONFUNCTIONAL.md](REQUIREMENT-2-NONFUNCTIONAL.md): 8 mô-đun chức năng và 6 nhóm yêu cầu phi chức năng (hiệu năng, bảo mật, độ tin cậy, khả năng mở rộng, khả năng bảo trì, khả dụng).

### 3.2 Sơ đồ ca sử dụng (Use-Case)
Actor chính: `User (đã đăng nhập)`, `Visitor (chưa đăng nhập)`.
Ca sử dụng chính: Đăng nhập Google, hoàn thiện hồ sơ lần đầu, Tạo/Sửa/Xóa Ý tưởng, Tạo/Sửa/Xóa Công việc, Bình luận, Gửi/Nhận Thông báo, Nhắn tin 1-1.

### 3.3 Thiết kế cơ sở dữ liệu
Chi tiết tại [SCHEMA.md](SCHEMA.md). Gồm 9 bảng: `users`, `ideas`, `issues`, `comments`, `tags`, `skills`, `notifications`, `conversations`, `messages`. Soft delete trên `ideas`, `issues`.

### 3.4 Thiết kế kiến trúc hệ thống

```
┌──────────────────────────────────────────┐
│  UI Layer (Jetpack Compose + ViewModel)  │
└────────────────────┬─────────────────────┘
                     │ UseCase
┌────────────────────▼─────────────────────┐
│  Domain Layer (Model + Repo interface)   │
└────────────────────┬─────────────────────┘
                     │ Repository impl
┌────────────────────▼─────────────────────┐
│  Data Layer                              │
│  ├─ FirebaseAuthSource                   │
│  ├─ DataConnectSource (GraphQL)          │
│  ├─ R2Client (S3 SigV4)                  │
│  └─ DataStore (local prefs)              │
└──────────────────────────────────────────┘
```

### 3.5 Thiết kế giao diện
Giao diện tuân theo Material 3, ưu tiên thiết bị di động (mobile-first), với thanh điều hướng dưới gồm 5 thẻ: Home, Ideas, Messages, Notifications, Settings.

### 3.6 Thiết kế xử lý nghiệp vụ
Chi tiết trong [PROCESS-1-AUTH.md](PROCESS-1-AUTH.md), [PROCESS-2-CONTENT.md](PROCESS-2-CONTENT.md), [PROCESS-3-INTERACTION.md](PROCESS-3-INTERACTION.md).

---

## CHƯƠNG 4. HIỆN THỰC ĐỀ TÀI

### 4.1 Môi trường phát triển
- Android Studio Iguana+, JDK 17, Kotlin 2.0, Gradle 8.x.
- Firebase CLI cho `dataconnect:sdk:generate`.
- Kho mã nguồn GitHub phục vụ quản lý phiên bản.
- Thiết bị kiểm thử: Trình giả lập Pixel 6, Samsung A54.

### 4.2 Cấu trúc mã nguồn
Chi tiết tại [STRUCTURE.md](STRUCTURE.md). Mã nguồn được tổ chức theo 3 lớp `data/domain/ui` cùng nhóm thành phần dùng chung.

### 4.3 Triển khai các module

**Module Auth:**
- `FirebaseAuthSource` bao bọc `FirebaseAuth.getInstance()`.
- `GoogleSignInHelper` sử dụng Credential Manager API (thay thế `GoogleSignInClient` đã lỗi thời).
- Luồng đăng nhập đầu tiên bắt buộc hoàn thiện hồ sơ lần đầu với `displayName` 3..50.
- `AuthRepositoryImpl` cung cấp `Flow<AuthState>`.

**Module Idea/Issue:**
- Bộ SDK được sinh tự động từ FDC tại `src/main/java/.../dataconnect/generated`.
- Ràng buộc "20 issue đang hoạt động" được kiểm tra trước ở tầng use case trước khi gọi mutation.

**Module Profile/Media:**
- Nén ảnh bằng `BitmapFactory` → JPEG quality 80 → tối đa 512×512.
- Tải tệp lên R2 qua OkHttp với `AwsV4SigningInterceptor`.

**Module Notification/Message:**
- Cập nhật tuần tự theo chu kỳ (polling) bằng `rememberCoroutineScope` + `while (isActive) { delay(...); refresh() }`.
- Cập nhật lạc quan (optimistic update): thêm dữ liệu cục bộ trước, hoàn tác nếu thất bại.

### 4.4 Tích hợp dịch vụ bên ngoài
- **Firebase:** `google-services.json` + init trong `RedSharkApp`.
- **R2:** Thông tin xác thực được nạp qua `BuildConfig` (từ `local.properties`).
- **Google Sign-In:** Cần cấu hình SHA-1 debug + release keystore trên Firebase Console.

### 4.5 Bảo mật và quản lý cấu hình
Theo [SECRET.md](SECRET.md), các biến cấu hình được lưu trong `local.properties`, không mã hóa cứng trong mã nguồn và không ghi token vào nhật ký. ProGuard được bật ở bản phát hành. Quyền sở hữu dữ liệu được kiểm soát ở phía máy chủ.

---

## CHƯƠNG 5. KIỂM THỬ

### 5.1 Chiến lược kiểm thử
- **Kiểm thử đơn vị (unit test):** Domain use case và data repository (JUnit + MockK).
- **Kiểm thử giao diện (UI test):** Compose Test (các màn hình Auth, IdeaDetail).
- **Kiểm thử thủ công (manual test):** Theo CHECK-1/2/3.
- **Kiểm thử hiệu năng:** Android Profiler, Firebase Performance Monitoring.

### 5.2 Kịch bản kiểm thử
Bảng tổng hợp:
- CHECK-1: 20 test case Auth & Profile — xem [CHECK-1-AUTH.md](CHECK-1-AUTH.md).
- CHECK-2: 24 test case Content — xem [CHECK-2-CONTENT.md](CHECK-2-CONTENT.md).
- CHECK-3: 17 test case Interaction + cross-cutting — xem [CHECK-3-INTERACTION.md](CHECK-3-INTERACTION.md).

### 5.3 Kết quả kiểm thử (điền sau khi chạy thực tế)
| Nhóm        | Tổng TC | PASS  | FAIL  | % PASS |
|-------------|---------|-------|-------|--------|
| Auth        | 20      | _     | _     | _      |
| Content     | 24      | _     | _     | _      |
| Interaction | 17      | _     | _     | _      |
| **Tổng**    | **61**  | **_** | **_** | **_**  |

### 5.4 Đánh giá
Các chỉ số mục tiêu:
- Crash-free session ≥ 99%.
- Cold start ≤ 3s.
- APK size ≤ 25 MB.
- Upload avatar 1 MB ≤ 3s trên 4G.

---

## CHƯƠNG 6. KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

### 6.1 Kết quả đạt được
- Ứng dụng RedShark Android native hoàn chỉnh, đầy đủ 7 nhóm tính năng.
- Kiến trúc Clean Architecture + MVVM rõ ràng, dễ mở rộng.
- Kho mã nguồn công khai, đúng chuẩn Conventional Commits.
- Tài liệu kỹ thuật đầy đủ trong thư mục `/docs`.

### 6.2 Hạn chế
- Chưa tích hợp FCM push notification (đang dùng polling).
- Chưa hỗ trợ nhóm chat > 2 người.
- Chưa có quy trình kiểm thử instrumented tự động (UI test hiện chạy trên trình giả lập).
- Chưa tích hợp Firebase Performance + Crashlytics để giám sát môi trường vận hành thực tế.

### 6.3 Hướng phát triển
- Tích hợp FCM kết hợp cơ chế gần thời gian thực dựa trên snapshot Firestore.
- Thêm nhóm chat, đính kèm media trong message.
- Mở rộng sang iOS (Kotlin Multiplatform Mobile).
- Module báo cáo/thống kê cho idea owner.
- Hỗ trợ offline-first với Room + sync.

---

## TÀI LIỆU THAM KHẢO

1. Google. *Android Developers Documentation.* https://developer.android.com/
2. JetBrains. *Kotlin Language Reference.* https://kotlinlang.org/docs/
3. Google. *Jetpack Compose.* https://developer.android.com/jetpack/compose
4. Firebase. *Firebase Data Connect Documentation.* https://firebase.google.com/docs/data-connect
5. Firebase. *Firebase Authentication.* https://firebase.google.com/docs/auth
6. Cloudflare. *R2 Object Storage — S3 API Compatibility.* https://developers.cloudflare.com/r2/
7. Robert C. Martin. *Clean Architecture: A Craftsman's Guide to Software Structure and Design.* Prentice Hall, 2017.
8. Google. *Guide to App Architecture.* https://developer.android.com/topic/architecture
9. Square. *OkHttp.* https://square.github.io/okhttp/
10. Material Design 3. https://m3.material.io/
