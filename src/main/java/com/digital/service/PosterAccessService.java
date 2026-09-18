package com.digital.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.digital.entity.User;
import com.digital.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PosterAccessService {

    private final AppSettingsService appSettingsService;
    private final UserRepository userRepo;

    public boolean canGeneratePoster(String username) {
        // Global toggle OFF => nobody can use except admin (optional – you decide)
        if (!appSettingsService.isPosterFeatureEnabled()) {
            // If you want admins to still generate even when OFF:
            return userRepo.findByUsername(username)
                    .filter(this::isAdmin)
                    .isPresent();
        }

        return userRepo.findByUsername(username)
                .map(this::canGenerateForUser)
                .orElse(false);
    }

    private boolean canGenerateForUser(User user) {
        // Admins always allowed
        if (isAdmin(user)) return true;

        // Normal users must be allowed explicitly
        return user.isPosterAllowed();
    }

    private boolean isAdmin(User u) {
        // adjust to your actual role mapping
        return u.getRole().equalsIgnoreCase("ADMIN");
               
    }
}
