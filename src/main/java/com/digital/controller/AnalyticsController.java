package com.digital.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.digital.dto.QuarterDetailDto;
import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.repository.QuarterlyRevenueDetailsRepository;
import com.digital.service.DashboardChartsService;

@Controller
@RequestMapping("/admin/analytics")
public class AnalyticsController {

    private final DashboardChartsService chartsService;
    private final QuarterlyRevenueDetailsRepository quarterlyRepo;   

    public AnalyticsController(DashboardChartsService chartsService, QuarterlyRevenueDetailsRepository quarterlyRepo) {
		super();
		this.chartsService = chartsService;
		this.quarterlyRepo = quarterlyRepo;
	}

	@GetMapping
    public String analyticsPage(@RequestParam(required = false) String channel,
                                @RequestParam(required = false) Integer year,
                                Model model) {

        chartsService.populateAnalyticsFilters(model);
        chartsService.populateAnalyticsCharts(channel, year, model);

        model.addAttribute("selectedChannel", channel);
        model.addAttribute("selectedYear", year);

        return "admin/analytics";
    }

 // Drilldown endpoint called via AJAX
    @GetMapping("/quarter-details")
    @ResponseBody
    public List<Map<String,Object>> getQuarterDetails(
            @RequestParam Integer year,
            @RequestParam String quarter,
            @RequestParam(required = false) String channel) {

        List<QuarterlyRevenueDetails> data = quarterlyRepo.findAll().stream()
                .filter(q -> q.getYear().equals(year))
                .filter(q -> q.getQuarter().equalsIgnoreCase(quarter))
                .filter(q -> (channel == null || channel.isBlank()) 
                             || channel.equalsIgnoreCase(q.getChannelName()))
                .toList();

        return data.stream().map(q -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("month", q.getMonthName());
            m.put("channel", q.getChannelName());
            m.put("netRevenue", q.getNetRevenue());
            m.put("inr", q.getInrValue());
            m.put("clientShare", q.getClientShare());
            m.put("companyShare", q.getCompanyShare());
            m.put("payable", q.getPayableAmount());
            return m;
        }).toList();
    }
    
    
}
