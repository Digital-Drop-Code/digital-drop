package net.codejava.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;
import net.codejava.config.MyUserDetails;
import net.codejava.entity.JazzAccount;
import net.codejava.entity.StoreInfo;
import net.codejava.entity.User;
import net.codejava.service.StoreService;
import net.codejava.utility.Constant;

@Controller
@Slf4j
public class StoreController {

	@Autowired
	StoreService storeService;
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/settings")
	public String settings(Model model, @AuthenticationPrincipal MyUserDetails userDetail) {
		User user = userDetail.getUser();
		model.addAttribute("section","settings");
		model.addAttribute("storeTypes",storeService.fetchAllStoreType());
		model.addAttribute("courierTypes",storeService.fetchAllCourierType());
		model.addAttribute("store",storeService.getStoreByUser(user));
		model.addAttribute("jazzAcc",storeService.getAccountByUser(user));
		return "settings";
	}
	

	@PreAuthorize("hasAuthority('USER')")
	@PostMapping("/saveSettings")
	public String saveSettings(Model model,
			@ModelAttribute StoreInfo storeInfo, RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal MyUserDetails userDetail) {
		try {
			storeService.saveStoreInfo(storeInfo, userDetail);
		}catch(Exception ex) {
		 log.error("error while saving details", ex);
		 redirectAttributes.addFlashAttribute(Constant.ERROR,"Error while saving.");
		}
		return "redirect:/settings";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@PostMapping("/saveJazzSettings")
	public String saveJazzSettings(Model model,
			@ModelAttribute JazzAccount acc, RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal MyUserDetails userDetail) {
		try {
			storeService.saveJazzSettings(acc, userDetail);
		}catch(Exception ex) {
		 log.error("error while saving details", ex);
		 redirectAttributes.addFlashAttribute(Constant.ERROR,"Error while saving.");
		}
		return "redirect:/settings";
	}
}
