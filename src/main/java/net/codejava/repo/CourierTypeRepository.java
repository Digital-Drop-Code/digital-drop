package net.codejava.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import net.codejava.entity.CourierType;

public interface CourierTypeRepository extends JpaRepository<CourierType, Long> {


}
