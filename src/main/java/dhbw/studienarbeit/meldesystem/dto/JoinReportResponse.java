package dhbw.studienarbeit.meldesystem.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinReportResponse {
    private Boolean success;
    private String message;
    private Integer totalHelpers;
}

