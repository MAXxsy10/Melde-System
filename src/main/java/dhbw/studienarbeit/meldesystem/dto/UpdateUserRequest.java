package dhbw.studienarbeit.meldesystem.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @Size(min = 3, max = 50)
    private String nickname;

    @Min(1)
    @Max(100)
    private Integer notificationRadius;

    @Pattern(regexp = "DE|EN")
    private String languagePreference;

    private String fcmToken;
}
