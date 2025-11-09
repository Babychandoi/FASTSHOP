package com.fastshop.net.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@SuppressWarnings("serial")
@Data
@Entity 
@Table(name = "Addresses")
public class Address implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place", length = 200, nullable = false)
    private String place;

    @Column(name = "is_choose") // Đổi tên column để tránh từ khóa
    private Boolean choose;

    @Column(name = "username", length = 30, nullable = false) // Tăng length cho username
    private String username;
}