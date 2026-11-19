package com.reecefoundation.reecefoundation.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
public class ProjectForm {
    @NotNull(message="Organisation is required")
    private Long organisationId;

    @NotEmpty(message="At least one classification is required")
    private List<Long> classificationIds = new ArrayList<>();

    private List<Long> subClassificationIds = new ArrayList<>();

    @NotBlank(message = "Project Name is required")
    private String projectName;

    @NotBlank(message = "Overview is required")
    private String overview;

    @NotBlank(message = "Status is required")
    private String status;

    @NotNull(message = "Project Length is required")
    @Min(0)
    private Integer projectLength;

    @NotNull(message = "Total value is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal totalValue;

    @NotNull(message = "Application Date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date applicationDate;

    private Long ownerId;

    private String projectNote;

    private Date approvedRejectedDate;

    private Boolean grantLetterReceived;

    private String grantLetterLink;

    @Size(max = 2048, message = "Application Link must be 2048 characters or fewer")
    private String applicationLink;

    private Boolean potentialFurtherFunding;

    @Size(max = 2048, message = "Budget Link must be 2048 characters or fewer")
    private String budgetLink;

    private BigDecimal amountAwardedValue;

}
