package com.secureuser.service.service;

import com.secureuser.service.exception.DatabaseOperationException;
import com.secureuser.service.model.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RegistrationConfirmationServiceTest {

    @Mock
    private RedisService redisService;

    @Mock
    private UsersService usersService;

    @InjectMocks
    private RegistrationConfirmationService confirmationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(confirmationService, "baseUrl", "http://localhost");
        ReflectionTestUtils.setField(confirmationService, "confirmationEndpoint", "/api/auth/confirm");
        ReflectionTestUtils.setField(confirmationService, "isRequireVerification", true);
        ReflectionTestUtils.setField(confirmationService, "time", 60L);
    }


    @Test
    void testSave_Success() {
        when(redisService.save(anyString(), anyString(), anyLong())).thenReturn(true);

        Map<String, String> result = confirmationService.save("testLogin");

        assertNotNull(result);
        assertTrue(result.containsKey("token"));
        assertTrue(result.containsKey("url"));
    }

    @Test
    void testSave_Failure() {
        when(redisService.save(anyString(), anyString(), anyLong())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> confirmationService.save("testLogin"));

        assertEquals("Failed to save confirmation link in Redis for user [testLogin]", exception.getMessage());
    }

    @Test
    void testValidateAndConfirmRegistration_Success() throws DatabaseOperationException {
        String uuid = "uuid-token";
        String login = "testLogin";

        when(redisService.get(uuid)).thenReturn(Optional.of(login));
        Users user = new Users();
        user.setLogin(login);
        when(usersService.findByLogin(login)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = confirmationService.validateAndConfirmRegistration(uuid);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account registration confirmed", response.getBody());
        verify(usersService).update(user);
        verify(redisService).delete(uuid);
    }

    @Test
    void testValidateAndConfirmRegistration_TokenNotFound() {
        when(redisService.get(anyString())).thenReturn(Optional.empty());

        ResponseEntity<String> response = confirmationService.validateAndConfirmRegistration("uuid-token");

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals("Link is inactive or out of date", response.getBody());
    }

    @Test
    void testValidateAndConfirmRegistration_UserNotFound() {
        String uuid = "uuid-token";

        when(redisService.get(uuid)).thenReturn(Optional.of("nonExistentLogin"));
        when(usersService.findByLogin("nonExistentLogin")).thenReturn(Optional.empty());

        ResponseEntity<String> response = confirmationService.validateAndConfirmRegistration(uuid);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void testValidateAndConfirmRegistration_DatabaseError() throws DatabaseOperationException {
        String uuid = "uuid-token";
        String login = "testLogin";

        when(redisService.get(uuid)).thenReturn(Optional.of(login));
        Users user = new Users();
        user.setLogin(login);
        when(usersService.findByLogin(login)).thenReturn(Optional.of(user));
        doThrow(new DatabaseOperationException("Database error")).when(usersService).update(user);

        ResponseEntity<String> response = confirmationService.validateAndConfirmRegistration(uuid);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error during verification", response.getBody());
    }
}