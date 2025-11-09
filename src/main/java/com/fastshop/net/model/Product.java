package com.fastshop.net.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@SuppressWarnings("serial")
@Data
@Entity 
@Table(name = "Products")
public class Product implements Serializable{
	@Id	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;

	@NotBlank(message = "Tên sản phẩm không được để trống.")
	@Column(name = "name", length = 50, nullable = false)
	String name;

	String image;
	
	@PositiveOrZero(message = "Giá sản phẩm phải lớn hơn 0.")
	Double price;

	@Temporal(TemporalType.DATE)
	@Column(name = "Createdate")
	Date createDate = new Date();

	@NotNull(message = "Bạn phải chọn trạng thái sản phẩm.")
	Boolean available;

	@PositiveOrZero(message = "Số lượng sản phẩm phải lớn hơn 0.")
	Integer number;

	@ManyToOne
	@JoinColumn(name = "Categoryid")
	Category category;

	@Column(name = "description", length = 10000, nullable = true)
	String describe;
	
	@JsonIgnore
	@OneToMany(mappedBy = "product")
	List<OrderDetail> orderDetails;	

	@JsonIgnore
	@OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	List<Comment> comments;
}
