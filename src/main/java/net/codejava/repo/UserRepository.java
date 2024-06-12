package net.codejava.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import net.codejava.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

	@Query("SELECT u FROM User u WHERE u.email = :email and u.enabled = true")
	public User getUserByEmail(@Param("email") String email );

	public User findByEmail(String email);
}
