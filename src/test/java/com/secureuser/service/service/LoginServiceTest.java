package com.secureuser.service.service;

import com.secureuser.service.model.Users;
import com.secureuser.service.proto.user.auth.AuthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UsersService usersService;

    @InjectMocks
    private LoginService loginService;

    @Test
    void authenticationWithEmail_userNotFound_setsUnauthorizedError() {
        ReflectionTestUtils.setField(loginService, "isRequireVerification", true);
        String email = "unknown@example.com";
        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();

        when(usersService.findByLoginOrEmail(email, email)).thenReturn(Optional.empty());

        loginService.authenticationWithEmail(email, "password", responseBuilder);

        AuthResponse response = responseBuilder.build();
        assertEquals(401, response.getStatusCode());
        assertEquals("INVALID_CREDENTIALS", response.getMessageCode());
        assertTrue(response.hasError());
    }

    @Test
    void authenticationWithEmail_userNotVerified_setsForbiddenError() {
        ReflectionTestUtils.setField(loginService, "isRequireVerification", true);
        String email = "user@example.com";
        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();

        Users user = new Users();
        user.setIsVerified(false);

        when(usersService.findByLoginOrEmail(email, email)).thenReturn(Optional.of(user));

        loginService.authenticationWithEmail(email, "password", responseBuilder);

        AuthResponse response = responseBuilder.build();
        assertEquals(403, response.getStatusCode());
        assertEquals("ACCOUNT_NOT_VERIFIED", response.getMessageCode());
        assertTrue(response.hasError());
    }

    @Test
    void authenticationWithEmail_userVerified_noError() {
        ReflectionTestUtils.setField(loginService, "isRequireVerification", true);
        String email = "verified@example.com";
        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();

        Users user = new Users();
        user.setIsVerified(true);

        when(usersService.findByLoginOrEmail(email, email)).thenReturn(Optional.of(user));

        loginService.authenticationWithEmail(email, "password", responseBuilder);

        AuthResponse response = responseBuilder.build();
        assertEquals(0, response.getStatusCode());
        assertFalse(response.hasError());
    }

    @Test
    void authenticationWithEmail_verificationDisabled_userNotFound_setsUnauthorizedError() {
        ReflectionTestUtils.setField(loginService, "isRequireVerification", false);
        String email = "unknown@example.com";
        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();

        when(usersService.findByLoginOrEmail(email, email)).thenReturn(Optional.empty());

        loginService.authenticationWithEmail(email, "password", responseBuilder);

        AuthResponse response = responseBuilder.build();
        assertEquals(401, response.getStatusCode());
        assertEquals("INVALID_CREDENTIALS", response.getMessageCode());
        assertTrue(response.hasError());
    }

    @Test
    void authenticationWithEmail_verificationDisabled_userExists_noError() {
        ReflectionTestUtils.setField(loginService, "isRequireVerification", false);
        String email = "user@example.com";
        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();

        Users user = new Users();
        user.setIsVerified(false);

        when(usersService.findByLoginOrEmail(email, email)).thenReturn(Optional.of(user));

        loginService.authenticationWithEmail(email, "password", responseBuilder);

        AuthResponse response = responseBuilder.build();
        assertEquals(0, response.getStatusCode());
        assertFalse(response.hasError());
    }
}