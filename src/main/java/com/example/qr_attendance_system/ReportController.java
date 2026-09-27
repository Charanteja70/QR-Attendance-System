package com.example.qr_attendance_system;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ReportController {

    private final ClassSessionRepository classSessionRepository;
    private final AttendanceRepository attendanceRepository;

    public ReportController(
            ClassSessionRepository classSessionRepository,
            AttendanceRepository attendanceRepository) {

        this.classSessionRepository = classSessionRepository;
        this.attendanceRepository = attendanceRepository;
    }

    // View attendance report for one class session
    @GetMapping("/attendance-report")
    public String attendanceReport(
            @RequestParam Long sessionId,
            Model model) {

        ClassSession session = classSessionRepository
                .findById(sessionId)
                .orElseThrow();

        List<Attendance> attendanceList =
                attendanceRepository.findBySessionId(sessionId);

        model.addAttribute("className", session.getClassName());
        model.addAttribute("subject", session.getSubject());
        model.addAttribute("attendanceList", attendanceList);
        model.addAttribute("totalStudents", attendanceList.size());

        return "attendance-report";
    }

    // Show all class sessions
    @GetMapping("/attendance-sessions")
    public String attendanceSessions(Model model) {

        model.addAttribute(
                "sessions",
                classSessionRepository.findAll()
        );

        return "attendance-sessions";
    }
}