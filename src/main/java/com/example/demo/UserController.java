package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;



@RestController
@RequestMapping("/register")
public class UserController{
    private final UserRepository userRepository;
	public UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
    @PostMapping
    public ResponseEntity<?> register(@RequestBody User user){
        if(userRepository.existsByUsername(user.getUsername()) 
                || userRepository.existsByEmail(user.getEmail() )){
            return ResponseEntity.status(409).body("username or Email already taken");
        }
        userRepository.save(user);
        user.setPassword(null);
        return ResponseEntity.status(201).body(user);
    }
    
}