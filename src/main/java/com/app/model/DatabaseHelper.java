package com.app.model;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_DIR = "./data";
    private static final String URL = "jdbc:sqlite:" + DB_DIR + "/nhahang.db";
    private static Connection connection = null;

    // Hàm lấy kết nối độc nhất (Singleton)
    public static synchronized Connection getConnection() {
        if (connection == null) {
            try {
                // Tự động tạo thư mục "data" nếu chưa có
                File directory = new File(DB_DIR);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Tải Driver SQLite và kết nối
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                System.out.println(">>> Kết nối CSDL SQLite thành công!");

                // Khởi tạo bảng dữ liệu ban đầu
                initializeDatabase();

            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Lỗi kết nối SQLite: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }

    // Hàm tạo cấu trúc bảng và chèn dữ liệu mẫu
    private static void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            // Kích hoạt tính năng hỗ trợ Khóa ngoại (FOREIGN KEY) trong SQLite (Mặc định SQLite tắt tính năng này)
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. BẢNG NHÂN VIÊN
            String createNhanVien = "CREATE TABLE IF NOT EXISTS nhan_vien (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "tai_khoan TEXT NOT NULL UNIQUE, " +
                    "mat_khau TEXT NOT NULL, " +
                    "ho_ten TEXT NOT NULL, " +
                    "vai_tro TEXT NOT NULL DEFAULT 'STAFF', " + // ADMIN hoặc STAFF
                    "trang_thai INTEGER DEFAULT 1" +            // 1: Đang làm, 0: Nghỉ việc
                    ");";
            stmt.execute(createNhanVien);

            // 2. BẢNG KHÁCH HÀNG
            String createKhachHang = "CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ho_ten TEXT NOT NULL, " +
                    "so_dien_thoai TEXT UNIQUE, " +
                    "diem_tich_luy INTEGER DEFAULT 0" +
                    ");";
            stmt.execute(createKhachHang);

            // 3. BẢNG DANH MỤC MÓN ĂN
            String createDanhMuc = "CREATE TABLE IF NOT EXISTS danh_muc (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ten_danh_muc TEXT NOT NULL UNIQUE" +
                    ");";
            stmt.execute(createDanhMuc);

            // 4. BẢNG MÓN ĂN
            String createMonAn = "CREATE TABLE IF NOT EXISTS mon_an (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ten_mon TEXT NOT NULL, " +
                    "gia_tien REAL NOT NULL, " +
                    "danh_muc_id INTEGER, " +
                    "con_hang INTEGER DEFAULT 1, " + // 1: Còn hàng, 0: Hết hàng
                    "FOREIGN KEY (danh_muc_id) REFERENCES danh_muc(id) ON DELETE SET NULL" +
                    ");";
            stmt.execute(createMonAn);

            // 5. BẢNG BÀN ĂN
            String createBanAn = "CREATE TABLE IF NOT EXISTS ban_an (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ten_ban TEXT NOT NULL UNIQUE, " +
                    "trang_thai TEXT NOT NULL DEFAULT 'TRONG'" + // TRONG, DANG_DUNG, DA_DAT
                    ");";
            stmt.execute(createBanAn);

            // 6. BẢNG ĐẶT BÀN TRƯỚC (BOOKING)
            String createDatBan = "CREATE TABLE IF NOT EXISTS dat_ban (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "khach_hang_id INTEGER, " +
                    "ban_an_id INTEGER, " +
                    "thoi_gian_dat TEXT NOT NULL, " + // Định dạng YYYY-MM-DD HH:MM:SS
                    "ghi_chu TEXT, " +
                    "trang_thai TEXT DEFAULT 'CHO_DEN', " + // CHO_DEN, DA_NHAN, HUY
                    "FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (ban_an_id) REFERENCES ban_an(id) ON DELETE CASCADE" +
                    ");";
            stmt.execute(createDatBan);

            // 7. BẢNG HÓA ĐƠN
            String createHoaDon = "CREATE TABLE IF NOT EXISTS hoa_don (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ban_an_id INTEGER, " +
                    "nhan_vien_id INTEGER, " +
                    "khach_hang_id INTEGER NULL, " +
                    "ngay_tao TEXT NOT NULL, " + // Định dạng YYYY-MM-DD HH:MM:SS
                    "tong_tien REAL DEFAULT 0, " +
                    "vat REAL DEFAULT 0.1, " +     // Mặc định 10% VAT
                    "giam_gia REAL DEFAULT 0, " +  // Số tiền được giảm thẳng
                    "trang_thai INTEGER DEFAULT 0, " + // 0: Chưa thanh toán, 1: Đã thanh toán
                    "FOREIGN KEY (ban_an_id) REFERENCES ban_an(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE SET NULL" +
                    ");";
            stmt.execute(createHoaDon);

            // 8. BẢNG CHI TIẾT HÓA ĐƠN (ORDER DETAILS)
            String createChiTietHoaDon = "CREATE TABLE IF NOT EXISTS chi_tiet_hoa_don (" +
                    "hoa_don_id INTEGER, " +
                    "mon_an_id INTEGER, " +
                    "so_luong INTEGER NOT NULL DEFAULT 1, " +
                    "gia_luc_mua REAL NOT NULL, " + // Lưu giá lúc mua đề phòng sau này menu đổi giá
                    "ghi_chu TEXT, " +
                    "PRIMARY KEY (hoa_don_id, mon_an_id), " +
                    "FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (mon_an_id) REFERENCES mon_an(id) ON DELETE CASCADE" +
                    ");";
            stmt.execute(createChiTietHoaDon);

            // NẠP DỮ LIỆU MẪU BAN ĐẦU (Chỉ chạy khi cơ sở dữ liệu trống)
            var resultSet = stmt.executeQuery("SELECT COUNT(*) FROM nhan_vien");
            if (resultSet.next() && resultSet.getInt(1) == 0) {
                mockData(stmt);
            }

        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo cấu trúc dữ liệu hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void mockData(Statement stmt) throws Exception {
        // Tài khoản đăng nhập mẫu
        stmt.execute("INSERT INTO nhan_vien (tai_khoan, mat_khau, ho_ten, vai_tro) VALUES ('admin', 'admin123', 'Quản Lý Trưởng', 'ADMIN');");
        stmt.execute("INSERT INTO nhan_vien (tai_khoan, mat_khau, ho_ten, vai_tro) VALUES ('staff', 'staff123', 'Nguyễn Văn Nhân Viên', 'STAFF');");

        // Khách hàng mẫu
        stmt.execute("INSERT INTO khach_hang (ho_ten, so_dien_thoai, diem_tich_luy) VALUES ('Khách Vãng Lai', '0000000000', 0);");
        stmt.execute("INSERT INTO khach_hang (ho_ten, so_dien_thoai, diem_tich_luy) VALUES ('Trần Chí Linh', '0912345678', 50);");

        // Danh mục mẫu
        stmt.execute("INSERT INTO danh_muc (ten_danh_muc) VALUES ('Món khai vị');");
        stmt.execute("INSERT INTO danh_muc (ten_danh_muc) VALUES ('Món chính');");
        stmt.execute("INSERT INTO danh_muc (ten_danh_muc) VALUES ('Đồ uống');");

        // Món ăn mẫu (Giả định ID danh mục tự tăng từ 1 đến 3)
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Súp cua tóc tiên', 45000, 1);");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Lẩu gà ớt hiểm', 280000, 2);");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Cơm chiên hải sản', 85000, 2);");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Trà đào cam sả', 30000, 3);");

        // Bàn ăn mẫu
        for (int i = 1; i <= i + 8; i++) { // Vòng lặp sinh nhanh từ Bàn 1 -> Bàn 9
            if (i > 9) break;
            stmt.execute("INSERT INTO ban_an (ten_ban, trang_thai) VALUES ('Bàn " + i + "', 'TRONG');");
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println(">>> Đã đóng kết nối SQLite an toàn.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}