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
@Table(name = "Productdetails")
public class ProductDetail implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "info", length = 255)
    String info;

    @Column(name = "categoryDetailId", length = 5, nullable = false)
    String categoryDetailId;

    @Column(name = "productId", nullable = false)
    Integer productId;
}
