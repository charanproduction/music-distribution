package com.digital.controller;

import com.digital.service.AdminAiAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminAiAnalyticsController {

	private final AdminAiAnalyticsService analyticsService;

    @GetMapping("/admin/ai-analytics")
    public String aiAnalytics(@RequestParam(required = false) String year,
                              @RequestParam(required = false, defaultValue = "ALL") String label,
                              Model model) {

        model.addAttribute("stats", analyticsService.dashboardStats(year, label));
        model.addAttribute("years", analyticsService.years());
        model.addAttribute("labels", analyticsService.labels());
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedLabel", label);
        model.addAttribute("activePage", "aiAnalytics");

        return "admin/ai-analytics";
    }
}