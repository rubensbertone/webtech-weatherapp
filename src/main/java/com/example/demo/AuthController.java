package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"https://weatherapp-frontend-orzv.onrender.com", "http://localhost:5173"})
public class AuthController {

    @Autowired
    AppUserService userService;

    @PostMapping("/register")
    public AppUser register(@RequestBody AppUser user) {
        return userService.registerUser(user.getUsername(), user.getPassword());
    }

    @GetMapping("/login")
    public String login() {
        return "Du bist erfolgreich eingeloggt!";
    }
}