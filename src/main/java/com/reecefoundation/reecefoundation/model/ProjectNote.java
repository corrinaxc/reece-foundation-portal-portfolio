package com.reecefoundation.reecefoundation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name="project_notes")
public class ProjectNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @Column(name="note", nullable = false)
    private String projectNote;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_at", nullable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
    }
}
