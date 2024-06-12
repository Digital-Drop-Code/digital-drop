package net.codejava.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "ddr_otp")
@Data
public class OtpCode {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "otp_value")
	private String otp;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "entity_id")
	private Long entityId;
	
	
}
