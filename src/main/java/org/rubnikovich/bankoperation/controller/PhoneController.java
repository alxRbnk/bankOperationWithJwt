package org.rubnikovich.bankoperation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.rubnikovich.bankoperation.dto.UserPhoneNumberDto;
import org.rubnikovich.bankoperation.entity.User;
import org.rubnikovich.bankoperation.entity.UserPhoneNumber;
import org.rubnikovich.bankoperation.security.JwtUtil;
import org.rubnikovich.bankoperation.service.PhoneService;
import org.rubnikovich.bankoperation.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/phone")
@Slf4j
public class PhoneController {

    private final PhoneService phoneService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public PhoneController(PhoneService phoneService, UserService userService, JwtUtil jwtUtil) {
        this.phoneService = phoneService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Get all phone numbers for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Phone numbers retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public Collection getAllPhones(@RequestHeader("Authorization") String token) {
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        Long userId = userService.getUserIdByLogin(login);
        Collection phones = phoneService.getAllPhones(userId);
        log.info("Retrieved {} phone numbers for user with login: {}", phones.size(), login);
        return phones;
    }

    @Operation(summary = "Add a phone number for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Phone number added successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping
    public ResponseEntity addPhone(@RequestHeader("Authorization") String token,
                                   @RequestBody UserPhoneNumberDto phoneDto) {
        ResponseEntity responseEntity = new ResponseEntity("phone don't added", HttpStatus.BAD_REQUEST);
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        User user = userService.getByLogin(login);
        if (phoneService.phoneAdd(phoneDto, user)) {
            log.info("Phone number {} added for user with login: {}", phoneDto.getNewPhone(), login);
            responseEntity = new ResponseEntity("phone added", HttpStatus.CREATED);
        } else {
            log.warn("Failed to add phone number {} for user with login: {}", phoneDto.getNewPhone(), login);
        }
        return responseEntity;
    }

    @Operation(summary = "Update a phone number for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Phone number updated successfully"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping()
    public ResponseEntity update(@RequestHeader("Authorization") String token,
                                 @RequestBody UserPhoneNumberDto phoneDto) {
        ResponseEntity responseEntity = new ResponseEntity("not updated", HttpStatus.NOT_FOUND);
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        Long userId = userService.getUserIdByLogin(login);
        phoneDto.setUserId(userId);
        log.info("current phone: " + phoneDto.getPhone());
        log.info("new phone: " + phoneDto.getNewPhone());
        if (phoneService.phoneUpdate(phoneDto, userId)) {
            log.info("Phone number updated from {} to {} for user with login: {}", phoneDto.getPhone(), phoneDto.getNewPhone(), login);
            responseEntity = new ResponseEntity("updated", HttpStatus.OK);
        } else {
            log.warn("Failed to update phone number from {} to {} for user with login: {}", phoneDto.getPhone(), phoneDto.getNewPhone(), login);
        }
        return responseEntity;
    }

    @Operation(summary = "Delete a phone number for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Phone number deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping
    public ResponseEntity delete(@RequestHeader("Authorization") String token,
                                 @RequestBody UserPhoneNumber userPhone) {
        ResponseEntity responseEntity = new ResponseEntity("not found", HttpStatus.NOT_FOUND);
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        Long userId = userService.getUserIdByLogin(login);
        String phoneStr = userPhone.getPhone();
        log.info("Phone to delete: " + phoneStr);
        if (phoneService.phoneDelete(phoneStr, userId)) {
            log.info("Phone number {} deleted for user with login: {}", phoneStr, login);
            responseEntity = new ResponseEntity("deleted", HttpStatus.OK);
        } else {
            log.warn("Failed to delete phone number {} for user with login: {}", phoneStr, login);
        }
        return responseEntity;
    }
}
