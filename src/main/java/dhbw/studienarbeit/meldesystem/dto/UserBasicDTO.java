package dhbw.studienarbeit.meldesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBasicDTO {
    private Long id;
    private String nickname;
    private Boolean isAnonymous;
}
