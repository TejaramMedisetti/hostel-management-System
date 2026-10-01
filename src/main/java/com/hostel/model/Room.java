package com.hostel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor
public class Room {
    public enum RoomType { SINGLE, DOUBLE, TRIPLE, DORMITORY }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roomNumber;

    private String block;
    private int floor;
    private int capacity;

    @Enumerated(EnumType.STRING)
    private RoomType type;

    private BigDecimal monthlyRent;
}
