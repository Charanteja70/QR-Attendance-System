
package com.example.qr_attendance_system;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    List<Attendance> findByRollNumber(String rollNumber);

    List<Attendance> findBySessionId(Long sessionId);

    boolean existsByRollNumberAndSessionId(
            String rollNumber,
            Long sessionId);

    boolean existsByDeviceIdAndSessionId(
            String deviceId,
            Long sessionId);
}