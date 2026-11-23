package com.learn.taskflow.identity.entity;

import com.learn.taskflow.identity.enums.PermissionEffect;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_permission_overrides")
public class UserPermissionOverride {
	@EmbeddedId
	private UserPermissionOverrideId id;
	
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("userId")
	@JoinColumn(name="user_id",nullable = false)
	User user;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("authorityId")
	@JoinColumn(name="authority_id",nullable = false)
	Authority authority ;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private PermissionEffect effect;
	
}
