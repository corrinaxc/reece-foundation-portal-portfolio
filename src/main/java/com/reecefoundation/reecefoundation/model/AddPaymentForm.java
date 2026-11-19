package com.reecefoundation.reecefoundation.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class AddPaymentForm {
    @NotNull(message = "Payment Amount is Required")
    @DecimalMin(value="0.1", inclusive = true)
    private BigDecimal paymentAmount;

    @NotNull(message="Payment Date is Required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date paymentDueDate;

    private Boolean invoiced;

    private String invoiceLink;

    private Boolean bankDetailsReceived;

    private Long bankDetailId;

    private Boolean paid;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date paymentActualDate;

    private Boolean receiptReceived;

    private String receiptLink;

}
