package com.fastshop.net.service;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class _SessionService {
    @Autowired
    HttpSession session;
    @Transactional
    public Object get(String name) {
        return session.getAttribute(name);
    }
    @Transactional
    public void set(String name, Object value) {
        session.setAttribute(name, value);
    }
    @Transactional
    public void remove(String name) {
        session.removeAttribute(name);
    }
}
