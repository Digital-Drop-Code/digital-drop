package net.codejava.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.codejava.utility.CustomException;

@Service
@Slf4j
public class EmailService {

	@Autowired
	private JavaMailSender emailSender;

	public void sendSimpleMessage(String to, String subject, String text) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			message.setSubject(subject);
			message.setText(text);
			emailSender.send(message);
		}catch(Exception ex) {
			log.error("error in sending email",ex);
			throw new CustomException("Error while sending email.",HttpStatus.CONFLICT);
		}
	}
}
