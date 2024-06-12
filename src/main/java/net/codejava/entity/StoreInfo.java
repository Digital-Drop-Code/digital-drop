package net.codejava.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "ddr_store")
public class StoreInfo {

	@Id
	@Column(name = "store_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;
	
	@Column(name = "store_link", length = 1000)
	private String link;
	
	@Column(name = "field_1", length = 1000)
	private String field1;
	
	@Column(name = "field_2", length = 1000)
	private String field2;
	
	@Column(name = "field_3", length = 1000)
	private String field3;
	
	@ManyToOne
    @JoinColumn(name = "type", referencedColumnName = "type_id")
	private StoreType storeType;
	
	@ManyToOne
    @JoinColumn(name = "courier_type", referencedColumnName = "type_id")
	private CourierType courierType;
}
