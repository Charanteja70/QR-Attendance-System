package com.example.qr_attendance_system;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Controller
public class HomeController {

    private final ClassSessionRepository classSessionRepository;
    private final QrCodeService qrCodeService;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public HomeController(
            ClassSessionRepository classSessionRepository,
            QrCodeService qrCodeService,
            TeacherRepository teacherRepository,
            PasswordEncoder passwordEncoder) {

        this.classSessionRepository = classSessionRepository;
        this.qrCodeService = qrCodeService;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/teacher-login")
    public String teacherLogin() {
        return "teacher-login";
    }

    @PostMapping("/teacher-login")
    public String checkTeacherLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession httpSession) {

        Teacher teacher = teacherRepository
                .findByTeacherId(username.trim())
                .orElse(null);

        if (teacher != null
                && passwordEncoder.matches(
                        password, teacher.getPassword())) {

            httpSession.setAttribute(
                    "teacherId", teacher.getTeacherId());

            httpSession.setAttribute("role", "TEACHER");

            return "redirect:/teacher-dashboard";
        }

        return "redirect:/teacher-login?error";
    }

    @GetMapping("/teacher-dashboard")
    public String teacherDashboard(HttpSession httpSession) {

        if (httpSession.getAttribute("teacherId") == null) {
            return "redirect:/teacher-login";
        }

        return "teacher-dashboard";
    }

    @GetMapping("/start-class")
    public String startClass(HttpSession httpSession) {

        if (httpSession.getAttribute("teacherId") == null) {
            return "redirect:/teacher-login";
        }

        return "start-class";
    }

    @PostMapping("/start-class")
    public String saveClass(
            @RequestParam String className,
            @RequestParam String subject,
            HttpSession httpSession) {

        String teacherId =
                (String) httpSession.getAttribute("teacherId");

        if (teacherId == null) {
            return "redirect:/teacher-login";
        }

        ClassSession session = new ClassSession();

        session.setClassName(className);
        session.setSubject(subject);
        session.setStartTime(LocalDateTime.now());
        session.setTeacherId(teacherId);
        session.setEnded(false);

        updateQrToken(session);

        classSessionRepository.save(session);

        return "redirect:/show-qr?id=" + session.getId();
    }

    @GetMapping("/show-qr")
    public String showQr(
            @RequestParam Long id,
            Model model,
            HttpSession httpSession) throws Exception {

        String teacherId =
                (String) httpSession.getAttribute("teacherId");

        if (teacherId == null) {
            return "redirect:/teacher-login";
        }

        ClassSession session = classSessionRepository
                .findById(id)
                .orElseThrow();

        if (!teacherId.equals(session.getTeacherId())) {
            return "redirect:/teacher-dashboard";
        }

        if (session.isEnded()) {
            return "redirect:/attendance-sessions";
        }

        LocalDateTime now = LocalDateTime.now();

        if (session.getQrExpiry() == null
                || !now.isBefore(session.getQrExpiry())) {

            updateQrToken(session);
            classSessionRepository.save(session);
        }

        // Railway public URL for student QR scanning
        String qrText =
                "https://kare-attendance.up.railway.app/scan?sessionId="
                + session.getId()
                + "&token="
                + session.getQrToken();

        String qrCode =
                qrCodeService.generateQrCode(qrText);

        long secondsLeft =
                ChronoUnit.SECONDS.between(
                        LocalDateTime.now(),
                        session.getQrExpiry());

        model.addAttribute("className", session.getClassName());
        model.addAttribute("subject", session.getSubject());
        model.addAttribute("qrCode", qrCode);
        model.addAttribute("secondsLeft", secondsLeft);
        model.addAttribute("sessionId", session.getId());

        return "qr-code";
    }

    @PostMapping("/end-class")
    public String endClass(
            @RequestParam Long id,
            HttpSession httpSession) {

        String teacherId =
                (String) httpSession.getAttribute("teacherId");

        if (teacherId == null) {
            return "redirect:/teacher-login";
        }

        ClassSession session = classSessionRepository
                .findById(id)
                .orElseThrow();

        if (!teacherId.equals(session.getTeacherId())) {
            return "redirect:/teacher-dashboard";
        }

        session.setEnded(true);
        session.setQrToken(null);
        session.setQrExpiry(LocalDateTime.now());

        classSessionRepository.save(session);

        return "redirect:/attendance-sessions";
    }

    @GetMapping("/teacher-logout")
    public String teacherLogout(HttpSession httpSession) {
        httpSession.invalidate();
        return "redirect:/teacher-login";
    }

    private void updateQrToken(ClassSession session) {

        LocalDateTime now = LocalDateTime.now();

        session.setQrToken(UUID.randomUUID().toString());
        session.setQrExpiry(now.plusSeconds(30));
    }
}