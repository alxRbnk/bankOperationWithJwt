package org.rubnikovich.bankoperation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPhoneNumberDto {

    private Long id;

    @NotEmpty(message = "Phone cannot be empty")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    private String phone;

    @NotEmpty(message = "New phone cannot be empty")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid new phone number format")
    private String newPhone;

    private Long userId;
}