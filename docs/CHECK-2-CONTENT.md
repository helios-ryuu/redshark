# CHECK-2-CONTENT.md — Manual Test: Ideas, Issues, Comments

| ID     | Chức năng                        | Các bước                                                | Kết quả mong đợi                                             | Trạng thái |
|--------|----------------------------------|---------------------------------------------------------|--------------------------------------------------------------|------------|
| TC-C01 | Tạo Idea hợp lệ                  | Tab Ideas → FAB → điền title + description + tags → Tạo | Idea xuất hiện ở danh sách, status ACTIVE                    | ⬜          |
| TC-C02 | Tạo Idea title < 3 ký tự         | Title = "ab"                                            | Chặn submit, error "Tối thiểu 3 ký tự"                       | ⬜          |
| TC-C03 | Tạo Idea title > 120             | Title 121 ký tự                                         | Chặn submit                                                  | ⬜          |
| TC-C04 | Xem chi tiết Idea                | Tap idea trong list                                     | Hiện title, desc, tags, collaborators, list Issues, Comments | ⬜          |
| TC-C05 | Edit Idea (chủ sở hữu)           | Detail → Edit → sửa → Lưu                               | UI cập nhật, mutation thành công                             | ⬜          |
| TC-C06 | Edit Idea (không phải chủ)       | Vào idea người khác                                     | Không thấy nút Edit                                          | ⬜          |
| TC-C07 | Soft delete Idea                 | Detail → Menu → Xóa → confirm                           | Biến mất khỏi list, DB set `deletedAt`                       | ⬜          |
| TC-C08 | Đóng Idea (CLOSED)               | Detail → chuyển status → CLOSED                         | Badge hiển thị CLOSED, không cho tạo issue mới               | ⬜          |
| TC-C09 | Tạo Issue hợp lệ                 | Idea detail → "Thêm Issue" → điền form                  | Issue xuất hiện trong list của idea                          | ⬜          |
| TC-C10 | Tạo Issue vượt quá 20 active     | Có sẵn 20 issue OPEN/IN_PROGRESS → tạo thêm             | Chặn, toast "Đạt giới hạn 20 issue active"                   | ⬜          |
| TC-C11 | Edit Issue (author)              | Detail → Edit                                           | Lưu thành công                                               | ⬜          |
| TC-C12 | Edit Issue (không phải author)   | Vào issue người khác                                    | Không thấy nút Edit                                          | ⬜          |
| TC-C13 | Chuyển status Issue hợp lệ       | OPEN → IN_PROGRESS                                      | Update OK                                                    | ⬜          |
| TC-C14 | Chuyển status Issue không hợp lệ | CLOSED → OPEN                                           | UI không cho chọn, nếu force API thì server reject           | ⬜          |
| TC-C15 | Soft delete Issue                | Author → Menu → Xóa                                     | Biến mất khỏi list                                           | ⬜          |
| TC-C16 | Assign issue cho user            | Edit → chọn assignee                                    | Hiển thị avatar assignee                                     | ⬜          |
| TC-C17 | Tab Home (feed)                  | Mở tab Home                                             | Hiện OPEN issue từ user khác, không hiện của chính mình      | ⬜          |
| TC-C18 | Gửi comment hợp lệ               | Idea detail → nhập comment → Send                       | Comment hiện ngay (optimistic), `CreateComment` gọi          | ⬜          |
| TC-C19 | Gửi comment rỗng                 | Nội dung trống                                          | Nút Send disabled                                            | ⬜          |
| TC-C20 | Gửi comment > 1000 ký tự         | Nhập 1001 ký tự                                         | Chặn submit                                                  | ⬜          |
| TC-C21 | Mất mạng khi tạo Idea            | Airplane mode → submit                                  | Error "Không có kết nối", form giữ nguyên                    | ⬜          |
| TC-C22 | Mất mạng khi load list           | Vào tab Ideas khi offline                               | Hiện empty/error state có nút Retry                          | ⬜          |
| TC-C23 | Xem idea đã xóa (deep link)      | Mở link idea đã soft delete                             | Hiện "Idea không tồn tại"                                    | ⬜          |
| TC-C24 | Tag filter                       | Home/Ideas → lọc theo tag                               | Kết quả đúng với tag đã chọn                                 | ⬜          |
