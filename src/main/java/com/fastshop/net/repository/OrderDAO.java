package com.fastshop.net.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fastshop.net.model.Account;
import com.fastshop.net.model.Order;

public interface OrderDAO extends JpaRepository<Order, Long> {

    // ✅ Lấy địa chỉ gần nhất (hoặc bất kỳ) theo tài khoản
    @Query("SELECT o.address FROM Order o WHERE o.account = :account GROUP BY o.address")
    Optional<String> findAddressByUsername(Account account);

    // ✅ Lọc theo ngày tạo (chính xác đến ngày)
    @Query("SELECT o FROM Order o WHERE DATE(o.createDate) = DATE(:createDate)")
    List<Order> findByCreateDate(Date createDate);

    // ✅ Lấy order có ngày nhỏ hơn ngày cho trước
    @Query("SELECT o FROM Order o WHERE o.createDate < :toNow")
    List<Order> findNotByCreateDate(Date toNow);

    // ✅ Tìm theo email hoặc phone của tài khoản
    @Query("SELECT o FROM Order o WHERE o.account.email = :email OR o.account.phone = :phone")
    List<Order> findAllByEmailOrPhone(String email, String phone);

    // ✅ Lọc theo khoảng thời gian
    @Query("SELECT o FROM Order o WHERE o.createDate BETWEEN :from AND :to")
    List<Order> findByCreateDateBetween(Date from, Date to);

    // ✅ Lọc theo status
    @Query("SELECT o FROM Order o WHERE o.status.id = :id")
    List<Order> findByStatus(Integer id);

    // ✅ Lọc theo status và account
    @Query("SELECT o FROM Order o WHERE o.status.id = :id AND o.account = :account")
    List<Order> findByStatusAndAccount(Integer id, Account account);

    // ✅ Tổng doanh thu theo năm (chuẩn MySQL)
    @Query("SELECT SUM(o.total) FROM Order o " +
            "WHERE FUNCTION('YEAR', o.createDate) = :year AND o.status.id = 4")
    Double totalRevenueByYear(int year);

    // ✅ Tổng đơn hàng thành công
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status.id = 4")
    Integer countOrderSuccess();

    // ✅ Danh sách username đã từng đặt hàng
    @Query("SELECT DISTINCT o.account.username FROM Order o")
    List<String> getListUsernameOrdered();
}

