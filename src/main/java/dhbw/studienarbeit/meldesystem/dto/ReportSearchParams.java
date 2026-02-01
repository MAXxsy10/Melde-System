package dhbw.studienarbeit.meldesystem.dto;

import dhbw.studienarbeit.meldesystem.model.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSearchParams {
    private Double latitude;
    private Double longitude;
    private Integer radius; // in km
    private Category category;
    private ReportStatus status;
    private Boolean myReports; // only ones own Reports
    private Boolean helpingReports;

    @Min(0)
    private Integer page = 0;

    @Min(1)
    @Max(100)
    private Integer size = 20;
}
