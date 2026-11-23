package com.learn.taskflow.identity.entity;

import com.learn.taskflow.identity.enums.RoleName;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name="roles")
public class Role {
	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	private UUID id;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false, unique=true, length=64)
	@Builder.Default
	private RoleName name = RoleName.USER;
	
	@OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
	@Builder.Default
	private Set<User> users = new HashSet<>();
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "role_authorities", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "authority_id"))
	@Builder.Default
	private Set<Authority> authorities = new HashSet<>();
}
