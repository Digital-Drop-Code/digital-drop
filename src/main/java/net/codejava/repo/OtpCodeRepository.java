package net.codejava.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import net.codejava.entity.OtpCode;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

	OtpCode findFirstByOtp(String  otpCode);

	OtpCode findFirstByOtpAndType(String otpCode, String entity);

	OtpCode findFirstByOtpAndTypeAndEntityId(String  otpCode, String entity, Long entityId);

}
