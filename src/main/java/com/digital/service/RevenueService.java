package com.digital.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.digital.dto.PaymentDetailsDTO;
import com.digital.dto.RevenueRowDto;
import com.digital.entity.PaymentDetails;
import com.digital.entity.PaymentStatus;
import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.repository.PaymentDetailsRepository;
import com.digital.repository.QuarterlyRevenueDetailsRepository;

@Service
public class RevenueService {

	private final QuarterlyRevenueDetailsRepository quarterlyRepo;
	private final PaymentDetailsRepository paymentRepo;

	public RevenueService(QuarterlyRevenueDetailsRepository quarterlyRepo, PaymentDetailsRepository paymentRepo) {
		this.quarterlyRepo = quarterlyRepo;
		this.paymentRepo = paymentRepo;
	}

	// ---------- TOP SUMMARY (all-time) ----------

	public Map<String, BigDecimal> allTimeSummary() {
		List<QuarterlyRevenueDetails> all = quarterlyRepo.findAll();

		BigDecimal total = all.stream().map(QuarterlyRevenueDetails::getInrValue).filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal clientShare = total.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal companyShare = total.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);

		Map<String, BigDecimal> map = new HashMap<>();
		map.put("total", total);
		map.put("clientShare", clientShare);
		map.put("companyShare", companyShare);
		return map;
	}

	// ---------- FILTER OPTIONS ----------

	public List<String> allChannels() {
		return quarterlyRepo.findAll().stream().map(QuarterlyRevenueDetails::getChannelName).filter(Objects::nonNull)
				.distinct().sorted().toList();
	}

	public List<Integer> allYears() {
		return quarterlyRepo.findAll().stream().map(QuarterlyRevenueDetails::getYear).filter(Objects::nonNull)
				.distinct().sorted().toList();
	}

	public List<String> allMonths() {
		return quarterlyRepo.findAll().stream().map(QuarterlyRevenueDetails::getMonthName).filter(Objects::nonNull)
				.distinct().sorted().toList();
	}

	public List<String> allQuarters() {
		return List.of("Q1", "Q2", "Q3", "Q4");
	}

	// ---------- MAIN SEARCH ----------

	public List<RevenueRowDto> search(String channel, Integer year, String quarter, String month) {

		String ch = normalizeFilter(channel);
		String q = normalizeFilter(quarter);
		String m = normalizeFilter(month);

		List<QuarterlyRevenueDetails> base = quarterlyRepo.findAll();

		return base.stream()
				.filter(r -> ch == null || r.getChannelName() != null && r.getChannelName().equalsIgnoreCase(ch))
				.filter(r -> year == null || Objects.equals(r.getYear(), year))
				.filter(r -> q == null || q.equalsIgnoreCase(r.getQuarter()))
				.filter(r -> m == null || m.equalsIgnoreCase(r.getMonthName())).map(this::toRow)
				.collect(Collectors.toList());
	}

	private String normalizeFilter(String v) {
		if (!StringUtils.hasText(v))
			return null;
		if ("ALL".equalsIgnoreCase(v))
			return null;
		return v.trim();
	}

	private RevenueRowDto toRow(QuarterlyRevenueDetails r) {
		// find payment (by channel + year + quarter)
		Optional<PaymentDetails> opt = Optional.empty();
		if (r.getChannelName() != null && r.getYear() != null && r.getQuarter() != null) {
			opt = paymentRepo.findByChannelNameIgnoreCaseAndYearAndQuarter(r.getChannelName(), r.getYear(),
					r.getQuarter());
		}

		RevenueRowDto.RevenueRowDtoBuilder b = RevenueRowDto.builder().channelName(r.getChannelName()).year(r.getYear())
				.quarter(r.getQuarter()).salesMonth(r.getMonthName()).netRevenue(nvl(r.getNetRevenue()))
				.inrValue(nvl(r.getInrValue())).clientShare(nvl(r.getClientShare()))
				.companyShare(nvl(r.getCompanyShare())).payableAmount(nvl(r.getPayableAmount()));

		opt.ifPresent(p -> {
			b.paid(true).paidAmount(nvl(p.getAmount())).paymentMethod(p.getPaymentMethod())
					.paymentReference(p.getPaymentReference()).paymentDate(p.getPaymentDate())
					.paymentStatus(p.getStatus());
		});

		return b.build();
	}

	private BigDecimal nvl(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v;
	}

	// ---------- SAVE PAYMENT ----------

	public void saveOrUpdatePayment(Long id, String channel, Integer year, String quarter, BigDecimal amount,
			String method, String reference, LocalDate date, String status) {

		PaymentDetails payment = (id != null) ? paymentRepo.findById(id).orElse(new PaymentDetails())
				: new PaymentDetails();

		payment.setChannelName(channel);
		payment.setYear(year);
		payment.setQuarter(quarter);
		payment.setAmount(amount);
		payment.setPaymentMethod(method);
		payment.setPaymentReference(reference);
		payment.setPaymentDate(date);
		payment.setStatus(PaymentStatus.valueOf(status));

		paymentRepo.save(payment);
	}

	public Page<QuarterlyRevenueDetails> getLatestRevenuePage(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
		return quarterlyRepo.findAll(pageable);
	}

	public Page<QuarterlyRevenueDetails> searchRevenue(String channel, Integer year, String quarter, String month,
			int page) {

		Pageable pageable = PageRequest.of(page, 10);

		return quarterlyRepo.findRevenue(channel, year, quarter, month, pageable);
	}

	public List<RevenueRowDto> searchChannelNameYearQuarter(Page<QuarterlyRevenueDetails> revenuePage) {

		return revenuePage.getContent().stream().map(r -> {
			// base data from quarterly_revenue_details
			RevenueRowDto dto = new RevenueRowDto(r);

			// look for payment record (channel + year + quarter)
			paymentRepo.findByChannelNameAndYearAndQuarter(r.getChannelName(), r.getYear(), r.getQuarter())
					.ifPresent(dto::applyPayment); // fills paid + details

			// if not present, dto.paid remains false => "Make Payment"
			return dto;
		}).toList();
	}

	public PaymentDetailsDTO getPaymentDetails(Long id) {
		PaymentDetails p = paymentRepo.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
		return new PaymentDetailsDTO(p);
	}

	public void deletePayment(Long id) {
		paymentRepo.deleteById(id);
	}
	
	public List<QuarterlyRevenueDetails> searchUserRevenue(String channels, Integer year, Integer quarter) {

	    List<QuarterlyRevenueDetails> revenueList;

	    if (year == null && quarter == null) {
	        revenueList = quarterlyRepo.findByChannelNameOrderByYearDesc(channels);
	    } else if (year != null && quarter == null) {
	        revenueList = quarterlyRepo.findByChannelNameAndYearOrderByYearDesc(channels, year);
	    } else {
	        revenueList = quarterlyRepo.findByChannelNameAndYearAndQuarterOrderByYearDesc(
	                channels, year, "Q" + quarter);
	    }

	    // ⬇️ Fetch payments for all these revenues
	    List<PaymentDetails> payments = paymentRepo.findAllByChannelName(channels);

	    // Convert to map for fast lookup
	    Map<String, PaymentDetails> paymentMap = payments.stream()
	            .collect(Collectors.toMap(
	                    p -> p.getChannelName()+"-"+p.getYear()+"-"+p.getQuarter(),
	                    p -> p,
	                    (existing, duplicate) -> existing
	            ));

	    // Merge payment flags into each revenue row
	    revenueList.forEach(r -> {
	        String key = r.getChannelName()+"-"+r.getYear()+"-"+r.getQuarter();
	        PaymentDetails pd = paymentMap.getOrDefault(key, null);

	        if (pd != null) {
	            r.setPaid(true);
	            r.setPaymentDetails(pd);
	        } else {
	            r.setPaid(false);
	            r.setPaymentDetails(null);
	        }
	    });

	    return revenueList;
	}

}
