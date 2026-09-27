
package com.example.qr_attendance_system;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByRollNumber(String rollNumber);

    Optional<Student> findByRollNumber(String rollNumber);

    // Find students based on registration status
    List<Student> findByStatus(String status);

    // Find a student by roll number and status
    Optional<Student> findByRollNumberAndStatus(
            String rollNumber, String status);
}