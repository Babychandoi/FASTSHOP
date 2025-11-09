package com.fastshop.net.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@Entity
@Table(name = "ATM")
public class ATM implements Serializable {
    @Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "company", length = 70, nullable = false)
    private String company;

	@Column(name = "number", length = 30, nullable = false, unique = true)
    private String number;

    @ManyToOne 
    @JoinColumn(name = "username", nullable = false)
	private Account account;

    @Column(name = "is_valid")
    private Boolean valid;
}