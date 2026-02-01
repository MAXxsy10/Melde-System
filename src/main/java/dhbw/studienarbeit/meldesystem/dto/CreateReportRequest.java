package dhbw.studienarbeit.meldesystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import dhbw.studienarbeit.meldesystem.model.Category;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReportRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Latitude is required")
    @Min(-90)
    @Max(90)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(-180)
    @Max(180)
    private Double longitude;

    private String street;
    private String postalCode;
    private String city;

    @Size(max = 500)
    private String locationDescription;

    private Boolean isAnonymous = false;
}
