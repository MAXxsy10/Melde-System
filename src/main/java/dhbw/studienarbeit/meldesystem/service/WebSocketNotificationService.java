package dhbw.studienarbeit.meldesystem.service;


import dhbw.studienarbeit.meldesystem.model.Report;
import dhbw.studienarbeit.meldesystem.model.User;
import dhbw.studienarbeit.meldesystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public void notifyNewReport(Report report) {
        try {
            var notification = new java.util.HashMap<String, Object>();
            notification.put("type", "NEW_REPORT");
            notification.put("reportId", report.getId());
            notification.put("title", report.getTitle());
            notification.put("category", report.getCategory());
            notification.put("latitude", report.getLatitude());
            notification.put("longitude", report.getLongitude());
            notification.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/reports", notification);

            log.info("WebSocket notification sent for new report {}", report.getId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }
    }

    public void notifyHelperJoined(Report report, User helper) {
        try {
            var notification = new java.util.HashMap<String, Object>();
            notification.put("type", "HELPER_JOINED");
            notification.put("reportId", report.getId());
            notification.put("helperName", helper.getNickname());
            notification.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/report/" + report.getId(), notification);

            log.info("WebSocket notification sent for helper joined report {}", report.getId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }
    }

    public void notifyStatusUpdate(Report report) {
        try {
            var notification = new java.util.HashMap<String, Object>();
            notification.put("type", "STATUS_UPDATE");
            notification.put("reportId", report.getId());
            notification.put("newStatus", report.getStatus());
            notification.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/report/" + report.getId(), notification);
            messagingTemplate.convertAndSend("/topic/reports", notification);

            log.info("WebSocket notification sent for status update report {}", report.getId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }
    }
}