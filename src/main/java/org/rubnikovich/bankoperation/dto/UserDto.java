package org.rubnikovich.bankoperation.dto;

import lombok.Getter;
import lombok.Setter;
import org.rubnikovich.bankoperation.entity.UserEmail;
import org.rubnikovich.bankoperation.entity.UserPhoneNumber;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UserDto {
    private String login;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private List<UserEmail> emails;
    private List<UserPhoneNumber> phones;
    private BigDecimal balance;
    private BigDecimal initialDeposit;
}


