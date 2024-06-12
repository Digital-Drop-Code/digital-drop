package net.codejava.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import net.codejava.entity.JazzAccount;
import net.codejava.entity.User;

public interface JazzAccountRepository extends JpaRepository<JazzAccount, Long> {

	JazzAccount findByUser(User user);

}
