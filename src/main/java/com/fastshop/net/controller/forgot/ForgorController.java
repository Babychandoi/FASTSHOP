package com.fastshop.net.controller.forgot;

import org.springframework.stereotype.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fastshop.net.model.Account;
import com.fastshop.net.model.AccountDTO;
import com.fastshop.net.model.Role;
import com.fastshop.net.security.Passwords;
import com.fastshop.net.security.TokenSigner;
import com.fastshop.net.service._CookieService;
import com.fastshop.net.service._MailService;
import com.fastshop.net.service.AccountService;
import com.fastshop.net.service.AuthorityService;
import com.fastshop.net.service.CategoryService;
import com.fastshop.net.service.HistoryService;
import com.fastshop.net.service.ProductService;
import com.fastshop.net.service._SessionService;
@Controller
public class ForgorController {
    private static final long RESET_TOKEN_TTL_SECONDS = 30 * 60;

    @Value("${app.base-url}")
    String baseUrl;
    @Autowired
    TokenSigner signer;
    @Autowired
    _CookieService cookie;
    @Autowired
    _SessionService session;
    @Autowired
    _MailService mailService;
    @Autowired
    _SessionService sessionService;
    @Autowired
    AuthorityService authService;
    @Autowired
    AccountService accountService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    HistoryService historyService;
    @Autowired
    ProductService cartSevice;
    
    @RequestMapping("/forgot/access")
    public String sendMail(Model model, @RequestParam("email") String email) {
        email = email.trim();
        if (email.isEmpty()) {
            model.addAttribute("error_forgot", "(*) Vui lòng nhập email.");
            return "forgot";
        }
        if (accountService.existsByEmail(email)) {
            // link chứa token ký kèm hạn dùng 30 phút, không thể tự tạo cho email khác
            String token = signer.signWithExpiry(email + "|" + passwordFingerprint(accountService.findByEmail(email)), RESET_TOKEN_TTL_SECONDS);
            String link = baseUrl + "/ChangeForgot?token=" + token;
            try {
                mailService.send(email, "Đặt lại mật khẩu Fastshop",
                        "<p>Bạn vừa yêu cầu đặt lại mật khẩu tài khoản Fastshop.</p>"
                        + "<p><a href='" + link + "'>Nhấn vào đây để đặt mật khẩu mới</a> (link có hiệu lực trong 30 phút).</p>"
                        + "<p>Nếu không phải bạn yêu cầu, hãy bỏ qua email này.</p>");
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error_forgot", "(*) Không gửi được email, vui lòng thử lại sau.");
                return "forgot";
            }
        }
        // luôn báo thành công để không lộ email nào đã đăng ký
        return "success/success";
    }

    @RequestMapping("/ChangeForgot")
    public String ChangeForgot(Model model, @RequestParam(value = "token", required = false) String token) {
        if (emailFromToken(token) == null) {
            model.addAttribute("error_forgot", "(*) Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn. Vui lòng gửi lại yêu cầu.");
            return "forgot";
        }
        model.addAttribute("accounts", new AccountDTO());
        model.addAttribute("token", token);
        return "change-forgot";
    }

    @RequestMapping("/ChangeForgotPassword")
    public String uploadPassword(Model model, @ModelAttribute("accounts") AccountDTO accountDTO,
                                 @RequestParam(value = "token", required = false) String token) {
        String email = emailFromToken(token);
        if (email == null) {
            model.addAttribute("error_forgot", "(*) Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn. Vui lòng gửi lại yêu cầu.");
            return "forgot";
        }
        String newPassword = accountDTO.getNewPassword() == null ? "" : accountDTO.getNewPassword();
        String confirmPassword = accountDTO.getConfirmPassword() == null ? "" : accountDTO.getConfirmPassword();
        model.addAttribute("error1", newPassword.isEmpty() ? "(*) Mật khẩu mới cần phải được nhập ..." :
                newPassword.length() < 6 ? "(*) Mật khẩu phải có ít nhất 6 ký tự." : "");
        model.addAttribute("error2", confirmPassword.isEmpty() ? "(*) Mật khẩu Xác nhận cần được nhập ..." :
                !newPassword.equals(confirmPassword) ? "(*) Không trùng mật khẩu" : "");
        model.addAttribute("token", token);
        if (newPassword.length() < 6 || !newPassword.equals(confirmPassword)) {
            return "change-forgot";
        }
        Account account = accountService.findByEmail(email);
        account.setPassword(Passwords.hash(newPassword));
        accountService.save(account);
        Role role = authService.findByAccount(account).getRole();
        return role.getId().equals("USER") ? "redirect:/login" : "redirect:/login.fastshop.com";
    }

    // token chỉ dùng được 1 lần: gắn với mật khẩu hiện tại, đổi mật khẩu xong token cũ hết hiệu lực
    private String emailFromToken(String token) {
        String value = signer.verifyWithExpiry(token);
        if (value == null || value.indexOf('|') < 0) {
            return null;
        }
        String email = value.substring(0, value.lastIndexOf('|'));
        String fingerprint = value.substring(value.lastIndexOf('|') + 1);
        if (!accountService.existsByEmail(email)) {
            return null;
        }
        return fingerprint.equals(passwordFingerprint(accountService.findByEmail(email))) ? email : null;
    }

    private static String passwordFingerprint(Account account) {
        // không đưa nguyên chuỗi hash vào link, chỉ dùng mã băm ngắn của nó
        return Integer.toHexString(account.getPassword().hashCode());
    }
}
