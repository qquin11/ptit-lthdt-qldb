package com.app.util;

import java.util.regex.Pattern;

/** Validation helpers cho form input. */
public final class ValidationUtils {

    private static final Pattern PHONE_VN = Pattern.compile("^0\\d{9}$"); // 10 số bắt đầu 0
    private static final Pattern EMAIL    = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");

    private ValidationUtils() {}

    public static boolean isRequired(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** Số điện thoại Việt Nam: 10 số, bắt đầu bằng 0 */
    public static boolean isPhoneVN(String s) {
        return s != null && PHONE_VN.matcher(s.trim()).matches();
    }

    public static boolean isEmail(String s) {
        return s != null && EMAIL.matcher(s.trim()).matches();
    }

    /** Username: 3-30 ký tự, chỉ chữ/số/_ */
    public static boolean isUsername(String s) {
        return s != null && USERNAME.matcher(s.trim()).matches();
    }

    public static boolean isPositive(double n) {
        return n > 0;
    }

    public static boolean isPositiveInteger(String s) {
        try {
            return Integer.parseInt(s.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
