package com.fastshop.net.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fastshop.net.model.Account;
import com.fastshop.net.model.Authority;
import com.fastshop.net.model.Role;
import com.fastshop.net.model.WaitForStaff;
import com.fastshop.net.repository.AuthorityDAO;
import com.fastshop.net.repository.RoleDAO;
import com.fastshop.net.repository.WaitForStaffDAO;
import com.fastshop.net.service.AuthorityService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorityServiceImpl implements AuthorityService{
    @Autowired
    AuthorityDAO authorityDAO;

    @Autowired
    RoleDAO roleRepository;
    @Autowired
    WaitForStaffDAO waitForStaffDAO;

    @Override
    @Transactional(readOnly = true)
    public Role getRoleByAccount(Account account) {
        return authorityDAO.findByAccount(account).get().getRole();
    }

    @Override
    @Transactional(readOnly = true)
    public Authority findByAccount(Account account) {
        return authorityDAO.findByAccount(account).get();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Authority> findAll() {
        return authorityDAO.findAll();
    }

    @Override
    @Transactional
    public void save(Authority authority) {
        authorityDAO.save(authority);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        authorityDAO.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByAuthority(Authority authority) {
        authorityDAO.delete(authority);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getListStaff() {
        return authorityDAO.getEmployee();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findByKeyword(String keyword) {
        return authorityDAO.findByKeyword(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countByRoleEqualsUser() {
        return authorityDAO.countByRoleEqualsUser();
    }

    @Override
    @Transactional
    public void updateRoleByAccount(Account account) {
        Role staffRole = roleRepository.findById("STAFF").get();
        authorityDAO.updateRoleByAccount(account, staffRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitForStaff> getWaitForStaff() {
        return waitForStaffDAO.findAll();
    }
}
