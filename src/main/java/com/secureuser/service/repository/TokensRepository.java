package com.secureuser.service.repository;

import com.secureuser.service.model.Tokens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokensRepository extends JpaRepository<Tokens, UUID> {

    Optional<Tokens> findByToken(String token);
}