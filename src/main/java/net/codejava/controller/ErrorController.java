package net.codejava.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ErrorController {

	@ExceptionHandler(Exception.class)
	public String Exception(Exception ex) {
		log.error("error", ex);
		return "errorPage";
	}
}
