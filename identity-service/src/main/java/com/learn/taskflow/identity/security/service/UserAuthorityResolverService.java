package com.learn.taskflow.identity.security.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.learn.taskflow.identity.entity.Authority;
import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.entity.UserPermissionOverride;
import com.learn.taskflow.identity.enums.PermissionEffect;
import com.learn.taskflow.identity.exception.RoleAuthorityMissingException;
import com.learn.taskflow.identity.exception.UserNotProvidedException;

@Service
public class UserAuthorityResolverService{

    public Collection<? extends GrantedAuthority> resolve(User user) {
    	if(user == null)
    		throw new UserNotProvidedException();
		if (user.getRole() == null) {
			throw new RoleAuthorityMissingException(user.getId());
		}
    	
    	Set<GrantedAuthority> authorities = new HashSet<>();
    	
	    	Set<Authority> roleAuthorities = user.getRole().getAuthorities();
	    	if (roleAuthorities == null) {
	    		throw new RoleAuthorityMissingException(user.getId());
	    	}
    	if(roleAuthorities.isEmpty())
    		throw new RoleAuthorityMissingException(user.getId());
    	
    	roleAuthorities.stream()
        .map(Authority::getName)
        .map(Enum::name)
        .map(SimpleGrantedAuthority::new)
        .forEach(authorities::add);

    
    	for(UserPermissionOverride override : user.getPermissionOverrides()) {
    		GrantedAuthority overrideAuthority = new SimpleGrantedAuthority(override.getAuthority().getName().name());


    		if(override.getEffect() == PermissionEffect.REVOKE)
    			authorities.remove(overrideAuthority);
    		if(override.getEffect() == PermissionEffect.GRANT)
    			authorities.add(overrideAuthority);
    	}
    	
    	return authorities;
    }
}