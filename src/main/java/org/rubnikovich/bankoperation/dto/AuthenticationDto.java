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

    private String password;
}
