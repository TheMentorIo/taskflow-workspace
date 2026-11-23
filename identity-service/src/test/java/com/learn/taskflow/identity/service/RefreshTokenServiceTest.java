package com.learn.taskflow.identity.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learn.taskflow.identity.entity.RefreshToken;
import com.learn.taskflow.identity.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    @Test
    void consumesUsingLockedLookup() {
        RefreshToken token = RefreshToken.builder()
                .tokenHash("hash")
                .familyId(UUID.randomUUID())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        when(repository.findByTokenHashForUpdate(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(token));
        RefreshTokenService service = new RefreshTokenService(repository);

        assertThat(service.consume("raw-token")).isSameAs(token);
        verify(repository).findByTokenHashForUpdate(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void replayRevokesTheWholeFamily() {
        UUID familyId = UUID.randomUUID();
        RefreshToken token = RefreshToken.builder()
                .tokenHash("hash")
                .familyId(familyId)
                .usedAt(LocalDateTime.now().minusMinutes(1))
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        when(repository.findByTokenHashForUpdate(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(token));
        RefreshTokenService service = new RefreshTokenService(repository);

        assertThatThrownBy(() -> service.consume("raw-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token replay detected");
        verify(repository).revokeFamily(familyId);
    }
}
