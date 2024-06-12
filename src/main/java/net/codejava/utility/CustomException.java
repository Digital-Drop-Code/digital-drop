package net.codejava.utility;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomException extends RuntimeException {

	private String message;
	private HttpStatus httpStatus;	
}
