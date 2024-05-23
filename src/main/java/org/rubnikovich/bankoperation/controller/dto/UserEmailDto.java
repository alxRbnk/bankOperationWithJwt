package org.rubnikovich.bankoperation.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEmailDto {
    private Long id;
    private String email;
    private String newEmail;
    private Long userId;
}