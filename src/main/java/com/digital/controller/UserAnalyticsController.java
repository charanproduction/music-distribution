package com.digital.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.digital.dto.UserAnalyticsSummaryDto;
import com.digital.model.CountryRevenueSum;
import com.digital.model.YearQuarterSum;
import com.digital.dto.TopSongDto;
import com.digital.dto.RevenueTrendPointDto;
import com.digital.dto.CountryRevenueDto;
import com.digital.service.UserAnalyticsService;
import com.digital.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserAnalyticsController {

    private final UserRepository userRepo;
    private final UserAnalyticsService analyticsService;

    @GetMapping("/user/analytics")
    public String userAnalytics(Principal principal, Model model) {

        String channel = userRepo.findByUsername(principal.getName())
                .get().getChannelName();

        // 1) Top summary
        UserAnalyticsSummaryDto summary = analyticsService.getSummary(channel);

        // 2) Quarterly revenue trend (for chart)
        List<YearQuarterSum> trend = analyticsService.getQuarterlyTrend(channel);
        List<String> labels = trend.stream().map(YearQuarterSum::getLabel).toList();
        List<Double> values = trend.stream().map(YearQuarterSum::getClientShare).toList();
        // 3) Top songs
        List<TopSongDto> topSongs = analyticsService.getTopSongs(channel, 10);

        // 4) Country-wise revenue
        List<CountryRevenueDto> countryRevenue = analyticsService.getCountryRevenue(channel);
        List<String> countryLabels = countryRevenue.stream().map(CountryRevenueDto::getCountry).toList();
        List<Double> countryValues = countryRevenue.stream().map(CountryRevenueDto::getRevenueInr).toList();
        model.addAttribute("countryLabels", countryLabels);
        model.addAttribute("countryValues", countryValues);
        model.addAttribute("trendLabels", labels);
        model.addAttribute("trendValues", values);
        model.addAttribute("channelName", channel);
        model.addAttribute("summary", summary);
        model.addAttribute("trend", trend);
        model.addAttribute("topSongs", topSongs);
        model.addAttribute("countryRevenue", countryRevenue);

        return "user/analytics";
    }
}
