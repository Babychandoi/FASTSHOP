package com.fastshop.net.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastshop.net.security.TokenSigner;

@Service
public class _CookieService {
    @Autowired
    HttpServletRequest request;
    @Autowired
    HttpServletResponse response;
    @Autowired
    TokenSigner signer;

    // cookie đăng nhập được ký để client không thể tự sửa thành tài khoản khác
    private static final String AUTH_COOKIE = "username";

    /**
    * Đọc cookie từ request
    * @param name tên cookie cần đọc
    * @return đối tượng cookie đọc được hoặc null nếu không tồn tại
    */
    @Transactional
    public Cookie get(String name) {
        Cookie[] cookies = request.getCookies();
        Cookie cookie = null;
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals(name)) {
                    cookie = c;
                    break;
                }
            }
        }
        return cookie;
    }

    /**
    * Đọc giá trị của cookie từ request
    * @param name tên cookie cần đọc
    * @return chuỗi giá trị đọc được hoặc rỗng nếu không tồn tại
    */@Transactional
    public String getValue(String name) {
        Cookie[] cookies = request.getCookies();
        Cookie c = null;
        if (cookies == null) {
            return null;
        }
        else {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    c = cookie;
                }
            }
        }
        if (c == null) {
            return null;
        }
        return AUTH_COOKIE.equals(name) ? signer.verify(c.getValue()) : c.getValue();
    }


    /**
    * Tạo và gửi cookie về client
    * @param name tên cookie
    * @param value giá trị cookie
    * @param seconds thời hạn (giây)
    * @return đối tượng cookie đã tạo
    */@Transactional
    public Cookie add(String name, String value, int seconds) {
        Cookie cookie = new Cookie(name, AUTH_COOKIE.equals(name) ? signer.sign(value) : value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(seconds);
        cookie.setPath("/");
        response.addCookie(cookie);
        return cookie;
    }


    /**
    * Xóa cookie khỏi client
    * @param name tên cookie cần xóa
    */@Transactional
    public void remove(String name) {
        Cookie cookie = get(name);
        if (cookie != null) {
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setValue(null);
            response.addCookie(cookie);
        }
    }
    @Transactional
    public String getUsernameFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AUTH_COOKIE)) {
                    return signer.verify(cookie.getValue());
                }
            }
        }
        return null;
    }
}
