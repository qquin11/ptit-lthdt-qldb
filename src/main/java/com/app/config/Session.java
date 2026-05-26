package com.app.config;

import com.app.model.NhanVien;

/** Singleton lưu nhân viên đăng nhập hiện tại. Thread-safe đủ cho single-user app. */
public final class Session {

    private static volatile NhanVien currentUser;

    private Session() {}

    public static void set(NhanVien u) { currentUser = u; }
    public static NhanVien get()       { return currentUser; }
    public static void clear()         { currentUser = null; }

    public static boolean isLoggedIn() { return currentUser != null; }
    public static boolean isAdmin()    { return currentUser != null && "ADMIN".equals(currentUser.getVaiTro()); }

    public static String currentName() { return currentUser == null ? "Khách" : currentUser.getHoTen(); }
    public static String currentRole() { return currentUser == null ? "GUEST" : currentUser.getVaiTro(); }
}
