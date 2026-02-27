package dhbw.studienarbeit.meldesystem;


import dhbw.studienarbeit.meldesystem.model.AuthorityMapping;
import dhbw.studienarbeit.meldesystem.model.Category;
import dhbw.studienarbeit.meldesystem.model.Report;
import dhbw.studienarbeit.meldesystem.model.ReportStatus;
import dhbw.studienarbeit.meldesystem.repository.AuthorityMappingRepository;
import dhbw.studienarbeit.meldesystem.service.AuthorityDispatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorityDispatcherServiceTest {

    @Mock
    private AuthorityMappingRepository authorityMappingRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthorityDispatcherService authorityDispatcherService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

    private Report testReport;
    private AuthorityMapping testMapping;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authorityDispatcherService, "fromEmail", "test@citizenreport.com");

        testReport = Report.builder()
                .id(1L)
                .title("Broken Streetlight")
                .category(Category.DAMAGE)
                .description("Light is completely dark")
                .street("Main St")
                .postalCode("12345")
                .city("TestCity")
                .latitude(49.0)
                .longitude(8.0)
                .status(ReportStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();

        testMapping = AuthorityMapping.builder()
                .id(1L)
                .category(Category.DAMAGE)
                .postalCode("12345")
                .authorityName("City Council")
                .authorityEmail("council@test.com")
                .apiEndpoint("https://api.test.com/reports")
                .apiKey("secret-key")
                .build();
    }

    @Test
    void notifyAuthority_WithEmailAndApi_Success() {
        when(authorityMappingRepository.findFirstByCategoryAndPostalCode(Category.DAMAGE, "12345"))
                .thenReturn(Optional.of(testMapping));

        authorityDispatcherService.notifyAuthority(testReport);

        verify(mailSender, times(1)).send(mailCaptor.capture());
        SimpleMailMessage sentMail = mailCaptor.getValue();
        assertEquals("test@citizenreport.com", sentMail.getFrom());
        assertEquals("council@test.com", sentMail.getTo()[0]);
        assertTrue(sentMail.getText().contains("Broken Streetlight"));

        verify(restTemplate, times(1)).postForEntity(eq("https://api.test.com/reports"), httpEntityCaptor.capture(), eq(String.class));
        HttpEntity<?> sentEntity = httpEntityCaptor.getValue();
        assertEquals("Bearer secret-key", sentEntity.getHeaders().getFirst("Authorization"));
    }

    @Test
    void notifyAuthority_FallbackToCategory_Success() {
        when(authorityMappingRepository.findFirstByCategoryAndPostalCode(Category.DAMAGE, "12345"))
                .thenReturn(Optional.empty());

        testMapping.setApiEndpoint(null); // Test email-only fallback
        when(authorityMappingRepository.findByCategory(Category.DAMAGE))
                .thenReturn(List.of(testMapping));

        authorityDispatcherService.notifyAuthority(testReport);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    void notifyAuthority_NoMappingFound() {
        when(authorityMappingRepository.findFirstByCategoryAndPostalCode(Category.DAMAGE, "12345"))
                .thenReturn(Optional.empty());
        when(authorityMappingRepository.findByCategory(Category.DAMAGE))
                .thenReturn(List.of());

        authorityDispatcherService.notifyAuthority(testReport);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    void notifyAuthority_EmailFails_ShouldCatchException() {
        when(authorityMappingRepository.findFirstByCategoryAndPostalCode(Category.DAMAGE, "12345"))
                .thenReturn(Optional.of(testMapping));

        doThrow(new RuntimeException("Mail server down")).when(mailSender).send(any(SimpleMailMessage.class));

        // Should not throw an exception to the caller
        assertDoesNotThrow(() -> authorityDispatcherService.notifyAuthority(testReport));

        // API should still be called even if email fails
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any());
    }

    @Test
    void notifyAuthority_ApiFails_ShouldCatchException() {
        when(authorityMappingRepository.findFirstByCategoryAndPostalCode(Category.DAMAGE, "12345"))
                .thenReturn(Optional.of(testMapping));

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("API timeout"));

        // Should not throw an exception to the caller
        assertDoesNotThrow(() -> authorityDispatcherService.notifyAuthority(testReport));

        // Email should still have been sent
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}