package net.codejava.service;

import org.apache.commons.text.CaseUtils;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.codejava.entity.Order;
import net.codejava.utility.CustomException;
@Service
@Slf4j
public class PostExService {

	private final RestTemplate restTemplate;
    ObjectMapper objectMapper = new ObjectMapper();

	public PostExService() {
        this.restTemplate = new RestTemplate();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    }
	public String generatTrackingNo(Order order, String token) {
	    // Set the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("token", token);
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

        if(order.getPhone() == null || order.getPhone().isEmpty()) {
        	throw new CustomException("PHONE NO IS MISSING", HttpStatus.BAD_REQUEST);

        }
        String API = "https://api.postex.pk/services/integration/api/order/v3/create-order";
        JSONObject request = new JSONObject();
		request.put("cityName",  CaseUtils.toCamelCase(order.getCity(), false,' '));
		request.put("customerName", order.getFirstName() + " " + order.getLastName());
		request.put("customerPhone", order.getPhone());
		request.put( "deliveryAddress", order.getAddress1());
		request.put("invoiceDivision", 0);
		request.put("invoicePayment", order.getTotal());
		request.put("items", 1);
		request.put("orderRefNumber", order.getId().getOrderNo());
		request.put("orderType", "Normal");
		request.put("pickupAddressCode","001");
        HttpEntity<String> requestEntity = new HttpEntity<>(request.toString(), headers);
        String responseBody = "";
        try {
        	responseBody = restTemplate.postForObject(API, requestEntity, String.class);
        }catch(HttpClientErrorException ex) {
        	responseBody = ex.getResponseBodyAsString();
        	//throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        String error = "";
        // Parse the response as a JSON object
        JSONObject jsonResponse = new JSONObject(responseBody);
        if(jsonResponse.get("statusCode").equals("200")) {
        	JSONObject dist = (JSONObject) jsonResponse.get("dist");
        	return (String) dist.get("trackingNumber");
        }else if(jsonResponse.get("statusMessage") != null) {
        	error = (String) jsonResponse.get("statusMessage");
        	throw new CustomException(error, HttpStatus.BAD_REQUEST);
        }else {
        	throw new CustomException("error while generating tracking no", HttpStatus.BAD_REQUEST);
        }
	}
}
