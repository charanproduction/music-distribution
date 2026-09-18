package com.digital.controller;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.digital.entity.MonthlyRevenueDetails;
import com.digital.repository.MonthlyRevenueDetailsRepository;
import com.digital.repository.QuarterlyRevenueDetailsRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin/reports/download")
public class ReportDownloadController {

    private final MonthlyRevenueDetailsRepository monthlyRevRepo;
    private final QuarterlyRevenueDetailsRepository quarterlyRevRepo;

    public ReportDownloadController(MonthlyRevenueDetailsRepository m,
                                    QuarterlyRevenueDetailsRepository q) {
        this.monthlyRevRepo = m;
        this.quarterlyRevRepo = q;
    }

    @GetMapping("/monthly/csv")
    public void downloadMonthlyCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=monthly_revenue.csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Channel,SalesMonth,ReportingMonth,NetRevenue,INR,ClientShare,CompanyShare");
            for (MonthlyRevenueDetails r : monthlyRevRepo.findAll()) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                        r.getChannelName(),
                        r.getSalesMonth(),
                        r.getReportingMonth(),
                        r.getNetRevenue(),
                        r.getInrValue(),
                        r.getClientShare(),
                        r.getCompanyShare());
            }
        }
    }

    @GetMapping("/monthly/excel")
    public void downloadMonthlyExcel(HttpServletResponse response) throws IOException {
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=monthly_revenue.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Monthly Revenue");

            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            String[] cols = {"Channel", "SalesMonth", "ReportingMonth",
                    "NetRevenue", "INR", "ClientShare", "CompanyShare"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }

            for (MonthlyRevenueDetails r : monthlyRevRepo.findAll()) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;
                row.createCell(c++).setCellValue(r.getChannelName());
                row.createCell(c++).setCellValue(r.getSalesMonth());
                row.createCell(c++).setCellValue(r.getReportingMonth());
                row.createCell(c++).setCellValue(r.getNetRevenue().doubleValue());
                row.createCell(c++).setCellValue(r.getInrValue().doubleValue());
                row.createCell(c++).setCellValue(r.getClientShare().doubleValue());
                row.createCell(c++).setCellValue(r.getCompanyShare().doubleValue());
            }

            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/monthly/pdf")
    public void downloadMonthlyPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=monthly_revenue.pdf");

        com.lowagie.text.Document doc = new com.lowagie.text.Document();
        try {
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, response.getOutputStream());
            doc.open();
            doc.add(new com.lowagie.text.Paragraph("Monthly Revenue Summary"));

            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(4);
            table.addCell("Channel");
            table.addCell("ReportingMonth");
            table.addCell("NetRevenue(INR)");
            table.addCell("ClientShare");

            for (MonthlyRevenueDetails r : monthlyRevRepo.findAll()) {
                table.addCell(r.getChannelName());
                table.addCell(r.getReportingMonth());
                table.addCell(r.getInrValue().toPlainString());
                table.addCell(r.getClientShare().toPlainString());
            }
            doc.add(table);
        } finally {
            doc.close();
        }
    }

    // You can repeat similar methods for quarterly: /quarterly/csv, /quarterly/excel, /quarterly/pdf
}
