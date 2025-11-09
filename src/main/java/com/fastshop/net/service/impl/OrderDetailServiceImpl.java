package com.fastshop.net.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fastshop.net.model.CatReport;
import com.fastshop.net.model.OrderDetail;
import com.fastshop.net.model.Product;
import com.fastshop.net.repository.OrderDetailDAO;
import com.fastshop.net.service.OrderDetailService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderDetailServiceImpl implements OrderDetailService{
    @Autowired
    OrderDetailDAO orderDetailDAO;

    @Override
    @Transactional
    public void save(OrderDetail orderDetail) {
        orderDetailDAO.save(orderDetail);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        orderDetailDAO.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetail findById(Long id) {
        return orderDetailDAO.findById(id).get();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDetail> findAll() {
        return orderDetailDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalRevenue() {
        try {
            return orderDetailDAO.getTotalRevenue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalOrder() {
        try {
            return orderDetailDAO.getTotalOrder();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getTop3BestSelling() {
        return orderDetailDAO.getTop3BestSelling();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countByProduct(Product product) {
        return orderDetailDAO.countByProduct(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalRevenueLast() {
        return orderDetailDAO.getTotalRevenueLast();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalOrderLast() {
        return orderDetailDAO.getTotalOrderLast();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatReport> getNumberOrderedEachCategory() {
        return orderDetailDAO.getNumberOrderedEachCategory();
    }
}