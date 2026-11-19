package com.reecefoundation.reecefoundation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name="organisation_contact_details")
public class OrganisationContactDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name="name", length=50)
    private String name;

    @Column(name="address", length=255)
    private String address;

    @Column(name="email", length=255)
    private String email;

    @Column(name="telephone", length = 30)
    private String telephone;

    @Column(name="job_title", length = 50)
    private String jobTitle;

    @Column(name="notes", columnDefinition = "TEXT")
    private String notes;
}
