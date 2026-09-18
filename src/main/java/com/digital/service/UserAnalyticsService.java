package com.digital.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.digital.dto.*;
import com.digital.model.SongRevenueSum;
import com.digital.model.YearQuarterSum;
import com.digital.repository.QuarterlyRevenueDetailsRepository;
import com.digital.repository.SongRepository;
import com.digital.repository.PaymentDetailsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAnalyticsService {

    private final QuarterlyRevenueDetailsRepository revenueRepo;
    private final PaymentDetailsRepository paymentRepo;
    private final SongRepository repo;

    public UserAnalyticsSummaryDto getSummary(String channel) {

        Double totalInr = revenueRepo.sumNetRevenueByChannel(channel);
        if (totalInr == null) totalInr = 0d;

        Double paidInr = paymentRepo.sumPaidAmountByChannel(channel);
        if (paidInr == null) paidInr = 0d;

        double pending = totalInr - paidInr;
        if (pending < 0) pending = 0;

        long songCount = revenueRepo.countDistinctTrackByChannel(channel);

        return UserAnalyticsSummaryDto.builder()
                .totalRevenueInr(totalInr)
                .paidInr(paidInr)
                .pendingInr(pending)
                .totalSongs(songCount)
                .build();
    }

    public List<YearQuarterSum> getQuarterlyTrend(String channel) {
        // returns (year, quarter, sumInr) sorted
    	List<Object[]> raw = revenueRepo.findRevenueGroupedByYearQuarter(channel);
    			List<YearQuarterSum> result = new ArrayList<>();

        for (Object[] row : raw) {
            Integer year = (Integer) row[0];
            String quarter = (String) row[1];
            Double clientShare = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;

            result.add(new YearQuarterSum(year, quarter, clientShare));
        }

        return result;
    }

    public List<TopSongDto> getTopSongs(String channel, int limit) {

        List<Object[]> raw = revenueRepo.findTopSongsByChannel(
                channel.toLowerCase(), PageRequest.of(0, limit));

        List<TopSongDto> result = new ArrayList<>();

        for (Object[] row : raw) {
            String track = (String) row[0];
            String artist = (String) row[1];
            Double revenue = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;

            result.add(new TopSongDto(track, artist, revenue));
        }

        return result;
    }

    public List<CountryRevenueDto> getCountryRevenue(String channel) {

        List<Object[]> raw = revenueRepo.sumRevenueByCountry(channel.toLowerCase());

        List<CountryRevenueDto> result = new ArrayList<>();

        for (Object[] row : raw) {
            String country = (String) row[0];
            Double revenue = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;

            result.add(new CountryRevenueDto(country, revenue));
        }

        return result;
    }
    
    public List<SongRevenueSum> findTopSongsByChannel(String channel, int limit) {

        List<Object[]> raw = revenueRepo.findTopSongsByChannel(
                channel.toLowerCase(),
                PageRequest.of(0, limit)
        );

        List<SongRevenueSum> result = new ArrayList<>();

        for (Object[] row : raw) {
            String trackTitle = (String) row[0];
            String artistName = (String) row[1];
            Double totalRevenue = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;

            result.add(new SongRevenueSum(trackTitle, artistName, totalRevenue));
        }

        return result;
    }
}
