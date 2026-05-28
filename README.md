# ptit-lthdt-qldb — Ứng Dụng Quản Lý Đặt Bàn và Món Ăn Nhà Hàng

Đồ án môn học Lập trình hướng đối tượng (OOP) thực hiện trong 2 tuần bằng Java + Java Swing. Hệ thống theo mô hình **MVC** (Model - View - Controller) + cơ sở dữ liệu nhúng SQLite. UI hiện đại dùng **FlatLaf** + **MigLayout**, hỗ trợ Light/Dark theme.

---

## Thành Viên Nhóm & Phân Công

* **DO HOANG DUY:** Thiết kế cấu trúc OOP, thiết kế Database, viết logic nghiệp vụ (gộp bàn, chuyển bàn, tính toán hóa đơn, xử lý dữ liệu).
* **QUYNH:** Thiết kế toàn bộ giao diện Java Swing (`JFrame`, `JTable`, sơ đồ bàn bằng `GridLayout`), bắt sự kiện UI.
* **THANG:** Phụ trách tính năng đặt bàn trước, xuất file hóa đơn, viết dữ liệu mẫu (Mock data) và kiểm thử hệ thống.

---

## Tính Năng Chính

### Đăng nhập & phân quyền
* Đăng nhập bằng tài khoản nhân viên với hash mật khẩu (SHA-256 + salt)
* 2 vai trò: `ADMIN` (quản lý) và `STAFF` (nhân viên)
* Ghi nhớ tài khoản, hiện/ẩn mật khẩu, khóa 30 giây sau 3 lần đăng nhập sai
* Đổi mật khẩu trong app

### Trang chủ (Dashboard)
* 3 thẻ KPI: doanh thu hôm nay, số bàn đang dùng, số hóa đơn
* Biểu đồ cột doanh thu 7 ngày qua
* Danh sách 10 hoạt động hóa đơn gần nhất

### Sơ đồ bàn
* Lưới bàn với 3 trạng thái màu (Trống / Đang dùng / Đã đặt)
* Filter theo trạng thái + tìm theo tên bàn
* Hiển thị thời gian dùng bàn + tổng tiền order hiện tại, cập nhật mỗi 60 giây
* Click chuột trái → mở màn gọi món; chuột phải → menu (gọi món / đặt trước / thanh toán)

### Gọi món (Order)
* Bố cục 2 cột: bên trái menu món ăn theo danh mục (có tìm món), bên phải hóa đơn hiện tại
* Tăng / giảm số lượng cho dòng được chọn, xóa món
* Tự tính tạm tính + VAT 10% + giảm giá theo %
* Phím tắt `F9` chuyển sang thanh toán

### Thanh toán
* Hiển thị chi tiết hóa đơn + form tính tiền
* Nút tiền nhanh (50K, 100K, 200K, 500K, 1M)
* Tính tiền thừa real-time, báo đỏ khi tiền khách thiếu
* Chọn hình thức: Tiền mặt / Chuyển khoản / Thẻ
* Xem trước hóa đơn định dạng máy in nhiệt 80mm, in qua Java Print API

### Quản lý Menu món ăn
* CRUD đầy đủ với form dialog
* Lọc theo danh mục + trạng thái còn hàng + tìm theo tên
* Phân trang 15 dòng / trang

### Quản lý Nhân viên
* CRUD nhân viên (chỉ ADMIN)
* Đặt lại mật khẩu
* Cho nghỉ việc (soft delete — giữ lại lịch sử)

### Đặt bàn trước
* Form đặt bàn + grid hiển thị bàn trống tại thời điểm chọn
* Tự tạo khách mới nếu SĐT chưa có
* Kiểm tra trùng lịch trong khoảng ±2 giờ
* Dialog danh sách booking (check-in / hủy)

### Tiện ích
* Theme Light / Dark, có thể đổi và lưu lại
* Sidebar có thể thu gọn
* Phím tắt toàn cục: `F1` help, `F5` reload, `Ctrl+K` focus tìm kiếm, `F9` thanh toán, `Esc` đóng dialog

---

## Kiến Trúc Mã Nguồn

Tách package theo trách nhiệm (MVC + service layer):

```
src/main/java/com/app/
├── Main.java                    ← Entry point, khởi tạo theme + DB + login
├── config/                      ← App config, theme, fonts, session
├── model/                       ← Entity + DatabaseHelper (schema + migration + seed)
├── dao/                         ← Data Access (BanAn, MonAn, HoaDon, ...)
├── service/                     ← Business logic (Auth, Order, Payment, Reservation, ...)
├── controller/                  ← Wire view với service
├── util/                        ← CurrencyFormatter, DateFormatter, PasswordHasher, ...
└── view/
    ├── auth/                    ← LoginFrame, ChangePasswordDialog
    ├── main/                    ← MainFrame, Sidebar, TopBar, StatusBar
    ├── dashboard/               ← DashboardPanel, RevenueChart, ActivityFeed
    ├── table/                   ← TableMapPanel, OrderPanel, TableCard
    ├── menu/                    ← MenuPanel + dialog
    ├── employee/                ← EmployeePanel + dialog
    ├── reservation/             ← ReservationPanel, BookingListDialog
    ├── payment/                 ← PaymentPanel, InvoicePreviewDialog
    └── common/                  ← Button, Toast, Pagination, ConfirmDialog, ...

src/main/resources/
├── FlatLightLaf.properties      ← Override accent #2563EB, radius 8, scrollbar...
└── FlatDarkLaf.properties

src/test/java/com/app/util/      ← Unit tests (JUnit 5) cho CurrencyFormatter, PasswordHasher, ...
```

---

## Công Nghệ Sử Dụng

* **Ngôn ngữ:** Java 21
* **Giao diện:** Java Swing + FlatLaf 3.5.4 (modern flat L&F) + MigLayout 11.4.2
* **Cơ sở dữ liệu:** SQLite (nhúng) — file `data/nhahang.db` tự sinh khi chạy lần đầu
* **Build:** Maven 3.9+ (có `maven-shade-plugin` để build fat-jar runnable)
* **Test:** JUnit 5
* **Logging:** SLF4J Simple

---

## Hướng Dẫn Chạy Dự Án

### Yêu cầu
* JDK 21 trở lên
* Maven 3.9+ (hoặc dùng IDE có embedded Maven như IntelliJ)

### Cách 1: Chạy bằng IDE (khuyến nghị cho developer)
1. Clone project: `git clone https://github.com/qquin11/ptit-lthdt-qldb.git`
2. Mở thư mục `ptit-lthdt-qldb` bằng IntelliJ IDEA
3. Phải chuột vào `pom.xml` → Maven → **Reload project** (IDE tự tải dependencies)
4. Mở file `src/main/java/com/app/Main.java` → nhấn nút **Run**

### Cách 2: Chạy bằng file batch (đơn giản cho người dùng cuối)
1. `build.bat` — build fat-jar (chạy 1 lần đầu hoặc sau khi sửa code)
2. `start.bat` — mở app không cần cửa sổ console

### Cách 3: Chạy bằng Maven CLI
```bash
mvn clean package -DskipTests
java -jar target/ptit-lthdt-qldb-1.0-SNAPSHOT.jar
```

### Tài khoản test
| Tài khoản | Mật khẩu  | Vai trò |
|-----------|-----------|---------|
| `admin`   | `admin123`| ADMIN   |
| `staff`   | `staff123`| STAFF   |

Dữ liệu mẫu (bàn, món ăn, danh mục) tự seed khi DB trống. Để reset hoàn toàn: xóa file `data/nhahang.db` rồi chạy lại.

---

## Lưu ý cài đặt
* Thư mục `data/` và file `nhahang.db` tự khởi tạo lần chạy đầu, không cần import SQL thủ công.
* Mật khẩu lưu trong DB ở dạng plain text (mock data cũ) sẽ tự được hash sau lần đăng nhập thành công đầu tiên.
* Migration cột mới (tiền khách đưa, phương thức thanh toán) chạy tự động qua `PRAGMA table_info` check.

---

## Quy Định Git Của Nhóm
* Không `push` trực tiếp lên nhánh `main`.
* Mỗi tính năng / màn hình code trên nhánh riêng (`feature/xxx`).
* Hoàn thành + test xong → tạo Pull Request, leader review và merge.
