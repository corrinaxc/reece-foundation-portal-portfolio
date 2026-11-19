package com.reecefoundation.reecefoundation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectBankDetailForm {
    @NotBlank(message = "Payee is Required")
    private String payee;

    @NotBlank(message = "Account Number is Required")
    @Size(min = 8, max = 8, message = "Account Number must be 8 digits")
    private String accountNumber;

    @NotBlank(message= "Sort Code is Required")
    @Size(min = 6, max = 6, message = "Sort Code must be 6 digits")
    private String sortCode;

    private String link;

    private String note;
}
