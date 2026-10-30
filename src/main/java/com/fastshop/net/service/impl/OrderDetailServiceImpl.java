package com.fastshop.net.service.impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fastshop.net.model.Account;
import com.fastshop.net.model.CatReport;
import com.fastshop.net.model.CheckoutRequest;
import com.fastshop.net.model.Discount;
import com.fastshop.net.model.Notify;
import com.fastshop.net.model.Order;
import com.fastshop.net.model.OrderDetail;
import com.fastshop.net.model.Product;
import com.fastshop.net.repository.DiscountDAO;
import com.fastshop.net.repository.NotifyDAO;
import com.fastshop.net.repository.OrderDAO;
import com.fastshop.net.repository.StatusDAO;
import com.fastshop.net.repository.OrderDetailDAO;
import com.fastshop.net.repository.ProductDAO;
import com.fastshop.net.service.OrderDetailService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderDetailServiceImpl implements OrderDetailService{
    @Autowired
    OrderDetailDAO orderDetailDAO;
    @Autowired
    ProductDAO productDAO;
    @Autowired
    OrderDAO orderDAO;

    public static final double SHIP_FEE = 1.2;

    @Autowired
    DiscountDAO discountDAO;
    @Autowired
    StatusDAO statusDAO;
    @Autowired
    NotifyDAO notifyDAO;

    // Tạo đơn hàng từ giỏ: giá, tồn kho, voucher và tổng tiền đều tính ở server trong 1 transaction
    @Override
    @Transactional
    public Order checkout(Account account, CheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng của bạn đang trống.");
        }
        if (request.getAddress() == null || request.getAddress().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn địa chỉ giao hàng.");
        }
        Order order = new Order();
        order.setAccount(account);
        order.setAddress(request.getAddress().trim());
        order.setCreateDate(new Date());
        order.setDateConfirm(null);
        order.setStatus(statusDAO.findById(0).orElseThrow());
        order.setTotal(0.0);
        order = orderDAO.save(order);

        double subtotal = 0;
        for (CheckoutRequest.Item item : request.getItems()) {
            Product product = productDAO.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));
            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setOrder(order);
            detail.setQuantity(item.getQuantity());
            placeItem(detail);
            subtotal += item.getQuantity() * product.getPrice();
        }

        double total = subtotal + SHIP_FEE;
        if (request.getVoucher() != null && !request.getVoucher().isBlank()) {
            Discount voucher = discountDAO.findById(request.getVoucher().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại."));
            LocalDate today = LocalDate.now();
            if (toLocalDate(voucher.getDateFrom()).isAfter(today) || toLocalDate(voucher.getDateEnd()).isBefore(today)) {
                throw new IllegalArgumentException("Mã giảm giá không còn hiệu lực.");
            }
            if (voucher.getNumber() != null && voucher.getNumber() <= 0) {
                throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng.");
            }
            if (voucher.getFree() != null && voucher.getFree() > 0) {
                total -= total * voucher.getFree() / 100;
            } else if (voucher.getDolar() != null && voucher.getDolar() > 0) {
                total -= Math.min(voucher.getDolar(), total);
            }
            if (voucher.getNumber() != null) {
                voucher.setNumber(voucher.getNumber() - 1);
                discountDAO.save(voucher);
            }
        }
        order.setTotal(Math.round(total * 100) / 100.0);
        order = orderDAO.save(order);

        Notify notify = new Notify();
        notify.setAccount(account);
        notify.setTitle("Bạn đã đặt hàng #" + order.getId() + " thành công. Tổng tiền $" + String.format("%.2f", order.getTotal()) + ".");
        notify.setFileName("- Tin nhắn thông báo -");
        notify.setSentDate(new Date());
        notify.setStatus(true);
        notifyDAO.save(notify);
        return order;
    }

    private static LocalDate toLocalDate(Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate();
    }

    // Lưu 1 dòng của đơn hàng và trừ tồn kho của sản phẩm
    @Override
    @Transactional
    public OrderDetail placeItem(OrderDetail orderDetail) {
        Product product = productDAO.findById(orderDetail.getProduct().getId()).orElseThrow();
        Order order = orderDAO.findById(orderDetail.getOrder().getId()).orElseThrow();
        int quantity = orderDetail.getQuantity() == null ? 1 : orderDetail.getQuantity();
        int stock = product.getNumber() == null ? 0 : product.getNumber();
        if (quantity <= 0 || quantity > stock) {
            throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ số lượng trong kho");
        }
        product.setNumber(stock - quantity);
        product.setAvailable(product.getNumber() > 0);
        orderDetail.setProduct(productDAO.save(product));
        orderDetail.setOrder(order);
        orderDetail.setQuantity(quantity);
        return orderDetailDAO.save(orderDetail);
    }

    // Trả lại số lượng sản phẩm vào kho khi đơn hàng bị hủy
    @Override
    @Transactional
    public void restoreStock(Order order) {
        for (OrderDetail detail : orderDetailDAO.findByOrder(order)) {
            Product product = detail.getProduct();
            int stock = product.getNumber() == null ? 0 : product.getNumber();
            product.setNumber(stock + detail.getQuantity());
            product.setAvailable(true);
            productDAO.save(product);
        }
    }

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