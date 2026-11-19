package com.reecefoundation.reecefoundation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class ProjectNoteForm {

    @NotBlank(message = "Project Note is required")
    private String projectNote;

    @NotNull(message = "Note Date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate noteDate = LocalDate.now();
}
