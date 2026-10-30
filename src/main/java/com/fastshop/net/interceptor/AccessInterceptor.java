package com.fastshop.net.interceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fastshop.net.repository.AccountDAO;
import com.fastshop.net.repository.AuthorityDAO;
import com.fastshop.net.service._CookieService;

/**
 * Phân quyền theo vai trò cho trang quản lý và REST API.
 * - Trang: /admin/**, /account/** chỉ ADMIN; /staff/**, /category/** ... cho STAFF và ADMIN.
 * - REST: một số GET danh mục công khai; khách hàng chỉ được thao tác trên dữ liệu của chính mình;
 *   còn lại dành cho STAFF/ADMIN, một vài API chỉ ADMIN.
 */
@Component
public class AccessInterceptor implements HandlerInterceptor {
    private static final String ADMIN = "ADMIN";
    private static final String STAFF = "STAFF";
    private static final String USER = "USER";

    // GET công khai (trang chi tiết sản phẩm, giỏ hàng, voucher ...)
    private static final Pattern PUBLIC_GET = Pattern.compile(
            "/rest/(products/\\d+|categories(/.*)?|categorydetails/.*|status/.*|product/detail/.*|productdetail/.*"
            + "|comments/.*|discounts/(?!all$).*)");

    // API mà khách hàng được gọi, {username} phải là chính mình
    private static final Pattern OWNED = Pattern.compile(
            "/rest/(accounts|notifications|atm|orders/status)/([^/]+)(/.*)?");

    // API khách hàng được gọi, controller tự kiểm tra quyền sở hữu
    private static final Pattern CUSTOMER_ALLOWED = Pattern.compile(
            "(POST /rest/orders/checkout|POST /rest/comments|POST /rest/atm"
            + "|(GET|PUT) /rest/notifications/notify/\\d+)");

    // API chỉ dành cho ADMIN
    private static final Pattern ADMIN_ONLY = Pattern.compile(
            "((GET|POST) /rest/accounts|DELETE /rest/accounts/.*|(POST|PUT|DELETE) /rest/authorities.*"
            + "|GET /rest/authorities(/(account|role)/.*)?|.* /rest/history.*|GET /rest/orders/year/.*"
            + "|GET /rest/orderdetails/category/report|GET /rest/roles)");

    @Autowired
    _CookieService cookie;
    @Autowired
    AccountDAO accountDAO;
    @Autowired
    AuthorityDAO authorityDAO;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();
        String username = cookie.getValue("username");
        String role = roleOf(username);

        if (path.startsWith("/rest/")) {
            return checkRest(method, path, username, role, response);
        }
        return checkPage(path, role, response);
    }

    private boolean checkPage(String path, String role, HttpServletResponse response) throws Exception {
        boolean adminPage = path.startsWith("/admin") || path.startsWith("/account/") || path.startsWith("/confirmStaff/")
                || path.startsWith("/delete-wait/") || path.startsWith("/check-wait/") || path.equals("/wait-for-staff");
        boolean staffPage = path.startsWith("/staff/") || path.startsWith("/category/") || path.equals("/notify/add");
        if (!adminPage && !staffPage) {
            return true;
        }
        if (role == null) {
            response.sendRedirect("/login.fastshop.com");
            return false;
        }
        if (ADMIN.equals(role) || (staffPage && STAFF.equals(role))) {
            return true;
        }
        response.sendRedirect("/");
        return false;
    }

    private boolean checkRest(String method, String path, String username, String role, HttpServletResponse response) throws Exception {
        if ("GET".equals(method) && PUBLIC_GET.matcher(path).matches()) {
            return true;
        }
        if (role == null) {
            return deny(response, HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        String call = method + " " + path;
        if (ADMIN.equals(role)) {
            return true;
        }
        Matcher owned = OWNED.matcher(path);
        boolean ownsPath = owned.matches() && owned.group(2).equals(username);
        if (STAFF.equals(role)) {
            boolean adminOnly = ADMIN_ONLY.matcher(call).matches() && !"GET /rest/authorities/staff/active".equals(call);
            // nhân viên chỉ được sửa tài khoản của chính mình
            boolean otherAccount = path.startsWith("/rest/accounts/") && !"GET".equals(method) && !ownsPath;
            if (adminOnly || otherAccount) {
                return deny(response, HttpServletResponse.SC_FORBIDDEN, "Không có quyền thực hiện thao tác này.");
            }
            return true;
        }
        // khách hàng
        if (CUSTOMER_ALLOWED.matcher(call).matches()) {
            return true;
        }
        if (ownsPath && !"DELETE".equals(method)) {
            return true;
        }
        return deny(response, HttpServletResponse.SC_FORBIDDEN, "Không có quyền thực hiện thao tác này.");
    }

    private String roleOf(String username) {
        if (username == null) {
            return null;
        }
        return accountDAO.findById(username)
                .flatMap(authorityDAO::findByAccount)
                .map(auth -> auth.getRole().getId())
                .orElse(null);
    }

    private static boolean deny(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
        return false;
    }
}
