package org.rubnikovich.bankoperation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.rubnikovich.bankoperation.dto.AuthenticationDto;
import org.rubnikovich.bankoperation.dto.UserDto;
import org.rubnikovich.bankoperation.entity.User;
import org.rubnikovich.bankoperation.security.JwtUtil;
import org.rubnikovich.bankoperation.validator.CustomValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;
    private final CustomValidator customValidator;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<Map<String, String>> registrationUser(UserDto userDto, BindingResult bindingResult) {
        User user = convertToUser(userDto);
        customValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(". "));
            log.warn("Registration failed: Validation errors found. " + errors);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Registration failed: " + errors));
        }

        user.setBalance(user.getInitialDeposit());
        userService.create(user);
        String token = jwtUtil.generateToken(user.getLogin());
        log.info("User registered successfully: {}", user.getLogin());
        return ResponseEntity.ok().body(Map.of("jwt token", token));
    }

    public ResponseEntity<Map<String, String>> loginUser(AuthenticationDto authenticationDto,
                                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(". "));
            log.warn("Registration failed: Validation errors found. " + errors);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Registration failed: " + errors));
        }
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authenticationDto.getLogin(),
                        authenticationDto.getPassword());
        try {
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException e) {
            log.warn("Login failed: Incorrect credentials for user: {}", authenticationDto.getLogin());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Incorrect login or password"));
        }
        String token = jwtUtil.generateToken(authenticationDto.getLogin());
        log.info("User logged in successfully: {}", authenticationDto.getLogin());
        return ResponseEntity.ok().body(Map.of("jwt token", token));
    }

    private User convertToUser(UserDto userDto) {
        return this.modelMapper.map(userDto, User.class);
    }

}
