package com.learn.taskflow.identity.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.enums.AccountStatus;

class AccountStatePolicyTest {

    private final AccountStatePolicy policy = new AccountStatePolicy();

    @Test
    void rejectsLockedAccounts() {
        User user = User.builder().accountStatus(AccountStatus.LOCKED).build();

        assertThatThrownBy(() -> policy.requireActive(user))
                .isInstanceOf(org.springframework.security.authentication.LockedException.class);
    }

    @Test
    void acceptsActiveAccounts() {
        User user = User.builder().accountStatus(AccountStatus.ACTIVE).build();

        policy.requireActive(user);
    }
}
