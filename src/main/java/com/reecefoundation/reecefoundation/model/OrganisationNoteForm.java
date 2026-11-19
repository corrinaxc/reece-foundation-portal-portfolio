package com.reecefoundation.reecefoundation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class OrganisationNoteForm {

    @NotBlank(message = "Organisation Note is required")
    private String organisationNote;

    @NotNull(message = "Note Date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate noteDate = LocalDate.now();
}
