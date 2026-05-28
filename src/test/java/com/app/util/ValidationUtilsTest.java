package com.app.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void requiredOk() {
        assertTrue(ValidationUtils.isRequired("abc"));
        assertFalse(ValidationUtils.isRequired(""));
        assertFalse(ValidationUtils.isRequired("   "));
        assertFalse(ValidationUtils.isRequired(null));
    }

    @Test
    void phoneVnHopLe() {
        assertTrue(ValidationUtils.isPhoneVN("0912345678"));
        assertTrue(ValidationUtils.isPhoneVN("0987654321"));
    }

    @Test
    void phoneVnSai() {
        assertFalse(ValidationUtils.isPhoneVN("1234567890"));   // không bắt đầu 0
        assertFalse(ValidationUtils.isPhoneVN("091234567"));    // 9 số
        assertFalse(ValidationUtils.isPhoneVN("09123456789"));  // 11 số
        assertFalse(ValidationUtils.isPhoneVN("091234567a"));   // có chữ
    }

    @Test
    void emailHopLe() {
        assertTrue(ValidationUtils.isEmail("test@example.com"));
        assertTrue(ValidationUtils.isEmail("a.b+c@domain.co.uk"));
    }

    @Test
    void emailSai() {
        assertFalse(ValidationUtils.isEmail("abc"));
        assertFalse(ValidationUtils.isEmail("@example.com"));
        assertFalse(ValidationUtils.isEmail("a@b"));
    }

    @Test
    void usernameHopLe() {
        assertTrue(ValidationUtils.isUsername("admin"));
        assertTrue(ValidationUtils.isUsername("user_01"));
    }

    @Test
    void usernameSai() {
        assertFalse(ValidationUtils.isUsername("a"));         // quá ngắn
        assertFalse(ValidationUtils.isUsername("user name"));  // có space
        assertFalse(ValidationUtils.isUsername("user@x"));     // có ký tự đặc biệt
    }

    @Test
    void positivePass() {
        assertTrue(ValidationUtils.isPositive(1));
        assertTrue(ValidationUtils.isPositive(0.01));
        assertFalse(ValidationUtils.isPositive(0));
        assertFalse(ValidationUtils.isPositive(-1));
    }
}
