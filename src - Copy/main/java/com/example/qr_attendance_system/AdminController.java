
package com.example.qr_attendance_system;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    private final AdminService adminService;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            AdminService adminService,
            TeacherRepository teacherRepository,
            PasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin-login")
    public String adminLogin() {
        return "admin-login";
    }

    @PostMapping("/admin-login")
    public String checkAdminLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        if (adminService.checkLogin(username, password)) {
            session.setAttribute("adminUsername", username);
            session.setAttribute("role", "ADMIN");
            return "redirect:/admin-dashboard";
        }

        model.addAttribute("error", "Invalid username or password");
        return "admin-login";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            return "redirect:/admin-login";
        }
        return "admin-dashboard";
    }

    @GetMapping("/add-teacher")
    public String addTeacherPage(HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            return "redirect:/admin-login";
        }
        return "add-teacher";
    }

    @PostMapping("/add-teacher")
    public String saveTeacher(
            @RequestParam String teacherId,
            @RequestParam String teacherName,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        if (!"ADMIN".equals(session.getAttribute("role"))) {
            return "redirect:/admin-login";
        }

        if (teacherRepository.existsByTeacherId(teacherId)) {
            model.addAttribute("error", "Teacher ID already exists");
            return "add-teacher";
        }

        if (!password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,}$")) {
            model.addAttribute("error",
                    "Password must be at least 12 characters with uppercase, lowercase, number and special character.");
            return "add-teacher";
        }

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);
        teacher.setTeacherName(teacherName);
        teacher.setPassword(passwordEncoder.encode(password));

        teacherRepository.save(teacher);

        model.addAttribute("message", "Teacher added successfully");
        return "add-teacher";
    }

    @GetMapping("/admin-logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin-login";
    }
}