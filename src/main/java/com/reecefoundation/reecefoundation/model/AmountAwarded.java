package com.reecefoundation.reecefoundation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name="finance_amount_awarded")
public class AmountAwarded {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name= "project_id", nullable = false)
    private Project project;

    @Column(name="awarded_amount", nullable = false)
    private BigDecimal amountAwardedValue;

    @Column(name="christmas_special")
    private Boolean christmasSpecial;

}
