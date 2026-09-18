package com.digital.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.digital.repository.UserRepository;
import com.digital.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/pending")
    public String pendingUsers(Model model) {
        model.addAttribute("pendingUsers", userService.findPendingUsers());
        return "admin/pending-users";
    }

    @PostMapping("/users/{id}/approve")
    public String approve(@PathVariable Long id) {
        userService.approveUser(id);
        return "redirect:/admin/users/pending?approved";
    }
}

