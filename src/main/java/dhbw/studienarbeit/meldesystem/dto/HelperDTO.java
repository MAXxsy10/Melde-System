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
public class HelperDTO {
    private Long id;
    private UserBasicDTO user;
    private LocalDateTime joinedAt;
}