package org.rubnikovich.bankoperation.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDto {
    private long id;
    private Long sender;
    private Long recipient;
    private BigDecimal amount;
    private LocalDateTime date;
}
