package com.digital.service;

import com.digital.repository.QuarterlyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAiAnalyticsService {   

    private final QuarterlyReportRepository repo;
    private final OpenAiExecutiveSummaryService openAiService;

    public Map<String, Object> dashboardStats(String year, String label) {
        Map<String, Object> stats = new LinkedHashMap<>();
        BigDecimal totalNetRevenue = repo.totalNetRevenue(year, label);
        stats.put("uniqueSongs", repo.countUniqueSongs(year, label));
        stats.put("uniqueArtists", repo.countUniqueArtists(year, label));
        stats.put("netRevenue", totalNetRevenue.multiply(new BigDecimal("85")));

        Map<String, BigDecimal> platformRevenue = convertRows(repo.revenueByPlatform(year, label), 10);
        Map<String, BigDecimal> countryRevenue = convertRows(repo.revenueByCountry(year, label), 10);
        Map<String, BigDecimal> topSongs = convertSongRows(repo.topSongs(year, label), 10);

        stats.put("platformRevenue", platformRevenue);
        stats.put("countryRevenue", countryRevenue);
        stats.put("topSongs", topSongs);

        stats.put("aiSummary", openAiService.generateExecutiveSummary(
                year, label, stats
        ));

        return stats;
    }

    public List<String> labels() {
        return repo.findDistinctLabels();
    }

    public List<String> years() {
        return repo.findDistinctYears();
    }

    private Map<String, BigDecimal> convertRows(List<Object[]> rows, int limit) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        rows.stream().limit(limit).forEach(row -> {
            String key = row[0] == null ? "Unknown" : row[0].toString();
            result.put(key, toBigDecimal(row[row.length - 1]));
        });
        return result;
    }

    private Map<String, BigDecimal> convertSongRows(List<Object[]> rows, int limit) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        rows.stream().limit(limit).forEach(row -> {
            String song = row[0] == null ? "Unknown Song" : row[0].toString();
            String artist = row.length > 2 && row[1] != null ? row[1].toString() : "Unknown Artist";
            result.put(song + " - " + artist, toBigDecimal(row[row.length - 1]));
        });
        return result;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    
}
