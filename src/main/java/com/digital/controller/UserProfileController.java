package com.digital.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.digital.entity.User;
import com.digital.enums.PasswordChangeStatus;
import com.digital.repository.UserRepository;
import com.digital.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProfileController {

    private final UserRepository userRepo;
    private final UserService userService;
   

    @GetMapping("/profile")
    public String userProfile(Model model, Principal principal) {
        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User updatedUser,
                                Principal principal,
                                Model model) {
        userService.updateUserProfile(principal.getName(), updatedUser);
        model.addAttribute("user", updatedUser);
        model.addAttribute("success", "Profile updated successfully!");
        return "user/profile";
    }

    
    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(Principal principal,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 Model model) {
        PasswordChangeStatus updated = userService.changePassword(
                principal.getName(),
                currentPassword,
                newPassword
        );
        model.addAttribute(updated != null ? "success" : "error",
                updated != null ? "Password updated!" : "Current password is incorrect!");
        return "user/change-password";
    }
    
}
