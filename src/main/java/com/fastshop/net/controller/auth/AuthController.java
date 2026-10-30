package com.fastshop.net.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fastshop.net.model.Account;
import com.fastshop.net.model.Authority;
import com.fastshop.net.model.DtoLogin;
import com.fastshop.net.model.Role;
import com.fastshop.net.security.Passwords;
import com.fastshop.net.service._CookieService;
import com.fastshop.net.service.AccountService;
import com.fastshop.net.service.AuthorityService;
import com.fastshop.net.service.RoleService;
import com.fastshop.net.service.UserService;

@Controller
public class AuthController {
    @Autowired
    _CookieService cookie;
    @Autowired
    AuthorityService authorityService;
    @Autowired
    AccountService accountService;
    @Autowired
    RoleService roleService;
    @Autowired
    UserService userService;

    // this is function access login when user login from form
    @RequestMapping("/auth/login")
    public String authLogin(@Validated @ModelAttribute("dtoLogin") DtoLogin dtoLogin, Errors errors, Model model) {
        if (errors.hasErrors()) {
            return "sign-in";
        }
        else {
            try {
                Account account = accountService.findByUsernameOrEmailAndPassword(dtoLogin.getUsernameOrEmail().trim(), dtoLogin.getUsernameOrEmail().trim(), dtoLogin.getPassword().trim());
                if (account == null) {
                    model.addAttribute("message", "(*) Tên đăng nhập hoặc mật khẩu không đúng.");
                    return "sign-in";
                }
                Authority auth = authorityService.findByAccount(account);
                cookie.add("username", auth.getAccount().getUsername(), 30*24*60*60);
                return "redirect:%s".formatted( auth.getRole().getId().equals("ADMIN") ? "/admin/home" : auth.getRole().getId().equals("STAFF") ? "/staff/home" : "/fastshop.com");
            } catch (NullPointerException e) {
                return "error/error-500";
            } catch (Exception k) {
                return "error/error-404";
            }
        }
    }



    // this is function access login when user login from form
    @RequestMapping("/auth/company/login")
    public String authLoginStaff(@Validated @ModelAttribute("dtoLogin") DtoLogin dtoLogin, Errors errors, Model model) {
        if (errors.hasErrors()) {
            return "login_staff_admin";
        }
        else {
            try {
                Account account = accountService.findByUsernameOrEmailAndPassword(dtoLogin.getUsernameOrEmail().trim(), dtoLogin.getUsernameOrEmail().trim(), dtoLogin.getPassword().trim());
                if (account == null) {
                    model.addAttribute("message", "(*) Tên đăng nhập hoặc mật khẩu không đúng.");
                    return "login_staff_admin";
                }
                Authority auth = authorityService.findByAccount(account);
                if (auth.getRole().getId().equals("USER")) {
                    model.addAttribute("message", "(*) Tài khoản khách hàng vui lòng đăng nhập tại trang mua sắm.");
                    return "login_staff_admin";
                }
                if (auth.getRole().getId().equals("STAFF") && Boolean.FALSE.equals(account.getActive())) {
                    model.addAttribute("message", "(*) Tài khoản nhân viên này đã bị tạm dừng.");
                    return "login_staff_admin";
                }
                else {
                    cookie.add("username", auth.getAccount().getUsername(), 1*24*60*60);
                    return auth.getRole().getId().equals("ADMIN") ? "redirect:/admin/home" : "redirect:/staff/home";
                }
            } catch (Exception e) {
                return "error/error-404";
            }
        }
    }


    // this is function access signup
    @RequestMapping("/auth/signup")
    public String signup(@Validated @ModelAttribute("account") Account account, Errors errors) {
        if (account.getUsername() != null && accountService.existsByUsername(account.getUsername().trim())) {
            errors.rejectValue("username", "duplicate", "(*) Tên đăng nhập đã được sử dụng.");
        }
        if (account.getEmail() != null && accountService.existsByEmail(account.getEmail().trim())) {
            errors.rejectValue("email", "duplicate", "(*) Email đã được sử dụng.");
        }
        if (errors.hasErrors()) {
            return "sign-up";
        }
        else {
            try {
                account.setActive(true);
                account.setPassword(Passwords.hash(account.getPassword()));
                accountService.save(account);
                Role role = roleService.findById("USER");
                Authority authority = new Authority();
                authority.setAccount(account);
                authority.setRole(role);
                authorityService.save(authority);
                cookie.add("username", authority.getAccount().getUsername(), 30*24*60*60);
                return "redirect:/";
            } catch (Exception e) {
                e.printStackTrace();
                return "error/error-404";
            }
        }
    }

    @RequestMapping("/auth/update")
    public String update(@ModelAttribute("item") Account form) {
        // chỉ cập nhật thông tin liên hệ của chính tài khoản đang đăng nhập
        String username = cookie.getValue("username");
        if (username == null) {
            return "redirect:/login";
        }
        Account account = accountService.findByUsernameOrEmail(username, username);
        if (form.getPhone() != null && !form.getPhone().isBlank()) {
            account.setPhone(form.getPhone().trim());
        }
        if (form.getEmail() != null && !form.getEmail().isBlank()) {
            account.setEmail(form.getEmail().trim());
        }
        try {
            accountService.save(account);
        } catch (Exception e) {
            return "redirect:/profile?updateError=true";
        }
        return "redirect:/profile";

    }

}
