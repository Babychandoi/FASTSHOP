package com.fastshop.net.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fastshop.net.model.Account;
import com.fastshop.net.model.Order;
import com.fastshop.net.repository.OrderDAO;
import com.fastshop.net.service.OrderService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    OrderDAO orderDAO;

    @Override
    @Transactional(readOnly = true)
    public String findAddressByUsername(Account account) {
        try {
            return orderDAO.findAddressByUsername(account).get();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        List<Order> orders = orderDAO.findAll();
        // Force load orderDetails to avoid LazyInitializationException
        orders.forEach(order -> order.getOrderDetails().size());
        return orders;
    }

    @Override
    @Transactional
    public void save(Order order) {
        orderDAO.save(order);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        orderDAO.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        Order order = orderDAO.findById(id).get();
        // Force load orderDetails to avoid LazyInitializationException
        order.getOrderDetails().size();
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOfOrderToday(Date date) {
        List<Order> orders = orderDAO.findByCreateDate(date);
        orders.forEach(order -> order.getOrderDetails().size());
        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findNotByCreateDate(Date toNow) {
        List<Order> orders = orderDAO.findNotByCreateDate(toNow);
        orders.forEach(order -> order.getOrderDetails().size());
        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByCreateDateBetween(Date from, Date to) {
        List<Order> orders = orderDAO.findByCreateDateBetween(from, to);
        orders.forEach(order -> order.getOrderDetails().size());
        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllByEmailOrPhone(String email, String phone) {
        List<Order> orders = orderDAO.findAllByEmailOrPhone(email, phone);
        orders.forEach(order -> order.getOrderDetails().size());
        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllByPriceBetween() {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByStatus(Integer statusId) {
        List<Order> orders = orderDAO.findByStatus(statusId);
        orders.forEach(order -> order.getOrderDetails().size());
        return orders;
    }

    @Override
    @Transactional
    public List<Order> findByStatusAndAccount(Integer id, Account account) {
        List<Order> orders = orderDAO.findByStatusAndAccount(id, account);
        // Force load orderDetails to avoid LazyInitializationException
        orders.forEach(order -> order.getOrderDetails().size());
        return orders;
    }

    @Override
    @Transactional
    public Double totalRevenueByYear(int year) {
        return orderDAO.totalRevenueByYear(year);
    }

    @Override
    @Transactional
    public Integer countOrderSuccess() {
        return orderDAO.countOrderSuccess();
    }

    @Override
    @Transactional
    public List<String> getListUsernameOrdered() {
        return orderDAO.getListUsernameOrdered();
    }
}
