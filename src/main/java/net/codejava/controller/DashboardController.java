package net.codejava.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        model.addAttribute("totalOrders", orderService.totalOrders(user, null, null) );
        model.addAttribute("trackedOrders", orderService.trackedOrders(user, null, null) );
        model.addAttribute("paidOrders", orderService.paidOrders(user, null, null) );
		return "dashboard";
	}
	
	@GetMapping("/errorView")
	public String view(){
		return "errorPage";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@RequestMapping("/analytics")
	public String viewAnalytics(Model model,@AuthenticationPrincipal MyUserDetails userDetails,
			@RequestParam(name = "fromDate", required = false)    @DateTimeFormat(pattern = "yyyy-MM-dd")
			 Date fromDate,
			@RequestParam(name = "toDate", required = false)    @DateTimeFormat(pattern = "yyyy-MM-dd")
			 Date toDate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


		 model.addAttribute("fromDate", fromDate );
		 model.addAttribute("toDate",toDate );
		 
		if(toDate == null && fromDate != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.YEAR, 18);
			toDate= c.getTime();
			String dateString = sdf.format(toDate);
			toDate = sdf.parse(dateString);
		}
		
		if(fromDate == null && toDate != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.YEAR, -18);
			fromDate = c.getTime();
			String dateString = sdf.format(toDate);
			fromDate = sdf.parse(dateString);
		}
		model.addAttribute("section","analytics");
		User user = userService.findByEmail(userDetails.getUsername());
		model.addAttribute("user", user);
		GraphData data = orderService.totalOrdersByMonth(user, fromDate ,toDate );
		GraphData data2 = orderService.totalOrdersByCity(user, fromDate ,toDate );
		GraphData data3 = orderService.amountOrdersByMonth(user, fromDate ,toDate );

		 
        model.addAttribute("monthGraph", data );
        model.addAttribute("cityGraph", data2 );
        model.addAttribute("amountGraph", data3 );
        
        model.addAttribute("totalOrders", orderService.totalOrders(user, fromDate ,toDate ) );
        model.addAttribute("trackedOrders", orderService.trackedOrders(user, fromDate ,toDate ) );
        model.addAttribute("paidOrders", orderService.paidOrders(user, fromDate ,toDate ) );
		return "analytics";
	}
}
