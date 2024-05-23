package org.rubnikovich.bankoperation.controller.service;

import org.rubnikovich.bankoperation.controller.entity.User;
import org.rubnikovich.bankoperation.controller.repository.UserRepository;
import org.rubnikovich.bankoperation.controller.security.UsersDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public UsersDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Optional<User> user = repository.findByLogin(login);
        if (user.isEmpty())
            throw new UsernameNotFoundException("User not found");
        return new UsersDetails(user.get());
    }
}

