
package com.example.qr_attendance_system;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TeacherPasswordResetController {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherPasswordResetController(
            TeacherRepository teacherRepository,
            PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/reset-teacher-password")
    public String resetPage(HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            return "redirect:/admin-login";
        }

        return "reset-teacher-password";
    }

    @PostMapping("/reset-teacher-password")
    public String resetPassword(
            @RequestParam String teacherId,
            @RequestParam String newPassword,
            HttpSession session,
            Model model) {

        if (!"ADMIN".equals(session.getAttribute("role"))) {
            return "redirect:/admin-login";
        }

        if (!newPassword.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,}$")) {
            model.addAttribute("error",
                    "Password must be at least 12 characters with uppercase, lowercase, number and special character.");
            return "reset-teacher-password";
        }

        Teacher teacher = teacherRepository
                .findByTeacherId(teacherId.trim())
                .orElse(null);

        if (teacher == null) {
            model.addAttribute("error", "Teacher ID not found.");
            return "reset-teacher-password";
        }

        teacher.setPassword(passwordEncoder.encode(newPassword));
        teacherRepository.save(teacher);

        model.addAttribute("message",
                "Teacher password reset successfully.");

        return "reset-teacher-password";
    }
}