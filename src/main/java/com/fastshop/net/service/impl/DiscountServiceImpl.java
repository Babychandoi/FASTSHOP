package com.fastshop.net.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fastshop.net.model.Discount;
import com.fastshop.net.repository.DiscountDAO;
import com.fastshop.net.service.DiscountService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscountServiceImpl implements DiscountService {
    @Autowired
    DiscountDAO discountDAO;

    @Override
    @Transactional
    public void save(Discount discount) {
        discountDAO.save(discount);
    }

    @Override
    @Transactional(readOnly = true)
    public Discount findById(String id) {
        return discountDAO.findById(id).get();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Discount> findByAll() {
        return discountDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Discount> findByDolarNotNull() {
        return discountDAO.findByDolarNotNull();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Discount> findByFreeNotNull() {
        return discountDAO.findByFreeNotNull();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Discount> findByFreeEqualNumber(Double free) {
        return discountDAO.findByFreeEqualNumber(free);
    }

    /**
     *  0: if both dates are equal.
     *  A value less than 0: if the date is before the argument date.
     *  A value greater than 0: if the date is after the argument date.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Discount> findByDiscountExpiry() {
        return findByAll().stream()
                          .filter(item -> item.getDateEnd().compareTo(new Date()) < 0)
                          .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Discount> findByDiscountUnexpiry() {
        return findByAll().stream()
                          .filter(item -> item.getDateEnd().compareTo(new Date()) >= 0)
                          .collect(Collectors.toList());
    }
}
