
package com.example.qr_attendance_system;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createAdmin(String username, String password) {

        if (adminRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Admin already exists");
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));

        adminRepository.save(admin);
    }

    public boolean checkLogin(String username, String password) {

        return adminRepository.findByUsername(username)
                .map(admin -> passwordEncoder.matches(
                        password, admin.getPassword()))
                .orElse(false);
    }
}