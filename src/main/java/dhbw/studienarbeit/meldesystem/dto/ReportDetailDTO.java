package dhbw.studienarbeit.meldesystem.dto;

import dhbw.studienarbeit.meldesystem.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDetailDTO {
    private Long id;
    private String title;
    private String description;
    private Category category;
    private String photoUrl;
    private LocationDTO location;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserBasicDTO createdBy;
    private Boolean isAnonymousReport;
    private List<HelperDTO> helpers;
    private List<ReportHistoryDTO> history;
    private Boolean currentUserIsHelper;
    private Boolean currentUserIsCreator;
}
