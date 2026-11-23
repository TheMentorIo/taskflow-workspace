package com.learn.taskflow.identity.security.service;

import java.util.Locale;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.repository.UserRepository;

import lombok.AllArgsConstructor;
@AllArgsConstructor
@Service
public class UserAuthenticationService {
    private final UserRepository userRepository;
    public User loadByEmail(String email){
        return userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email.trim().toLowerCase(Locale.ROOT)).orElseThrow(() ->
                    new UsernameNotFoundException("Invalid credentials"));
    }

}
