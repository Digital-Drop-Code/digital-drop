package net.codejava.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ShippingModel {
	 
	@JsonProperty("first_name")
	private String firstName;
	@JsonProperty("last_name")
	private String lastName;
	@JsonProperty("address_1") 
	private String address1;
	@JsonProperty("address_2") 
	private String address2;     
	private String city;     
	private String state;     
	private String postcode;    
	private String country;
	private String phone;
}
