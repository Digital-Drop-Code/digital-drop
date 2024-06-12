package net.codejava.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;
import net.codejava.config.MyUserDetails;
import net.codejava.entity.User;
import net.codejava.model.UserModel;
import net.codejava.service.OtpCodeService;
import net.codejava.service.UserService;
import net.codejava.utility.Constant;
import net.codejava.utility.CustomException;

@Controller
@Slf4j
public class RegisterController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	OtpCodeService otpCodeService;
	
	@RequestMapping("/")
	public String viewHomePage(Model model) {
		 return "redirect:/dashboard";
	}

	@GetMapping("/login")
	public String viewLogiPage(Model model,  @AuthenticationPrincipal MyUserDetails userDetail) {
   	   if(userDetail == null)
   		   return "login";
   	   else
   		   return "redirect:/dashboard";
	}
	
	@PostMapping("/user/register")
	public String registerUser(Model model,@Validated @ModelAttribute("user") UserModel userModel) throws Exception {
		String errorMsg = "";
		if(userService.findByEmail(userModel.getEmail()) != null) {
			errorMsg = "User already exists.";
			model.addAttribute(Constant.ERROR, errorMsg);
			return "login";
		}
		User user = userService.createUser(userModel);
		model.addAttribute("user", user);
		return "emailVerification";
	}
	
	@ResponseBody
	@PostMapping("/user/exists")
	public Boolean userExists(@RequestParam("email") String email) {
		if(userService.findByEmail(email) != null) {
			return true;
		}
		return false;
	}
	@ResponseBody
	@PostMapping("/user/activeExists")
	public Boolean useractiveExists(@RequestParam("email") String email) {
		if(userService.activeExists(email) != null) {
			return true;
		}
		return false;
	}
	
	@RequestMapping("/user/resendCode")
	public String resendOtp(Model model, @ModelAttribute("userEmail") String userEmail) throws Exception {
		User user = userService.findByEmail(userEmail);
		if(user == null) {
			throw new CustomException("User doesn't exists", HttpStatus.BAD_REQUEST);
		}else {
			otpCodeService.generateAndEmailOtp(user);
		}
		model.addAttribute("user", user);
		return "emailVerification";
	}
	
	@RequestMapping("/user/verifyCode")
	public String verifyCode(Model model, @ModelAttribute("userEmail") String userEmail,
			 @ModelAttribute("otp") String otp, RedirectAttributes redirectAttributes) throws Exception {
		try {
			User user = userService.findByEmail(userEmail);
			model.addAttribute("user", user);
			otpCodeService.verifyOtp(user, otp);
		}catch(CustomException ex) {
			model.addAttribute(Constant.ERROR,"Invalid OTP!");
			return "emailVerification";
		}catch(Exception ex) {
			throw ex;
		}
		 redirectAttributes.addFlashAttribute(Constant.SUCCESS,"User Registered Successfully.");
		 return "redirect:/login";

	}
}
