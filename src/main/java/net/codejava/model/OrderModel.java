package net.codejava.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderModel {

   @JsonProperty("id")
   private String orderNo;
   @JsonProperty("date_created")
   private String dateCreated;
   @JsonProperty("status")
   private String externalStatus;
   private Double total;
   private String currency;
   
	private String firstName;
	private String lastName;
	private String address1;
	private String address2;     
	private String city;     
	private String state;     
	private String postcode;    
	private String country;
	private String phone;
	
	private String billEmail;
	
    @JsonProperty("shipping")
    private void unpackShipping(Map<String, String> shipping) {
    	firstName = shipping.get("first_name");
    	lastName = shipping.get("last_name");
    	address1 = shipping.get("address_1");
    	address2 = shipping.get("address_2");
    	city = shipping.get("city");
    	state = shipping.get("state");
    	postcode = shipping.get("postcode");
    	country = shipping.get("country");
    	phone= shipping.get("phone");
    }
    
    @JsonProperty("billing")
    private void unpackBilling(Map<String, String> billing) {
    	 billEmail = billing.get("email");
    }

	
}
