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
        String createTableTable = "CREATE TABLE IF NOT EXISTS ban_an (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ten_ban TEXT NOT NULL, " +
                "trang_thai TEXT DEFAULT 'TRONG'" +
                ");";

        String createFoodTable = "CREATE TABLE IF NOT EXISTS mon_an (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ten_mon TEXT NOT NULL, " +
                "gia_tien REAL NOT NULL, " +
                "danh_muc TEXT, " +
                "con_hang INTEGER DEFAULT 1" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableTable);
            stmt.execute(createFoodTable);

            // Chèn dữ liệu mẫu nếu bảng đang trống
            var resultSet = stmt.executeQuery("SELECT COUNT(*) FROM ban_an");
            if (resultSet.next() && resultSet.getInt(1) == 0) {
                stmt.execute("INSERT INTO ban_an (ten_ban, trang_thai) VALUES ('Bàn 1', 'TRONG');");
                stmt.execute("INSERT INTO ban_an (ten_ban, trang_thai) VALUES ('Bàn 2', 'TRONG');");
                stmt.execute("INSERT INTO ban_an (ten_ban, trang_thai) VALUES ('Bàn 3', 'TRONG');");

                stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc, con_hang) VALUES ('Phở Bò', 55000, 'Món chính', 1);");
                stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc, con_hang) VALUES ('Chả Giò', 40000, 'Khai vị', 1);");
                stmt.execute("INSERT INTO mon_an (ten_mon, gia_tien, danh_muc, con_hang) VALUES ('Cà Phê Sữa', 25000, 'Đồ uống', 1);");
                System.out.println(">>> Đã nạp dữ liệu mẫu thành công vào SQLite!");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khởi tạo cấu trúc dữ liệu: " + e.getMessage());
        }
    }

    // Hàm đóng kết nối an toàn
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