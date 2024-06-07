package org.rubnikovich.bankoperation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDto {

    private long id;

    @NotNull(message = "Sender ID cannot be null")
    private Long sender;

    @NotNull(message = "Recipient ID cannot be null")
    private Long recipient;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Date cannot be null")
    private LocalDateTime date;
}
