package dhbw.studienarbeit.meldesystem.service;


import dhbw.studienarbeit.meldesystem.dto.ChatMessageDTO;
import dhbw.studienarbeit.meldesystem.dto.SendChatMessageRequest;
import dhbw.studienarbeit.meldesystem.dto.UserBasicDTO;
import dhbw.studienarbeit.meldesystem.exceptions.ForbiddenException;
import dhbw.studienarbeit.meldesystem.exceptions.ResourceNotFoundException;
import dhbw.studienarbeit.meldesystem.model.ChatMessage;
import dhbw.studienarbeit.meldesystem.model.Report;
import dhbw.studienarbeit.meldesystem.model.User;
import dhbw.studienarbeit.meldesystem.repository.ChatMessageRepository;
import dhbw.studienarbeit.meldesystem.repository.HelperRepository;
import dhbw.studienarbeit.meldesystem.repository.ReportRepository;
import dhbw.studienarbeit.meldesystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final HelperRepository helperRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageDTO sendMessage(Long reportId, Long userId, SendChatMessageRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Prüfen ob User am Report beteiligt ist (Creator oder Helper)
        boolean isCreator = report.getCreatedBy() != null &&
                report.getCreatedBy().getId().equals(userId);
        boolean isHelper = helperRepository.existsByReportAndUser(report, user);

        if (!isCreator && !isHelper) {
            throw new ForbiddenException("You must be involved in this report to send messages");
        }

        ChatMessage message = ChatMessage.builder()
                .report(report)
                .user(user)
                .message(request.getMessage())
                .build();

        message = chatMessageRepository.save(message);

        // WebSocket-Benachrichtigung
        ChatMessageDTO dto = mapToDTO(message);
        messagingTemplate.convertAndSend("/topic/report/" + reportId + "/chat", dto);

        log.info("Chat message sent for report {} by user {}", reportId, userId);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessages(Long reportId, Long userId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Prüfen ob User Zugriff hat
        boolean isCreator = report.getCreatedBy() != null &&
                report.getCreatedBy().getId().equals(userId);
        boolean isHelper = helperRepository.existsByReportAndUser(report, user);

        if (!isCreator && !isHelper) {
            throw new ForbiddenException("You must be involved in this report to view messages");
        }

        return chatMessageRepository.findByReportOrderBySentAtAsc(report)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ChatMessageDTO mapToDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .reportId(message.getReport().getId())
                .user(UserBasicDTO.builder()
                        .id(message.getUser().getId())
                        .nickname(message.getUser().getNickname())
                        .isAnonymous(message.getUser().getIsAnonymous())
                        .build())
                .message(message.getMessage())
                .sentAt(message.getSentAt())
                .build();
    }
}