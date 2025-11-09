package com.fastshop.net.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fastshop.net.model.CatReport;
import com.fastshop.net.model.Order;
import com.fastshop.net.model.OrderDetail;
import com.fastshop.net.model.Product;

public interface OrderDetailDAO extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrder(Order order);

    // ✅ Tổng doanh thu tháng hiện tại
    @Query("SELECT SUM(od.quantity * od.product.price) " +
            "FROM OrderDetail od " +
            "WHERE FUNCTION('MONTH', od.order.createDate) = FUNCTION('MONTH', CURRENT_DATE()) " +
            "AND FUNCTION('YEAR', od.order.createDate) = FUNCTION('YEAR', CURRENT_DATE())")
    Double getTotalRevenue();

    // ✅ Tổng doanh thu tháng trước
    @Query("SELECT SUM(od.quantity * od.product.price) " +
            "FROM OrderDetail od " +
            "WHERE FUNCTION('MONTH', od.order.createDate) = FUNCTION('MONTH', CURRENT_DATE()) - 1 " +
            "AND FUNCTION('YEAR', od.order.createDate) = FUNCTION('YEAR', CURRENT_DATE())")
    Double getTotalRevenueLast();

    // ✅ Tổng đơn hàng tháng hiện tại
    @Query("SELECT COUNT(DISTINCT od.order.id) " +
            "FROM OrderDetail od " +
            "WHERE FUNCTION('MONTH', od.order.createDate) = FUNCTION('MONTH', CURRENT_DATE()) " +
            "AND FUNCTION('YEAR', od.order.createDate) = FUNCTION('YEAR', CURRENT_DATE())")
    Integer getTotalOrder();

    // ✅ Tổng đơn hàng tháng trước
    @Query("SELECT COUNT(DISTINCT od.order.id) " +
            "FROM OrderDetail od " +
            "WHERE FUNCTION('MONTH', od.order.createDate) = FUNCTION('MONTH', CURRENT_DATE()) - 1 " +
            "AND FUNCTION('YEAR', od.order.createDate) = FUNCTION('YEAR', CURRENT_DATE())")
    Integer getTotalOrderLast();

    // ✅ Đếm số lần sản phẩm xuất hiện trong order detail
    @Query("SELECT COUNT(od.product) FROM OrderDetail od WHERE od.product = ?1")
    Integer countByProduct(Product product);

    // ✅ Top 3 sản phẩm bán chạy nhất
    @Query("SELECT od.product.name FROM OrderDetail od " +
            "GROUP BY od.product.name " +
            "ORDER BY SUM(od.quantity) DESC")
    List<String> getTop3BestSelling();

    // ✅ Số lượng đơn hàng theo category
    @Query("SELECT new CatReport(od.product.category, COUNT(od)) " +
            "FROM OrderDetail od " +
            "GROUP BY od.product.category")
    List<CatReport> getNumberOrderedEachCategory();
}

