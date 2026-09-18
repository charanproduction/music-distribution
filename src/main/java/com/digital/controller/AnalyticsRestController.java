package com.digital.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digital.dto.ChannelSummary;
import com.digital.dto.MonthlySeriesPoint;
import com.digital.dto.QuarterlySeriesPoint;
import com.digital.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsRestController {

    private final AnalyticsService analyticsService;

    public AnalyticsRestController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/monthly")
    public List<MonthlySeriesPoint> monthly(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) Integer year) {
        return analyticsService.getMonthlySeries(channel, year);
    }

    @GetMapping("/quarterly")
    public List<QuarterlySeriesPoint> quarterly(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) Integer year) {
        return analyticsService.getQuarterlySeries(channel, year);
    }

    @GetMapping("/channel-summary")
    public List<ChannelSummary> channelSummary(
            @RequestParam(required = false) Integer year) {
        return analyticsService.getChannelSummary(year);
    }
}
