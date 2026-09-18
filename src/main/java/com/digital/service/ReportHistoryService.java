package com.digital.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.digital.entity.QuarterlyRevenueDetails;
import com.digital.entity.ReportChannelPublish;
import com.digital.entity.ReportUploadHistory;
import com.digital.repository.MonthlyReportRepository;
import com.digital.repository.MonthlyRevenueDetailsRepository;
import com.digital.repository.QuarterlyReportRepository;
import com.digital.repository.QuarterlyRevenueDetailsRepository;
import com.digital.repository.ReportChannelPublishRepository;
import com.digital.repository.ReportUploadHistoryRepository;
import org.springframework.data.domain.Page;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class ReportHistoryService {
	private final MonthlyReportRepository monthlyReportRepo;
	private final QuarterlyReportRepository quarterlyReportRepo;
	private final ReportUploadHistoryRepository historyRepo;
	private final QuarterlyRevenueDetailsRepository quarterlyRevenueRepo;
	private final MonthlyRevenueDetailsRepository monthlyRevenueRepo;
	private final ReportChannelPublishRepository publishRepository;

	public List<ReportUploadHistory> getAll() {
		return historyRepo.findAllByOrderByUploadedDateDesc();
	}

	public void saveUploadHistory(String fileName, Integer year, String quarter) {
		ReportUploadHistory h = ReportUploadHistory.builder().fileName(fileName).year(year).quarter(quarter)
				.uploadedDate(LocalDate.now()).build();
		historyRepo.save(h);
	}

	// 🔥 DELETE REPORT + RELATED REVENUE ROWS
	@Transactional
	public void deleteReport(List<Long> ids) {
		List<ReportUploadHistory> records = historyRepo.findAllById(ids);

		for (ReportUploadHistory h : records) {
			if ("QUARTERLY".equalsIgnoreCase(h.getReportType())) {
				String[] range = getQuarterRange(h.getYear(), h.getQuarter());
				quarterlyReportRepo.deleteByYearAndQuarter(range[0], range[1]);
				quarterlyRevenueRepo.deleteByYearAndQuarter(h.getYear(), h.getQuarter());
			}

			if ("MONTHLY".equalsIgnoreCase(h.getReportType())) {
				// calculate valid date range based on quarter
				String[] range = getQuarterRange(h.getYear(), h.getQuarter());
				monthlyReportRepo.deleteByYearAndQuarter(range[0], range[1]);
				monthlyRevenueRepo.deleteMonthly(h.getYear(), range[0], range[1]);
			}
		}
		historyRepo.deleteAll(records);
	}

	private String[] getQuarterRange(int year, String q) {
		switch (q.toUpperCase()) {
		case "Q1":
			return new String[] { year + "-01-01", year + "-03-31" };
		case "Q2":
			return new String[] { year + "-04-01", year + "-06-30" };
		case "Q3":
			return new String[] { year + "-07-01", year + "-09-30" };
		case "Q4":
			return new String[] { year + "-10-01", year + "-12-31" };
		default:
			return new String[] { year + "-01-01", year + "-12-31" };
		}
	}

	// 🔥 EXPORT SELECTED REPORTS TO EXCEL
	public byte[] exportToExcel(List<Long> reportIds) {
		try (Workbook workbook = new XSSFWorkbook()) {
			CellStyle headerStyle = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			headerStyle.setFont(font);

			for (Long id : reportIds) {
				ReportUploadHistory h = historyRepo.findById(id)
						.orElseThrow(() -> new RuntimeException("Invalid report ID"));

				List<QuarterlyRevenueDetails> rows = quarterlyRevenueRepo.findByYearAndQuarter(h.getYear(),
						h.getQuarter());

				String sheetName = h.getQuarter() + "-" + h.getYear();
				Sheet sheet = workbook.createSheet(sheetName);

				int rowIdx = 0;
				Row header = sheet.createRow(rowIdx++);

				String[] headers = { "Channel", "Month", "Quarter", "Year", "INR", "Client Share", "Company Share",
						"Payable" };

				for (int i = 0; i < headers.length; i++) {
					Cell cell = header.createCell(i);
					cell.setCellValue(headers[i]);
					cell.setCellStyle(headerStyle);
				}

				for (QuarterlyRevenueDetails r : rows) {
					Row row = sheet.createRow(rowIdx++);
					row.createCell(0).setCellValue(r.getChannelName());
					row.createCell(1).setCellValue(r.getSalesMonth());
					row.createCell(2).setCellValue(r.getQuarter());
					row.createCell(3).setCellValue(r.getYear());
					row.createCell(4).setCellValue(r.getInrValue().doubleValue());
					row.createCell(5).setCellValue(r.getClientShare().doubleValue());
					row.createCell(6).setCellValue(r.getCompanyShare().doubleValue());
					row.createCell(7).setCellValue(r.getPayableAmount().doubleValue());
				}

				for (int i = 0; i < headers.length; i++) {
					sheet.autoSizeColumn(i);
				}
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			return out.toByteArray();

		} catch (Exception e) {
			throw new RuntimeException("Excel export failed", e);
		}
	}
//	public List<String> getReportChannels(Long uploadId) {
//        return quarterlyReportRepo.findChannelsByUploadId(uploadId);
//    }

//    public void publishReport(Long uploadId, List<String> channels) {
//        ReportUploadHistory rh = historyRepo.findById(uploadId)
//                .orElseThrow();
//
//        rh.setPublished(true);
//        rh.setPublishedOn(LocalDateTime.now());
//        rh.setPublishedChannels(String.join(",", channels)); // new column
//        historyRepo.save(rh);
//
//        // store revenue publish mapping per channel
//        quarterlyReportRepo.updatePublishedFlag(uploadId, channels);
//    }

	/**
	 * Load paginated and filtered upload history — used by controller already
	 */
	public Page<ReportUploadHistory> findAllFiltered(String search, String channel, String sort, Pageable pageable) {

		// NULL protection
		String searchTerm = (search == null) ? "" : search.trim();
		boolean filterChannel = (channel != null && !"ALL".equalsIgnoreCase(channel));

		if (filterChannel) {
			return historyRepo.searchByFileAndChannel(searchTerm, channel, pageable);
		}

		return historyRepo.searchByFile(searchTerm, pageable);
	}

	/**
	 * Fetch channels from parsed revenue table for selected upload
	 */
	public List<String> getReportChannels(Long uploadId) {
		return quarterlyReportRepo.findDistinctLabelNameByReportId(uploadId);
	}
	
	public List<String> getAllChannels() {
		return quarterlyReportRepo.findDistinctLabelName();
	}

	/**
	 * Admin action: Publish report for selected channels
	 */
//    public void publishReport(Long uploadId, List<String> channels) {
//
//        ReportUploadHistory rh = historyRepo.findById(uploadId)
//                .orElseThrow(() -> new RuntimeException("Upload not found: " + uploadId));
//
//        rh.setPublished(true);
//        rh.setPublishedOn(LocalDateTime.now());
//        rh.setPublishedChannels(String.join(",", channels)); // save mapping
//
//        historyRepo.save(rh);
//
//        // Update children revenue records → mark published only for selected channels
//        quarterlyReportRepo.publishRecordsForChannels(uploadId, channels);
//    }
	@Transactional
	public void publishReportToChannels(Long uploadId, List<String> channels) {

		// Create rows if missing → idempotent
		channels.forEach(channel -> {
			boolean exists = publishRepository.existsByUpload_IdAndChannelName(uploadId, channel);
			if (!exists) {
				publishRepository
						.save(ReportChannelPublish.builder().upload(historyRepo.findById(uploadId).orElseThrow())
								.channelName(channel).published(true).build());
			} else {
				publishRepository.publishForChannels(uploadId, channels);
			}
		});
	}

	public Page<ReportUploadHistory> getHistory(String search, Pageable pageable) {
		if (search != null && !search.isBlank()) {
			return historyRepo.searchByFile(search, pageable);
		}
		return historyRepo.findAll(pageable);
	}

	public List<String> getDistinctChannelsForUpload(Long uploadId) {
		return quarterlyReportRepo.findDistinctChannelsByReportId(uploadId);
	}

//	@Transactional
//	public void publishToChannels(Long uploadId, List<String> channels) {
//
//		ReportUploadHistory upload = historyRepo.findById(uploadId)
//				.orElseThrow(() -> new RuntimeException("Upload not found: " + uploadId));
//
//		channels.forEach(channel -> {
//
//			if (!publishRepository.existsByUpload_IdAndChannelName(uploadId, channel)) {
//
//				ReportChannelPublish rpc = ReportChannelPublish.builder().upload(upload).channelName(channel)
//						.published(true).build();
//
//				publishRepository.save(rpc);
//			}
//		});
//	}

	public boolean isPublishedForChannel(Long uploadId, String channel) {
		return publishRepository.existsByUpload_IdAndChannelName(uploadId, channel);
	}

	public List<String> getPublishedChannels(Long uploadId) {
		return publishRepository.findChannelsByUpload_Id(uploadId);
	}

	public Page<ReportUploadHistory> searchHistory(String search, String channel, Pageable pageable) {

		if (search == null)
			search = "";
		if (channel == null || channel.equalsIgnoreCase("ALL")) {
			return historyRepo.findByFileNameContainingIgnoreCase(search, pageable);
		} else {
			return historyRepo.searchByFileAndChannel(search, channel, pageable);
		}
	}

	public List<String> getDistinctChannels(Long uploadId) {
		return publishRepository.findDistinctLabelNameByUpload(uploadId);
	}

	@Transactional
	public void publishToChannels(Long uploadId, List<String> channels) {

	    ReportUploadHistory upload = historyRepo.findById(uploadId)
	            .orElseThrow(() -> new RuntimeException("Upload not found"));

	    List<ReportChannelPublish> existing = publishRepository.findByUploadId(uploadId);

	    Set<String> existingChannels = existing.stream()
	            .map(ReportChannelPublish::getChannelName)
	            .collect(Collectors.toSet());

	    for (String channel : channels) {
	        if (!existingChannels.contains(channel)) {
	        	publishRepository.save(
	                ReportChannelPublish.builder()
	                    .upload(upload)
	                    .channelName(channel)
	                    .published(true)
	                    .build()
	            );
	        }
	    }
	}

	public void deleteReports(List<Long> ids) {
		ids.forEach(id -> {
			publishRepository.findByUpload_Id(id).forEach(publishRepository::delete);

			publishRepository.deleteByUploadId(id);
			historyRepo.deleteById(id);
		});
	}
	
	public boolean isFullyPublishedForUpload(Long uploadId) {

        // 1️⃣ Get all labels in this report file
        List<String> allLabels = quarterlyReportRepo.findDistinctLabelsByUploadId(uploadId);
        if (allLabels.isEmpty()) return false; // No labels exist → cannot consider published

        // 2️⃣ Get labels already published for this file
        List<String> published = publishRepository.findPublishedLabelsForUpload(uploadId);

        if (published.size() == 0) return false;

        // 3️⃣ Return true only if all labels are published
        return published.containsAll(allLabels);
    }
	
	@Transactional
	public boolean publishToLabels(Long uploadId, List<String> labels) {

	    labels.forEach(label -> {
	        publishRepository.findByUploadIdAndChannelName(uploadId, label)
	            .orElseGet(() -> publishRepository.save(
	                ReportChannelPublish.builder()
	                    .upload(ReportUploadHistory.builder().id(uploadId).build())
	                    .channelName(label)
	                    .published(true)
	                    .build()
	            ));
	    });

	    long totalDistinct =
	            quarterlyReportRepo.countDistinctLabelNamesByUploadId(uploadId); // FIXED

	    long published =
	            publishRepository.countByUploadId(uploadId); // CORRECT

	    return totalDistinct == published;
	}
	
	public boolean publishedForAllChannels(Long uploadId) {

	    // How many distinct channels exist in the CSV uploaded for this file?
	    long totalDistinctChannels =
	            quarterlyReportRepo.countDistinctLabelNamesByUploadId(uploadId);

	    if (totalDistinctChannels == 0) {
	        return false; // No channels = nothing to publish
	    }

	    // How many channels were marked published in mapping table?
	    long publishedCount =
	            publishRepository.countDistinctByUploadId(uploadId);

	    return publishedCount == totalDistinctChannels;
	}
}