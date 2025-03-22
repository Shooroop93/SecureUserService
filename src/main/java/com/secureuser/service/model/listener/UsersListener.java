package com.secureuser.service.model.listener;

import com.secureuser.service.model.Users;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class UsersListener {

    @PrePersist
    public void beforeCreate(Users users) {
        LocalDateTime now = LocalDateTime.now();
        users.setCreatedAt(now);
        users.setUpdatedAt(now);
        if (users.getIsVerified() == null) {
            users.setIsVerified(false);
        }
        log.info("Creating user with login: {}", users.getLogin());
    }

    @PreUpdate
    public void beforeUpdate(Users users) {
        users.setUpdatedAt(LocalDateTime.now());
        log.info("Updating user with login: {}", users.getLogin());
    }

    @PostRemove
    public void afterRemove(Users users) {
        log.info("User removed with login: {}", users.getLogin());
    }
}