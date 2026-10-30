package com.fastshop.net.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Ký / kiểm tra chuỗi bằng HMAC-SHA256 để client không thể tự giả mạo
 * (cookie đăng nhập, link đặt lại mật khẩu ...).
 * Nếu không cấu hình app.secret thì sinh khóa ngẫu nhiên mỗi lần khởi động.
 */
@Component
public class TokenSigner {
    private final byte[] key;

    public TokenSigner(@Value("${app.secret:}") String secret) {
        if (secret == null || secret.isBlank()) {
            key = new byte[32];
            new SecureRandom().nextBytes(key);
        } else {
            key = secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    /** payload -> "payload.chữ_ký" */
    public String sign(String payload) {
        return payload + "." + hmac(payload);
    }

    /** Trả về payload nếu chữ ký hợp lệ, ngược lại null. */
    public String verify(String token) {
        if (token == null) {
            return null;
        }
        int dot = token.lastIndexOf('.');
        if (dot <= 0) {
            return null;
        }
        String payload = token.substring(0, dot);
        byte[] expected = hmac(payload).getBytes(StandardCharsets.UTF_8);
        byte[] actual = token.substring(dot + 1).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual) ? payload : null;
    }

    /** Token có hạn dùng: payload được mã hóa base64 kèm thời điểm hết hạn. */
    public String signWithExpiry(String value, long ttlSeconds) {
        long expiresAt = System.currentTimeMillis() / 1000 + ttlSeconds;
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((value + "|" + expiresAt).getBytes(StandardCharsets.UTF_8));
        return sign(payload);
    }

    /** Trả về giá trị nếu token hợp lệ và chưa hết hạn, ngược lại null. */
    public String verifyWithExpiry(String token) {
        String payload = verify(token);
        if (payload == null) {
            return null;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            int sep = decoded.lastIndexOf('|');
            long expiresAt = Long.parseLong(decoded.substring(sep + 1));
            return expiresAt >= System.currentTimeMillis() / 1000 ? decoded.substring(0, sep) : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
