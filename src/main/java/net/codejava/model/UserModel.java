package net.codejava.model;


import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class UserModel {

	private Long id;


	private String fullName;
	
	
	private String email;
	
	
	private String password;
	
	private String website;
	
	private String businessName;
	
	private String businessDescr;
	
	private MultipartFile image;
	
}
