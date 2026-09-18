package com.digital.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.digital.dto.ChannelSummary;
import com.digital.dto.QuarterDetailDto;
import com.digital.dto.YearlySummary;
import com.digital.entity.MonthlyRevenueDetails;
import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.repository.MonthlyRevenueDetailsRepository;
import com.digital.repository.PaymentDetailsRepository;
import com.digital.repository.QuarterlyRevenueDetailsRepository;
import com.digital.repository.QuarterlyReportRepository;

@Service
public class DashboardChartsService {

    private final MonthlyRevenueDetailsRepository monthlyRepo;
    private final QuarterlyRevenueDetailsRepository quarterlyRepo;
    private final PaymentDetailsRepository paymentRepo;
    private final QuarterlyReportRepository quarterlyReport;

    public DashboardChartsService(MonthlyRevenueDetailsRepository monthlyRepo,
                                  QuarterlyRevenueDetailsRepository quarterlyRepo, PaymentDetailsRepository paymentRepo, QuarterlyReportRepository quarterlyReport) {
        this.monthlyRepo = monthlyRepo;
        this.quarterlyRepo = quarterlyRepo;
		this.paymentRepo = paymentRepo;
		this.quarterlyReport = quarterlyReport;
    }
    /** Dropdown Filters */
    public void populateAnalyticsFilters(Model model) {
        model.addAttribute("channels", quarterlyRepo.fetchAllChannels());
        model.addAttribute("years", quarterlyRepo.fetchAllYears());
    }
    /** ADMIN DASHBOARD CHARTS */
    public void populateChartModel(Model model) {

        // ---- MONTHLY REVENUE ----
        List<MonthlyRevenueDetails> monthly = monthlyRepo.findAll();

        List<String> monthlyLabels = monthly.stream()
                .map(MonthlyRevenueDetails::getSalesMonth)     // yyyy-MM
                .sorted()
                .toList();

        List<Double> monthlyInrValues = monthly.stream()
                .sorted(Comparator.comparing(MonthlyRevenueDetails::getSalesMonth))
                .map(m -> m.getInrValue().doubleValue())
                .toList();

        // ---- QUARTERLY REVENUE ----
        List<Object[]> raw = quarterlyRepo.fetchQuarterlyChartData();

        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for (Object[] row : raw) {
            Integer year = (Integer) row[0];
            String quarter = row[1].toString();
            Double total = ((Number) row[2]).doubleValue();

            labels.add(year + "-" + quarter);   // Example → "2023-Q1"
            values.add(total);
        }

        // ADD TO MODEL FOR THYMELEAF
        model.addAttribute("monthlyLabels", monthlyLabels);
        model.addAttribute("monthlyInrValues", monthlyInrValues);

        model.addAttribute("quarterlyLabels", labels);
        model.addAttribute("quarterlyInrValues", values);
    }

    /** ANALYTICS FILTER DROP-DOWNS */
    public void populateFilterData(Model model) {

        // Unique channels
        List<String> channels = monthlyRepo.findAll().stream()
                .map(MonthlyRevenueDetails::getChannelName)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        // Unique years
        List<Integer> years = monthlyRepo.findAll().stream()
                .map(m -> Integer.valueOf(m.getSalesMonth().substring(0, 4)))
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("channels", channels);
        model.addAttribute("years", years);
    }

    /* -------------- ANALYTICS CHARTS + QUARTER TABLE -------------- */

    public void populateAnalyticsCharts(String channel, Integer year, Model model) {

        // --- BASE SETS ---
        List<MonthlyRevenueDetails> monthly = monthlyRepo.findAll();
        List<QuarterlyRevenueDetails> quarterly = quarterlyRepo.findAll();

        if (channel != null && !channel.isBlank()) {
            monthly = monthly.stream()
                    .filter(m -> channel.equalsIgnoreCase(m.getChannelName()))
                    .toList();
            quarterly = quarterly.stream()
                    .filter(q -> channel.equalsIgnoreCase(q.getChannelName()))
                    .toList();
        }

        if (year != null) {
            final int y = year;
            // assume salesMonth/reportingMonth like "2024-03" or "2024/03/01"
            monthly = monthly.stream()
                    .filter(m -> m.getSalesMonth() != null && m.getSalesMonth().startsWith(String.valueOf(y)))
                    .toList();
            quarterly = quarterly.stream()
                    .filter(q -> Objects.equals(q.getYear(), y))
                    .toList();
        }

        /* -------- Monthly client share (by reportingMonth) -------- */
        Map<String, BigDecimal> monthlyMap = monthly.stream()
                .collect(Collectors.groupingBy(
                        MonthlyRevenueDetails::getReportingMonth,
                        LinkedHashMap::new,
                        Collectors.mapping(MonthlyRevenueDetails::getClientShare,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        model.addAttribute("analyticsMonthlyLabels", new ArrayList<>(monthlyMap.keySet()));
        model.addAttribute("analyticsMonthlyValues",
                monthlyMap.values().stream().map(BigDecimal::doubleValue).toList());

        /* -------- Quarterly client share (by year + quarter) ------ */
        Map<String, BigDecimal> quarterMap = quarterly.stream()
                .collect(Collectors.groupingBy(
                        q -> q.getYear() + "-Q" + q.getQuarter(),   // e.g. "2024-Q1"
                        LinkedHashMap::new,
                        Collectors.mapping(QuarterlyRevenueDetails::getClientShare,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        model.addAttribute("analyticsQuarterlyLabels", new ArrayList<>(quarterMap.keySet()));
        model.addAttribute("analyticsQuarterlyValues",
                quarterMap.values().stream().map(BigDecimal::doubleValue).toList());

        /* -------- Yearly client share (sum of quarters) ----------- */
        Map<Integer, BigDecimal> yearlyMap = quarterly.stream()
                .collect(Collectors.groupingBy(
                        QuarterlyRevenueDetails::getYear,
                        TreeMap::new,
                        Collectors.mapping(QuarterlyRevenueDetails::getClientShare,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        model.addAttribute("analyticsYearlyLabels", new ArrayList<>(yearlyMap.keySet()));
        model.addAttribute("analyticsYearlyValues",
                yearlyMap.values().stream().map(BigDecimal::doubleValue).toList());

        /* -------- Top 5 channels (client share, all data) --------- */
        Map<String, BigDecimal> channelMap = quarterly.stream()
                .collect(Collectors.groupingBy(
                        QuarterlyRevenueDetails::getChannelName,
                        Collectors.mapping(QuarterlyRevenueDetails::getClientShare,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        List<Map.Entry<String, BigDecimal>> sortedChannels = channelMap.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .toList();

        model.addAttribute("topChannelLabels",
                sortedChannels.stream().map(Map.Entry::getKey).toList());
        model.addAttribute("topChannelValues",
                sortedChannels.stream().map(e -> e.getValue().doubleValue()).toList());

        /* -------- Quarter detail rows for the table ---------------- */
        // these are the raw rows we’ll show in the "Quarter Details" table
        model.addAttribute("analyticsQuarterDetails", quarterly);
    }
    
 // ------------- DRILLDOWN: QUARTER DETAILS -------------

    public List<QuarterDetailDto> getQuarterDetails(String channel, Integer year, String quarter) {
        if (channel != null && channel.isBlank()) {
            channel = null;
        }
        List<QuarterlyRevenueDetails> list = quarterlyRepo.findQuarterDetails(channel, year, quarter);
        List<QuarterDetailDto> dtos = new ArrayList<>();
        for (QuarterlyRevenueDetails q : list) {
            dtos.add(QuarterDetailDto.builder()
                    .monthName(q.getMonthName())
                    .channelName(q.getChannelName())
                    .netRevenue(q.getNetRevenue())
                    .inrValue(q.getInrValue())
                    .clientShare(q.getClientShare())
                    .companyShare(q.getCompanyShare())
                    .payableAmount(q.getPayableAmount())
                    .build());
        }
        return dtos;
    }
    
    public BigDecimal getTotalRevenue() {
        return quarterlyRepo.findTotalRevenue();
    }

    public BigDecimal getTotalDistributed() {
        return paymentRepo.findTotalPaidAmount();
    }

    public BigDecimal getProfit() {
        return getTotalRevenue().subtract(getTotalDistributed());
    }
    
    public long getTotalUniqueSongs() {
        return quarterlyReport.countUniqueSongs();
    }

}
