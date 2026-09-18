package com.digital.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.digital.entity.QuarterlyReport;
import com.digital.entity.ReportChannelPublish;
import com.digital.entity.ReportPublishMapping;
import com.digital.entity.ReportUploadHistory;
import com.digital.entity.User;
import com.digital.repository.QuarterlyReportRepository;
import com.digital.repository.ReportChannelPublishRepository;
import com.digital.repository.ReportPublishMappingRepository;
import com.digital.repository.UserRepository;
import com.digital.util.ReportFileUtil;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UserReportController {
	private final UserRepository userRepo;
	private final ReportPublishMappingRepository mappingRepo;
	private final ReportChannelPublishRepository channelPublishRepo;
	private final QuarterlyReportRepository quarterlyRepo;
	private final ReportFileUtil reportFileUtil;

	public UserReportController(UserRepository userRepo, ReportPublishMappingRepository mappingRepo,
			QuarterlyReportRepository quarterlyRepo, ReportFileUtil reportFileUtil,
			ReportChannelPublishRepository channelPublishRepo) {
		super();
		this.userRepo = userRepo;
		this.mappingRepo = mappingRepo;
		this.channelPublishRepo = channelPublishRepo;
		this.quarterlyRepo = quarterlyRepo;
		this.reportFileUtil = reportFileUtil;
	}

//	@GetMapping("/user/reports")
//	public String userReports(Model model, Principal principal,
//	                          @RequestParam(required = false) Integer year,
//	                          @RequestParam(required = false) Integer month,
//	                          @RequestParam(required = false) Integer quarter) {
//
//	    String channel = userRepo.findByUsername(principal.getName())
//	            .get().getChannelName();
//
//	    List<ReportPublishMapping> reports =
//	        mappingRepo.findByChannelNameAndIsPublishedToClientTrue(channel);
//
//	    model.addAttribute("reports", reports);
//	    return "user/reports";
//	}

	@GetMapping("/user/reports/{mapId}/download")
	public void downloadReport(@PathVariable Long mapId, HttpServletResponse response, Principal principal)
			throws IOException {
		Long reportId = 0L;
		String quarter = "";
		Integer year = 0;
		String channel = userRepo.findByUsername(principal.getName()).get().getChannelName();
		Optional<ReportPublishMapping> mapping = mappingRepo.findById(mapId);
		if(ObjectUtils.isNotEmpty(mapping)) {
		reportId = mapping.get().getReportId();
		quarter = mapping.get().getQuarter();
		year = mapping.get().getYear();
	}else {
		ReportChannelPublish channelPublishning = channelPublishRepo.findById(mapId).orElseThrow();
		if(ObjectUtils.isNotEmpty(channelPublishning)) {
			reportId = channelPublishning.getUpload().getQuarterlyReportId();
			quarter = channelPublishning.getUpload().getQuarter();
			year = channelPublishning.getUpload().getYear();
		}
	}
		
		List<QuarterlyReport> rows = quarterlyRepo.findDistinctByReportIdAndLabelName(reportId, channel);

		ByteArrayOutputStream out = reportFileUtil.exportQuarterlyReportExcel(rows);

		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition",
				"attachment; filename=Quarter-" + quarter + "_" + year + "-Revenue_Report.xlsx");
		response.getOutputStream().write(out.toByteArray());
	}

	@GetMapping("/user/reports")
	public String userReports(Model model, Principal principal) {

	    String channel = userRepo.findByUsername(principal.getName())
	            .get().getChannelName();

	    List<ReportPublishMapping> published = 
	            mappingRepo.findByChannelNameAndIsPublishedToClientTrue(channel);

	    List<ReportPublishMapping> visibleToUser = new ArrayList<>();
	    if(!CollectionUtils.isEmpty(published)) {
	    for (ReportPublishMapping m : published) {
	        boolean exists = quarterlyRepo
	            .findDistinctByReportIdAndLabelName(m.getReportId(), channel)
	            .size() > 0;

	        if (exists) visibleToUser.add(m);
	    }
	    }else {
	    	List<ReportChannelPublish> chhanelPublished = 
	    			channelPublishRepo.findReportsForChannel(channel);
	    	for (ReportChannelPublish m : chhanelPublished) {
		        boolean exists = quarterlyRepo.findDistinctByReportIdAndLabelName(m.getUpload().getQuarterlyReportId(), channel)
		            .size() > 0;

		        if (exists) {
		        	ReportPublishMapping reportPublishing = new ReportPublishMapping();
		        	reportPublishing.setId(m.getId());
		        	reportPublishing.setChannelName(m.getChannelName());
		        	reportPublishing.setQuarter(m.getUpload().getQuarter());
		        	reportPublishing.setYear(m.getUpload().getYear());
		        	reportPublishing.setPublishedOn(m.getUpload().getPublishedOn());
		        	//reportPublishing.setPublishedOn(m.getPublishedOn());)
		        	visibleToUser.add(reportPublishing);
		        }
	    	}
	    
	    }

	    model.addAttribute("reports", visibleToUser);
	    return "user/reports";
	}
//	@GetMapping("/user/reports")
//	public String getAvailableReports(Model model, Principal principal) {
//
//	    String username = principal.getName();
//	    User u = userRepo.findByUsername(username).get();
//	    String channel = u.getChannelName();
//
//	    List<ReportPublishMapping> published = 
//	            mappingRepo.findByIsPublishedToClientTrue();
//
//	    List<ReportPublishMapping> userVisible = new ArrayList<>();
//
//	    for (ReportPublishMapping m : published) {
//	        boolean hasData = quarterlyRepo
//	                .existsByReportIdAndChannel(m.getReportId(), channel);
//
//	        if (hasData) userVisible.add(m);
//	    }
//
//	    model.addAttribute("reports", userVisible);
//	    return "user/reports";
//	}

}
