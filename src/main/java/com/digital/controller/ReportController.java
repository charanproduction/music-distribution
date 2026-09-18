//package com.digital.controller;
//
//import java.io.IOException;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import com.digital.service.ReportService;
//
//@Controller
//@RequestMapping("/admin/reports")
//public class ReportController {
//
//    private final ReportService reportService;
//
//    public ReportController(ReportService reportService) {
//        this.reportService = reportService;
//    }
//
////    @GetMapping
////    public String reportsPage() {
////        return "admin/reports";
////    }
//
//    @PostMapping("/upload")
//    public String uploadReport(@RequestParam("type") String type,
//                               @RequestParam("file") MultipartFile file,
//                               RedirectAttributes redirect) throws Exception {
//
//        if ("MONTHLY".equalsIgnoreCase(type)) {
//            reportService.processMonthlyReport(file);
//        } else if ("QUARTERLY".equalsIgnoreCase(type)) {
//            reportService.processQuarterlyReport(file);
//        }
//        redirect.addFlashAttribute("message", "Report uploaded and processed");
//        return "redirect:/admin/reports";
//    }
//}
//
