package com.secureuser.service.service;

import com.secureuser.service.exception.DatabaseOperationException;
import com.secureuser.service.model.Users;
import com.secureuser.service.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UsersServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private UsersService usersService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave_Success() throws DatabaseOperationException {
        when(encoder.encode(anyString())).thenReturn("encodedPassword");
        when(usersRepository.save(any(Users.class))).thenReturn(new Users());
        assertDoesNotThrow(() -> usersService.save("testLogin", "test@example.com", "password"));
    }

    @Test
    void testSave_Failure() {
        when(encoder.encode(anyString())).thenReturn("encodedPassword");
        doThrow(new RuntimeException("DB error")).when(usersRepository).save(any(Users.class));
        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
                () -> usersService.save("testLogin", "test@example.com", "password"));
        assertEquals("An error occurred while saving a user", exception.getMessage());
    }

    @Test
    void testUpdate_Success() throws DatabaseOperationException {
        Users user = new Users();
        user.setLogin("testLogin");
        when(usersRepository.save(any(Users.class))).thenReturn(user);
        assertDoesNotThrow(() -> usersService.update(user));
    }

    @Test
    void testUpdate_Failure() {
        Users user = new Users();
        user.setLogin("testLogin");
        doThrow(new RuntimeException("DB error")).when(usersRepository).save(any(Users.class));
        DatabaseOperationException exception = assertThrows(DatabaseOperationException.class,
                () -> usersService.update(user));
        assertEquals("An error occurred while update a user", exception.getMessage());
    }

    @Test
    void testFindByLogin_Found() {
        Users user = new Users();
        user.setLogin("testLogin");
        when(usersRepository.findByLogin("testLogin")).thenReturn(Optional.of(user));
        Optional<Users> result = usersService.findByLogin("testLogin");
        assertTrue(result.isPresent());
        assertEquals("testLogin", result.get().getLogin());
    }

    @Test
    void testFindByLogin_NotFound() {
        when(usersRepository.findByLogin("testLogin")).thenReturn(Optional.empty());
        Optional<Users> result = usersService.findByLogin("testLogin");
        assertFalse(result.isPresent());
    }

    @Test
    void testExistsByLoginOrEmail() {
        when(usersRepository.existsByLoginOrEmail("testLogin", "test@example.com")).thenReturn(true);
        boolean exists = usersService.existsByLoginOrEmail("testLogin", "test@example.com");
        assertTrue(exists);
    }
}