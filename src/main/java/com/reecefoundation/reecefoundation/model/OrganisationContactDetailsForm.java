package com.reecefoundation.reecefoundation.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganisationContactDetailsForm {

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be 50 characters or fewer")
    private String name;

    @Size(max = 50, message = "Job title must be 50 characters or fewer")
    private String jobTitle;

    @Size(max = 255, message = "Address must be 255 characters or fewer")
    private String address;

    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must be 255 characters or fewer")
    private String email;

    @Size(max = 30, message = "Telephone must be 30 characters or fewer")
    private String telephone;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;
}
