
package com.example.qr_attendance_system;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminSetupController {

    private final AdminRepository adminRepository;
    private final AdminService adminService;

    public AdminSetupController(
            AdminRepository adminRepository,
            AdminService adminService) {
        this.adminRepository = adminRepository;
        this.adminService = adminService;
    }

    @GetMapping("/setup-admin")
    public String setupPage(Model model) {
        if (adminRepository.count() > 0) {
            return "redirect:/admin-login";
        }
        return "setup-admin";
    }

    @PostMapping("/setup-admin")
    public String createFirstAdmin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            HttpSession session) {

        if (adminRepository.count() > 0) {
            return "redirect:/admin-login";
        }

        if (username.trim().isEmpty()) {
            model.addAttribute("error", "Enter a username.");
            return "setup-admin";
        }

        if (!password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,}$")) {
            model.addAttribute("error",
                    "Password must be at least 12 characters and include uppercase, lowercase, number and special character.");
            return "setup-admin";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "setup-admin";
        }

        try {
            adminService.createAdmin(username.trim(), password);
            session.setAttribute("adminUsername", username.trim());
            session.setAttribute("role", "ADMIN");
            return "redirect:/admin-dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "setup-admin";
        }
    }
}
