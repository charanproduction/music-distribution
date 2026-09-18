package com.digital.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.digital.dto.PaymentDetailsDTO;
import com.digital.dto.RevenueRowDto;
import com.digital.entity.PaymentDetails;
import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.service.RevenueService;

@Controller
@RequestMapping("/admin/revenue")
public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping
    public String revenuePage(@RequestParam(required = false) String channel,
                              @RequestParam(required = false) Integer year,
                              @RequestParam(required = false) String quarter,
                              @RequestParam(required = false) String month,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {

        // Normalize filters
        String ch = normalize(channel);
        Integer yr = normalize(year);
        String q = normalize(quarter);
        String m = normalize(month);

        // Fetch page & DTO conversion
        Page<QuarterlyRevenueDetails> revenuePage = revenueService.searchRevenue(ch, yr, q, m, page);
        List<RevenueRowDto> revenueList = revenueService.searchChannelNameYearQuarter(revenuePage);

        // Summary
        Map<String, BigDecimal> summary = revenueService.allTimeSummary();
        model.addAttribute("totalRevenueAllTime", summary.get("total"));
        model.addAttribute("clientShareAllTime", summary.get("clientShare"));
        model.addAttribute("companyShareAllTime", summary.get("companyShare"));

        // UI Data
        model.addAttribute("revenueList", revenueList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", revenuePage.getTotalPages());

        // Filter dropdowns
        model.addAttribute("channels", revenueService.allChannels());
        model.addAttribute("years", revenueService.allYears());
        model.addAttribute("months", revenueService.allMonths());
        model.addAttribute("quarters", revenueService.allQuarters());

        // Preserve selection
        model.addAttribute("selectedChannel", channel);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedQuarter", quarter);
        model.addAttribute("selectedMonth", month);

        return "admin/revenue";
    }

    private String normalize(String s) {
        return (s == null || s.isBlank() || s.equalsIgnoreCase("ALL")) ? null : s;
    }
    private Integer normalize(Integer i) {
        return (i == null || i == 0) ? null : i;
    }

    @PostMapping("/payment")
    public String savePayment(@RequestParam(required = false) Long id,
                              @RequestParam String channelName,
                              @RequestParam Integer year,
                              @RequestParam String quarter,
                              @RequestParam BigDecimal amount,
                              @RequestParam String paymentMethod,
                              @RequestParam String paymentReference,
                              @RequestParam String paymentDate,
                              @RequestParam(defaultValue = "PAID") String status,
                              @RequestParam(required = false) String filterChannel,
                              @RequestParam(required = false) Integer filterYear,
                              @RequestParam(required = false) String filterQuarter,
                              @RequestParam(required = false) String filterMonth) {

        revenueService.saveOrUpdatePayment(
                id, channelName, year, quarter, amount,
                paymentMethod, paymentReference,
                LocalDate.parse(paymentDate), status
        );

        return "redirect:/admin/revenue";
    }

    @GetMapping("/payment/{id}")
    @ResponseBody
    public PaymentDetailsDTO getPayment(@PathVariable Long id) {
        return revenueService.getPaymentDetails(id);
    }

    @PostMapping("/payment/delete")
    @ResponseBody
    public ResponseEntity<?> deletePayment(@RequestParam Long id) {
        revenueService.deletePayment(id);
        return ResponseEntity.ok().build();
    }
}
