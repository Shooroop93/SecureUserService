package com.secureuser.service.service;

import com.secureuser.service.exception.DatabaseOperationException;
import com.secureuser.service.model.Users;
import com.secureuser.service.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void save(String login, String email, String password) throws DatabaseOperationException {
        String encodePassword = encoder.encode(password);
        try {
            usersRepository.save(new Users(email, login, encodePassword));
            log.info("User saved to database: {}", login);
        } catch (Exception e) {
            log.error("Error while saving user: {}", e.getMessage(), e);
            throw new DatabaseOperationException("An error occurred while saving a user", e);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void update(Users users) throws DatabaseOperationException {
        try {
            usersRepository.save(users);
            log.info("User update to database: {}", users.getLogin());
        } catch (Exception e) {
            log.error("Error while update user: {}", e.getMessage(), e);
            throw new DatabaseOperationException("An error occurred while update a user", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Users> findByLogin(String login) {
        return usersRepository.findByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<Users> findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByLoginOrEmail(String login, String email) {
        return usersRepository.existsByLoginOrEmail(login, email);
    }

    @Transactional(readOnly = true)
    public Optional<Users> findByLoginOrEmail(String login, String email) {
        return usersRepository.findByLoginOrEmail(login, email);
    }
}