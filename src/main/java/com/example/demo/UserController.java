package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import lombok.*;


import java.util.Map;


@RestController
@RequiredArgsConstructor
public class UserController{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
        if(userRepository.existsByUsername(user.getUsername()) 
                || userRepository.existsByEmail(user.getEmail() )){
            return ResponseEntity.status(409).body("username or Email already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        return ResponseEntity.status(201).body(
             Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
            )
        );
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request){
        var userOpt=userRepository.findByUsername(request.getUsername());
        if(userOpt.isEmpty()){
            return ResponseEntity.status(401).body(Map.of("error","invalid creditentials"));
        }
        User user = userOpt.get();
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            return ResponseEntity.status(401).body(Map.of("error","invalid creditentials"));
        }
        return ResponseEntity.ok(Map.of(
             "id", user.getId(),
            "username", user.getUsername(),
            "token", jwtService.createToken(
                    user.getUsername(),
                    user.getRole() == null ? "USER" : user.getRole())
        ));
    }
    @GetMapping("/me")
    public Map<String,String> me(Authentication authentication){
        return Map.of("username",authentication.getName());
    }
    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String,String> adminPing(){
        return Map.of("ok","admin");
    }    
}