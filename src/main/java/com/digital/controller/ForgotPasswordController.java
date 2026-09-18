package com.digital.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.digital.entity.PasswordResetToken;
import com.digital.entity.User;
import com.digital.repository.PasswordResetTokenRepository;
import com.digital.repository.UserRepository;

@Controller
public class ForgotPasswordController {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder encoder;

    public ForgotPasswordController(UserRepository userRepo, PasswordResetTokenRepository tokenRepo,
                                    PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.tokenRepo = tokenRepo;
        this.encoder = encoder;
    }

    @GetMapping("/forgot-password")
    public String forgotPage() {
        return "forgot-password";
    }

    @PostMapping("/send-reset-link")
    public String sendReset(@RequestParam String email) {

        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null)
            return "redirect:/forgot-password?notfound";

        String token = UUID.randomUUID().toString();

        PasswordResetToken reset = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiry(LocalDateTime.now().plusHours(1))
                .build();

        tokenRepo.save(reset);

        // TODO: Send email in production
        System.out.println("RESET LINK: http://localhost:8080/reset-password?token=" + token);

        return "redirect:/forgot-password?sent";
    }

    @GetMapping("/reset-password")
    public String resetPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String password) {

        PasswordResetToken prt = tokenRepo.findByToken(token).orElse(null);

        if (prt == null || prt.getExpiry().isBefore(LocalDateTime.now()))
            return "redirect:/forgot-password?expired";

        User user = prt.getUser();
        user.setPassword(encoder.encode(password));

        userRepo.save(user);
        tokenRepo.delete(prt);

        return "redirect:/login?resetSuccess";
    }
}
