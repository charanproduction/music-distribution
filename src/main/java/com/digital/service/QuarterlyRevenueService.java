package com.digital.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.repository.QuarterlyRevenueDetailsRepository;

@Service
public class QuarterlyRevenueService {

    private final QuarterlyRevenueDetailsRepository repo;

    public QuarterlyRevenueService(QuarterlyRevenueDetailsRepository repo) {
        this.repo = repo;
    }
    public record QuarterRevenuePoint(
            Integer year,
            String quarter,           // "Q1", "Q2"...
            BigDecimal clientShare
    ) {
        public String label() {
            return year + " " + quarter;
        }
    }
    /**
     * Total revenue for a channel → CLIENT SHARE (70%)
     */
    public BigDecimal totalClientShareForChannel(String channel) {
        if (channel == null || channel.isBlank()) return BigDecimal.ZERO;
        return repo.totalClientShareForChannel(channel);
    }

    /**
     * Last 5 quarters for dashboard chart/cards
     */
    public List<QuarterRevenuePoint> last5QuartersForChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return List.of();
        }

        List<QuarterlyRevenueDetails> list =
        		repo.findByChannelNameIgnoreCase(channel);

        if (list.isEmpty()) {
            return List.of();
        }

        // group by (year, quarter)
        Map<String, BigDecimal> grouped = list.stream()
                .filter(r -> r.getYear() != null && r.getQuarter() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getYear() + "-" + r.getQuarter(), // key "2025-Q3"
                        Collectors.mapping(
                                QuarterlyRevenueDetails::getClientShare,
                                Collectors.reducing(BigDecimal.ZERO, (a, b) -> a.add(b == null ? BigDecimal.ZERO : b))
                        )
                ));

        // Convert to list of QuarterRevenuePoint
        List<QuarterRevenuePoint> points = grouped.entrySet().stream()
                .map(e -> {
                    String key = e.getKey(); // "2025-Q3"
                    String[] parts = key.split("-");
                    Integer year = Integer.valueOf(parts[0]);
                    String quarter = parts[1];
                    return new QuarterRevenuePoint(year, quarter, e.getValue());
                })
                // sort chronologically (year then quarter number)
                .sorted(Comparator.comparing(QuarterRevenuePoint::year)
                        .thenComparing(p -> quarterNumber(p.quarter())))
                .toList();

        // pick last 5
        int size = points.size();
        if (size <= 5) {
            return points;
        }
        return points.subList(size - 5, size);
    }

    /**
     * Distinct Years for Dropdown
     */
    public List<Integer> allYears() {
        return repo.uniqueYears();
    }

    /**
     * Distinct Quarters for Dropdown
     */
    public List<String> allQuarters() {
        return repo.uniqueQuarters();
    }
    
    private int quarterNumber(String q) {
        if (q == null) return 0;
        return switch (q.trim().toUpperCase()) {
            case "Q1" -> 1;
            case "Q2" -> 2;
            case "Q3" -> 3;
            case "Q4" -> 4;
            default -> 0;
        };
    }
    
    public List<QuarterlyRevenueDetails> searchUserRevenue(
	        String channels, Integer year, String quarter) {

	    if (year == null && quarter == null) {
	        return repo.findByChannelNameOrderByYearDesc(channels);
	    }
	    if (year != null && quarter == null) {
	        return repo.findByChannelNameAndYearOrderByYearDesc(channels, year);
	    }
	    return repo.findByChannelNameAndYearAndQuarterOrderByYearDesc(
	            channels, year, quarter
	    );
	}
}
