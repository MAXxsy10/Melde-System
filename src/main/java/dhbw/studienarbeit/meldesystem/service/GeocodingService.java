package dhbw.studienarbeit.meldesystem.service;


import dhbw.studienarbeit.meldesystem.dto.LocationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeocodingService {

    private final RestTemplate restTemplate;

    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/reverse";

    public LocationDTO reverseGeocode(Double latitude, Double longitude) {
        try {
            String url = String.format("%s?format=json&lat=%.6f&lon=%.6f&addressdetails=1",
                    NOMINATIM_API, latitude, longitude);

            var response = restTemplate.getForObject(url, java.util.Map.class);

            if (response != null && response.containsKey("address")) {
                @SuppressWarnings("unchecked")
                var address = (java.util.Map<String, String>) response.get("address");

                return LocationDTO.builder()
                        .latitude(latitude)
                        .longitude(longitude)
                        .street(address.getOrDefault("road", null))
                        .postalCode(address.getOrDefault("postcode", null))
                        .city(address.getOrDefault("city",
                                address.getOrDefault("town",
                                        address.getOrDefault("village", null))))
                        .build();
            }
        } catch (Exception e) {
            log.error("Geocoding failed for coordinates: {}, {}", latitude, longitude, e);
        }

        // Return coordinates only if geocoding fails
        return LocationDTO.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
