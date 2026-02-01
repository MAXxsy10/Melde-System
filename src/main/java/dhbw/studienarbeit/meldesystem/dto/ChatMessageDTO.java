package dhbw.studienarbeit.meldesystem.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ============= Chat DTOs =============
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long id;
    private Long reportId;
    private UserBasicDTO user;
    private String message;
    private LocalDateTime sentAt;
}
