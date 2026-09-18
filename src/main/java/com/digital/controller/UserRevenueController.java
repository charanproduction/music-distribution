package com.digital.controller;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.digital.entity.PaymentDetails;
import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.repository.PaymentDetailsRepository;
import com.digital.repository.QuarterlyRevenueDetailsRepository;
import com.digital.repository.UserRepository;
import com.digital.service.QuarterlyRevenueService;
@Controller
public class UserRevenueController {
	private final PaymentDetailsRepository paymentRepo;
	private final UserRepository userRepo;
	private final QuarterlyRevenueService revenueService;
	
	
	public UserRevenueController(PaymentDetailsRepository paymentRepo, UserRepository userRepo, QuarterlyRevenueService revenueService) {
		super();
		this.paymentRepo = paymentRepo;
		this.userRepo = userRepo;
		this.revenueService = revenueService;
	}

	@GetMapping("/user/revenue/payment")
	public String paymentDetails(@RequestParam String channel,
	                             @RequestParam Integer year,
	                             @RequestParam String quarter,
	                             Model model) {

	    PaymentDetails details = paymentRepo
	            .findByChannelNameAndYearAndQuarter(channel, year, quarter)
	            .orElse(null);

	    model.addAttribute("details", details);
	    return "user/fragments/payment-details :: details";
	}
	
	@GetMapping("/user/revenue")
	public String viewRevenue(
	        @RequestParam(required = false) Integer year,
	        @RequestParam(required = false) String quarter,
	        Principal principal,
	        Model model) {
		String channel = userRepo.findByUsername(principal.getName())
	            .get().getChannelName();
	   

	    List<QuarterlyRevenueDetails> revenue = revenueService.searchUserRevenue(channel, year, quarter);
	    List<Integer> years = revenue.stream()
	            .map(QuarterlyRevenueDetails::getYear)
	            .filter(Objects::nonNull)
	            .distinct()
	            .sorted(Comparator.reverseOrder())
	            .toList();

	    // Unique Quarter values (Q1, Q2, Q3, Q4)
	    List<String> quarters = revenue.stream()
	            .map(QuarterlyRevenueDetails::getQuarter)
	            .filter(q -> q != null && !q.isBlank())
	            .distinct()
	            .sorted()
	            .toList();
	    //Optional<PaymentDetails> paymentDetails = paymentRepo.findByChannelNameIgnoreCaseAndYearAndQuarter(channel, year, quarter);
	   // model.addAttribute("paymentDetails", paymentDetails);
	    model.addAttribute("revenueList", revenue);
	    model.addAttribute("year", years);
	    model.addAttribute("quarter", quarters);

	    return "user/revenue";
	}

}
