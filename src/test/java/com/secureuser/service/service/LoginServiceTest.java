package com.secureuser.service.service;

import com.secureuser.service.dto.TokenObject;
import com.secureuser.service.model.Users;
import com.secureuser.service.proto.user.auth.AuthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.secureuser.service.constants.JWTokenType.ACCESS;
import static com.secureuser.service.constants.JWTokenType.REFRESH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

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
        String rawPassword = "password";
        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();

        Users user = new Users();
        user.setIsVerified(true);
        user.setPassword("hashed-password");

        when(usersService.findByLoginOrEmail(email, email)).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(rawPassword, "hashed-password")).thenReturn(true);

        TokenObject accessToken = new TokenObject("access-token", 1);
        TokenObject refreshToken = new TokenObject("refresh-token", 3);
        when(tokenService.generateToken(user, ACCESS)).thenReturn(accessToken);
        when(tokenService.generateToken(user, REFRESH)).thenReturn(refreshToken);

        loginService.authenticationWithEmail(email, rawPassword, responseBuilder);

        AuthResponse response = responseBuilder.build();
        assertEquals(200, response.getStatusCode()); // Исправлено с 0 на 200
        assertFalse(response.hasError());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
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
        String rawPassword = "password";
        AuthResponse.Builder responseBuilder = AuthResponse.newBuilder();

        Users user = new Users();
        user.setIsVerified(false);
        user.setPassword("hashed-password");

        when(usersService.findByLoginOrEmail(email, email)).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(rawPassword, "hashed-password")).thenReturn(true);

        TokenObject accessToken = new TokenObject("access-token", 1);
        when(tokenService.generateToken(user, ACCESS)).thenReturn(accessToken);

        TokenObject refreshToken = new TokenObject("refresh-token", 3);
        when(tokenService.generateToken(user, REFRESH)).thenReturn(refreshToken);

        loginService.authenticationWithEmail(email, rawPassword, responseBuilder);

        AuthResponse response = responseBuilder.build();
        assertEquals(200, response.getStatusCode());
        assertFalse(response.hasError());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }
}