# REQUIREMENT-2-NONFUNCTIONAL.md — Yêu cầu phi chức năng

## 1. Hiệu năng (Performance)

| ID          | Yêu cầu                                 | Target                                       |
|-------------|-----------------------------------------|----------------------------------------------|
| NFR-PERF-01 | Cold start đến màn Home (user đã login) | ≤ 3s trên thiết bị mid-tier (Snapdragon 680) |
| NFR-PERF-02 | Thời gian phản hồi query FDC            | p95 ≤ 1.5s trên 4G                           |
| NFR-PERF-03 | Upload avatar 1MB lên R2                | ≤ 3s trên 4G                                 |
| NFR-PERF-04 | Scroll list 100 items                   | 60fps, không dropped frame rõ rệt            |
| NFR-PERF-05 | APK size                                | ≤ 25MB (không tính bundled resource thừa)    |
| NFR-PERF-06 | RAM runtime                             | ≤ 150MB trong phiên sử dụng thường           |

## 2. Bảo mật (Security)

| ID         | Yêu cầu                                                                                                 |
|------------|---------------------------------------------------------------------------------------------------------|
| NFR-SEC-01 | Mọi biến cấu hình nhạy cảm (R2 token, keystore password) lưu trong `local.properties`, **không** commit |
| NFR-SEC-02 | Mọi mutation/query ngoại trừ `tags`, `skills` yêu cầu `@auth(level: USER)`                              |
| NFR-SEC-03 | Ownership check server-side bằng `where: { authorId: { eq_expr: "auth.uid" } }`                         |
| NFR-SEC-04 | Password không log, không lưu plaintext                                                                 |
| NFR-SEC-05 | R2 token có scope giới hạn (chỉ 1 bucket, WRITE/READ)                                                   |
| NFR-SEC-06 | ProGuard/R8 bật trên release build                                                                      |
| NFR-SEC-07 | Network traffic qua HTTPS; reject cleartext (Android Manifest `cleartextTrafficPermitted=false`)        |
| NFR-SEC-08 | Không hiển thị stack trace cho user cuối                                                                |
| NFR-SEC-09 | Soft delete thay vì hard delete — đảm bảo audit                                                         |
| NFR-SEC-10 | Google Sign-In SHA-1 config đúng cho debug + release                                                    |

## 3. Độ tin cậy (Reliability)

| ID         | Yêu cầu                                                          |
|------------|------------------------------------------------------------------|
| NFR-REL-01 | Crash-free session rate ≥ 99%                                    |
| NFR-REL-02 | Offline handling: hiển thị empty/error state có nút Retry        |
| NFR-REL-03 | Mất mạng giữa chừng không dẫn đến corrupt state                  |
| NFR-REL-04 | Retry tự động cho network transient (3 lần, exponential backoff) |

## 4. Khả năng mở rộng (Scalability)

| ID         | Yêu cầu                                                                                    |
|------------|--------------------------------------------------------------------------------------------|
| NFR-SCA-01 | Cloudflare R2 hỗ trợ mở rộng object storage không giới hạn, không cần thay đổi client code |
| NFR-SCA-02 | Cloud SQL (FDC) có thể scale vertical; thiết kế schema dùng UUID, tránh hot key            |
| NFR-SCA-03 | Pagination trên mọi list endpoint (limit/offset hoặc cursor)                               |
| NFR-SCA-04 | Indexes tối thiểu: xem [SECTION 12 — SCHEMA.md](SCHEMA.md)                                 |
| NFR-SCA-05 | Cloudflare CDN cache public avatar giảm tải R2                                             |

## 5. Khả năng bảo trì (Maintainability)

| ID         | Yêu cầu                                                         |
|------------|-----------------------------------------------------------------|
| NFR-MNT-01 | Clean Architecture 3 layer (data/domain/ui)                     |
| NFR-MNT-02 | Code coverage unit test ≥ 60% cho domain + data                 |
| NFR-MNT-03 | Ktlint + Detekt zero violation trên CI                          |
| NFR-MNT-04 | Dependency Injection bằng Hilt                                  |
| NFR-MNT-05 | Tài liệu trong `/docs` luôn đồng bộ với code (review mỗi Phase) |

## 6. Khả dụng (Usability / UX)

| ID        | Yêu cầu                                                     |
|-----------|-------------------------------------------------------------|
| NFR-UX-01 | Hỗ trợ Dark Mode tự động theo hệ thống                      |
| NFR-UX-02 | Mọi text user-facing bằng tiếng Việt (strings.xml)          |
| NFR-UX-03 | Tối thiểu 1 empty state + 1 error state cho mỗi list screen |
| NFR-UX-04 | Loading indicator xuất hiện cho mọi async > 500ms           |
| NFR-UX-05 | Hỗ trợ accessibility: content description cho icon button   |

## 7. Tương thích (Compatibility)

| ID         | Yêu cầu                                                                 |
|------------|-------------------------------------------------------------------------|
| NFR-CMP-01 | Android 8.0 (API 26) trở lên                                            |
| NFR-CMP-02 | Portrait + Landscape                                                    |
| NFR-CMP-03 | Màn hình 4.7" – 7"                                                      |
| NFR-CMP-04 | Google Play Services hiện diện (bắt buộc cho Firebase + Google Sign-In) |
