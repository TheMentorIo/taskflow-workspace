package com.learn.taskflow.identity.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class UserPermissionOverrideId {
	@Column(name = "user_id")
    private UUID userId;

    @Column(name = "authority_id")
    private UUID authorityId;
}
