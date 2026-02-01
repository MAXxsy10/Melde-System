package dhbw.studienarbeit.meldesystem.dto;

import dhbw.studienarbeit.meldesystem.model.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportHistoryDTO {
    private Long id;
    private ReportStatus status;
    private String changedBy;
    private LocalDateTime timestamp;
    private String comment;
}