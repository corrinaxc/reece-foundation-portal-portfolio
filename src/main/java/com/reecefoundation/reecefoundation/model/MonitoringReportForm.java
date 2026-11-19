package com.reecefoundation.reecefoundation.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class MonitoringReportForm {

    @NotNull(message = "Date Due is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date reportDueDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date reportReceivedDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date reportApprovedDate;

    @Size(max = 2048, message = "Report Link must be 2048 characters or fewer")
    private String reportLink;
}
