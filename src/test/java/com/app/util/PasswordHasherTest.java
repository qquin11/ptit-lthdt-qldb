package com.app.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashKhongTraVeNguyenBan() {
        String hash = PasswordHasher.hash("admin123");
        assertNotEquals("admin123", hash);
        assertTrue(hash.contains(":"));
    }

    @Test
    void verifyDungVoiMatKhauDung() {
        String hash = PasswordHasher.hash("matkhau123");
        assertTrue(PasswordHasher.verify("matkhau123", hash));
    }

    @Test
    void verifySaiVoiMatKhauSai() {
        String hash = PasswordHasher.hash("matkhau123");
        assertFalse(PasswordHasher.verify("matkhauKhac", hash));
    }

    @Test
    void hashHaiLanRaKhacNhau() {
        // Salt random nên mỗi lần hash khác nhau, nhưng verify vẫn ok
        String h1 = PasswordHasher.hash("abc123");
        String h2 = PasswordHasher.hash("abc123");
        assertNotEquals(h1, h2);
        assertTrue(PasswordHasher.verify("abc123", h1));
        assertTrue(PasswordHasher.verify("abc123", h2));
    }

    @Test
    void verifyPlainTextLegacy() {
        // Tài khoản cũ chưa hash, verify so sánh trực tiếp
        assertTrue(PasswordHasher.verify("admin123", "admin123"));
        assertFalse(PasswordHasher.verify("sai", "admin123"));
    }

    @Test
    void needsRehashDungVoiPlainText() {
        assertTrue(PasswordHasher.needsRehash("admin123"));
        assertFalse(PasswordHasher.needsRehash(PasswordHasher.hash("admin123")));
    }

    @Test
    void verifyNullTraVeFalse() {
        assertFalse(PasswordHasher.verify(null, "x"));
        assertFalse(PasswordHasher.verify("x", null));
    }
}
