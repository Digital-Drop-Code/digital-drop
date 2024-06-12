package net.codejava.repo;

import org.springframework.data.repository.CrudRepository;

import net.codejava.entity.StoreInfo;
import net.codejava.entity.User;

public interface StoreRepository extends CrudRepository<StoreInfo, Long> {

	StoreInfo findByUser(User user);

}
