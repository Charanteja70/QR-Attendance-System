
package com.example.qr_attendance_system;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class AttendanceController {

    private final ClassSessionRepository classSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceController(
            ClassSessionRepository classSessionRepository,
            AttendanceRepository attendanceRepository,
            StudentRepository studentRepository) {

        this.classSessionRepository = classSessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    // ================= SCAN QR =================

    @GetMapping("/scan")
    public String scanQr(
            @RequestParam Long sessionId,
            @RequestParam String token,
            Model model) {

        ClassSession classSession = classSessionRepository
                .findById(sessionId)
                .orElse(null);

        if (classSession == null) {
            model.addAttribute("message", "Invalid class session");
            return "attendance-result";
        }

        if (classSession.isEnded()) {
            model.addAttribute("message",
                    "This class has ended. Attendance is closed.");
            return "attendance-result";
        }

        if (!token.equals(classSession.getQrToken())) {
            model.addAttribute("message",
                    "QR code is invalid or expired");
            return "attendance-result";
        }

        if (classSession.getQrExpiry() == null
                || !LocalDateTime.now().isBefore(
                        classSession.getQrExpiry())) {
            model.addAttribute("message", "QR code has expired");
            return "attendance-result";
        }

        model.addAttribute("sessionId", sessionId);
        model.addAttribute("className", classSession.getClassName());
        model.addAttribute("subject", classSession.getSubject());
        model.addAttribute("token", token);

        return "attendance";
    }

    // ================= MARK ATTENDANCE =================

    @PostMapping("/mark-attendance")
    public String markAttendance(
            @RequestParam Long sessionId,
            @RequestParam String token,
            @RequestParam String rollNumber,
            @RequestParam(required = false) String deviceId,
            Model model) {

        ClassSession classSession = classSessionRepository
                .findById(sessionId)
                .orElse(null);

        if (classSession == null) {
            model.addAttribute("message", "Invalid class session");
            return "attendance-result";
        }

        if (classSession.isEnded()) {
            model.addAttribute("message",
                    "This class has ended. Attendance is closed.");
            return "attendance-result";
        }

        if (!token.equals(classSession.getQrToken())) {
            model.addAttribute("message",
                    "QR code is invalid or expired");
            return "attendance-result";
        }

        if (classSession.getQrExpiry() == null
                || !LocalDateTime.now().isBefore(
                        classSession.getQrExpiry())) {
            model.addAttribute("message", "QR code has expired");
            return "attendance-result";
        }

        // Check the device ID received from the attendance form.
        if (deviceId == null || deviceId.trim().isEmpty()) {
            model.addAttribute("message",
                    "Device ID is missing. Refresh the attendance page "
                    + "and try again.");
            return "attendance-result";
        }

        deviceId = deviceId.trim();
        rollNumber = rollNumber.trim();

        Student student = studentRepository
                .findByRollNumber(rollNumber)
                .orElse(null);

        if (student == null) {
            model.addAttribute("message",
                    "Student not registered. Please register first.");
            return "attendance-result";
        }

        if (!"APPROVED".equalsIgnoreCase(student.getStatus())) {
            model.addAttribute("message",
                    "Your registration is not approved. "
                    + "Please contact your teacher.");
            return "attendance-result";
        }

        if (student.getClassName() == null
                || classSession.getClassName() == null
                || !student.getClassName().trim()
                        .equalsIgnoreCase(
                                classSession.getClassName().trim())) {
            model.addAttribute("message",
                    "This student is not registered in this class.");
            return "attendance-result";
        }

        // Prevent duplicate attendance for the same student/session.
        if (attendanceRepository.existsByRollNumberAndSessionId(
                student.getRollNumber(), sessionId)) {

            model.addAttribute("message",
                    "Attendance already marked for "
                    + student.getStudentName());

            return "attendance-result";
        }

        // Prevent one device from marking attendance for multiple students.
        if (attendanceRepository.existsByDeviceIdAndSessionId(
                deviceId, sessionId)) {

            model.addAttribute("message",
                    "This device has already marked attendance "
                    + "for this class session.");

            return "attendance-result";
        }

        Attendance attendance = new Attendance();

        attendance.setStudentName(student.getStudentName());
        attendance.setRollNumber(student.getRollNumber());
        attendance.setSessionId(sessionId);
        attendance.setDeviceId(deviceId);
        attendance.setAttendanceTime(LocalDateTime.now());

        attendanceRepository.save(attendance);

        model.addAttribute("message",
                "Attendance marked successfully for "
                + student.getStudentName());

        return "attendance-result";
    }
}
