package net.codejava.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;

@Data
public class OrderKey implements Serializable {


	@Column(name = "order_no")	
	private String orderNo;
	
	@ManyToOne
    @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    private StoreInfo store;
	
}
