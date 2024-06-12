package net.codejava.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "ddr_jazz_acc")
public class JazzAccount {

	@Id
	@Column(name = "acc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;
	
	@Column(name = "jazz_id", length = 1000)
	private String jazzId;
	
	@Column(name = "jazz_password", length = 1000)
	private String jazzPassword;
	
}
