package net.codejava.controller;

import java.util.Base64;

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
import net.codejava.entity.User;
import net.codejava.model.UserModel;
import net.codejava.service.UserService;
import net.codejava.utility.Constant;

@Controller
@Slf4j
public class UserController {
	
	@Autowired
	UserService userService;

	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/userAccount")
	public String viewUserPage(Model model,@AuthenticationPrincipal MyUserDetails userDetails) {
		model.addAttribute("section","user");
		User user = userService.findByEmail(userDetails.getUsername());
		model.addAttribute("user", user);
        model.addAttribute("imageData", user.getImageData() != null  ? Base64.getEncoder().encodeToString(user.getImageData()) : null);
		return "user";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@PostMapping("/saveUser")
	public String saveUser(Model model, RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal MyUserDetails userDetails,
			@ModelAttribute("user") UserModel userModel) {
		try {
			userService.updateUser(userDetails.getUser(), userModel);
			redirectAttributes.addFlashAttribute(Constant.SUCCESS,"User Updated Successfully.");

		}catch(Exception ex) {
		 log.error("error while saving user details", ex);
		 redirectAttributes.addFlashAttribute(Constant.ERROR,"Error while saving user.");
		}
		return "redirect:/userAccount";
	}
}
