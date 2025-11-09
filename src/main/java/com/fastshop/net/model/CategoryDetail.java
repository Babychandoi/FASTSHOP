package com.fastshop.net.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@SuppressWarnings("serial")
@Data
@Entity
@Table(name = "Categorydetails")
public class CategoryDetail implements Serializable {
    
    @Id
    @Column(name = "id", length = 5, nullable = false)
    private String id;

    @Column(name = "property", length = 70, nullable = false)
    private String property;

    @ManyToOne 
    @JoinColumn(name = "category_id")
    private Category category;
}