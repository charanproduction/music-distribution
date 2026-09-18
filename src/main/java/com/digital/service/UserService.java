package com.digital.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.digital.entity.User;
import com.digital.enums.PasswordChangeStatus;
import com.digital.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    public void register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("NORMAL");
        user.setEnabled(false);
        userRepo.save(user);
    }

    public List<User> findPendingUsers() {
        return userRepo.findAll().stream()
            .filter(u -> !u.isEnabled())
            .toList();
    }

    public void approveUser(Long id) {
        User u = userRepo.findById(id).orElseThrow();
        u.setEnabled(true);
        userRepo.save(u);
    }
    public void updateUserProfile(String username, User updated) {
        User user = userRepo.findByUsername(username).orElseThrow();
        user.setFullName(updated.getFullName());
        user.setContactNo(updated.getContactNo());
        user.setEmail(updated.getEmail());
        user.setAddress(updated.getAddress());
        userRepo.save(user);
    }   

    @Transactional
    public PasswordChangeStatus changePassword(String username,
                                               String currentPassword,
                                               String newPassword) {

        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return PasswordChangeStatus.USER_NOT_FOUND;

        if (!encoder.matches(currentPassword, user.getPassword())) {
            return PasswordChangeStatus.INVALID_CURRENT;
        }

        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);

        return PasswordChangeStatus.SUCCESS;
    }
}
