package org.rubnikovich.bankoperation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.rubnikovich.bankoperation.dto.TransactionDto;
import org.rubnikovich.bankoperation.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Make a transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction made successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping
    public ResponseEntity makeTransaction(@RequestHeader("Authorization") String token,
                                          @RequestBody TransactionDto transactionDto) {
        if (transactionService.makeTransaction(transactionDto, token)) {
            log.info("Transaction made successfully");
            return new ResponseEntity("the money has been sent", HttpStatus.OK);
        }
        log.warn("Failed to make transaction");
        return new ResponseEntity("Failed to make transaction", HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Get all transactions of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user")
    public List<TransactionDto> getAllUserTransactions(@RequestHeader("Authorization") String token) {
        return transactionService.getAllUserTransactions(token);
    }

    @GetMapping
    @Operation(summary = "Get all transactions")
    public Collection getAllTransactions() {
        return transactionService.getAllTransactions();
    }

}
