package com.app.model;

import com.app.config.AppConfig;
import com.app.util.PasswordHasher;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connection singleton SQLite + auto-init schema + migration + seed.
 *
 * <p>Phase 04 changes:
 * <ul>
 *   <li>Fix bug infinite-loop pattern khi tạo bàn ăn</li>
 *   <li>Tạo 20 bàn (UI demo cần)</li>
 *   <li>Hash password mock data (SHA-256+salt)</li>
 *   <li>Migration: ALTER TABLE hoa_don ADD tien_khach_dua + phuong_thuc_tt</li>
 * </ul>
 */
public class DatabaseHelper {

    private static final String URL = "jdbc:sqlite:" + AppConfig.DB_PATH;
    private static Connection connection = null;

    public static synchronized Connection getConnection() {
        try {
            // Một số DAO trong project dùng try-with-resources với connection,
            // khiến singleton bị close. Re-open nếu cần để tránh crash.
            if (connection == null || connection.isClosed()) {
                File directory = new File(AppConfig.DB_DIR);
                if (!directory.exists()) directory.mkdirs();

                Class.forName("org.sqlite.JDBC");
                boolean firstTime = (connection == null);
                connection = DriverManager.getConnection(URL);
                if (firstTime) {
                    System.out.println(">>> Kết nối CSDL SQLite thành công!");
                    initializeDatabase();
                    runMigrations();
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Lỗi kết nối SQLite: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    private static void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS nhan_vien (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tai_khoan TEXT NOT NULL UNIQUE,
                    mat_khau TEXT NOT NULL,
                    ho_ten TEXT NOT NULL,
                    vai_tro TEXT NOT NULL DEFAULT 'STAFF',
                    trang_thai INTEGER DEFAULT 1
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS khach_hang (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ho_ten TEXT NOT NULL,
                    so_dien_thoai TEXT UNIQUE,
                    diem_tich_luy INTEGER DEFAULT 0
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS danh_muc (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ten_danh_muc TEXT NOT NULL UNIQUE
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS mon_an (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ten_mon TEXT NOT NULL,
                    gia_tien REAL NOT NULL,
                    danh_muc_id INTEGER,
                    con_hang INTEGER DEFAULT 1,
                    FOREIGN KEY (danh_muc_id) REFERENCES danh_muc(id) ON DELETE SET NULL
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ban_an (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ten_ban TEXT NOT NULL UNIQUE,
                    trang_thai TEXT NOT NULL DEFAULT 'TRONG'
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS dat_ban (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    khach_hang_id INTEGER,
                    ban_an_id INTEGER,
                    thoi_gian_dat TEXT NOT NULL,
                    ghi_chu TEXT,
                    trang_thai TEXT DEFAULT 'CHO_DEN',
                    FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE CASCADE,
                    FOREIGN KEY (ban_an_id) REFERENCES ban_an(id) ON DELETE CASCADE
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS hoa_don (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ban_an_id INTEGER,
                    nhan_vien_id INTEGER,
                    khach_hang_id INTEGER NULL,
                    ngay_tao TEXT NOT NULL,
                    tong_tien REAL DEFAULT 0,
                    vat REAL DEFAULT 0.1,
                    giam_gia REAL DEFAULT 0,
                    trang_thai INTEGER DEFAULT 0,
                    FOREIGN KEY (ban_an_id) REFERENCES ban_an(id) ON DELETE SET NULL,
                    FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id) ON DELETE SET NULL,
                    FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(id) ON DELETE SET NULL
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS chi_tiet_hoa_don (
                    hoa_don_id INTEGER,
                    mon_an_id INTEGER,
                    so_luong INTEGER NOT NULL DEFAULT 1,
                    gia_luc_mua REAL NOT NULL,
                    ghi_chu TEXT,
                    PRIMARY KEY (hoa_don_id, mon_an_id),
                    FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE,
                    FOREIGN KEY (mon_an_id) REFERENCES mon_an(id) ON DELETE CASCADE
                )""");

            // Seed lần đầu
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM nhan_vien")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    mockData(stmt);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo CSDL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Migration thêm cột mới — idempotent (check trước khi ALTER). */
    private static void runMigrations() {
        try (Statement stmt = connection.createStatement()) {
            if (!columnExists(stmt, "hoa_don", "tien_khach_dua")) {
                stmt.execute("ALTER TABLE hoa_don ADD COLUMN tien_khach_dua REAL DEFAULT 0");
                System.out.println(">>> Migration: added hoa_don.tien_khach_dua");
            }
            if (!columnExists(stmt, "hoa_don", "phuong_thuc_tt")) {
                stmt.execute("ALTER TABLE hoa_don ADD COLUMN phuong_thuc_tt TEXT DEFAULT 'TIEN_MAT'");
                System.out.println(">>> Migration: added hoa_don.phuong_thuc_tt");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi migration: " + e.getMessage());
        }
    }

    private static boolean columnExists(Statement stmt, String table, String column) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) return true;
            }
        }
        return false;
    }

    private static void mockData(Statement stmt) throws Exception {
        // Hash passwords trước khi insert (Phase 04 security)
        String adminHash = PasswordHasher.hash("admin123");
        String staffHash = PasswordHasher.hash("staff123");

        stmt.execute("INSERT INTO nhan_vien (tai_khoan, mat_khau, ho_ten, vai_tro) VALUES " +
                "('admin', '" + adminHash + "', 'Quản Lý Trưởng', 'ADMIN')");
        stmt.execute("INSERT INTO nhan_vien (tai_khoan, mat_khau, ho_ten, vai_tro) VALUES " +
                "('staff', '" + staffHash + "', 'Nguyễn Văn Nhân Viên', 'STAFF')");

        stmt.execute("INSERT INTO khach_hang (ho_ten, so_dien_thoai, diem_tich_luy) VALUES ('Khách Vãng Lai', '0000000000', 0)");
        stmt.execute("INSERT INTO khach_hang (ho_ten, so_dien_thoai, diem_tich_luy) VALUES ('Trần Chí Linh', '0912345678', 50)");

        stmt.execute("INSERT INTO danh_muc (ten_danh_muc) VALUES ('Món khai vị')");
        stmt.execute("INSERT INTO danh_muc (ten_danh_muc) VALUES ('Món chính')");
        stmt.execute("INSERT INTO danh_muc (ten_danh_muc) VALUES ('Đồ uống')");
        stmt.execute("INSERT INTO danh_muc (ten_danh_muc) VALUES ('Tráng miệng')");

        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Súp cua tóc tiên', 45000, 1)");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Gỏi cuốn tôm thịt', 40000, 1)");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Lẩu gà ớt hiểm', 280000, 2)");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Cơm chiên hải sản', 85000, 2)");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Phở bò tái nạm', 65000, 2)");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Bún chả Hà Nội', 55000, 2)");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Trà đào cam sả', 30000, 3)");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Coca Cola', 15000, 3)");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Bia Heineken', 35000, 3)");
        stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc_id) VALUES ('Chè khúc bạch', 25000, 4)");

        // Tạo 20 bàn (fix bug for-loop cũ)
        for (int i = 1; i <= 20; i++) {
            stmt.execute("INSERT INTO ban_an (ten_ban, trang_thai) VALUES ('Bàn " + i + "', 'TRONG')");
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
