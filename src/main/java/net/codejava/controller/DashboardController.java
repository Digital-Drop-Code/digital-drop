package net.codejava.controller;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;
import net.codejava.config.MyUserDetails;
import net.codejava.entity.Order;
import net.codejava.entity.User;
import net.codejava.model.GraphData;
import net.codejava.service.OrderService;
import net.codejava.service.UserService;

@Controller
@Slf4j

public class DashboardController {

	@Autowired
	UserService userService;
	
	@Autowired
	OrderService orderService;
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/dashboard")
	public String viewHomePage(Model model,@AuthenticationPrincipal MyUserDetails userDetails) {
		model.addAttribute("section","dashboard");
		User user = userService.findByEmail(userDetails.getUsername());
		model.addAttribute("user", user);
        model.addAttribute("imageData", user.getImageData() != null ? Base64.getEncoder().encodeToString(user.getImageData()) : null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("dateCreated").ascending());
        Page<Order> orders = orderService.fetchOrders("",user, pageable);
        model.addAttribute("orders", orders );
        model.addAttribute("totalOrders", orderService.totalOrders(user) );
        model.addAttribute("trackedOrders", orderService.trackedOrders(user) );
        model.addAttribute("paidOrders", orderService.paidOrders(user) );
		return "dashboard";
	}
	
	@GetMapping("/errorView")
	public String view(){
		return "errorPage";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/analytics")
	public String viewAnalytics(Model model,@AuthenticationPrincipal MyUserDetails userDetails) {
		model.addAttribute("section","analytics");
		User user = userService.findByEmail(userDetails.getUsername());
		model.addAttribute("user", user);
		GraphData data = orderService.totalOrdersByMonth(user);
		GraphData data2 = orderService.totalOrdersByCity(user);
		GraphData data3 = orderService.amountOrdersByMonth(user);

        model.addAttribute("monthGraph", data );
        model.addAttribute("cityGraph", data2 );
        model.addAttribute("amountGraph", data3 );
        
        model.addAttribute("totalOrders", orderService.totalOrders(user) );
        model.addAttribute("trackedOrders", orderService.trackedOrders(user) );
        model.addAttribute("paidOrders", orderService.paidOrders(user) );
		return "analytics";
	}
}
