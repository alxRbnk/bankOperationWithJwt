package org.rubnikovich.bankoperation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationDto {

    @NotEmpty(message = "don't empty")
    @Size(min = 2, max = 100, message = "from 2 to 100 symbols")
    private String login;

    @NotEmpty(message = "password cannot be empty")
    @Size(min = 4, max = 100, message = "password must be between 4 and 100 characters")
    private String password;
}
