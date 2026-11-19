package com.reecefoundation.reecefoundation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name="project_bank_details")
public class ProjectBankDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "projectBankDetail", cascade = CascadeType.ALL)
    private List<Payment> payments = new ArrayList<>();

    @Column(name="payee", nullable = false)
    private String payee;

    @Column(name="account_number", nullable=false, length=8)
    private String accountNumber;

    @Column(name="sort_code", nullable = false, length=6)
    private String sortCode;

    @Column(name="note")
    private String note;

    @Column(name="bank_details_link")
    private String bankDetailsLink;
}
