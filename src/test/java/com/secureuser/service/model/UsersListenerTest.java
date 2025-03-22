package com.secureuser.service.model;

import com.secureuser.service.model.listener.UsersListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class UsersListenerTest {

    private UsersListener usersListener;
    private Users user;

    @BeforeEach
    void setUp() {
        usersListener = new UsersListener();
        user = new Users("test@example.com", "testLogin", "testPassword");
    }

    @Test
    void testBeforeCreate_SetsTimestampsAndDefaultVerification() {
        usersListener.beforeCreate(user);
        assertNotNull(user.getCreatedAt(), "CreatedAt should be set");
        assertNotNull(user.getUpdatedAt(), "UpdatedAt should be set");
        assertFalse(user.getIsVerified(), "isVerified should be false by default");
    }

    @Test
    void testBeforeCreate_PreservesIsVerifiedIfSet() {
        user.setIsVerified(true);
        usersListener.beforeCreate(user);
        assertTrue(user.getIsVerified(), "isVerified should remain true if already set");
    }

    @Test
    void testBeforeUpdate_UpdatesUpdatedAt() throws InterruptedException {
        LocalDateTime initialUpdateTime = LocalDateTime.now();
        user.setUpdatedAt(initialUpdateTime);
        Thread.sleep(10);
        usersListener.beforeUpdate(user);
        assertTrue(user.getUpdatedAt().isAfter(initialUpdateTime), "UpdatedAt should be updated to a later time");
    }

    @Test
    void testAfterRemove_LogsRemoval() {
        Users spyUser = spy(user);
        usersListener.afterRemove(spyUser);
        verify(spyUser).getLogin();
    }
}