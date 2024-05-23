package org.rubnikovich.bankoperation.controller.validator;

import lombok.extern.slf4j.Slf4j;
import org.rubnikovich.bankoperation.controller.entity.User;
import org.rubnikovich.bankoperation.controller.entity.UserEmail;
import org.rubnikovich.bankoperation.controller.entity.UserPhoneNumber;
import org.rubnikovich.bankoperation.controller.service.EmailService;
import org.rubnikovich.bankoperation.controller.service.PhoneService;
import org.rubnikovich.bankoperation.controller.service.UsersDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Slf4j
@Component
public class CustomValidator implements Validator {
    private final UsersDetailsService usersDetailsService;
    private final PhoneService phoneService;
    private final EmailService emailService;

    public CustomValidator(UsersDetailsService usersDetailsService, PhoneService phoneService, EmailService emailService) {
        this.usersDetailsService = usersDetailsService;
        this.phoneService = phoneService;
        this.emailService = emailService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;
        try {
            usersDetailsService.loadUserByUsername(user.getLogin());
            errors.rejectValue("login", "", "Login already exists");
            log.warn("Login already exists: {}", user.getLogin());
        } catch (UsernameNotFoundException ignored) {
        }
        if (user.getEmails() != null && !user.getEmails().isEmpty()) {
            for (UserEmail userEmail : user.getEmails()) {
                if (emailService.emailExists(userEmail.getEmail())) {
                    errors.rejectValue("emails", "", "Email already exists");
                    log.warn("Email already exists: {}", userEmail.getEmail());
                    break;
                }
            }
        }
        if (user.getPhones() != null && !user.getPhones().isEmpty()) {
            for (UserPhoneNumber userPhone : user.getPhones()) {
                if (phoneService.phoneExists(userPhone.getPhone())) {
                    errors.rejectValue("phones", "", "Phone number already exists");
                    log.warn("Phone number already exists: {}", userPhone.getPhone());
                    break;
                }
            }
        }
    }
}