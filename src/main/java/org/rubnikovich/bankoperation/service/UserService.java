package org.rubnikovich.bankoperation.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.rubnikovich.bankoperation.dto.UserDto;
import org.rubnikovich.bankoperation.entity.User;
import org.rubnikovich.bankoperation.entity.UserEmail;
import org.rubnikovich.bankoperation.entity.UserPhoneNumber;
import org.rubnikovich.bankoperation.repository.EmailRepository;
import org.rubnikovich.bankoperation.repository.PhoneRepository;
import org.rubnikovich.bankoperation.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailRepository emailRepository;
    private final PhoneRepository phoneRepository;

    public UserService(UserRepository repository, BCryptPasswordEncoder passwordEncoder, EmailRepository emailRepository, PhoneRepository phoneRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.emailRepository = emailRepository;
        this.phoneRepository = phoneRepository;
    }

    public User getById(Long id) {
        return repository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public User getByLogin(String login) {
        return repository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<UserDto> getAll() {
        List<User> users = repository.findAll();
        List<UserDto> usersDto = new ArrayList<>();
        for (User user : users) {
            usersDto.add(toUserDto(user));
        }
        return usersDto;
    }

    public void create(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        List<UserPhoneNumber> phoneNumbers = user.getPhones();
        List<UserEmail> userEmails = user.getEmails();
        for (UserPhoneNumber phone : phoneNumbers) {
            phone.setUser(user);
        }
        for (UserEmail email : userEmails) {
            email.setUser(user);
        }
        for (UserPhoneNumber phone : user.getPhones()) {
            phone.setUser(user);
        }
        repository.save(user);
        phoneRepository.saveAll(phoneNumbers);
        emailRepository.saveAll(userEmails);
    }

    public boolean update(UserDto userDto, String currentLogin) {
        if (repository.findByLogin(currentLogin).isEmpty()) {
            return false;
        }
        User user = repository.findByLogin(currentLogin).orElseThrow();
        String hashedPassword = passwordEncoder.encode(userDto.getPassword());
        user.setPassword(hashedPassword);
        user.setId(user.getId());
        user.setLogin(userDto.getLogin());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        repository.save(user);
        return true;
    }

    public boolean delete(Long id) {
        if (repository.findById(id).isEmpty()) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    public Long getUserIdByLogin(String login) {
        return repository.findByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Page<UserDto> getAllByBirthDateAfter(LocalDate birthDate, Pageable pageable) {
        Page<User> usersPage = repository.findAllByBirthDateAfter(birthDate, pageable);
        return usersPage.map(this::toUserDto);
    }

    public Page<UserDto> getAllUsers(String login, LocalDate birthDate, String phone, String lastName, String email, Pageable pageable) {
        Page<User> usersPage;
        if (email != null) {
            Page<UserEmail> emailPage = emailRepository.findByEmail(email, pageable);
            List<User> users = emailPage.stream().map(UserEmail::getUser).collect(Collectors.toList());
            usersPage = new PageImpl<>(users, pageable, emailPage.getTotalElements());
        } else if (lastName != null) {
            usersPage = repository.findByLastNameLike(lastName, pageable);
        } else {
            usersPage = repository.findAll(pageable);
        }
        return usersPage.map(this::toUserDto);
    }

    private UserDto toUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setLogin(user.getLogin());
        userDto.setBalance(user.getBalance());
        userDto.setInitialDeposit(user.getInitialDeposit());
        userDto.setBirthDate(user.getBirthDate());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
//        userDto.setEmails(user.getEmails());
//        userDto.setPhones(user.getPhones());
//        userDto.setPassword(user.getPassword());
        return userDto;
    }
}
