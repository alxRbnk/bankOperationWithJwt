package org.rubnikovich.bankoperation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.rubnikovich.bankoperation.dto.UserDto;
import org.rubnikovich.bankoperation.security.JwtUtil;
import org.rubnikovich.bankoperation.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/show")
    public List getAllUsers(@RequestHeader("Authorization") String token) {
        token = token.substring(7);
        jwtUtil.validateTokenAndGetClaim(token);
        log.info("Fetching all users");
        return userService.getAll();
    }

    @Operation(summary = "Update user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PutMapping()
    public ResponseEntity update(@RequestHeader("Authorization") String token,
                                 @RequestBody UserDto userDto) {
        ResponseEntity responseEntity = new ResponseEntity("not updated", HttpStatus.NOT_FOUND);
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        if (userService.update(userDto, login)) {
            responseEntity = new ResponseEntity("updated", HttpStatus.OK);
            log.info("User updated successfully: {}", login);
        } else {
            log.warn("User update failed: {}", login);
        }
        return responseEntity;
    }

    @Operation(summary = "Delete user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity delete(@RequestHeader("Authorization") String token,
                                 @PathVariable Long id) {
        token = token.substring(7);
        jwtUtil.validateTokenAndGetClaim(token);
        log.info("Deleting user with ID: {}", id);
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
        if (userService.delete(id)) {
            responseEntity = new ResponseEntity("deleted", HttpStatus.OK);
            log.info("User deleted successfully: ID {}", id);
        } else {
            log.warn("User deletion failed: ID {}", id);
        }
        return responseEntity;
    }

    @Operation(summary = "Get users by birth date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/date")
    public ResponseEntity<Page<UserDto>> getUsersByBirthDate(
            @RequestParam("birthDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthDate,
            Pageable pageable) {
        log.info("Fetching users born after: {}", birthDate);
        Page<UserDto> users = userService.getAllByBirthDateAfter(birthDate, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Find users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/find")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) LocalDate birthDate,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            Pageable pageable) {
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        log.info("Finding users with criteria - birthDate: {}, phone: {}, lastName: {}, email: {}", birthDate, phone, lastName, email);
        Page<UserDto> users = userService.getAllUsers(login, birthDate, phone, lastName, email, pageable);
        return ResponseEntity.ok(users);
    }
}
// http://localhost:8080/users/date?birthDate=1990-01-01
// http://localhost:8080/users/find?email=mail@mail.com&page=0&size=10&sort=email,asc
// http://localhost:8080/users/find?lastName=bob&page=0&size=10&sort=lastName,asc
