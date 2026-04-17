# REQUIREMENT-2-NONFUNCTIONAL.md — Yêu cầu phi chức năng

WBS tham chiếu: [WBS.md](WBS.md) — ánh xạ chính theo `6.0` và `7.0`.

## 1. Hiệu năng

| ID          | Yêu cầu                                                | Mục tiêu                                          |
|-------------|--------------------------------------------------------|---------------------------------------------------|
| NFR-PERF-01 | Khởi động nguội đến màn Home (người dùng đã đăng nhập) | ≤ 3 giây trên thiết bị tầm trung (Snapdragon 680) |
| NFR-PERF-02 | Thời gian phản hồi truy vấn Firestore                  | p95 ≤ 1,5 giây trên 4G                            |
| NFR-PERF-03 | Tải ảnh đại diện 1MB lên R2                            | ≤ 3 giây trên 4G                                  |
| NFR-PERF-04 | Cuộn danh sách 100 phần tử                             | 60fps, không giật khung hình rõ rệt               |
| NFR-PERF-05 | Kích thước APK                                         | ≤ 25MB (không tính tài nguyên gói kèm dư thừa)    |
| NFR-PERF-06 | Dung lượng RAM khi chạy                                | ≤ 150MB trong phiên sử dụng thông thường          |

## 2. Bảo mật

| ID         | Yêu cầu                                                                                                 |
|------------|---------------------------------------------------------------------------------------------------------|
| NFR-SEC-01 | Mọi biến cấu hình nhạy cảm (R2 token, mật khẩu keystore) lưu trong `local.properties`, **không** commit |
| NFR-SEC-02 | Mọi collection ngoại trừ `tags`, `skills` đều yêu cầu `request.auth != null` trong Firestore rules      |
| NFR-SEC-03 | Kiểm tra quyền sở hữu phía máy chủ bằng Firestore security rules: `request.auth.uid == resource.data.authorId` |
| NFR-SEC-04 | Mật khẩu không ghi log, không lưu dạng rõ chữ                                                           |
| NFR-SEC-05 | R2 token có phạm vi giới hạn (chỉ 1 bucket, WRITE/READ)                                                 |
| NFR-SEC-06 | ProGuard/R8 bật trên bản dựng phát hành                                                                 |
| NFR-SEC-07 | Lưu lượng mạng qua HTTPS; chặn cleartext (Android Manifest `cleartextTrafficPermitted=false`)           |
| NFR-SEC-08 | Không hiển thị stack trace cho người dùng cuối                                                          |
| NFR-SEC-09 | Xóa mềm thay vì xóa cứng — bảo đảm khả năng đối soát                                                    |
| NFR-SEC-10 | Cấu hình SHA-1 Google Sign-In đúng cho bản dựng debug + phát hành                                       |

## 3. Độ tin cậy

| ID         | Yêu cầu                                                       |
|------------|---------------------------------------------------------------|
| NFR-REL-01 | Tỷ lệ phiên không sự cố ≥ 99%                                 |
| NFR-REL-02 | Khi ngoại tuyến: hiển thị trạng thái trống/lỗi có nút Thử lại |
| NFR-REL-03 | Mất mạng giữa chừng không làm hỏng trạng thái dữ liệu         |
| NFR-REL-04 | Tự thử lại với lỗi mạng tạm thời (3 lần, giãn cách lũy tiến)  |

## 4. Khả năng mở rộng

| ID         | Yêu cầu                                                                                            |
|------------|----------------------------------------------------------------------------------------------------|
| NFR-SCA-01 | Cloudflare R2 hỗ trợ mở rộng lưu trữ đối tượng không giới hạn, không cần thay đổi mã phía ứng dụng |
| NFR-SCA-02 | Firestore tự mở rộng theo chiều ngang; thiết kế collection tránh điểm nóng (hot document)           |
| NFR-SCA-03 | Phân trang trên mọi điểm cuối trả danh sách (`limit/offset` hoặc `cursor`)                         |
| NFR-SCA-04 | Chỉ mục tối thiểu: xem [Mục 12 — SCHEMA.md](SCHEMA.md)                                             |
| NFR-SCA-05 | Bộ đệm Cloudflare CDN cho ảnh đại diện công khai giúp giảm tải R2                                  |

## 5. Khả năng bảo trì

| ID         | Yêu cầu                                                                  |
|------------|--------------------------------------------------------------------------|
| NFR-MNT-01 | Clean Architecture 3 tầng (`data/domain/ui`)                             |
| NFR-MNT-02 | Độ bao phủ kiểm thử đơn vị ≥ 60% cho tầng `domain` + `data`              |
| NFR-MNT-03 | Ktlint + Detekt không có vi phạm trên CI                                 |
| NFR-MNT-04 | Tiêm phụ thuộc bằng Hilt                                                 |
| NFR-MNT-05 | Tài liệu trong `/docs` luôn đồng bộ với mã nguồn (rà soát mỗi giai đoạn) |

## 6. Khả dụng

| ID        | Yêu cầu                                                               |
|-----------|-----------------------------------------------------------------------|
| NFR-UX-01 | Hỗ trợ Dark Mode tự động theo hệ thống                                |
| NFR-UX-02 | Mọi văn bản hiển thị cho người dùng bằng tiếng Việt (`strings.xml`)   |
| NFR-UX-03 | Tối thiểu 1 trạng thái trống + 1 trạng thái lỗi cho mỗi màn danh sách |
| NFR-UX-04 | Chỉ báo tải xuất hiện cho mọi tác vụ bất đồng bộ > 500ms              |
| NFR-UX-05 | Hỗ trợ khả năng tiếp cận: có mô tả nội dung cho nút biểu tượng        |

## 7. Tương thích

| ID         | Yêu cầu                                                                 |
|------------|-------------------------------------------------------------------------|
| NFR-CMP-01 | Android 8.0 (API 26) trở lên                                            |
| NFR-CMP-02 | Hỗ trợ cả dọc và ngang màn hình                                         |
| NFR-CMP-03 | Màn hình 4.7" – 7"                                                      |
| NFR-CMP-04 | Google Play Services hiện diện (bắt buộc cho Firebase + Google Sign-In) |
