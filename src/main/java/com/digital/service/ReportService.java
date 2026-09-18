package com.digital.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.digital.entity.*;
import com.digital.repository.*;
import com.digital.util.DateUtil;
import com.digital.util.ReportFileUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;

@Service
public class ReportService {
	@Autowired
	private DateUtil dateutil;
	private final MonthlyReportRepository monthlyRepo;
	private final QuarterlyReportRepository quarterlyRepo;
	private final MonthlyRevenueDetailsRepository monthlyRevenueRepo;
	private final QuarterlyRevenueDetailsRepository quarterlyRevenueRepo;
	private final ReportUploadHistoryRepository reportHistoryRepo;
	@Autowired
	private ReportIdService reportIdService;

	public ReportService(MonthlyReportRepository monthlyRepo, QuarterlyReportRepository quarterlyRepo,
			MonthlyRevenueDetailsRepository monthlyRevenueRepo, QuarterlyRevenueDetailsRepository quarterlyRevenueRepo,
			ReportUploadHistoryRepository reportHistoryRepo) {

		this.monthlyRepo = monthlyRepo;
		this.quarterlyRepo = quarterlyRepo;
		this.monthlyRevenueRepo = monthlyRevenueRepo;
		this.quarterlyRevenueRepo = quarterlyRevenueRepo;
		this.reportHistoryRepo = reportHistoryRepo;
	}

	// ---------------------------------------------------------
	// MONTHLY REPORT PROCESSING
	// ---------------------------------------------------------

	public String processMonthlyReport(MultipartFile file) throws Exception {
		Long reportId = reportIdService.getNextReportId();
		System.out.println("Generated report_id = " + reportId);
		List<String[]> rows = parseCsv(file);

		List<MonthlyReport> entries = new ArrayList<>();

		// Skip header (start at i = 1)
		for (int i = 1; i < rows.size(); i++) {

			String[] c = rows.get(i);

			MonthlyReport report = MonthlyReport.builder().reportId(reportId)
					.reportingMonth(dateutil.normalizeMonth(c[0])).salesMonth(c[1]).platform(c[2]).countryRegion(c[3])
					.labelName(c[4]).artistName(c[5]).releaseTitle(c[6]).trackTitle(c[7]).upc(c[8]).isrc(c[9])
					.catalogNumber(c[10]).subscriptionType(c[11]).releaseType(c[12]).salesType(c[13])
					.quantity(parseInt(c[14])).clientCurrency(c[15]).unitPrice(parseDecimal(c[16]))
					.mechanicalFee(parseDecimal(c[17])).grossRevenue(parseDecimal(c[18]))
					.clientShareRate(parseDecimal(c[19])).netRevenue(parseDecimal(c[20])).build();

			entries.add(report);
		}

		List<MonthlyReport> list = monthlyRepo.saveAll(entries);

		// Trigger revenue aggregation
		List<MonthlyRevenueDetails> monthlyDetails = generateMonthlyRevenue(entries);
		if (CollectionUtils.isNotEmpty(monthlyDetails)) {
			String message = saveUploadHistory(monthlyDetails, file, "monthly", list.get(0).getReportId());
			return message;
		}
		return null;
	}

	// ---------------------------------------------------------
	// QUARTERLY REPORT PROCESSING
	// ---------------------------------------------------------

	public String processQuarterlyReport(MultipartFile file) throws Exception {
		Long reportId = reportIdService.getNextReportId();
		System.out.println("Generated report_id = " + reportId);
		List<String[]> rows = parseCsv(file);

		List<QuarterlyReport> entries = new ArrayList<>();

		for (int i = 1; i < rows.size(); i++) {
			String[] c = rows.get(i);

			QuarterlyReport q = QuarterlyReport.builder().reportId(reportId)
					.reportingMonth(dateutil.normalizeMonth(c[0]).toString())
					.salesMonth(dateutil.normalizeMonth(c[1]).toString()).platform(c[2]).countryRegion(c[3])
					.labelName(c[4]).artistName(c[5]).releaseTitle(c[6]).trackTitle(c[7]).upc(c[8]).isrc(c[9])
					.catalogNumber(c[10]).subscriptionType(c[11]).releaseType(c[12]).salesType(c[13])
					.quantity(parseInt(c[14])).clientCurrency(c[15]).unitPrice(parseDecimal(c[16]))
					.mechanicalFee(parseDecimal(c[17])).grossRevenue(parseDecimal(c[18]))
					.clientShareRate(parseDecimal(c[19])).netRevenue(parseDecimal(c[20])).build();

			entries.add(q);
		}

		List<QuarterlyReport> savedQuarterData = quarterlyRepo.saveAll(entries);

		List<QuarterlyRevenueDetails> processRevenue = generateQuarterlyRevenue(entries);
		if (CollectionUtils.isNotEmpty(processRevenue)) {
			String message = saveUploadHistory(processRevenue, file, "quarterly",
					savedQuarterData.get(0).getReportId());
			return message;
		}
		return null;
	}

	public Page<ReportUploadHistory> searchReports(String search, String channel, PageRequest pageable) {

// No filters → return all
		if ((search == null || search.isBlank())
				&& (channel == null || channel.isBlank() || channel.equalsIgnoreCase("ALL"))) {
			return reportHistoryRepo.findAll(pageable);
		}

		if (channel != null && !channel.isBlank() && !channel.equalsIgnoreCase("ALL")) {
			if (search != null && !search.isBlank()) {
				return reportHistoryRepo.findByChannelNameContainingIgnoreCaseAndFileNameContainingIgnoreCase(channel, search,
						pageable);
			} else {
				return reportHistoryRepo.findByChannelNameContainingIgnoreCase(channel, pageable);
			}
		}

// Only search text filter
		return reportHistoryRepo.findByFileNameContainingIgnoreCase(search, pageable);
	}

	public int totalPages(String search, String channel) {
		long count;

		if ((search == null || search.isBlank())
				&& (channel == null || channel.isBlank() || channel.equalsIgnoreCase("ALL"))) {
			count = reportHistoryRepo.count();
		} else if (channel != null && !channel.isBlank() && !channel.equalsIgnoreCase("ALL")) {
			if (search != null && !search.isBlank()) {
				count = reportHistoryRepo.countByChannelNameContainingIgnoreCaseAndFileNameContainingIgnoreCase(channel, search);
			} else {
				count = reportHistoryRepo.countByChannelNameContainingIgnoreCase(channel);
			}
		} else {
			count = reportHistoryRepo.countByFileNameContainingIgnoreCase(search);
		}

		return (int) Math.ceil((double) count / 10);
	}

	public List<String> getAllChannels() {
		return reportHistoryRepo.findDistinctChannelNames().stream().sorted().collect(Collectors.toList());
	}

	public ReportUploadHistory publish(Long id) {
		ReportUploadHistory report = reportHistoryRepo.findById(id).orElseThrow();
		report.setPublished(true);
		return reportHistoryRepo.save(report);
	}

	public void deleteReport(List<Long> ids) {
		reportHistoryRepo.deleteAllById(ids);
	}

	// ---------------------------------------------------------
	// MONTHLY REVENUE AGGREGATION
	// ---------------------------------------------------------

	private List<MonthlyRevenueDetails> generateMonthlyRevenue(List<MonthlyReport> entries) {

		Map<String, List<MonthlyReport>> grouped = entries.stream()
				.collect(Collectors.groupingBy(MonthlyReport::getLabelName));

		List<MonthlyRevenueDetails> result = new ArrayList<>();

		for (String channel : grouped.keySet()) {

			List<MonthlyReport> list = grouped.get(channel);

			BigDecimal net = list.stream().map(MonthlyReport::getNetRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal inr = net.multiply(BigDecimal.valueOf(85));
			BigDecimal client = inr.multiply(BigDecimal.valueOf(0.70));
			BigDecimal company = inr.multiply(BigDecimal.valueOf(0.30));

			// Reporting Month from first row
			String month = list.get(0).getReportingMonth();
			YearMonth ym = toYearMonth(month);
			MonthlyRevenueDetails m = MonthlyRevenueDetails.builder().channelName(channel)
					.salesMonth(list.get(0).getSalesMonth()).reportingMonth(dateutil.monthName(ym)).netRevenue(net)
					.inrValue(inr).clientShare(client).companyShare(company).build();

			result.add(m);
		}

		List<MonthlyRevenueDetails> revenueDetails = monthlyRevenueRepo.saveAll(result);
		return (CollectionUtils.isNotEmpty(revenueDetails)) ? revenueDetails : null;
	}

	// ---------------------------------------------------------
	// QUARTERLY REVENUE AGGREGATION
	// ---------------------------------------------------------

	private List<QuarterlyRevenueDetails> generateQuarterlyRevenue(List<QuarterlyReport> entries) {

		Map<String, List<QuarterlyReport>> grouped = entries.stream()
				.collect(Collectors.groupingBy(QuarterlyReport::getLabelName));

		List<QuarterlyRevenueDetails> result = new ArrayList<>();

		for (String channel : grouped.keySet()) {
			List<QuarterlyReport> list = grouped.get(channel);

			BigDecimal net = list.stream().map(QuarterlyReport::getNetRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal inr = net.multiply(BigDecimal.valueOf(85));
			BigDecimal client = inr.multiply(BigDecimal.valueOf(0.70));
			BigDecimal company = inr.multiply(BigDecimal.valueOf(0.30));
			BigDecimal payable = client.setScale(0, BigDecimal.ROUND_HALF_UP);

			String month = list.get(0).getReportingMonth();
			YearMonth ym = toYearMonth(month);
			String quarter = toQuarter(ym.getMonthValue());

			QuarterlyRevenueDetails q = QuarterlyRevenueDetails.builder().channelName(channel)
					.salesMonth(list.get(0).getSalesMonth()).monthName(dateutil.monthName(ym)).quarter(quarter)
					.year(ym.getYear()).netRevenue(net).inrValue(inr).clientShare(client).companyShare(company)
					.payableAmount(payable).build();

			result.add(q);
		}

		List<QuarterlyRevenueDetails> listOfDetails = quarterlyRevenueRepo.saveAll(result);
		return (CollectionUtils.isNotEmpty(listOfDetails)) ? listOfDetails : null;
	}

	private String saveUploadHistory(List<?> revenueList, MultipartFile file, String reportType, Long reportId) {
		String fileName = file.getOriginalFilename();

		Integer year = ReportFileUtil.extractYear(fileName);
		String quarter = ReportFileUtil.extractQuarter(fileName);

		reportHistoryRepo.save(ReportUploadHistory.builder().fileName(fileName).reportType(reportType).year(year)
				.quarter(quarter).quarterlyReportId(reportId).uploadedDate(LocalDate.now()).build());
		return "Successfully processed " + revenueList.size() + " rows!";
	}

	// ---------------------------------------------------------
	// UTILITIES
	// ---------------------------------------------------------

	private List<String[]> parseCsv(MultipartFile file) throws Exception {

		CSVReader reader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream()))
				.withCSVParser(new CSVParserBuilder().withSeparator(';') // semicolon separated
						.build())
				.build();

		return reader.readAll();
	}

	private Integer parseInt(String s) {
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			return 0;
		}
	}

	private BigDecimal parseDecimal(String s) {
		try {
			return new BigDecimal(s.trim());
		} catch (Exception e) {
			return BigDecimal.ZERO;
		}
	}

	private String toQuarter(int month) {
		if (month <= 3)
			return "Q1";
		if (month <= 6)
			return "Q2";
		if (month <= 9)
			return "Q3";
		return "Q4";
	}

	private YearMonth toYearMonth(String value) {

		// Try YearMonth formats first
		DateTimeFormatter[] ymFormats = { DateTimeFormatter.ofPattern("yyyy-MM"),
				DateTimeFormatter.ofPattern("yyyy/MM") };

		for (DateTimeFormatter fmt : ymFormats) {
			try {
				return YearMonth.parse(value, fmt);
			} catch (Exception ignored) {
			}
		}

		// Try full date formats → convert to YearMonth
		DateTimeFormatter[] dateFormats = { DateTimeFormatter.ofPattern("yyyy-MM-dd"),
				DateTimeFormatter.ofPattern("yyyy/MM/dd"), DateTimeFormatter.ofPattern("dd-MM-yyyy"),
				DateTimeFormatter.ofPattern("dd/MM/yyyy") };

		for (DateTimeFormatter fmt : dateFormats) {
			try {
				LocalDate d = LocalDate.parse(value, fmt);
				return YearMonth.of(d.getYear(), d.getMonth());
			} catch (Exception ignored) {
			}
		}

		throw new IllegalArgumentException("Unsupported date format: " + value);
	}

	private String normalizeMonth(String raw) {
		raw = raw.trim();

		// Case: yyyy/MM/dd
		if (raw.contains("/") && raw.length() == 10)
			return raw.substring(0, 7).replace("/", "-"); // 2025-07

		// Case: yyyy/MM
		if (raw.contains("/") && raw.length() == 7)
			return raw.replace("/", "-");

		// Case: yyyy-MM-dd
		if (raw.contains("-") && raw.length() == 10)
			return raw.substring(0, 7); // 2025-07

		// Case: Month name (Jul-25)
		try {
			DateTimeFormatter f = DateTimeFormatter.ofPattern("MMM-yy");
			YearMonth ym = YearMonth.parse(raw, f);
			return ym.toString(); // 2025-07
		} catch (Exception ignored) {
		}

		return raw;
	}

}
