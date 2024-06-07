package org.rubnikovich.bankoperation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.rubnikovich.bankoperation.dto.AuthenticationDto;
import org.rubnikovich.bankoperation.dto.UserDto;
import org.rubnikovich.bankoperation.entity.User;
import org.rubnikovich.bankoperation.security.JwtUtil;
import org.rubnikovich.bankoperation.service.UserService;
import org.rubnikovich.bankoperation.validator.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final CustomValidator userValidator;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(JwtUtil jwtUtil, ModelMapper modelMapper, CustomValidator userValidator, UserService userService, AuthenticationManager authenticationManager) {
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
        this.userValidator = userValidator;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Operation(summary = "User registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @PostMapping("/registration")
    public Map<String, String> performRegistration(@RequestBody @Valid UserDto userDto,
                                                   BindingResult bindingResult) {
        log.info("Starting registration for user: {}", userDto.getLogin());
        if (userDto.getEmails() == null || userDto.getEmails().isEmpty() ||
                userDto.getPhones() == null || userDto.getPhones().isEmpty()) {
            log.warn("Registration failed: At least one email and one phone number are required");
            return Map.of("message", "At least one email and one phone number are required");
        }
        User user = convertToUser(userDto);
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            log.warn("Registration failed: Validation errors found");
            return Map.of("message", "error authController");
        }
        user.setBalance(user.getInitialDeposit());
        userService.create(user);
        String token = jwtUtil.generateToken(user.getLogin());
        log.info("User registered successfully: {}", user.getLogin());
        return Map.of("jwt-token", token);
    }

    @Operation(summary = "User login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/login")
    public Map<String, String> performLogin(@RequestBody AuthenticationDto authenticationDto) {
        log.info("Login attempt for user: {}", authenticationDto.getLogin());
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authenticationDto.getLogin(),
                        authenticationDto.getPassword());
        try {
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException e) {
            log.warn("Login failed: Incorrect credentials for user: {}", authenticationDto.getLogin());
            return Map.of("message", "Incorrect credentials");
        }
        String token = jwtUtil.generateToken(authenticationDto.getLogin());
        log.info("User logged in successfully: {}", authenticationDto.getLogin());
        return Map.of("jwt-token", token);
    }

    public User convertToUser(UserDto userDto) {
        return this.modelMapper.map(userDto, User.class);
    }
}


