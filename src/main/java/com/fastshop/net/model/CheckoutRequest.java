package com.fastshop.net.model;

import java.util.List;

import lombok.Data;

/** Dữ liệu khách gửi lên khi đặt hàng; giá và tổng tiền luôn được tính lại ở server. */
@Data
public class CheckoutRequest {
    private String address;
    private String voucher;
    private List<Item> items;

    @Data
    public static class Item {
        private Integer productId;
        private Integer quantity;
    }
}
