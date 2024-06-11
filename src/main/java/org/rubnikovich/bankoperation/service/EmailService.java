package org.rubnikovich.bankoperation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rubnikovich.bankoperation.dto.UserEmailDto;
import org.rubnikovich.bankoperation.entity.User;
import org.rubnikovich.bankoperation.entity.UserEmail;
import org.rubnikovich.bankoperation.repository.EmailRepository;
import org.rubnikovich.bankoperation.repository.UserRepository;
import org.rubnikovich.bankoperation.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final EmailRepository emailRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public boolean emailExists(String email) {
        return emailRepository.existsByEmail(email);
    }

    public ResponseEntity<List<String>> getAllUserEmails(String token) {
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        Long userId = userService.getUserIdByLogin(login);
        List<UserEmail> emails = emailRepository.findAllByUserId(userId);
        List<String> emailStrings = new ArrayList<>();
        for (UserEmail userEmail : emails) {
            emailStrings.add(userEmail.getEmail());
        }
        log.info("Retrieved {} emails for user with login: {}", emails.size(), login);
        return ResponseEntity.ok(emailStrings);
    }

    public ResponseEntity<String> emailAdd(String token, UserEmailDto emailDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(". "));
            log.warn("Failed to add email " + errors);
            return ResponseEntity.badRequest().body("Failed to add email, " + errors);
        }
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        User user = userService.getByLogin(login);
        if (emailRepository.existsByEmail(emailDto.getNewEmail())) {
            log.warn("Failed to add email {} for user with login: {}", emailDto.getNewEmail(), login);
            return ResponseEntity.badRequest().body("Failed to add email, this email already exists");
        }
        UserEmail email = new UserEmail();
        email.setUser(user);
        email.setEmail(emailDto.getNewEmail());
        emailRepository.save(email);
        log.info("Email {} added for user with login: {}", emailDto.getNewEmail(), login);
        return ResponseEntity.ok().body("Email added");
    }

    public ResponseEntity<String> emailUpdate(String token, UserEmailDto emailDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(". "));
            log.warn("Failed to update email " + errors);
            return ResponseEntity.badRequest().body("Failed to update email, " + errors);
        }
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        Long userId = userService.getUserIdByLogin(login);
        emailDto.setUserId(userId);
        if (!emailRepository.existsByEmail(emailDto.getEmail()) ||
                emailRepository.existsByEmail(emailDto.getNewEmail())) {
            log.info("Failed to update email");
            return ResponseEntity.badRequest().body("Failed to update email");
        }
        UserEmail currentEmail = emailRepository.findByEmail(emailDto.getEmail());
        UserEmail email = convertToUserEmail(emailDto);
        email.setId(currentEmail.getId());
        emailRepository.save(email);
        log.info("Email updated from {} to {} for user with login: {}", emailDto.getEmail(), emailDto.getNewEmail(), login);
        return ResponseEntity.ok().body("Email updated");
    }

    @Transactional
    public ResponseEntity<String> emailDelete(String token, UserEmailDto emailDto) {
        token = token.substring(7);
        String login = jwtUtil.validateTokenAndGetClaim(token);
        Long userId = userService.getUserIdByLogin(login);
        String emailStr = emailDto.getEmail();
        List<UserEmail> emails = emailRepository.findAllByUserId(userId);
        List<String> emailStrings = emails.stream().map(UserEmail::getEmail).toList();
        if (emailStrings.contains(emailStr)) {
            emailRepository.deleteByEmail(emailStr);
            log.info("Email {} deleted for user with login: {}", emailStr, login);
            return ResponseEntity.ok().body("deleted " + emailStr);
        }
        log.warn("Failed to delete email {} for user with login: {}", emailStr, login);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found " + emailStr);
    }

    private UserEmail convertToUserEmail(UserEmailDto emailDto) {
        UserEmail email = new UserEmail();
        User user = userRepository.findById(emailDto.getUserId()).orElseThrow();
        email.setUser(user);
        email.setEmail(emailDto.getNewEmail());
        email.setId(email.getId());
        return email;
    }
}
