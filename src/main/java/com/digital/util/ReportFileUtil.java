package com.digital.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.digital.entity.QuarterlyReport;

@Component
public class ReportFileUtil {

	public static Integer extractYear(String filename) {
		try {
			String[] parts = filename.split("_");
			if (parts.length < 4)
				return null;

			String startDateStr = parts[2].substring(0, 10); // yyyy-MM-dd
			LocalDate startDate = LocalDate.parse(startDateStr);
			return startDate.getYear();
		} catch (Exception e) {
			return null;
		}
	}

	public static String extractQuarter(String filename) {
		try {
			String[] parts = filename.split("_");
			if (parts.length < 4)
				return null;

			String startDateStr = parts[2].substring(0, 10);
			LocalDate startDate = LocalDate.parse(startDateStr);

			int month = startDate.getMonthValue();
			int quarter = (month - 1) / 3 + 1;

			return "Q" + quarter;
		} catch (Exception e) {
			return null;
		}
	}

	public ByteArrayOutputStream exportQuarterlyReportExcel(List<QuarterlyReport> data) throws IOException {

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Royalty");

// Header style
			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.WHITE.getIndex());
			headerStyle.setFont(headerFont);
			headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);

// Normal cell style
			CellStyle normalStyle = workbook.createCellStyle();
			normalStyle.setWrapText(true);

// Bold style for totals
			CellStyle totalStyle = workbook.createCellStyle();
			Font boldFont = workbook.createFont();
			boldFont.setBold(true);
			totalStyle.setFont(boldFont);

// Create header row
			Row headerRow = sheet.createRow(0);
			String[] columns = { "Reporting Month", "Platform", "Country / Region", "Label Name", "Artist Name",
					"Track Title", "Sales Type", "Quantity", "Net Revenue (USD)", "Net Revenue (INR)",
					"Client Share (70%)" };

			for (int i = 0; i < columns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerStyle);
			}

			BigDecimal totalPayable = BigDecimal.ZERO;
			int rowIdx = 1;

			for (QuarterlyReport r : data) {
				Row row = sheet.createRow(rowIdx++);

				//double netInr = r.getNetRevenue() * 85;
				  BigDecimal nrInr = r.getNetRevenue().multiply(BigDecimal.valueOf(85));
				//double clientShare = netInr * 0.7;
				BigDecimal clientShare = nrInr.multiply(BigDecimal.valueOf(0.70));
				//totalPayable += clientShare;
				totalPayable = totalPayable.add(clientShare);

				int col = 0;
				row.createCell(col++).setCellValue(r.getReportingMonth());
				row.createCell(col++).setCellValue(r.getPlatform());
				row.createCell(col++).setCellValue(r.getCountryRegion());
				row.createCell(col++).setCellValue(r.getLabelName());
				row.createCell(col++).setCellValue(r.getArtistName());
				row.createCell(col++).setCellValue(r.getTrackTitle());
				row.createCell(col++).setCellValue(r.getSalesType());
				row.createCell(col++).setCellValue(r.getQuantity());
				row.createCell(col++).setCellValue(r.getNetRevenue().doubleValue());
				row.createCell(col++).setCellValue(nrInr.doubleValue());
				row.createCell(col).setCellValue(clientShare.doubleValue());
			}

// Total row
			Row totalRow = sheet.createRow(rowIdx + 1);
			Cell totalLabelCell = totalRow.createCell(columns.length - 2);
			totalLabelCell.setCellValue("Total Payable");
			totalLabelCell.setCellStyle(totalStyle);

			Cell totalValueCell = totalRow.createCell(columns.length - 1);
			totalValueCell.setCellValue(totalPayable.doubleValue());
			totalValueCell.setCellStyle(totalStyle);

// Autosize columns
			for (int i = 0; i < columns.length; i++) {
				sheet.autoSizeColumn(i);
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			workbook.close();
			return out;
		}
	}

}
