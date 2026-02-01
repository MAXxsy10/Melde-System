package dhbw.studienarbeit.meldesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDTO {
    private Double latitude;
    private Double longitude;
    private String street;
    private String postalCode;
    private String city;
    private String locationDescription;
    private String fullAddress;
}
