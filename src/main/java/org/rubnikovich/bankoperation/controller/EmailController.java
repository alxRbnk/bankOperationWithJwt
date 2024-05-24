package org.rubnikovich.bankoperation.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.rubnikovich.bankoperation.dto.UserEmailDto;
import org.rubnikovich.bankoperation.entity.User;
import org.rubnikovich.bankoperation.entity.UserEmail;
import org.rubnikovich.bankoperation.security.JwtUtil;
import org.rubnikovich.bankoperation.service.EmailService;
import org.rubnikovich.bankoperation.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/email")
@Slf4j
public class EmailController {

    private final EmailService emailService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public EmailController(EmailService emailService, UserService userService, JwtUtil jwtUtil) {
        this.emailService = emailService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Get all emails for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emails retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public Collection getAllEmails(@RequestHeader("Authorization") String token) {
        log.info("Getting all emails for authenticated user");
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        Long userId = userService.getUserIdByLogin(login);
        Collection emails = emailService.getAllEmails(userId);
        log.info("Retrieved {} emails for user with login: {}", emails.size(), login);
        return emails;
    }

    @Operation(summary = "Add an email for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email added successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping
    public ResponseEntity addEmail(@RequestHeader("Authorization") String token,
                                   @RequestBody UserEmailDto emailDto) {
        ResponseEntity responseEntity = new ResponseEntity("email don't added", HttpStatus.BAD_REQUEST);
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        User user = userService.getByLogin(login);
        if (emailService.emailAdd(emailDto, user)) {
            log.info("Email {} added for user with login: {}", emailDto.getNewEmail(), login);
            responseEntity = new ResponseEntity("email added", HttpStatus.CREATED);
        } else {
            log.warn("Failed to add email {} for user with login: {}", emailDto.getNewEmail(), login);
        }
        return responseEntity;
    }

    @Operation(summary = "Update an email for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email updated successfully"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping()
    public ResponseEntity update(@RequestHeader("Authorization") String token,
                                 @RequestBody UserEmailDto emailDto) {
        log.info("Updating email for authenticated user");
        ResponseEntity responseEntity = new ResponseEntity("not updated", HttpStatus.NOT_FOUND);
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        Long userId = userService.getUserIdByLogin(login);
        emailDto.setUserId(userId);
        log.info("current email: " + emailDto.getEmail());
        log.info("new email: " + emailDto.getNewEmail());
        if (emailService.emailUpdate(emailDto, userId)) {
            log.info("Email updated from {} to {} for user with login: {}", emailDto.getEmail(), emailDto.getNewEmail(), login);
            responseEntity = new ResponseEntity("updated", HttpStatus.OK);
        } else {
            log.warn("Failed to update email from {} to {} for user with login: {}", emailDto.getEmail(), emailDto.getNewEmail(), login);
        }
        return responseEntity;
    }

    @Operation(summary = "Delete an email for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping
    public ResponseEntity delete(@RequestHeader("Authorization") String token, @RequestBody UserEmail userEmail) {

        ResponseEntity responseEntity = new ResponseEntity("not found", HttpStatus.NOT_FOUND);
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        Long userId = userService.getUserIdByLogin(login);
        String emailStr = userEmail.getEmail();
        log.info("Email to delete: " + emailStr);
        if (emailService.emailDelete(emailStr, userId)) {
            log.info("Email {} deleted for user with login: {}", emailStr, login);
            responseEntity = new ResponseEntity("deleted", HttpStatus.OK);
        } else {
            log.warn("Failed to delete email {} for user with login: {}", emailStr, login);
        }
        return responseEntity;
    }

}
