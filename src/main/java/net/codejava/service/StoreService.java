package net.codejava.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.codejava.config.MyUserDetails;
import net.codejava.entity.CourierType;
import net.codejava.entity.JazzAccount;
import net.codejava.entity.StoreInfo;
import net.codejava.entity.StoreType;
import net.codejava.entity.User;
import net.codejava.repo.CourierTypeRepository;
import net.codejava.repo.JazzAccountRepository;
import net.codejava.repo.StoreRepository;
import net.codejava.repo.StoreTypeRepository;

@Service
@Slf4j
public class StoreService {

	@Autowired
	StoreRepository repository;
	
	@Autowired
	StoreTypeRepository storeTyperepository;
	
	@Autowired
	JazzAccountRepository jazzAccountRepository;
	
	@Autowired
	CourierTypeRepository courierTyperepository;
	
	public StoreInfo getStoreByUser(User user) {
		try {
			return repository.findByUser(user);
		}catch(Exception ex) {
			log.error("error in getstoreby user", ex);
			throw ex;
		}
	}
	
	public List<StoreType> fetchAllStoreType(){
		return storeTyperepository.findAll();
	}
	
	public List<CourierType> fetchAllCourierType(){
		return courierTyperepository.findAll();
	}
	
	public JazzAccount getAccountByUser(User user) {
		try {
			return jazzAccountRepository.findByUser(user);
		}catch(Exception ex) {
			log.error("error in getstoreby user", ex);
			throw ex;
		}
	}

	@Transactional
	public void saveJazzSettings(JazzAccount acc, MyUserDetails userDetail) {

		User user = new User();
		user.setId(userDetail.getUser().getId());
		acc.setUser(user);
		jazzAccountRepository.save(acc);
		
	}

	@Transactional
	public void saveStoreInfo(StoreInfo storeInfo, MyUserDetails userDetail) {

		User user = new User();
		user.setId(userDetail.getUser().getId());
		storeInfo.setUser(user);
		repository.save(storeInfo);
	}
}
