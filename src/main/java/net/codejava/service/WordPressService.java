
package net.codejava.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.codejava.entity.StoreInfo;
import net.codejava.model.OrderModel;
import java.io.File;
import java.io.IOException;
@Service
public class WordPressService {

    private final String BASE_URL = "/wp/wp-json/wc/v3/";

	private final RestTemplate restTemplate;
	
    ObjectMapper objectMapper = new ObjectMapper();


    public WordPressService() {
        this.restTemplate = new RestTemplate();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    }
    
    public  List<OrderModel> getTodaysOrders(StoreInfo store) throws Exception {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        LocalDate yes= LocalDate.now().minusDays(1);

        String url = UriComponentsBuilder.fromHttpUrl(store.getLink() + BASE_URL + "orders")
                .queryParam("consumer_key", store.getField1())
                .queryParam("consumer_secret", store.getField2())
                .queryParam("after", yes.format(formatter) + "T00:00:00")
                .queryParam("before", today.format(formatter) + "T23:59:59")
                .build().toUriString();

        String data =  restTemplate.getForObject(url, String.class);
        List<OrderModel> orderList = objectMapper.readValue(data, new TypeReference<List<OrderModel>>() {});
        return orderList;
    }
    
}
