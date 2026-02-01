package dhbw.studienarbeit.meldesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String nickname;
    private Boolean isAnonymous;
    private Boolean verified;
    private Integer notificationRadius;
    private String languagePreference;
    private LocalDateTime createdAt;
}
