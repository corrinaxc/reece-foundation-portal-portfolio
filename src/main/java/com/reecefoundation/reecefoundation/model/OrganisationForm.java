package com.reecefoundation.reecefoundation.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganisationForm {
    @NotBlank(message = "Organisation Name is required")
    private String organisationName;

    @NotBlank(message= "Organisation Type is required")
    @Pattern(regexp = "Charity|CIC|Company", message = "Organisation type must be Charity, CIC, or Company")
    private String organisationType;

    @NotBlank(message = "Registration Number is required")
    private String regNumber;

    private String organisationNote;

    private String name;

    private String address;

    private String jobTitle;

    private String telephone;

    @Email(message="Invalid email address")
    private String email;
}
