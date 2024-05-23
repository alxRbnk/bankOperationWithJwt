package org.rubnikovich.bankoperation.controller.service;

import lombok.extern.slf4j.Slf4j;
import org.rubnikovich.bankoperation.controller.dto.UserPhoneNumberDto;
import org.rubnikovich.bankoperation.controller.entity.User;
import org.rubnikovich.bankoperation.controller.entity.UserPhoneNumber;
import org.rubnikovich.bankoperation.controller.repository.PhoneRepository;
import org.rubnikovich.bankoperation.controller.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class PhoneService {
    private final PhoneRepository phoneRepository;
    private final UserRepository userRepository;

    public PhoneService(PhoneRepository phoneRepository, UserRepository userRepository) {
        this.phoneRepository = phoneRepository;
        this.userRepository = userRepository;
    }

    public boolean phoneExists(String phoneNumber) {
        return phoneRepository.existsByPhone(phoneNumber);
    }

    public Collection<String> getAllPhones(Long userId) {
        Collection<UserPhoneNumber> phones = phoneRepository.findAllByUserId(userId);
        List<String> listStr = new ArrayList<>();
        for (UserPhoneNumber phone : phones) {
            listStr.add(phone.getPhone());
        }
        return listStr;
    }

    public boolean phoneUpdate(UserPhoneNumberDto phoneDto, Long userId) {
        List<UserPhoneNumber> phones = phoneRepository.findAllByUserId(userId);
        if (phones.isEmpty()) {
            log.info("No phone found for user ID: " + userId);
            return false;
        }
        if (!phoneRepository.existsByPhone(phoneDto.getPhone())) {
            return false;
        }
        UserPhoneNumber currentPhone = phoneRepository.findByPhone(phoneDto.getPhone());
        UserPhoneNumber phone = convertToUserPhone(phoneDto);
        phone.setId(currentPhone.getId());
        phoneRepository.save(phone);
        return true;
    }

    public boolean phoneAdd(UserPhoneNumberDto phoneDto, User user) {
        if (phoneRepository.existsByPhone(phoneDto.getNewPhone())) {
            return false;
        }
        UserPhoneNumber phone = new UserPhoneNumber();
        phone.setUser(user);
        phone.setPhone(phoneDto.getNewPhone());
        phoneRepository.save(phone);
        return true;
    }

    @Transactional
    public boolean phoneDelete(String phone, Long userId) {
        List<UserPhoneNumber> phones = phoneRepository.findAllByUserId(userId);
        if (phones.isEmpty()) {
            log.info("No phone found for user ID: " + userId);
            return false;
        }
        if (!phoneRepository.existsByPhone(phone)) {
            return false;
        }
        UserPhoneNumber lastPhone = phones.get(phones.size() - 1);
        if (phone.equals(lastPhone.getPhone()) && phones.size() == 1) {
            log.info("Cannot delete the last phone or only one phone present");
            return false;
        }
        phoneRepository.deleteByPhone(phone);
        return true;
    }

    private UserPhoneNumber convertToUserPhone(UserPhoneNumberDto phoneDto) {
        UserPhoneNumber phone = new UserPhoneNumber();
        User user = userRepository.findById(phoneDto.getUserId()).orElseThrow();
        phone.setUser(user);
        phone.setPhone(phoneDto.getNewPhone());
        phone.setId(phone.getId());
        return phone;
    }
}
