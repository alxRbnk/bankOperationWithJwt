package org.rubnikovich.bankoperation.controller.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.rubnikovich.bankoperation.controller.dto.TransactionDto;
import org.rubnikovich.bankoperation.controller.security.JwtUtil;
import org.rubnikovich.bankoperation.controller.service.TransactionService;
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
    private final JwtUtil jwtUtil;

    public TransactionController(TransactionService transactionService, JwtUtil jwtUtil) {
        this.transactionService = transactionService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    @Operation(summary = "Get all transactions")
    public Collection getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @Operation(summary = "Get all transactions of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user")
    public List<TransactionDto> getAllUserTransactions(@RequestHeader("Authorization") String token) {
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        return transactionService.getAllUserTransactions(login);
    }

    @Operation(summary = "Make a transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction made successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping
    public ResponseEntity makeTransaction(@RequestHeader("Authorization") String token,
                                          @RequestBody TransactionDto transactionDto) {
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            log.warn("Invalid token format");
            return responseEntity;
        }
        jwtUtil.validateTokenAndGetClaim(token);
        if (transactionService.makeTransaction(transactionDto)) {
            log.info("Transaction made successfully for user: {}");
            responseEntity = new ResponseEntity("the money has been sent", HttpStatus.OK);
        } else {
            log.warn("Failed to make transaction for user: {}");
        }
        return responseEntity;
    }
}
