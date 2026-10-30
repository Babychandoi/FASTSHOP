package com.fastshop.net.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Băm và kiểm tra mật khẩu tài khoản (BCrypt). */
public final class Passwords {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private Passwords() {
    }

    public static String hash(String raw) {
        return ENCODER.encode(raw);
    }

    public static boolean matches(String raw, String stored) {
        return raw != null && isHashed(stored) && ENCODER.matches(raw, stored);
    }

    public static boolean isHashed(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
