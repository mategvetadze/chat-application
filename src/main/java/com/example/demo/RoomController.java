package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import lombok.*;
import java.util.Map;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private Map<String,Object> toMap(Room room){
        return Map.of(
            "id",room.getId(),
            "name",room.getName(),
            "createdBy",room.getCreatedBy().getUsername()
        );
    }

    @PostMapping("/rooms")
    public ResponseEntity <?> create(@RequestBody Room incoming,Authentication auth){
        User creator= userRepository.findByUsername(auth.getName()).orElseThrow();
        Room room = new Room();
        room.setName(incoming.getName());
        room.setCreatedBy(creator);
        roomRepository.save(room);
        return ResponseEntity.status(201).body(toMap(room));

    }  

    @GetMapping("/rooms")
    public List<Map<String,Object>> list(){
        return roomRepository.findAll().stream().map(this::toMap).toList();
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity <?> delete(@PathVariable Long id,Authentication auth){
        var roomOpt = roomRepository.findById(id);
        if(roomOpt.isEmpty()){
            return ResponseEntity.status(404).body(Map.of("error","not found"));
        }
        Room room = roomOpt.get();
        boolean owner = room.getCreatedBy().getUsername().equals(auth.getName());
        boolean admin = auth.getAuthorities().stream().anyMatch(a-> a.getAuthority().equals("ROLE_ADMIN"));
        if(!owner && !admin){
            return ResponseEntity.status(403).body(Map.of("error","forbidden"));
        }
        roomRepository.delete(room);
        return ResponseEntity.noContent().build();

    }
    
}
