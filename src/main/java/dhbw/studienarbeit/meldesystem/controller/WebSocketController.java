package dhbw.studienarbeit.meldesystem.controller;


import dhbw.studienarbeit.meldesystem.dto.ChatMessageDTO;
import dhbw.studienarbeit.meldesystem.dto.SendChatMessageRequest;
import dhbw.studienarbeit.meldesystem.security.UserPrincipal;
import dhbw.studienarbeit.meldesystem.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatService chatService;

    @MessageMapping("/report/{reportId}/chat")
    @SendTo("/topic/report/{reportId}/chat")
    public ChatMessageDTO handleChatMessage(
            @DestinationVariable Long reportId,
            SendChatMessageRequest request,
            @Payload UserPrincipal user) {

        log.info("WebSocket chat message received for report {}", reportId);
        return chatService.sendMessage(reportId, user.getId(), request);
    }
}
