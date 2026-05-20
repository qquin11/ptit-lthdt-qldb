# ptit-lthdt-qldb - Ứng Dụng Quản Lý Đặt Bàn và Món Ăn Nhà Hàng

Đồ án môn học Lập trình hướng đối tượng (OOP) thực hiện trong thời gian 2 tuần sử dụng ngôn ngữ Java và thư viện giao diện Java Swing. Hệ thống được thiết kế theo mô hình kiến trúc **MVC (Model - View - Controller)** kết hợp cơ sở dữ liệu nhúng SQLite tiện lợi.

---

## Thành Viên Nhóm & Phân Công (Resource Allocation)
* **DO HOANG DUY:** Thiết kế cấu trúc OOP, thiết kế Database, viết logic nghiệp vụ (gộp bàn, chuyển bàn, tính toán hóa đơn, xử lý dữ liệu).
* **QUYNH:** Thiết kế toàn bộ giao diện Java Swing (`JFrame`, `JTable`, sơ đồ bàn bằng `GridLayout`), bắt sự kiện UI.
* **THANG:** Phụ trách tính năng đặt bàn trước, xuất file hóa đơn, viết dữ liệu mẫu (Mock data) và thực hiện kiểm thử hệ thống (Bug Hunting).

---

## Tính Năng Chính Của Hệ Thống
1.  **Quản trị Hệ thống (Admin):** Đăng nhập bảo mật, phân quyền tài khoản (`ADMIN` / `STAFF`), quản lý danh sách nhân viên và thống kê doanh thu trực quan.
2.  **Quản lý Sơ Đồ Bàn:** Hiển thị lưới sơ đồ bàn bằng màu sắc theo thời gian thực (Trống, Đang ăn, Đã đặt). Hỗ trợ chuyển bàn và gộp bàn nhanh chóng.
3.  **Quản lý Thời Gian (Reservation):** Ghi nhận thông tin đặt bàn trước của khách, kiểm tra trùng lịch, tự động giải phóng bàn nếu quá giờ hẹn.
4.  **Quản lý Món Ăn & Đặt Món:** Menu gọi món chia theo danh mục rõ ràng, bảng hiển thị chi tiết gọi món của từng bàn, cập nhật giá tiền tự động.
5.  **Xuất Hóa Đơn:** Tính tổng tiền tự động (+VAT, -Giảm giá), màn hình xem trước (Preview) hóa đơn và xuất file thành định dạng văn bản `.txt`.

---

## Kiến Trúc Mã Nguồn (MVC Pattern)
Mã nguồn được phân tách rõ ràng thành các package độc lập nhằm tuân thủ tính đóng gói của OOP:
* `com.app.model`: Chứa các lớp thực thể dữ liệu (`User`, `Table`, `Food`, `Order`) và lớp kết nối CSDL `DatabaseHelper`.
* `com.app.view`: Chứa toàn bộ giao diện đồ họa Java Swing (Các class kế thừa từ `JFrame`, `JPanel`, `JDialog`).
* `com.app.controller`: Chứa các lớp điều hướng, xử lý logic trung gian liên kết dữ liệu từ Model đổ lên View và ngược lại.
---

## Cấu Trúc Thư Mục

Mã nguồn được phân tách rõ ràng thành các package độc lập nhằm tuân thủ tính đóng gói của OOP và kiến trúc MVC. Cấu trúc cây thư mục của dự án được tổ chức như sau:

```text
ptit-lthdt-qldb/               <-- Thư mục gốc ngoài cùng (Root Folder)
├── .gitignore               <-- Chặn file rác cấu hình và file nhật ký SQLite
├── LICENSE                  <-- Giấy phép nguồn mở MIT của nhóm
├── pom.xml                  <-- File cấu hình Maven (Quản lý các thư viện như sqlite-jdbc)
├── README.md                <-- Tài liệu hướng dẫn và bảng tiến độ dự án
├── data/                    <-- Thư mục tự động sinh ra khi ứng dụng chạy lần đầu
│   └── nhahang.db           <-- File cơ sở dữ liệu SQLite duy nhất của hệ thống
└── src/                     
    └── main/
        └── java/
            └── com/
                └── app/
                    ├── Main.java         <-- Hàm main khởi chạy (Hiển thị Dialog kết nối DB)
                    │
                    ├── controller/       <-- Nhóm CONTROLLER (Xử lý sự kiện và điều hướng)
                    │   └── MainController.java
                    │
                    ├── model/            <-- Nhóm MODEL (Quản lý dữ liệu và cấu trúc đối tượng)
                    │   ├── DatabaseHelper.java  <-- Lớp kết nối và cấu hình SQLite tự động
                    │   ├── BanAn.java           <-- Thực thể Quản lý Bàn ăn
                    │   ├── MonAn.java           <-- Thực thể Quản lý Món ăn
                    │   └── HoaDon.java          <-- Thực thể Quản lý Hóa đơn
                    │
                    └── view/             <-- Nhóm VIEW (Giao diện đồ họa Java Swing)
                        ├── MainFrame.java       <-- Cửa sổ giao diện chính của ứng dụng
                        ├── LoginDialog.java     <-- Hộp thoại đăng nhập / Phân quyền
                        └── OrderPanel.java      <-- Giao diện gọi món tại bàn
---

## Công Nghệ & Thư Viện Sử Dụng
* **Ngôn ngữ:** Java (21)
* **Giao diện:** Java Swing & AWT
* **Cơ sở dữ liệu:** **SQLite (Chế độ Embedded - Nhúng)**
    * *Ưu điểm:* Toàn bộ dữ liệu tự động lưu trữ vào một file đơn nhất `./data/nhahang.db` ngay trong project mà không cần cài đặt phần mềm quản trị bên thứ ba (MySQL, SQL Server...). Giúp thầy cô chỉ cần mở project lên và `Run` là ứng dụng hoạt động ngay lập tức với đầy đủ dữ liệu mẫu.

---

## Hướng Dẫn Chạy Dự Án (Cài đặt nhanh)

### ⚠️ Lưu ý quan trọng cho Git:
Dự án sử dụng cơ sở dữ liệu nhúng, thư mục `data` và file dữ liệu `nhahang.db` sẽ **tự động khởi tạo** khi bạn nhấn `Run` lần đầu tiên. Không cần cấu hình hay import file SQL thủ công.

### Các bước thực hiện trên máy thành viên:
1.  Clone project về máy cục bộ:
    ```bash
    git clone https://github.com/qquin11/ptit-lthdt-qldb.git
    ```
2.  Mở thư mục dự án `ptit-lthdt-qldb` bằng **IntelliJ IDEA**.
3.  Bấm vào biểu tượng **Maven** ở góc phải màn hình -> Chọn **Reload All Maven Projects** để IDE tự động tải driver SQLite về máy.
4.  Tìm đến file `Main.java` (nằm tại `src/main/java/com/app/Main.java`).
5.  Nhấn nút **Run** để chạy chương trình.

---

## Quy Định Làm Việc Với Git Của Nhóm
* Tuyệt đối không được `push` code trực tiếp lên nhánh `main`.
* Mỗi tính năng/màn hình được code trên một nhánh độc lập (Ví dụ: `feature/login`, `feature/table-layout`, `feature/database`).
* Sau khi hoàn thành và tự kiểm thử không có lỗi phát sinh, thực hiện tạo **Pull Request (PR)** trên GitHub để Leader duyệt và gộp (`merge`) vào nhánh chính.