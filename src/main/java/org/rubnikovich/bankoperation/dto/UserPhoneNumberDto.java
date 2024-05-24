package org.rubnikovich.bankoperation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPhoneNumberDto {
    private Long id;
    private String phone;
    private String newPhone;
    private Long userId;
}