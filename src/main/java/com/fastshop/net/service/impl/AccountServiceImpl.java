package com.fastshop.net.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fastshop.net.model.Account;
import com.fastshop.net.repository.AccountDAO;
import com.fastshop.net.service.AccountService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountServiceImpl implements AccountService{
    @Autowired
    AccountDAO accountDAO;

    @Override
    @Transactional(readOnly = true)
    public Account findById(String username) {
        return accountDAO.findById(username).get();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAdministrators() {
        return accountDAO.getAdministrators();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean existsByUsernameOrEmail(String username, String email) {
        return accountDAO.existsByUsernameOrEmail(username, email);
    }

    @Override
    @Transactional(readOnly = true)
    public Account findByEmail(String email) {
        return accountDAO.findByEmail(email).get();
    }

    @Override
    @Transactional(readOnly = true)
    public Account findByUsernameOrEmail(String username, String email) {
        return accountDAO.findByUsernameOrEmail(username, email).get();
    }

    @Override
    @Transactional(readOnly = true)
    public Account findByUsername(String username) {
        return accountDAO.findByUsername(username).get();
    }

    @Override
    @Transactional(readOnly = true)
    public Account findByUsernameOrEmailAndPassword(String username, String email, String password) {
        return accountDAO.findByUsernameOrEmailAndPassword(username, email, password);
    }

    @Override
    @Transactional
    public void save(Account account) {
        accountDAO.save(account);
    }

    @Override
    @Transactional
    public void deleteById(String username) {
        accountDAO.deleteById(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean existsByEmail(String email) {
        return accountDAO.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean existsByUsername(String username) {
        return accountDAO.existsByUsername(username);
    }   
}
