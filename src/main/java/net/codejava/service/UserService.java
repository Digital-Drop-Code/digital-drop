package net.codejava.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import antlr.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.codejava.entity.Role;
import net.codejava.entity.User;
import net.codejava.model.UserModel;
import net.codejava.repo.UserRepository;
import net.codejava.utility.CustomException;

@Slf4j
@Service
public class UserService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	OtpCodeService otpsCodeService;
	
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Transactional(rollbackOn = {Exception.class, CustomException.class})
	public User createUser(@Validated UserModel userModel) throws Exception {
		try {
			User user = new User();
			BeanUtils.copyProperties(userModel,user);
			user.setPassword(passwordEncoder.encode(userModel.getPassword()));
			user = userRepository.save(user);
			Role role = new Role();
			role.setId(2);
			role.setName("USER");
			Set<Role> roles = new HashSet<>();
			roles.add(role);
			user.setRoles(roles);
			user = userRepository.save(user);
			otpsCodeService.generateAndEmailOtp(user);
			return user;
		}catch(Exception ex) {
			log.error("error in create user",ex);
			throw new CustomException("Error in creating user.", HttpStatus.BAD_REQUEST);
		}
	}
	
	public User activeExists(String email) {
		return userRepository.getUserByEmail(email);
	}

	@Transactional
	public void updateUser(User user, UserModel userModel) throws IOException {
		User userObj = userRepository.findById(user.getId())
		.orElseThrow(() -> new CustomException("no user", null));
		String[] ignoreProperties = {"id", "email", "password"};
		BeanUtils.copyProperties(userModel,userObj,ignoreProperties);
		if(userModel.getImage() != null && !userModel.getImage().isEmpty()) {
			userObj.setImageData(userModel.getImage().getBytes());
		}
		if(userModel.getPassword() != null && !org.thymeleaf.util.StringUtils.isEmpty(userModel.getPassword())) {
			userObj.setPassword(passwordEncoder.encode(userModel.getPassword()));
		}
		userRepository.save(userObj);
	}
}
