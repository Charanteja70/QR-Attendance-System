package com.example.qr_attendance_system;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByRollNumber(String rollNumber);

    Optional<Student> findByRollNumber(String rollNumber);

    List<Student> findByStatus(String status);

    Optional<Student> findByRollNumberAndStatus(
            String rollNumber, String status);

    List<Student> findByTeacherId(String teacherId);

    List<Student> findByTeacherIdAndStatus(
            String teacherId, String status);
}