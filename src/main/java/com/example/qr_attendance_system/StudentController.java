
package com.example.qr_attendance_system;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Controller
public class StudentController {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentController(
            StudentRepository studentRepository,
            AttendanceRepository attendanceRepository,
            TeacherRepository teacherRepository,
            PasswordEncoder passwordEncoder) {

        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ================= STUDENT REGISTRATION =================

    @GetMapping("/register-student")
    public String showRegistrationPage() {
        return "register-student";
    }

    @PostMapping("/register-student")
    public String registerStudent(
            @RequestParam String studentName,
            @RequestParam String rollNumber,
            @RequestParam String className,
            @RequestParam String department,
            @RequestParam String teacherId,
            @RequestParam String password,
            Model model) {

        String name = studentName.trim();
        String roll = rollNumber.trim();
        String studentClass = className.trim();
        String studentDepartment = department.trim();
        String selectedTeacherId = teacherId.trim();

        if (name.isEmpty()
                || roll.isEmpty()
                || studentClass.isEmpty()
                || studentDepartment.isEmpty()
                || selectedTeacherId.isEmpty()) {

            model.addAttribute("message",
                    "Please fill in all fields.");

            return "register-student";
        }

        if (password == null || password.length() < 8) {
            model.addAttribute("message",
                    "Password must contain at least 8 characters.");

            return "register-student";
        }

        Optional<Teacher> teacher =
                teacherRepository.findByTeacherId(selectedTeacherId);

        if (teacher.isEmpty()) {
            model.addAttribute("message",
                    "Invalid Teacher ID. Please check with your teacher.");

            return "register-student";
        }

        if (studentRepository.findByRollNumber(roll).isPresent()) {
            model.addAttribute("message",
                    "This register number is already registered.");

            return "register-student";
        }

        Student student = new Student();

        student.setStudentName(name);
        student.setRollNumber(roll);
        student.setClassName(studentClass);
        student.setDepartment(studentDepartment);
        student.setTeacherId(selectedTeacherId);
        student.setPassword(passwordEncoder.encode(password));
        student.setStatus("PENDING");

        studentRepository.save(student);

        model.addAttribute("message",
                "Registration successful. Please log in.");

        return "student-login";
    }

    // ================= STUDENT LOGIN =================

    @GetMapping("/student-login")
    public String showStudentLoginPage() {
        return "student-login";
    }

    @PostMapping("/student-login")
    public String studentLogin(
            @RequestParam String rollNumber,
            @RequestParam String password,
            HttpSession httpSession,
            Model model) {

        Student student = studentRepository
                .findByRollNumber(rollNumber.trim())
                .orElse(null);

        if (student == null
                || student.getPassword() == null
                || !passwordEncoder.matches(
                        password, student.getPassword())) {

            model.addAttribute("message",
                    "Invalid register number or password.");

            return "student-login";
        }

        httpSession.setAttribute(
                "studentRollNumber", student.getRollNumber());

        if ("APPROVED".equalsIgnoreCase(student.getStatus())) {
            return "redirect:/student-dashboard";
        }

        return "redirect:/student-status";
    }

    // ================= STUDENT STATUS =================

    @GetMapping("/student-status")
    public String showStudentStatusPage(
            HttpSession httpSession,
            Model model) {

        String rollNumber = (String) httpSession.getAttribute(
                "studentRollNumber");

        if (rollNumber == null) {
            return "redirect:/student-login";
        }

        Student student = studentRepository
                .findByRollNumber(rollNumber)
                .orElse(null);

        if (student == null) {
            httpSession.removeAttribute("studentRollNumber");
            return "redirect:/student-login";
        }

        model.addAttribute("student", student);

        return "student-status";
    }

    @PostMapping("/check-student-status")
    public String checkStudentStatus(HttpSession httpSession) {

        if (httpSession.getAttribute("studentRollNumber") == null) {
            return "redirect:/student-login";
        }

        return "redirect:/student-status";
    }

    // ================= STUDENT DASHBOARD =================

    @GetMapping("/student-dashboard")
    public String showStudentDashboard(
            HttpSession httpSession,
            Model model) {

        String rollNumber = (String) httpSession.getAttribute(
                "studentRollNumber");

        if (rollNumber == null) {
            return "redirect:/student-login";
        }

        Student student = studentRepository
                .findByRollNumber(rollNumber)
                .orElse(null);

        if (student == null) {
            httpSession.removeAttribute("studentRollNumber");
            return "redirect:/student-login";
        }

        if (!"APPROVED".equalsIgnoreCase(student.getStatus())) {
            return "redirect:/student-status";
        }

        List<Attendance> attendanceList =
                attendanceRepository.findByRollNumber(rollNumber);

        model.addAttribute("student", student);
        model.addAttribute("attendanceList", attendanceList);
        model.addAttribute("totalAttended", attendanceList.size());

        return "student-dashboard";
    }

    // ================= STUDENT LOGOUT =================

    @GetMapping("/student-logout")
    public String studentLogout(HttpSession httpSession) {

        httpSession.removeAttribute("studentRollNumber");

        return "redirect:/student-login";
    }

    // ================= TEACHER STUDENT REQUESTS =================

    @GetMapping("/student-requests")
    public String showStudentRequests(
            HttpSession httpSession,
            Model model) {

        String teacherId = (String) httpSession.getAttribute("teacherId");

        if (teacherId == null) {
            return "redirect:/teacher-login";
        }

        List<Student> pendingStudents =
                studentRepository.findByTeacherIdAndStatus(
                        teacherId, "PENDING");

        model.addAttribute("students", pendingStudents);

        return "student-requests";
    }

    // ================= APPROVE STUDENT =================

    @PostMapping("/approve-student/{id}")
    public String approveStudent(
            @PathVariable Long id,
            HttpSession httpSession) {

        String teacherId = (String) httpSession.getAttribute("teacherId");

        if (teacherId == null) {
            return "redirect:/teacher-login";
        }

        Optional<Student> optionalStudent =
                studentRepository.findById(id);

        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();

            if (teacherId.equals(student.getTeacherId())
                    && "PENDING".equalsIgnoreCase(student.getStatus())) {

                student.setStatus("APPROVED");
                studentRepository.save(student);
            }
        }

        return "redirect:/student-requests";
    }

    // ================= REJECT STUDENT =================

    @PostMapping("/reject-student/{id}")
    public String rejectStudent(
            @PathVariable Long id,
            HttpSession httpSession) {

        String teacherId = (String) httpSession.getAttribute("teacherId");

        if (teacherId == null) {
            return "redirect:/teacher-login";
        }

        Optional<Student> optionalStudent =
                studentRepository.findById(id);

        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();

            if (teacherId.equals(student.getTeacherId())
                    && "PENDING".equalsIgnoreCase(student.getStatus())) {

                student.setStatus("REJECTED");
                studentRepository.save(student);
            }
        }

        return "redirect:/student-requests";
    }
}
