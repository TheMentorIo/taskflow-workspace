package com.learn.taskflow.identity.service;

import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.enums.AccountStatus;

@Service
public class AccountStatePolicy {

    public void requireActive(User user) {
        if (user == null) {
            throw new LockedException("Account is not active");
        }
        if (user.getAccountStatus() == AccountStatus.LOCKED
                && user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new LockedException("Account is temporarily locked");
        }
        if (user.getAccountStatus() == AccountStatus.LOCKED
                && user.getLockedUntil() != null
                && !user.getLockedUntil().isAfter(LocalDateTime.now())) {
            user.setAccountStatus(AccountStatus.ACTIVE);
            user.setLockedUntil(null);
        }
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new LockedException("Account is not active");
        }
    }
}
