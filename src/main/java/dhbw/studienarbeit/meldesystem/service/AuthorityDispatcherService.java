package dhbw.studienarbeit.meldesystem.service;


import dhbw.studienarbeit.meldesystem.model.AuthorityMapping;
import dhbw.studienarbeit.meldesystem.model.Report;
import dhbw.studienarbeit.meldesystem.repository.AuthorityMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// ============= Authority Dispatcher Service =============
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorityDispatcherService {

    private final AuthorityMappingRepository authorityMappingRepository;
    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    @Value("${app.authority.email.from:noreply@citizenreport.com}")
    private String fromEmail;

    public void notifyAuthority(Report report) {
        log.info("Notifying authority for report {}", report.getId());

        AuthorityMapping authority = authorityMappingRepository
                .findFirstByCategoryAndPostalCode(report.getCategory(), report.getPostalCode())
                .orElseGet(() -> {
                    // Fallback: Suche ohne PLZ
                    return authorityMappingRepository.findByCategory(report.getCategory())
                            .stream()
                            .findFirst()
                            .orElse(null);
                });

        if (authority == null) {
            log.warn("No authority mapping found for category {} and postal code {}",
                    report.getCategory(), report.getPostalCode());
            return;
        }

        // Email-Benachrichtigung
        if (authority.getAuthorityEmail() != null) {
            sendEmailNotification(authority, report);
        }

        // API-Benachrichtigung (falls konfiguriert)
        if (authority.getApiEndpoint() != null) {
            sendApiNotification(authority, report);
        }
    }

    private void sendEmailNotification(AuthorityMapping authority, Report report) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(authority.getAuthorityEmail());
            message.setSubject("Neue Bürgermeldung: " + report.getTitle());
            //todo: adjust structure and team name if needed
            String body = String.format("""
                Sehr geehrte Damen und Herren,
                
                es wurde eine neue Bürgermeldung eingereicht:
                
                Titel: %s
                Kategorie: %s
                Beschreibung: %s
                
                Standort:
                Adresse: %s, %s %s
                Koordinaten: %.6f, %.6f
                %s
                
                Status: %s
                Gemeldet am: %s
                
                Bitte prüfen Sie diese Meldung und leiten Sie die erforderlichen Schritte ein.
                
                Mit freundlichen Grüßen,
                Ihr Citizen Report Team
                """,
                    report.getTitle(),
                    report.getCategory(),
                    report.getDescription() != null ? report.getDescription() : "Keine Beschreibung",
                    report.getStreet() != null ? report.getStreet() : "",
                    report.getPostalCode() != null ? report.getPostalCode() : "",
                    report.getCity() != null ? report.getCity() : "",
                    report.getLatitude(),
                    report.getLongitude(),
                    report.getLocationDescription() != null ?
                            "Genaue Ortsbeschreibung: " + report.getLocationDescription() : "",
                    report.getStatus(),
                    report.getCreatedAt()
            );

            message.setText(body);
            mailSender.send(message);

            log.info("Email notification sent to {}", authority.getAuthorityEmail());
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
        }
    }

    private void sendApiNotification(AuthorityMapping authority, Report report) {
        try {
            // Erstelle API-Payload
            var payload = new java.util.HashMap<String, Object>();
            payload.put("reportId", report.getId());
            payload.put("title", report.getTitle());
            payload.put("category", report.getCategory());
            payload.put("latitude", report.getLatitude());
            payload.put("longitude", report.getLongitude());
            payload.put("address", String.format("%s, %s %s",
                    report.getStreet(), report.getPostalCode(), report.getCity()));

            var headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            if (authority.getApiKey() != null) {
                headers.set("Authorization", "Bearer " + authority.getApiKey());
            }

            var entity = new org.springframework.http.HttpEntity<>(payload, headers);

            restTemplate.postForEntity(authority.getApiEndpoint(), entity, String.class);

            log.info("API notification sent to {}", authority.getApiEndpoint());
        } catch (Exception e) {
            log.error("Failed to send API notification", e);
        }
    }
}

// ============= File Storage Service =============
// ============= Geocoding Service =============
// ============= WebSocket Notification Service =============

// ============= Chat Service =============