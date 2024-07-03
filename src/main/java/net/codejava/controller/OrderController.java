package net.codejava.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;
import net.codejava.config.MyUserDetails;
import net.codejava.entity.Order;
import net.codejava.entity.OrderKey;
import net.codejava.entity.StoreInfo;
import net.codejava.entity.User;
import net.codejava.service.OrderService;
import net.codejava.service.StoreService;
import net.codejava.service.UserService;
import net.codejava.utility.Constant;
import net.codejava.utility.CustomException;

@Controller
@Slf4j
public class OrderController {

	@Autowired
	OrderService orderService;

	@Autowired
	StoreService storeService;
	
	@Autowired
	UserService userService;
	
	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    dateFormat.setLenient(false);
	    webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/order/todayOrder")
	public String todayOrder(RedirectAttributes redirectAttributes,Model model,  @AuthenticationPrincipal MyUserDetails userDetail) {
		try {
			StoreInfo store = storeService.getStoreByUser(userDetail.getUser());
	   	   orderService.fetchTodayOrder(store,userDetail.getUser());
		}catch(Exception ex) {
			log.error("error in saving order", ex);
			redirectAttributes.addFlashAttribute(Constant.ERROR,"Error while fetching orders");
		}
   	   return "redirect:/orders";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/orders")
    public String listProducts(Model model, @RequestParam(defaultValue = "") String keyword,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "dateCreated") String sortBy,
                               @RequestParam(name="sortDir", defaultValue="asc") String sortDir
                               , @AuthenticationPrincipal MyUserDetails userDetail) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() :
            Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderService.fetchOrders(keyword,userDetail.getUser(), pageable);
        
        model.addAttribute("orders", orders );
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalItems", orders.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("keyword", keyword);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "order";
    }
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/newOrder")
	public String showNewOrder(@AuthenticationPrincipal MyUserDetails userDetail,Model model, @RequestParam(name="orderNo", required = false) String orderNo){
		Order order = orderService.findFirstByOrderNo(orderNo,userDetail.getUser());
		if(order == null) {
			model.addAttribute("isEdit", false);
		}else {
			model.addAttribute("isEdit", true);
		}
		model.addAttribute("order", orderService.findFirstByOrderNo(orderNo,userDetail.getUser()));
		
		return "newOrder";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@PostMapping("/saveOrders")
	public String saveOrders(Model model,@ModelAttribute Order order, RedirectAttributes redirectAttributes,
			@RequestParam("isEdit") Boolean isEdit, @AuthenticationPrincipal MyUserDetails userDetail){
		try {
			Order oldOrder = orderService.findFirstByOrderNo(order.getId().getOrderNo(), userDetail.getUser());

			if(!isEdit && oldOrder != null) {
				redirectAttributes.addFlashAttribute(Constant.ERROR,"Order already exists.");
				return "redirect:/newOrder";
			}
			if(oldOrder == null) {
				User user = userService.findByEmail(userDetail.getUsername());
				StoreInfo store = storeService.getStoreByUser(userDetail.getUser());

				oldOrder = new Order();
				oldOrder.setId(new OrderKey());
				oldOrder.getId().setStore(store);
				oldOrder.setUser(user);
				oldOrder.getId().setOrderNo(order.getId().getOrderNo());
			}
			BeanUtils.copyProperties(order, oldOrder, "id","user");
			orderService.save(oldOrder);
			redirectAttributes.addFlashAttribute(Constant.SUCCESS,"Order Successfully Created/Updated.");

		}catch(Exception ex) {
			log.error("error in saving order", ex);
			redirectAttributes.addFlashAttribute(Constant.ERROR,"Error while saving order");
		}
		return "redirect:/newOrder?orderNo="+order.getId().getOrderNo();
	}
	
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/order/trackingNo")
	public String trackingNo(RedirectAttributes redirectAttributes,@RequestParam("orderNo") String orderNo,Model model,  @AuthenticationPrincipal MyUserDetails userDetail) {
		try {
			StoreInfo store = storeService.getStoreByUser(userDetail.getUser());
	   	   orderService.generateTrackingNo(orderNo, store, userDetail.getUser());
		}catch(CustomException ex) {
			log.error("error in saving order", ex);
			redirectAttributes.addFlashAttribute(Constant.ERROR,ex.getMessage());

		}catch(Exception ex) {
			log.error("error in saving order", ex);
			redirectAttributes.addFlashAttribute(Constant.ERROR,"Error while generating tracking no.");
		}
   	   return "redirect:/orders";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/order/shippingLabel")
	public String shippingLabel(RedirectAttributes redirectAttributes,@RequestParam("orderNo") String orderNo,Model model,  @AuthenticationPrincipal MyUserDetails userDetail) {
		try {
			Order order = orderService.findFirstByOrderNo(orderNo,userDetail.getUser());
			model.addAttribute("order", order);
			model.addAttribute("user", userDetail.getUser());
		}catch(CustomException ex) {
			log.error("error in saving order", ex);
			redirectAttributes.addFlashAttribute(Constant.ERROR,ex.getMessage());

		}catch(Exception ex) {
			log.error("error in saving order", ex);
			redirectAttributes.addFlashAttribute(Constant.ERROR,"Error while generating tracking no.");
		}
   	   return "shippingLabel";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/order/showReport")
	public String showReport(RedirectAttributes redirectAttributes,Model model,  @AuthenticationPrincipal MyUserDetails userDetail) {
		try {
			 model.addAttribute("fromDate", null );
			 model.addAttribute("toDate",null );
		}catch(CustomException ex) {
			log.error("error in saving order", ex);
			redirectAttributes.addFlashAttribute(Constant.ERROR,ex.getMessage());

		}catch(Exception ex) {
			log.error("error in saving order", ex);
			redirectAttributes.addFlashAttribute(Constant.ERROR,"Error while showing reports.");
		}
   	   return "reports";
	}
	
	@PreAuthorize("hasAuthority('USER')")
	@RequestMapping("/updateReport")
	public String updateReport(Model model,@AuthenticationPrincipal MyUserDetails userDetails,
			@RequestParam(name = "fromDate", required = false)    @DateTimeFormat(pattern = "yyyy-MM-dd")
			 Date fromDate,
			@RequestParam(name = "toDate", required = false)    @DateTimeFormat(pattern = "yyyy-MM-dd")
			 Date toDate) throws ParseException {

		 model.addAttribute("fromDate", fromDate );
		 model.addAttribute("toDate",toDate );
    	   return "reports";
	}
	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/order/generateReport")
	public String generateReport(
			@RequestParam(name = "name", required = false)   String name,
			@RequestParam(name = "fromDate", required = false, defaultValue ="") String fromDateS,
			@RequestParam(name = "toDate", required = false, defaultValue ="")String toDateS,
			HttpServletResponse response,RedirectAttributes redirectAttributes,Model model,  @AuthenticationPrincipal MyUserDetails userDetail) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
                    Locale.ENGLISH);

			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

			
			Date toDate = null;
			Date fromDate = null;
			if(toDateS != null && !StringUtils.isEmpty(toDateS) && !toDateS.equals("null")) {
				toDate = sdf.parse(toDateS);
			}
			
			if(fromDateS!= null && !StringUtils.isEmpty(fromDateS) && !fromDateS.equals("null")) {
				fromDate = sdf.parse(fromDateS);
			}
			

			model.addAttribute("fromDate", fromDate );
			model.addAttribute("toDate",toDate );
			
			if(toDate == null && fromDate != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.YEAR, 18);
				toDate= c.getTime();
				String dateString = sdf1.format(toDate);
				toDate = sdf1.parse(dateString);
			}
			
			if(fromDate == null && toDate != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.YEAR, -18);
				fromDate = c.getTime();
				String dateString = sdf1.format(toDate);
				fromDate = sdf1.parse(dateString);
			}
			
			orderService.generateReport(userDetail.getUser(), response,name, fromDate, toDate);
		}catch(CustomException ex) {
			log.error("error in saving order", ex);
			redirectAttributes.addFlashAttribute(Constant.ERROR,ex.getMessage());

		}catch(Exception ex) {
			log.error("error in saving order", ex);
			redirectAttributes.addFlashAttribute(Constant.ERROR,"Error while showing reports.");
		}
   	   return "reports";
	}
}
