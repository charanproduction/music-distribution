package com.digital.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.digital.dto.ChannelSummary;
import com.digital.dto.MonthlySeriesPoint;
import com.digital.dto.QuarterlySeriesPoint;
import com.digital.entity.MonthlyRevenueDetails;
import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.repository.MonthlyRevenueDetailsRepository;
import com.digital.repository.QuarterlyRevenueDetailsRepository;

@Service
public class AnalyticsService {

    private final MonthlyRevenueDetailsRepository monthlyRepo;
    private final QuarterlyRevenueDetailsRepository quarterlyRepo;

    public AnalyticsService(MonthlyRevenueDetailsRepository m, QuarterlyRevenueDetailsRepository q) {
        this.monthlyRepo = m;
        this.quarterlyRepo = q;
    }

    public List<MonthlySeriesPoint> getMonthlySeries(String channel, Integer year) {
        List<MonthlyRevenueDetails> all = monthlyRepo.findAll();
        return all.stream()
            .filter(r -> channel == null || channel.isBlank() || channel.equals(r.getChannelName()))
            .filter(r -> year == null || parseYear(r.getReportingMonth()) == year)
            .collect(Collectors.groupingBy(
                MonthlyRevenueDetails::getReportingMonth,
                LinkedHashMap::new,
                Collectors.toList()
            ))
            .entrySet().stream()
            .map(e -> {
                double inr = e.getValue().stream()
                        .map(MonthlyRevenueDetails::getInrValue)
                        .mapToDouble(BigDecimal::doubleValue).sum();
                double client = e.getValue().stream()
                        .map(MonthlyRevenueDetails::getClientShare)
                        .mapToDouble(BigDecimal::doubleValue).sum();
                double company = e.getValue().stream()
                        .map(MonthlyRevenueDetails::getCompanyShare)
                        .mapToDouble(BigDecimal::doubleValue).sum();
                return new MonthlySeriesPoint(e.getKey(), inr, client, company);
            })
            .toList();
    }

    public List<QuarterlySeriesPoint> getQuarterlySeries(String channel, Integer year) {
        List<QuarterlyRevenueDetails> all = quarterlyRepo.findAll();
        return all.stream()
            .filter(r -> channel == null || channel.isBlank() || channel.equals(r.getChannelName()))
            .filter(r -> year == null || r.getYear().equals(year))
            .collect(Collectors.groupingBy(
                QuarterlyRevenueDetails::getQuarter,
                LinkedHashMap::new,
                Collectors.toList()
            ))
            .entrySet().stream()
            .map(e -> {
                double inr = e.getValue().stream()
                        .map(QuarterlyRevenueDetails::getInrValue)
                        .mapToDouble(BigDecimal::doubleValue).sum();
                return new QuarterlySeriesPoint(e.getKey(), inr);
            })
            .toList();
    }

    public List<ChannelSummary> getChannelSummary(Integer year) {
        List<MonthlyRevenueDetails> all = monthlyRepo.findAll();
        return all.stream()
            .filter(r -> year == null || parseYear(r.getReportingMonth()) == year)
            .collect(Collectors.groupingBy(
                MonthlyRevenueDetails::getChannelName,
                LinkedHashMap::new,
                Collectors.toList()
            ))
            .entrySet().stream()
            .map(e -> {
                double client = e.getValue().stream()
                        .map(MonthlyRevenueDetails::getClientShare)
                        .mapToDouble(BigDecimal::doubleValue).sum();
                double company = e.getValue().stream()
                        .map(MonthlyRevenueDetails::getCompanyShare)
                        .mapToDouble(BigDecimal::doubleValue).sum();
                return new ChannelSummary(e.getKey(), client, company);
            })
            .toList();
    }

    private int parseYear(String reportingMonth) {
        // e.g. "2024-05"
        return YearMonth.parse(reportingMonth).getYear();
    }
}
