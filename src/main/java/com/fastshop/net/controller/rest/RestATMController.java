package com.fastshop.net.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fastshop.net.model.ATM;
import com.fastshop.net.model.Account;
import com.fastshop.net.service.ATMService;
import com.fastshop.net.service.AccountService;
import com.fastshop.net.service._CookieService;

@RestController
public class RestATMController {
    @Autowired
    AccountService accountService;
    @Autowired
    ATMService atmService;
    @Autowired
    _CookieService cookie;

    @GetMapping("/rest/atm/{accountId}")
    public ATM findAll(@PathVariable("accountId") String username) {
        Account account = accountService.findById(username);
        return atmService.findByAccount(account);
    }


    @PostMapping("/rest/atm")
    public ResponseEntity<?> create(@RequestBody ATM atm) {
        Account account = accountService.findById(cookie.getValue("username"));
        if (atm.getNumber() == null || atm.getNumber().isBlank() || atm.getCompany() == null || atm.getCompany().isBlank()) {
            return ResponseEntity.badRequest().body("(*) Vui lòng nhập đủ số tài khoản và ngân hàng.");
        }
        if (atmService.findByNumber(atm.getNumber().trim()) != null) {
            return ResponseEntity.badRequest().body("(*) Số tài khoản này đã được sử dụng.");
        }
        ATM current = atmService.findByAccount(account);
        if (current != null) {
            atmService.save(current); // vô hiệu thẻ cũ
        }
        atm.setId(null);
        atm.setNumber(atm.getNumber().trim());
        atm.setAccount(account);
        try {
            atmService.create(atm);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("(*) Số tài khoản này đã được sử dụng.");
        }
        return ResponseEntity.ok(atm);
    }

    @PutMapping("/rest/atm/{accountId}")
    public ATM update(@PathVariable("accountId") String username, @RequestBody ATM atm) {
        Account account = accountService.findById(username);
        if (atmService.findByAccount(account) != null) {
            atmService.save(atm);
        }
        return atm;
    }
}
