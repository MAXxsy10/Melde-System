package dhbw.studienarbeit.meldesystem.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendChatMessageRequest {
    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;
}




