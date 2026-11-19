package com.reecefoundation.reecefoundation.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class ActionForm {
    @NotBlank(message = "Action Description is required")
    private String actionDescription;

    private Long ownerId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    private boolean completed;
}
