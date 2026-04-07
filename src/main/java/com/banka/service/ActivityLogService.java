package com.banka.service;

import com.banka.model.ActivityLog;
import com.banka.model.LoginHistory;
import com.banka.model.User;
import com.banka.repository.ActivityLogRepository;
import com.banka.repository.LoginHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AKTİVİTE LOG SERVİSİ
 * 
 * Kullanıcı aktivitelerini kaydetme ve sorgulama.
 * Veri bilimi analizleri için veri toplar.
 */
@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    /**
     * Yeni aktivite logu kaydet
     */
    public void logActivity(User user, String actionType, String pageUrl,
                            String ipAddress, String userAgent, String sessionId) {
        ActivityLog log = new ActivityLog(user, actionType, pageUrl, ipAddress, userAgent, sessionId);
        activityLogRepository.save(log);
    }

    /**
     * Başarılı giriş kaydı
     */
    public LoginHistory logSuccessfulLogin(User user, String ipAddress, String deviceType) {
        LoginHistory history = new LoginHistory();
        history.setUser(user);
        history.setIpAddress(ipAddress);
        history.setDeviceType(deviceType);
        history.setLoginSuccess(true);
        return loginHistoryRepository.save(history);
    }

    /**
     * Başarısız giriş kaydı
     */
    public void logFailedLogin(User user, String ipAddress, String reason) {
        LoginHistory history = new LoginHistory();
        history.setUser(user);
        history.setIpAddress(ipAddress);
        history.setLoginSuccess(false);
        history.setFailureReason(reason);
        loginHistoryRepository.save(history);
    }

    /**
     * Çıkış kaydı - logout zamanını güncelle
     */
    public void logLogout(User user) {
        List<LoginHistory> histories = loginHistoryRepository.findByUserIdOrderByLoginTimeDesc(user.getId());
        if (!histories.isEmpty()) {
            LoginHistory lastLogin = histories.get(0);
            if (lastLogin.getLogoutTime() == null) {
                lastLogin.setLogoutTime(LocalDateTime.now());
                loginHistoryRepository.save(lastLogin);
            }
        }
    }

    // ==================== ANALİTİK SORGULAR ====================

    public List<Object[]> getActionTypeCounts() {
        return activityLogRepository.countByActionType();
    }

    public List<Object[]> getDeviceTypeCounts() {
        return activityLogRepository.countByDeviceType();
    }

    public List<Object[]> getHourlyActivity() {
        return activityLogRepository.countByHour();
    }

    public List<Object[]> getDailyActivity() {
        return activityLogRepository.countByDay();
    }

    public List<Object[]> getPageVisitCounts() {
        return activityLogRepository.countByPageUrl();
    }

    public List<Object[]> getDailySuccessfulLogins() {
        return loginHistoryRepository.countSuccessfulLoginsByDay();
    }

    public List<Object[]> getDailyFailedLogins() {
        return loginHistoryRepository.countFailedLoginsByDay();
    }

    public List<Object[]> getHourlyLogins() {
        return loginHistoryRepository.countLoginsByHour();
    }

    public long getTotalLogs() {
        return activityLogRepository.count();
    }

    public long getTotalSuccessfulLogins() {
        return loginHistoryRepository.countByLoginSuccessTrue();
    }

    public long getTotalFailedLogins() {
        return loginHistoryRepository.countByLoginSuccessFalse();
    }
}
