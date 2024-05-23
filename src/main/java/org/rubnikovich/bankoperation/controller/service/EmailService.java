package org.rubnikovich.bankoperation.controller.service;

import lombok.extern.slf4j.Slf4j;
import org.rubnikovich.bankoperation.controller.dto.UserEmailDto;
import org.rubnikovich.bankoperation.controller.entity.User;
import org.rubnikovich.bankoperation.controller.entity.UserEmail;
import org.rubnikovich.bankoperation.controller.repository.EmailRepository;
import org.rubnikovich.bankoperation.controller.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class EmailService {

    private final EmailRepository emailRepository;
    private final UserRepository userRepository;

    public EmailService(EmailRepository emailRepository, UserRepository userRepository) {
        this.emailRepository = emailRepository;
        this.userRepository = userRepository;
    }

    public boolean emailExists(String email) {
        return emailRepository.existsByEmail(email);
    }

    public Collection<String> getAllEmails(Long userId) {
        Collection<UserEmail> emails = emailRepository.findAllByUserId(userId);
        List<String> listStr = new ArrayList<>();
        for (UserEmail email : emails) {
            listStr.add(email.getEmail());
        }
        return listStr;
    }

    public boolean emailUpdate(UserEmailDto emailDto, Long userId) {
        List<UserEmail> emails = emailRepository.findAllByUserId(userId);
        if (emails.isEmpty()) {
            log.info("No emails found for user ID: " + userId);
            return false;
        }
        if (!emailRepository.existsByEmail(emailDto.getEmail())) {
            return false;
        }
        UserEmail currentEmail = emailRepository.findByEmail(emailDto.getEmail());
        UserEmail email = convertToUserEmail(emailDto);
        email.setId(currentEmail.getId());
        emailRepository.save(email);
        return true;
    }

    public boolean emailAdd(UserEmailDto emailDto, User user) {
        if (emailRepository.existsByEmail(emailDto.getNewEmail())) {
            return false;
        }
        UserEmail email = new UserEmail();
        email.setUser(user);
        email.setEmail(emailDto.getNewEmail());
        emailRepository.save(email);
        return true;
    }

    @Transactional
    public boolean emailDelete(String email, Long userId) {
        List<UserEmail> emails = emailRepository.findAllByUserId(userId);
        if (emails.isEmpty()) {
            log.info("No emails found for user ID: " + userId);
            return false;
        }
        if (!emailRepository.existsByEmail(email)) {
            return false;
        }
        UserEmail lastEmail = emails.get(emails.size() - 1);
        if (email.equals(lastEmail.getEmail()) && emails.size() == 1) {
            log.info("Cannot delete the last email or only one email present");
            return false;
        }
        emailRepository.deleteByEmail(email);
        return true;
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
