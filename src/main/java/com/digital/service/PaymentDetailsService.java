package com.digital.service;

import java.util.List;
import java.util.Optional;

import com.digital.entity.PaymentDetails;
import com.digital.repository.PaymentDetailsRepository;

public class PaymentDetailsService {
	private final PaymentDetailsRepository paymentRepo;
	
	
	public PaymentDetailsService(PaymentDetailsRepository paymentRepo) {
		super();
		this.paymentRepo = paymentRepo;
	}


	public Optional<PaymentDetails> findPaymentStatus(String channel, Integer year, String quarter) {
	    return paymentRepo.findByChannelNameAndYearAndQuarter(channel, year, quarter);
	}
	
	
}
