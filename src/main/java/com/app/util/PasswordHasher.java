package com.app.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SHA-256 + random salt. Format storage: <code>{base64Salt}:{base64Hash}</code>.
 *
 * <p>Dùng SHA-256 thay BCrypt để không phải thêm dep, đủ tốt cho đồ án.
 *
 * <p>Migration: tài khoản cũ còn lưu plain text — {@link #verify} tự detect và migrate
 * lần đầu user đăng nhập đúng. Caller cần check {@link #needsRehash} sau verify success
 * để re-save hash.
 */
public final class PasswordHasher {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int SALT_LENGTH = 16;

    private PasswordHasher() {}

    /** Hash plain password → "{base64Salt}:{base64Hash}" */
    public static String hash(String plain) {
        byte[] salt = new byte[SALT_LENGTH];
        RNG.nextBytes(salt);
        byte[] digest = digest(plain, salt);
        return Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(digest);
    }

    /** Verify plain với stored value. Hỗ trợ cả plain text legacy + hashed format. */
    public static boolean verify(String plain, String stored) {
        if (plain == null || stored == null) return false;
        // Legacy plain text (no ":") — direct compare
        if (!stored.contains(":")) {
            return plain.equals(stored);
        }
        String[] parts = stored.split(":", 2);
        if (parts.length != 2) return false;
        try {
            byte[] salt   = Base64.getDecoder().decode(parts[0]);
            byte[] expect = Base64.getDecoder().decode(parts[1]);
            byte[] actual = digest(plain, salt);
            return constantTimeEquals(expect, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** True nếu stored vẫn ở dạng plain text legacy — caller nên re-hash sau verify success. */
    public static boolean needsRehash(String stored) {
        return stored != null && !stored.contains(":");
    }

    // INTERNAL

    private static byte[] digest(String plain, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            md.update(plain.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) diff |= a[i] ^ b[i];
        return diff == 0;
    }
}
