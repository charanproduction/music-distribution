package com.digital.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.digital.entity.Song;
import com.digital.entity.SongRequest;
import com.digital.entity.SongRequestComment;
import com.digital.repository.SongRepository;
import com.digital.repository.UserRepository;
import com.digital.service.SongRequestCommentService;
import com.digital.service.SongRequestService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/songs")
@RequiredArgsConstructor
public class UserSongsController {

    private final SongRepository songRepo;
    private final UserRepository userRepo;
    private final SongRequestService reqService;
    private final SongRequestCommentService commentService;

    @GetMapping
    public String allSongs(Model model,
                           Principal principal,
                           @RequestParam(defaultValue = "0") int page) {

        String channel = userRepo.findByUsername(principal.getName())
                .get().getChannelName();

        Page<Song> songs = songRepo.findByChannelNameIgnoreCase(
                channel, PageRequest .of(page, 10,Sort.by("releaseDate").descending())
        );

        model.addAttribute("songs", songs.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", songs.getTotalPages());
        model.addAttribute("totalSongs", songs.getTotalElements());
        model.addAttribute("activePage", "songs");

        return "user/songs";
    }
    
    @PostMapping("/requests/{id}/mark-for-review")
	public String markForReview(@PathVariable Long id,
	                            @RequestParam(required = false) String comment,
	                            Principal principal) {

	    reqService.markForReview(id, comment, principal.getName());

	    return "redirect:/user/requests/songs";
	}
    
    @GetMapping("/requests/{id}/details")
    @ResponseBody
    public SongRequest getRequestDetails(@PathVariable Long id, Principal principal) {
        SongRequest req = reqService.getSongRequestForUser(id,
                userRepo.findByUsername(principal.getName()).get().getChannelName());

        req.getComments().size(); // force initialize to avoid LazyInitializationException
        return req;
    }

    @PostMapping("/requests/{id}/edit")
    public String editRequest(@PathVariable Long id,
                              @ModelAttribute SongRequest updated,
                              Principal principal) {
    	String channel = userRepo.findByUsername(principal.getName()).get().getChannelName();
        reqService.updateSongRequest(id, updated, channel);
        return "redirect:/user/songs/requests";
    }
    @GetMapping("/requests/{id}")
	public String viewRequest(@PathVariable Long id, Model model,Principal principal) {
    	model.addAttribute("username",principal.getName());
	    model.addAttribute("req", reqService.get(id));
	    return "fragments/request-view :: view";
	}
    @GetMapping("/{id}/comments")
	@ResponseBody
	public List<SongRequestComment> getComments(@PathVariable Long id, Principal principal) {
	    //reqService.validateUserAccess(id, principal.getName());
	    return commentService.getComments(id);
	}

	@PostMapping("/{id}/comment")
	@ResponseBody
	public String addComment(@PathVariable Long id,
	                         @RequestParam String message,
	                         Principal principal) {

	    boolean isAdmin = false; // user view only
	    commentService.addComment(id, message, principal.getName(), isAdmin);
	    return "OK";
	}
	@GetMapping("/requests/{id}/meta")
	public String getRequestMeta(@PathVariable Long id, Model model, Principal principal) {
	    model.addAttribute("req", reqService.getSongRequestForUser(id,
	            userRepo.findByUsername(principal.getName()).get().getChannelName()));
	    return "fragments/request-meta :: meta";
	}

}

