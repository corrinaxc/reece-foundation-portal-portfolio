package com.reecefoundation.reecefoundation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ParsedPaymentRow {
    private int rowNumber;

    private String rawProjectId;
    private Long projectId;
    private String projectName;

    private BigDecimal amount;
    private Date date;

    private String accountName;
    private String accountNo;
    private String sortCode;

    private boolean bankDetailsConfirmed;
    private boolean invoiced;
    private boolean paid;

    private Long matchedBankDetailId;
    private String matchedBankDetailLabel;

    private String status;
    private String message;
}
