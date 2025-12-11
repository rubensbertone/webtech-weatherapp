package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AppUserService implements UserDetailsService {

    @Autowired
    private AppUserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AppUser registerUser(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        return repo.save(new AppUser(username, encodedPassword));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(user.getUsername(), user.getPassword(), Collections.emptyList());
    }
}