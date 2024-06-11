package org.rubnikovich.bankoperation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEmailDto {

    private Long id;

    @Email(message = "Invalid email format")
    private String email;

    @NotEmpty(message = "New email cannot be empty")
    @Email(message = "Invalid new email format")
    private String newEmail;

    private Long userId;
}