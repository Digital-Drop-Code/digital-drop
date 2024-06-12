package net.codejava.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.expression.ParseException;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Entity
@Table(name = "ddr_order")
@Data
public class Order implements Serializable {
	
	private static final long serialVersionUID = 7745612847836313294L;

	@EmbeddedId
	private OrderKey id;
	  
	@Column(name = "status")
	private String status;
	
	@Column(name = "settlement_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date settledDate;
	
	@Column(name = "order_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date dateCreated;
	
	@Column(name = "order_external_status")
	private String externalStatus;
	
	@Column(name = "order_amount")
	private Double total;
	
	@Column(name = "order_currency")
	private String currency;
	
	@Column(name = "fname")
	private String firstName;
	
	@Column(name = "lname")
	private String lastName;
	
	@Column(name = "adress1")
	private String address1;
	
	@Column(name = "address2")
	private String address2; 
	
	@Column(name = "city")
	private String city;  
	
	@Column(name = "state")
	private String state;  
	
	@Column(name = "postcode")
	private String postcode; 
	
	@Column(name = "country")
	private String country;
	
	@Column(name = "phone")
	private String phone;
	
	@Column(name = "email")
	private String billEmail;

	@Column(name = "tracking_no")
	private String trackingNo;
	
	@Column(name = "transaction_id")
	private String transactionId;
	
	@ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;
	
}
