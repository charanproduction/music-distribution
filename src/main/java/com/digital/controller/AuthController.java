package com.digital.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.digital.entity.User;
import com.digital.service.UserService;
import com.digital.repository.UserRepository;

@Controller
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepo;

    public AuthController(UserService userService, UserRepository userRepo) {
        this.userService = userService;
        this.userRepo = userRepo;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute("user") User user) {
        userService.register(user);
        return "redirect:/login?registered";
    }

    @GetMapping("/home")
    public String home(Principal principal) {
        // you can redirect based on role if you want
        User u = userRepo.findByUsername(principal.getName()).orElseThrow();
        if ("ADMIN".equals(u.getRole())) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/user/dashboard";
        }
    }
}
