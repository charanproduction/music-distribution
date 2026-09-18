package com.digital.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.digital.entity.SongRequest;
import com.digital.entity.SongRequestComment;
import com.digital.entity.User;
import com.digital.enums.RequestStatus;
import com.digital.enums.SongRequestStatus;
import com.digital.repository.SongRequestRepository;
import com.digital.repository.UserRepository;
import com.digital.service.SongRequestCommentService;
import com.digital.service.SongRequestService;
import com.digital.service.UserService; // or UserRepository
import com.digital.service.UserSongRequestService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/requests")
@RequiredArgsConstructor
public class UserSongRequestController {
	private final UserSongRequestService songRequestService;
	private final SongRequestService reqService;
	private final UserRepository userRepo;
	private final SongRequestRepository songRequestRepo;
	

	@GetMapping("/songs")
	public String requests(@RequestParam(defaultValue = "0") int page, Model model, Principal principal) {

		String channel = userRepo.findByUsername(principal.getName()).orElseThrow().getChannelName();

		Page<SongRequest> requestPage = listRequests(channel, page);

		model.addAttribute("requestPage", requestPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", requestPage.getTotalPages());
		model.addAttribute("activePage", "requests");

		return "user/song-requests";
	}
	public Page<SongRequest> listRequests(String channel, int page) {
	    Pageable pageable = PageRequest.of(page, 10, Sort.by("createdDate").descending());
	    return songRequestRepo.findByChannelSorted(channel, pageable);
	}
//	@GetMapping("/{id}")
//	@ResponseBody
//	public SongRequest view(@PathVariable Long id) {
//		return reqService.get(id);
//	}
	
	@GetMapping("/{id}")
	public String viewRequest(@PathVariable Long id, Model model) {
	    model.addAttribute("req", reqService.get(id));
	    return "fragments/request-view :: view";
	}
	@GetMapping("/requests/{id}/json")
	@ResponseBody
	public SongRequest editJson(@PathVariable Long id) {
	    return reqService.get(id);
	}
	@PostMapping("/{id}/update-status")
	public String updateStatus(@PathVariable Long id, @RequestParam SongRequestStatus status,
			@RequestParam(required = false) List<SongRequestComment> comment) {

		reqService.updateStatus(id, status);
		return "redirect:/user/requests";
	}

	@PostMapping("/{id}/cancel")
	public String cancel(@PathVariable Long id, @RequestParam(required = false) String comment) {
		reqService.updateStatus(id, SongRequestStatus.CANCELLED);
		return "redirect:/user/requests";
	}

	@DeleteMapping("/{id}")
	@ResponseBody
	public void delete(@PathVariable Long id) {
		reqService.delete(id);
	}

	@GetMapping
	public String showUploadPage(Model model) {
		model.addAttribute("activePage", "upload");
		return "user/upload-song";
	}

	@PostMapping
	public String saveSong(@ModelAttribute SongRequest request, @RequestParam("files") MultipartFile[] files,
			Model model, Principal principal) throws Exception {
		String channel = userRepo.findByUsername(principal.getName()).orElseThrow().getChannelName();
		// TODO: Replace with logged in channel name
		//request.setChannelName("Charan Records");
		request.setChannelName(channel);

		reqService.submitRequest(request, files);

		model.addAttribute("message", "Song submitted successfully!");
		model.addAttribute("activePage", "upload");

		return "redirect:/user/requests";
	}

	@GetMapping("/requests")
	public String requestsPage(@RequestParam(defaultValue = "0") int page, Model model, Principal principal) {
		User user = userRepo.findByUsername(principal.getName()).orElseThrow();

		String channel = user.getChannelName();
		Page<SongRequest> paged = songRequestService.getRequests(channel, page);

		model.addAttribute("requests", paged.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paged.getTotalPages());
		model.addAttribute("activePage", "requests");

		return "user/requests";
	}
	
	
}
