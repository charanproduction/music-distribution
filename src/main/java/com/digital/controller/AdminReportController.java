package com.digital.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.digital.dto.ReportHistoryDTO;
import com.digital.entity.PublishedReport;
import com.digital.entity.ReportPublishMapping;
import com.digital.entity.ReportUploadHistory;
import com.digital.enums.PeriodType;
import com.digital.repository.MonthlyReportRepository;
import com.digital.repository.PublishedReportRepository;
import com.digital.repository.QuarterlyReportRepository;
import com.digital.repository.ReportPublishMappingRepository;
import com.digital.repository.ReportUploadHistoryRepository;
import com.digital.service.ReportService;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final ReportService reportService;
    private final ReportUploadHistoryRepository reportHistoryRepo;
    private final ReportPublishMappingRepository mappingRepo;
    private final PublishedReportRepository publishedRepo;
    private final QuarterlyReportRepository qReportRepo;
    private final MonthlyReportRepository monthlyRepo;
    //@Autowired
    //EmailService emailService;

    public AdminReportController(ReportService reportService, ReportUploadHistoryRepository reportHistoryRepo, ReportPublishMappingRepository mappingRepo, PublishedReportRepository publishedRepo, QuarterlyReportRepository qReportRepo, MonthlyReportRepository monthlyRepo) {
        this.reportService = reportService;
		this.reportHistoryRepo = reportHistoryRepo;
		this.mappingRepo = mappingRepo;
		this.publishedRepo = publishedRepo;
		this.qReportRepo = qReportRepo;
		this.monthlyRepo = monthlyRepo;
    }

    @GetMapping
    public String reportsPage(Model model) {
    	List<ReportHistoryDTO> history = reportHistoryRepo.findAllByOrderByUploadedDateDesc().stream()
    	.map(r -> new ReportHistoryDTO(
                r.getId(),
                r.getFileName(),
                r.getReportType(),
                r.getYear(),
                r.getQuarter(),
                r.getUploadedDate(),
                publishedRepo.existsByQuarterlyReportId(r.getId())
        ))
        .collect(Collectors.toList());
        model.addAttribute("history", history);
        return "admin/upload-reports";
    }

    @PostMapping("/upload")
    public String uploadReport(@RequestParam("reportType") String reportType,
                               @RequestParam("file") MultipartFile file,
                               Model model) throws Exception {

        if (file.isEmpty()) {
            model.addAttribute("error", "Please upload a valid file.");
            return "admin/upload-reports";
        }

        if (reportType.equals("monthly")) {
            reportService.processMonthlyReport(file);
            model.addAttribute("success", "Monthly report processed successfully.");
        } else {
            reportService.processQuarterlyReport(file);
            model.addAttribute("success", "Quarterly report processed successfully.");
        }

        return "admin/upload-reports";
    }
    
//    @PostMapping("/{id}/publish")
//    @ResponseBody
//    public String publishToUsers(@PathVariable Long id,
//                                 @RequestParam PeriodType type,
//                                 @RequestParam Integer year,
//                                 @RequestParam(required = false) Integer month,
//                                 @RequestParam(required = false) Integer quarter,
//                                 Principal principal) {
//
//        String channel = "GLOBAL"; // or derive from principal if per-channel rule needed
//
//        ReportPublishMapping mapping = mappingRepo.findByReportId(id)
//            .orElse(new ReportPublishMapping());
//
//        mapping.setReportId(id);
//        mapping.setChannelName(channel);
//        mapping.setPeriodType(type);
//        mapping.setYear(year);
//        mapping.setMonth(month);
//        mapping.setQuarter(quarter);
//        mapping.setPublishedToClient(true);
//
//        mappingRepo.save(mapping);
//        return "OK";
//    }
    
    @PostMapping("/{historyId}/publish")
    @ResponseBody
    public ResponseEntity<String> publishReport(@PathVariable Long historyId
            ) {
    	ReportUploadHistory history = reportHistoryRepo.findById(historyId)
                .orElseThrow(() -> new RuntimeException("History not found"));

        
        String reportType = history.getReportType().toUpperCase();

        if ("QUARTERLY".equals(reportType)) {
        	Long reportId = history.getQuarterlyReportId();
            List<String> channels = qReportRepo.findDistinctLabelNameByReportId(reportId);

            for (String ch : channels) {
                ReportPublishMapping mapping = ReportPublishMapping.builder()
                        .reportId(reportId)
                        .channelName(ch)
                        .periodType(PeriodType.QUARTERLY)
                        .year(history.getYear())
                        .quarter(history.getQuarter())
                        .isPublishedToClient(true)
                        .build();

                mappingRepo.save(mapping);
            }
        }
        else if ("MONTHLY".equals(reportType)) {
        	Long reportId = history.getMonthlyReportId();
            List<String> channels = monthlyRepo.findDistinctLabelNameByReportId(reportId);

            for (String ch : channels) {
                ReportPublishMapping mapping = ReportPublishMapping.builder()
                        .reportId(reportId)
                        .channelName(ch)
                        .periodType(PeriodType.MONTHLY)
                        .year(history.getYear())
                        //.month(history.getMonth())
                        .isPublishedToClient(true)
                        .build();

                mappingRepo.save(mapping);
            }
        }

        // Mark history published
        //history.setPublished(true);
       // historyRepo.save(history);

        return ResponseEntity.ok("Published");
    }
    
//    @PostMapping("/{historyId}/publish")
//    public ResponseEntity<?> publish(@PathVariable Long historyId,
//                                     @RequestParam Integer year,
//                                     @RequestParam(required = false) Integer quarter,
//                                     @RequestParam(required = false) Integer month) {
//
//        ReportUploadHistory history = reportHistoryRepo.findById(historyId).orElseThrow();
//
//        if ("QUARTERLY".equalsIgnoreCase(history.getReportType())) {
//            Long quarterlyId = history.getQuarterlyReportId();
//            mappingRepo.save(new PublishedReport(quarterlyId, "QUARTERLY", year, quarter));
//        }
//
//        if ("MONTHLY".equalsIgnoreCase(history.getReportType())) {
//            Long monthlyId = history.getMonthlyReportId();
//            mappingRepo.save(new PublishedMapping(monthlyId, "MONTHLY", year, null, month));
//        }
//
//        history.setPublished(true);
//        reportHistoryRepo.save(history);
//
//        return ResponseEntity.ok().build();
//    }
    
    @GetMapping("/reports")
    public String listReports(
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 25);

        Page<ReportUploadHistory> reportsPage;

        if(channel != null && !channel.equalsIgnoreCase("ALL")) {
            reportsPage = reportHistoryRepo.findByChannelName(channel, pageable);
        } else {
            reportsPage = reportHistoryRepo.findAll(pageable);
        }

        model.addAttribute("reports", reportsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportsPage.getTotalPages());
        model.addAttribute("channels", reportHistoryRepo.findDistinctChannelNames());
        model.addAttribute("selectedChannel", channel != null ? channel : "ALL");

        return "admin/report-history";
    }
    
//    @PostMapping("/admin/reports/{id}/publish")
//    @ResponseBody
//    public ResponseEntity<?> publishReport(@PathVariable Long id) {
//        ReportUploadHistory report = reportService.publish(id);
//
//        emailService.send(
//            report.getUser().getEmail(),
//            "New Streaming Report Published!",
//            "Hi " + report.getUser().getFullName() +
//            ",\n\nYour revenue report is published for:\n" +
//            "Channel: " + report.getChannelName() + "\n" +
//            "Report: " + report.getReportType() + " " + report.getYear() + "\n\n" +
//            "Login to view details.\n\nThanks,\nCharan Production Digital"
//        );
//
//        return ResponseEntity.ok("PUBLISHED");
//    }
    
}
