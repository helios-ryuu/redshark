# TESTING.md - Kế hoạch kiểm thử RedShark

Tài liệu này là nguồn chính cho kiểm thử hồi quy trước khi merge, release hoặc reset production.

## 1. Lệnh kiểm thử bắt buộc

Chạy từ thư mục gốc:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat compileDebugKotlin
.\gradlew.bat assembleDebug
```

Nếu thay đổi Firestore Rules/Indexes:

```powershell
firebase emulators:start --only firestore
firebase deploy --only firestore:rules,firestore:indexes
```

Nếu chuẩn bị production trắng dữ liệu:

```powershell
python scripts/reset_production.py dry-run --project redshark-application
python scripts/reset_production.py verify --project redshark-application
```

## 2. Xác thực và hồ sơ

| ID | Kịch bản | Kết quả mong đợi |
|---|---|---|
| AUTH-01 | Mở app khi chưa đăng nhập | Đi tới màn hình login/register, không vào Home |
| AUTH-02 | Đăng nhập Google lần đầu | Tạo/upsert user Firestore, nếu thiếu display name thì vào profile setup |
| AUTH-03 | Đăng nhập Google user cũ | Vào Home, hiển thị avatar/name/email đúng |
| AUTH-04 | Đăng xuất | Xóa local preference, quay về auth flow |
| AUTH-05 | Đăng ký email/password hợp lệ | Tạo Firebase Auth user, ghi `users/{uid}`, vào Home |
| AUTH-06 | Email sai format | Hiển thị lỗi field email, không gọi Firebase sign-up |
| AUTH-07 | Email đã tồn tại | Hiển thị lỗi field email, không hiển thị network/generic error |
| AUTH-08 | Username sai format/uppercase | Hiển thị lỗi field username, không query Firestore nếu invalid |
| AUTH-09 | Username đã tồn tại | Hiển thị lỗi field username, không tạo Auth user |
| AUTH-10 | Password dưới 8 ký tự, thiếu chữ hoa hoặc số | Hiển thị lỗi password |
| AUTH-11 | Confirm password khác password | Hiển thị lỗi confirm password |
| AUTH-12 | DOB dưới 13 tuổi | Hiển thị lỗi DOB |
| AUTH-13 | Sửa profile name/bio/skills | Lưu thành công, refresh UI |
| AUTH-14 | Upload avatar hợp lệ | Upload R2, cập nhật `avatarUrl`, avatar hiển thị lại trong Profile/Drawer |
| AUTH-15 | Upload avatar sai mime/quá lớn | Hiển thị lỗi, không cập nhật profile |

## 3. Ý tưởng, media, công việc, bình luận

| ID | Kịch bản | Kết quả mong đợi |
|---|---|---|
| CONTENT-01 | Tạo idea title 3..120, description hợp lệ | Idea ACTIVE xuất hiện trên Home/Ideas |
| CONTENT-02 | Tạo idea title quá ngắn/dài | Hiển thị lỗi validation |
| CONTENT-03 | Home feed có idea của user khác | Card hiển thị title, description, ngày tạo, count, tên tác giả thật |
| CONTENT-04 | My Ideas gồm idea mình tạo và idea mình cộng tác | Danh sách gộp không trùng lặp |
| CONTENT-05 | Chi tiết idea | Hiển thị tên/avatar tác giả, status, description, issues, comments, media |
| CONTENT-06 | Tác giả sửa/xóa mềm idea | Update thành công; idea đã xóa không hiển thị trên list |
| CONTENT-07 | User không phải tác giả gửi request cộng tác | Tạo notification COLLAB_REQUEST |
| CONTENT-08 | Tác giả accept collab request | UID được thêm vào `collaboratorIds` |
| CONTENT-09 | Collaborator upload ảnh vào idea | Upload R2, thêm item vào `mediaAttachments`, feed/detail render ảnh |
| CONTENT-10 | Collaborator upload video hợp lệ | Upload R2, thêm item type VIDEO, detail hiển thị attachment video |
| CONTENT-11 | User không phải author/collaborator upload media | Bị chặn ở UI/rules |
| CONTENT-12 | Media mime không hỗ trợ hoặc quá lớn | Hiển thị lỗi, không ghi Firestore |
| CONTENT-13 | Sửa title/description idea đã có media | Media cũ không bị xóa |
| CONTENT-14 | Tạo issue trên idea ACTIVE | Issue xuất hiện trong detail |
| CONTENT-15 | Tạo issue trên idea CLOSED/CANCELLED | Bị chặn với lỗi nghiệp vụ |
| CONTENT-16 | Quá 20 issue active/user | Bị chặn với lỗi limit |
| CONTENT-17 | Đổi trạng thái issue hợp lệ | Cập nhật đúng state machine |
| CONTENT-18 | Đổi trạng thái issue sai | Bị chặn |
| CONTENT-19 | Xóa mềm issue | Không hiển thị trong danh sách active |
| CONTENT-20 | Gửi comment hợp lệ | Optimistic comment hiển thị ngay, server emission thay thế |
| CONTENT-21 | Gửi comment lỗi mạng | Rollback optimistic comment, hiển thị lỗi retry |
| CONTENT-22 | Comment list | Hiển thị tên/avatar tác giả thật, sắp xếp theo thời gian |

## 4. Thông báo và tin nhắn

| ID | Kịch bản | Kết quả mong đợi |
|---|---|---|
| INT-01 | Có notification mới | Badge unread trên top bar tăng |
| INT-02 | Mở notification sheet | List hiển thị đúng type/message/time |
| INT-03 | Mark read | Badge giảm, notification đã đọc |
| INT-04 | Delete all notifications | List rỗng, badge về 0 |
| INT-05 | Accept/reject collab | Cập nhật notification và collaborator đúng |
| INT-06 | Tạo direct conversation bằng email | Không tạo trùng conversation deterministic |
| INT-07 | Search conversation | Lọc theo display name/email |
| INT-08 | Gửi message | Tin nhắn hiển thị trong thread, preview list cập nhật |
| INT-09 | Nhận message từ user khác | Badge unread hiển thị nếu thread/list chưa đọc |
| INT-10 | Share idea qua message | Gửi text có title và deep link |
| INT-11 | Mở deep link idea | Điều hướng tới IdeaDetail đúng ID |

## 5. UI/UX và NFR

| ID | Kịch bản | Kết quả mong đợi |
|---|---|---|
| UX-01 | Home tab | Loading/error/empty/content state rõ ràng, không layout jump lớn |
| UX-02 | Ideas tab | FAB không che nội dung cuối, filter chip không tràn màn hình |
| UX-03 | Messages tab | Search/input/list item đồng nhất, badge không bị crop |
| UX-04 | Profile | Avatar, name, bio, skills căn lề đúng trên màn nhỏ |
| UX-05 | Hamburger menu | Header hiển thị đúng user, Settings hoạt động |
| UX-06 | Offline khi load list/detail | Hiển thị lỗi có retry, không crash |
| UX-07 | Orientation/recompose | State quan trọng không mất bất thường |
| NFR-01 | Cold start vào Home đã login | Mục tiêu không quá 3 giây trên thiết bị test |
| NFR-02 | Scroll feed 50 item | Không giật rõ rệt, không tạo query user per item |
| NFR-03 | APK debug/release | Build thành công, không hardcode secret |
| NFR-04 | Leak check smoke | Không có leak rõ ràng khi vào/ra Home, Detail, Messages |

## 6. Kiểm thử production trắng dữ liệu

| ID | Kịch bản | Kết quả mong đợi |
|---|---|---|
| PROD-01 | Chạy dry-run reset | In đúng số Firestore documents, Auth users, R2 objects sẽ xóa |
| PROD-02 | Deploy rules/indexes | Firebase CLI chạy thành công, indexes ở trạng thái enabled |
| PROD-03 | Reset có confirm token | Xóa Firestore documents, Auth users, R2 objects |
| PROD-04 | Verify sau reset | Firestore = 0 document, Auth = 0 user, R2 = 0 object |
| PROD-05 | Smoke tạo user mới | Đăng ký/đăng nhập tạo `users/{uid}` đúng schema |
| PROD-06 | Reset lần cuối sau smoke | Production trở lại trạng thái trắng dữ liệu |

## 7. Tiêu chí hoàn thành

- Tất cả unit test pass.
- Build debug thành công.
- Manual test các mục liên quan đến thay đổi gần nhất pass.
- Không còn tham chiếu docs cũ đã xóa trong README/REPORT/SCHEMA.
- Firestore Rules đã được test với author, collaborator và outsider.
- Production sau reset không còn dữ liệu runtime nhưng rules/indexes/storage bucket vẫn sẵn sàng cho người dùng thật.
