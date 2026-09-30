package com.example.demo;

import lombok.*;
import jakarta.persistence.*;

@Table(name="rooms")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Room {
    @Id   
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable=false)
    private String name;

    @ManyToOne(optional=false)
    @JoinColumn(name="created_by_id")
    private User createdBy;

}
