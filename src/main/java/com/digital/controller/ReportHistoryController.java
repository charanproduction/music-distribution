package com.digital.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.digital.entity.ReportUploadHistory;
import com.digital.repository.ReportUploadHistoryRepository;
import com.digital.service.ReportHistoryService;
import com.digital.service.ReportService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class ReportHistoryController {

    private final ReportHistoryService reportHistoryService;
    private final ReportService reportService;
    private final ReportUploadHistoryRepository reportHistoryRepo;

    @GetMapping("/history")
    public String viewReports(Model model) {
        model.addAttribute("reportHistory", reportHistoryService.getAll());
        return "admin/upload-reports";
    }

    @DeleteMapping("/delete")
    @ResponseBody
    public ResponseEntity<?> deleteReports(@RequestParam("ids") String ids) {
    	List<Long> idList = Arrays.stream(ids.split(","))
                 .filter(s -> !s.isBlank())
                 .map(Long::valueOf)
                 .collect(Collectors.toList());
    	 reportHistoryService.deleteReport(idList);
        return ResponseEntity.ok("Deleted");
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadReports(@RequestParam List<Long> reportIds) {
        // TODO: Fetch revenue rows for selected quarter+year and export to Excel
        byte[] excelFile = reportHistoryService.exportToExcel(reportIds);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=RevenueReports.xlsx")
                .body(excelFile);
    }
    
//    @GetMapping("/history/service")
//    public String history(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(required = false) String search,
//            @RequestParam(required = false) String channel,
//            @RequestParam(required = false) String sort,
//            Model model) {
//
//        PageRequest pageable = PageRequest.of(
//                page, 10,
//                sort != null ? Sort.by(sort).ascending()
//                             : Sort.by("uploadedDate").descending()
//        );
//
//        model.addAttribute("reports",
//        		reportService.searchReports(search, channel, pageable)
//        );
//
//        model.addAttribute("search", search);
//        model.addAttribute("selectedChannel", channel);
//        model.addAttribute("channels", reportService.getAllChannels());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", reportService.totalPages(search, channel));
//        model.addAttribute("activePage", "reportsHistory");
//
//        return "admin/report-history";
//    }
    @GetMapping("/history/service")
    public String history(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(required = false) String search,
          @RequestParam(required = false) String channel,
          @RequestParam(required = false) String sort,
          Model model) {

        Pageable pageable = PageRequest.of(page, 25);

        Page<ReportUploadHistory> reportsPage;

        if(channel != null && !channel.equalsIgnoreCase("ALL")) {
            reportsPage = reportHistoryRepo.findByChannelName(channel, pageable);
        } else {
            reportsPage = reportHistoryRepo.findAll(pageable);
        }
        reportsPage.getContent().forEach(r -> {
            boolean fullyPublished = reportHistoryService.publishedForAllChannels(r.getId());
            r.setPublishedForAllChannels(fullyPublished);
        });
        List<String>channels = reportHistoryRepo.findAllChannels();
        model.addAttribute("reports", reportsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportsPage.getTotalPages());
        model.addAttribute("channels", channels);
        model.addAttribute("selectedChannel", channel != null ? channel : "ALL");

        return "admin/report-history";
    }
    //@GetMapping("/{uploadId}/channels")
    @GetMapping("/channels")
    @ResponseBody
    public List<String> loadChannels(@PathVariable Long uploadId) {
        return reportHistoryService.getReportChannels(uploadId);
    }

//    @PostMapping("/{uploadId}/publish")
//    @ResponseBody
//    public ResponseEntity<?> publishReport(@PathVariable Long uploadId,
//                                          @RequestBody List<String> channels) {
//
//        if(channels.isEmpty())
//            return ResponseEntity.badRequest().body("Select channels");
//
//        reportHistoryService.publishReport(uploadId, channels);
//
//        return ResponseEntity.ok("Published!");
//    }
    @PostMapping("/history/{uploadId}/publish")
    @ResponseBody
    public ResponseEntity<?> publishReport(
            @PathVariable Long uploadId,
            @RequestBody List<String> channels) {

        reportHistoryService.publishToChannels(uploadId, channels);
        return ResponseEntity.ok("Published");
    }
    
    @GetMapping("/{uploadId}/labels")
    @ResponseBody
    public List<String> fetchChannelsByFileId(@PathVariable Long uploadId) {
    	List<String>channelsForUpload = reportHistoryService.getDistinctChannelsForUpload(uploadId);
    	return channelsForUpload;
        
    }
//    @PostMapping("/{uploadId}/publish-channels")
//    @ResponseBody
//    public Map<String, Boolean> publishForChannels(@PathVariable Long uploadId,
//                                                   @RequestBody List<String> labels) {
//        reportHistoryService.publishToChannels(uploadId, labels);
//        boolean fully = reportHistoryService.isFullyPublishedForUpload(uploadId);
//        return Map.of("fullyPublished", fully);
//    }    

    @PostMapping("/{uploadId}/publish-channels")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> publishToChannels(
            @PathVariable Long uploadId,
            @RequestBody List<String> channels) {

        if (channels == null || channels.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No channels selected"
            ));
        }

        // Save publish mapping for selected channels
        reportHistoryService.publishToChannels(uploadId, channels);

        // Check fully published state
        boolean fullyPublished = reportHistoryService.publishedForAllChannels(uploadId);

        return ResponseEntity.ok(Map.of(
                "uploadId", uploadId,
                "fullyPublished", fullyPublished
        ));
    }
    
    @GetMapping("/admin/reports/channels")
    @ResponseBody
    public List<String> getChannels(@RequestParam Long uploadId) {
        return reportHistoryService.getDistinctChannels(uploadId);
    }

    // Publish selected channels
//    @PostMapping("/admin/reports/{uploadId}/publish")
//    @ResponseBody
//    public ResponseEntity<?> publishReportToChannels(@PathVariable Long uploadId,
//                                                     @RequestBody List<String> channels) {
//    	reportHistoryService.publishToChannels(uploadId, channels);
//        return ResponseEntity.ok("Published Successfully");
//    }

    // Filter + Sort + Paging View
    @GetMapping("/admin/reports/history")
    public String showHistory(@RequestParam(required = false) String search,
                              @RequestParam(required = false) String channel,
                              @RequestParam(defaultValue = "fileName") String sort,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort).ascending());

        Page<ReportUploadHistory> reports =
        		reportHistoryService.searchHistory(search, channel, pageable);

        model.addAttribute("reports", reports);
        model.addAttribute("search", search);
        model.addAttribute("selectedChannel", channel);
        model.addAttribute("channels", reportHistoryService.getAllChannels());

        model.addAttribute("activePage", "reportsHistory");
        return "admin/report-history";
    }

    // Bulk Delete
    @DeleteMapping("/admin/reports/delete")
    @ResponseBody
    public ResponseEntity<?> deleteReports(@RequestParam("ids") List<Long> ids) {
    	reportHistoryService.deleteReports(ids);
        return ResponseEntity.ok("Deleted");
    }
}
