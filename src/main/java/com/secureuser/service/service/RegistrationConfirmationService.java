package com.secureuser.service.service;

import com.secureuser.service.exception.DatabaseOperationException;
import com.secureuser.service.model.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationConfirmationService {

    @Value("${server.url}")
    private String baseUrl;
    @Value("${server.confirmation-endpoint:/api/auth/confirm}")
    private String confirmationEndpoint;
    @Value("${spring.user.registration.require-verification}")
    private boolean isRequireVerification;
    @Value("${spring.user.confirmation-link.retention-time}")
    private long time;

    private final RedisService redisService;
    private final UsersService usersService;

    public Map<String, String> save(String login) {
        if (!isRequireVerification) {
            log.info("The function to create a link to confirm registration is disabled");
            return null;
        }

        Map<String, String> value = generateUrlConfirm();

        boolean isSaved = redisService.save(value.get("token"), login, time);
        if (!isSaved) {
            String message = String.format("Failed to save confirmation link in Redis for user [%s]", login);
            log.error(message);
            throw new RuntimeException(message);
        }
        log.info("Saved confirmation token [{}] for user [{}] with TTL [{} minutes]", value.get("token"), login, time);
        return value;
    }

    public ResponseEntity<String> validateAndConfirmRegistration(String uuid) {
        Optional<String> loginOptional = findByToken(uuid);

        if (loginOptional.isEmpty()) {
            log.warn("Invalid or expired confirmation token: {}", uuid);
            return ResponseEntity.status(HttpStatus.GONE).body("Link is inactive or out of date");
        }

        Optional<Users> userOptional = usersService.findByLogin(loginOptional.get());
        if (userOptional.isEmpty()) {
            log.warn("User not found for token: {}", uuid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        try {
            Users user = userOptional.get();
            user.setIsVerified(true);
            usersService.update(user);
            log.info("User [{}] successfully verified", user.getLogin());
            deleteToken(uuid);
            log.info("Confirmation token [{}] has been removed from Redis", uuid);
        } catch (DatabaseOperationException e) {
            log.error("Error while updating user verification status", e);
            return ResponseEntity.internalServerError().body("Internal error during verification");
        }

        return ResponseEntity.ok("Account registration confirmed");
    }

    public Optional<String> findByToken(String token) {
        return redisService.get(token);
    }

    public void deleteToken(String token) {
        redisService.delete(token);
    }

    private Map<String, String> generateUrlConfirm() {
        String uuid = UUID.randomUUID().toString();
        String confirmationUrl = String.format("%s%s/%s", baseUrl, confirmationEndpoint, uuid);
        return Map.of(
                "url", confirmationUrl,
                "token", uuid
        );
    }
}