package com.reecefoundation.reecefoundation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.Fetch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL)
    private AmountAwarded amountAwarded;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToMany
    @JoinTable(name = "project_classifications",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "classification_id"))
    @OrderBy("id ASC")
    private Set<Classification> classifications = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "project_sub_classifications",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "sub_classification_id"))
    @OrderBy("subClassificationName ASC")
    private Set<SubClassification> subClassifications = new LinkedHashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Action> actions = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectBankDetail> bankDetails;

    @OneToMany(mappedBy="project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<ProjectNote> projectNotes = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonitoringReport> monitoringReports = new ArrayList<>();

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "overview", nullable = false, columnDefinition = "TEXT")
    private String overview;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "date_entered", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEntered;

    @Column(name = "application_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date applicationDate;

    @Column(name = "approved_rejected_date")
    @Temporal(TemporalType.DATE)
    private Date approvedRejectedDate;

    @Column(name = "total_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "project_length", nullable = false)
    private int projectLength;

    @Column(name = "grant_letter_received")
    private Boolean grantLetterReceived;

    @Column(name= "grant_letter_link")
    private String grantLetterLink;

    @Column(name = "application_link", length = 2048)
    private String applicationLink;

    @Column(name = "potential_further_funding", nullable = false)
    private Boolean potentialFurtherFunding = false;

    @Column(name = "budget_link", length = 2048)
    private String budgetLink;

    // e.g. "Education (Primary, Secondary), Environment (Rewilding)"
    public String getClassificationLabel() {
        return classifications.stream()
                .map(c -> {
                    String subs = subClassifications.stream()
                            .filter(s -> s.getClassification().getId().equals(c.getId()))
                            .map(SubClassification::getSubClassificationName)
                            .collect(Collectors.joining(", "));
                    return subs.isEmpty() ? c.getClassificationName() : c.getClassificationName() + " (" + subs + ")";
                })
                .collect(Collectors.joining(", "));
    }

    public String getClassificationNames() {
        return classifications.stream().map(Classification::getClassificationName).collect(Collectors.joining(", "));
    }

    public String getSubClassificationNames() {
        return subClassifications.stream().map(SubClassification::getSubClassificationName).collect(Collectors.joining(", "));
    }

    public boolean hasClassification(Long classificationId) {
        return classifications.stream().anyMatch(c -> c.getId().equals(classificationId));
    }

    public boolean hasSubClassification(Long subClassificationId) {
        return subClassifications.stream().anyMatch(s -> s.getId().equals(subClassificationId));
    }

    // Actions for display: not completed first, then soonest due date
    public List<Action> getSortedActions() {
        return actions.stream().sorted(Action.DISPLAY_ORDER).toList();
    }

    @PrePersist
    protected void onCreate() {
        this.dateEntered = new Date();
    }
}
