package org.example.qlttngoaingu.scheduler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.example.qlttngoaingu.entity.Session;
import org.example.qlttngoaingu.repository.SessionRepository;
import org.example.qlttngoaingu.service.enums.SessionStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler tu dong cap nhat trang thai buoi hoc
 * 3 trang thai: Chua hoc, Da hoc, Da huy
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionScheduler {

    private final SessionRepository sessionRepository;

   
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void updateSessionStatus() {
        log.info("Running scheduled task: update session status");
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        // Chi chay trong khung gio 7:00 - 20:30
        if (now.isBefore(LocalTime.of(7, 0)) || now.isAfter(LocalTime.of(20, 30))) {
            log.debug("Outside working hours (7:00-20:30), skipping session status update");
            return;
        }
        
        // Lay tat ca buoi hoc cua hom nay
        List<Session> todaySessions = sessionRepository.findBySessionDate(today);
        
        int updated = 0;
        for (Session session : todaySessions) {
            String currentStatus = session.getStatus();
            
            // Neu da huy thi khong thay doi
            if (SessionStatus.Canceled.name().equals(currentStatus) ) {
                continue;
            }
            
            // Neu chua hoc va da toi gio hoc thi chuyen thanh da hoc
            if ((SessionStatus.NotCompleted.name().equals(currentStatus) )
                    && shouldMarkAsCompleted(session, now)) {
                session.setStatus(SessionStatus.Completed.name());
                sessionRepository.save(session);
                updated++;
                log.info("Updated session {} from '{}' to 'Da hoc' (date: {}, class: {})", 
                        session.getSessionId(), currentStatus, 
                        session.getSessionDate(), 
                        session.getCourseClass() != null ? session.getCourseClass().getClassName() : "N/A");
            }
        }
        
        log.info("Completed scheduled task: updated {} session statuses", updated);
    }

    /**
     * Kiem tra co nen danh dau buoi hoc la da hoc khong
     * Da hoc khi: da toi gio bat dau hoc
     */
    private boolean shouldMarkAsCompleted(Session session, LocalTime now) {
        LocalTime classStartTime = getClassStartTime(session);
        // Da toi gio hoc hoac qua gio hoc
        return now.isAfter(classStartTime) || now.equals(classStartTime);
    }

    /**
     * Lay gio bat dau tu lop hoc
     * Neu khong co thong tin, mac dinh 8:00
     */
    private LocalTime getClassStartTime(Session session) {
        if (session.getCourseClass() != null && session.getCourseClass().getStartTime() != null) {
            return session.getCourseClass().getStartTime();
        }
        // Mac dinh 8:00 sang
        return LocalTime.of(8, 0);
    }
}
