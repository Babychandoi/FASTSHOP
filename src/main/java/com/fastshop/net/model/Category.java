package com.fastshop.net.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@SuppressWarnings("serial")
@Data
@Entity 
@Table(name = "Categories")
public class Category implements Serializable{
    
    @Id
    @Column(name = "id", length = 50, nullable = false, unique = true)
    private String id;

    @NotBlank(message = "Vui lòng nhập tên thể loại hàng.")
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "unit", length = 25)
    private String unit;

    @JsonIgnore
    @OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
    private List<Product> products;

    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<CategoryDetail> categoryDetails;
}