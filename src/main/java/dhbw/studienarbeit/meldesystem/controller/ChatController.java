package dhbw.studienarbeit.meldesystem.controller;


import dhbw.studienarbeit.meldesystem.dto.ApiResponse;
import dhbw.studienarbeit.meldesystem.dto.ChatMessageDTO;
import dhbw.studienarbeit.meldesystem.dto.SendChatMessageRequest;
import dhbw.studienarbeit.meldesystem.security.CurrentUser;
import dhbw.studienarbeit.meldesystem.security.UserPrincipal;
import dhbw.studienarbeit.meldesystem.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/{reportId}/chat")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated() and !@authService.isAnonymous(#currentUser)")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessageDTO>> sendMessage(
            @PathVariable Long reportId,
            @Valid @RequestBody SendChatMessageRequest request,
            @CurrentUser UserPrincipal currentUser) {

        log.info("Sending chat message to report {} by user {}",
                reportId, currentUser.getId());

        ChatMessageDTO message = chatService.sendMessage(
                reportId, currentUser.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatMessageDTO>>> getMessages(
            @PathVariable Long reportId,
            @CurrentUser UserPrincipal currentUser) {

        List<ChatMessageDTO> messages = chatService.getMessages(
                reportId, currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(messages));
    }
}