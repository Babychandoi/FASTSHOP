package com.fastshop.net.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fastshop.net.model.Account;
import com.fastshop.net.model.Notify;
import com.fastshop.net.service.AccountService;
import com.fastshop.net.service.AuthorityService;
import com.fastshop.net.service.NotifyService;
import com.fastshop.net.service._CookieService;

@RestController
public class RestNotifyController {
    @Autowired
    NotifyService notifyService;
    @Autowired
    AccountService accountService;
    @Autowired
    AuthorityService authorityService;
    @Autowired
    _CookieService cookie;

    @GetMapping("/rest/notifications/{username}")
    public List<Notify> findAll(@PathVariable("username") String username) {
        Account account = accountService.findByUsername(username);
        return notifyService.findAll(account);
    }

    @GetMapping("/rest/notifications/{username}/true")
    public List<Notify> findAllTrue(@PathVariable("username") String username) {
        Account account = accountService.findByUsername(username);
        return notifyService.findAllByAccAndNowAndStatusTrueOrderBy(account);
    }

    @GetMapping("/rest/notifications/{username}/false")
    public List<Notify> findAllFalse(@PathVariable("username") String username) {
        Account account = accountService.findByUsername(username);
        return notifyService.findAllByAccAndNowAndStatusFalseOrderBy(account);
    }

    @GetMapping("/rest/notifications/notify/{id}")
    public ResponseEntity<Notify> findById(@PathVariable("id") Long id) {
        Notify notify = notifyService.findById(id);
        return canAccess(notify) ? ResponseEntity.ok(notify) : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping("/rest/notifications")
    public void save(@RequestBody Notify notify) {
        notifyService.save(notify);
    }

    @PutMapping("/rest/notifications/notify/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Long id) {
        Notify notify = notifyService.findById(id);
        if (!canAccess(notify)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        notify.setStatus(false);
        notifyService.save(notify);
        return ResponseEntity.ok().build();
    }

    private boolean canAccess(Notify notify) {
        String username = cookie.getValue("username");
        if (notify == null || username == null) {
            return false;
        }
        String role = authorityService.findByAccount(accountService.findByUsername(username)).getRole().getId();
        return !"USER".equals(role) || notify.getAccount().getUsername().equals(username);
    }
}
