package com.digital.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.digital.entity.SongRequest;
import com.digital.entity.SongRequestComment;
import com.digital.enums.RequestStatus;
import com.digital.enums.SongRequestStatus;
import com.digital.service.SongRequestService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/song-requests")
@RequiredArgsConstructor
public class AdminSongRequestController {

    private final SongRequestService songRequestService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) SongRequestStatus status,
                       Model model) {

        List<SongRequestStatus> queueStatuses = (status != null)
                ? List.of(status)
                : List.of(
                    SongRequestStatus.PENDING_REVIEW,
                    SongRequestStatus.MARKED_FOR_REVIEW,
                    SongRequestStatus.ACTION_REQUIRED
                  );

        Page<SongRequest> reqPage = songRequestService.findAdminQueue(queueStatuses, page, 10);

        model.addAttribute("requestPage", reqPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("allStatuses", SongRequestStatus.values());
        model.addAttribute("selectedStatus", status);

        return "admin/song-requests";
    }

    @GetMapping("/{id}")
    public String viewModal(@PathVariable Long id, Model model) {
        SongRequest req = songRequestService.getRequest(id);
        List<SongRequestComment> comments = songRequestService.getComments(id);

        model.addAttribute("req", req);
        model.addAttribute("comments", comments);
        return "admin/fragments/song-request-view :: modalContent";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam SongRequestStatus newStatus,
                               @RequestParam(required = false) String comment,
                               Principal principal) {

        songRequestService.adminSetStatus(id, newStatus, comment, principal.getName());
        return "redirect:/admin/song-requests?updated";
    }

    @PostMapping("/{id}/comment")
    public String addAdminComment(@PathVariable Long id,
                                  @RequestParam String message,
                                  Principal principal) {
        songRequestService.addComment(id, "ADMIN", principal.getName(), message);
        return "redirect:/admin/song-requests?commented";
    }
}
