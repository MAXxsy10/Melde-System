package dhbw.studienarbeit.meldesystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "authority_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorityMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(nullable = false)
    private String authorityName;

    @Column(nullable = false)
    private String authorityEmail;

    private String apiEndpoint;

    private String apiKey;
}