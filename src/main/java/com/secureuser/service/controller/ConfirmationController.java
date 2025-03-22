package com.secureuser.service.controller;

import com.secureuser.service.service.RegistrationConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ConfirmationController {

    @Value("${spring.user.registration.require-verification}")
    private boolean isRequireVerification;

    private final RegistrationConfirmationService registrationConfirmationService;

    @GetMapping("/confirm/{uuid}")
    public ResponseEntity<String> confirm(@PathVariable String uuid) {
        if (!isRequireVerification)
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("The service is temporarily unavailable");
        return registrationConfirmationService.validateAndConfirmRegistration(uuid);
    }
}