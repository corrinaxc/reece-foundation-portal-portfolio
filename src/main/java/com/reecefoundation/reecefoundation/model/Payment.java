package com.reecefoundation.reecefoundation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name="finance_payment_dates")
public class Payment {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "bank_details_id", nullable = true)
    private ProjectBankDetail projectBankDetail;

    @Column(name="payment_amount", nullable = false)
    private BigDecimal paymentAmount;

    @Column(name="payment_due_date", nullable = false)
    private Date paymentDueDate;

    @Column(name="paid")
    private Boolean paid;

    @Column(name="payment_actual_date")
    private Date paymentActualDate;

    @Column(name="receipt_received")
    private Boolean receiptReceived;

    @Column(name = "bank_details_confirmed")
    private Boolean bankDetailsConfirmed;

    @Column(name = "invoiced")
    private Boolean invoiced;

    @Column(name="invoice_link")
    private String invoiceLink;

    @Column(name="receipt_received_link")
    private String receiptLink;

}
