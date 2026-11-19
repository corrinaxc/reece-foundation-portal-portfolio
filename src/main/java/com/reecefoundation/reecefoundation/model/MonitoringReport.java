package com.reecefoundation.reecefoundation.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name="monitoring_reports")
public class MonitoringReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @Column(name="report_due_date")
    private Date reportDueDate;

    @Column(name="monitoring_report_received_date")
    private Date reportReceivedDate;

    @Column(name="monitoring_report_approved_date")
    private Date reportApprovedDate;

    @Column(name="report_link", length = 2048)
    private String reportLink;

}
