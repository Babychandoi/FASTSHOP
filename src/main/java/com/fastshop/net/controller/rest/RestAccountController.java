package com.fastshop.net.controller.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fastshop.net.model.Account;
import com.fastshop.net.security.Passwords;
import com.fastshop.net.service.AccountService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin("*")
@RestController
public class RestAccountController {
    @Autowired
    AccountService accountService;

    @GetMapping("/rest/accounts")
    public List<Account> get(Model model) {
        return accountService.findAll();
    }

    @GetMapping("/rest/accounts/{username}")
    public Account getById(@PathVariable("username") String username) {
        return accountService.findById(username);
    }

    @PostMapping("/rest/accounts")
    public Account post(@RequestBody Account account) {
        account.setPassword(Passwords.hash(account.getPassword()));
        accountService.save(account);
        return account;
    }

    @PutMapping("/rest/accounts/{username}")
    public Account put(@PathVariable("username") String username, @RequestBody Account account) {
        if (account.getPassword() == null || account.getPassword().isBlank()) {
            account.setPassword(accountService.findById(username).getPassword());
        } else if (!Passwords.isHashed(account.getPassword())) {
            account.setPassword(Passwords.hash(account.getPassword()));
        }
        accountService.save(account);
        return account;
    }

    // Đổi mật khẩu: body {"currentPassword": "...", "newPassword": "..."}
    @PostMapping("/rest/accounts/{username}/password")
    public ResponseEntity<String> changePassword(@PathVariable("username") String username, @RequestBody Map<String, String> body) {
        Account account = accountService.findById(username);
        String current = body.getOrDefault("currentPassword", "");
        String next = body.getOrDefault("newPassword", "");
        if (account == null || !Passwords.matches(current, account.getPassword())) {
            return ResponseEntity.badRequest().body("(*) Mật khẩu hiện tại không chính xác.");
        }
        if (next.length() < 6) {
            return ResponseEntity.badRequest().body("(*) Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        account.setPassword(Passwords.hash(next));
        accountService.save(account);
        return ResponseEntity.ok("OK");
    }
    
    @DeleteMapping("/rest/accounts/{username}")
    public void delete(@PathVariable("username") String username) {
        accountService.deleteById(username);
    }
}
