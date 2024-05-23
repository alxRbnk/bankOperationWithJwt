package org.rubnikovich.bankoperation.controller.repository;

import org.rubnikovich.bankoperation.controller.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);

    List<User> findAll();

    Page<User> findAllByBirthDateAfter(LocalDate birthDate, Pageable pageable);

    Page<User> findByLastName(String lastName, Pageable pageable);

}