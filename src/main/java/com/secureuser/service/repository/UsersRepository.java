package com.secureuser.service.repository;

import com.secureuser.service.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByLogin(String login);
    Optional<Users> findByEmail(String email);
    boolean existsByLoginOrEmail(String login, String email);
}