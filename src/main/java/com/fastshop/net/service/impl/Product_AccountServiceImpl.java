package com.fastshop.net.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fastshop.net.model.Account;
import com.fastshop.net.model.Product;
import com.fastshop.net.model.Product_Account;
import com.fastshop.net.repository.Product_AccountRepository;
import com.fastshop.net.service.Product_AccountService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Product_AccountServiceImpl implements Product_AccountService{
    @Autowired
    private Product_AccountRepository product_accountRepository;

    @Override
    @Transactional
    public Account getAccountWhoPostedProduct(Product product) {
        Product_Account product_account = product_accountRepository.findByProduct(product);
        return product_account != null ? product_account.getAccount() : null;
    }

    @Override
    @Transactional
    public Product_Account addProduct_Account(Product_Account product_account) {
        return product_accountRepository.save(product_account);
    }

    @Override
    @Transactional
    public List<Product_Account> getProduct_AccountsByAccount(Account account) {
        return product_accountRepository.findByAccount(account);
    }
    
}
