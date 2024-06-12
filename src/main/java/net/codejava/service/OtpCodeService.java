package net.codejava.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.codejava.entity.OtpCode;
import net.codejava.entity.User;
import net.codejava.repo.OtpCodeRepository;
import net.codejava.repo.UserRepository;
import net.codejava.utility.CustomException;

@Service
@Slf4j
public class OtpCodeService {

	@Autowired
	OtpCodeRepository repository;
	
	@Autowired
	EmailService emailService;

	@Autowired
	UserRepository userRepository;
	
	@Transactional
	public OtpCode generateAndEmailOtp(User user) {
		try {
			OtpCode otp = new OtpCode();
			otp.setEntityId(user.getId());
			otp.setType("USER");
			otp.setOtp(generateRandomOTP());
			otp = repository.save(otp);
			String subject = "OTP for email verification";
			String body = "Your OTP for email verification is " + otp.getOtp();
			emailService.sendSimpleMessage(user.getEmail(), subject, body);
			return otp;
		}catch(Exception ex) {
			log.error("error in saving otp", ex);
			throw new CustomException("Error in generating OTP", HttpStatus.BAD_REQUEST);
		}
	}
	
	public OtpCode getOtp(String otpCode,String entity, Long entityId) {
		try {
			
			return repository.findFirstByOtpAndTypeAndEntityId(otpCode, entity, entityId);
		}catch(Exception ex) {
			log.error("error in getting otp", ex);
			throw new CustomException("Error in getting OTP", HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	public void deleteOtp(String otpCode,String entity) {
		try {
			
			OtpCode otp =  repository.findFirstByOtpAndType(otpCode, entity);
			if(otp != null) {
				repository.delete(otp);
			}
		}catch(Exception ex) {
			log.error("error in getting otp", ex);
			throw new CustomException("Error in getting OTP", HttpStatus.BAD_REQUEST);
		}
	}
	
	public String generateRandomOTP() {
        // Generate a random 6-digit OTP (you can customize this as needed)
        StringBuilder generatedToken = new StringBuilder();
        try {
            SecureRandom number = SecureRandom.getInstance("SHA1PRNG");
            for (int i = 0; i < 6; i++) {
                generatedToken.append(number.nextInt(9));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return generatedToken.toString();
    }

	@Transactional
	public void verifyOtp(User user, String otp) {
		try {
			if(user == null)
				throw new CustomException("User not found", HttpStatus.BAD_REQUEST);
			OtpCode otpCode = repository.findFirstByOtpAndTypeAndEntityId(otp, "USER", user.getId());
			if(otpCode == null)
				throw new CustomException("OTP not found", HttpStatus.BAD_REQUEST);
			else {
				user.setEnabled(Boolean.TRUE);
				userRepository.save(user);
				repository.delete(otpCode);
			}			
		}catch (Exception ex) {
			log.error("error in verify otp", ex);
			throw ex;
		}
		
	}
}
