package com.hostel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
public class Payment {
    public enum Status { PENDING, PAID }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Student student;

    private BigDecimal amount;
    private String forMonth;      // e.g. 2026-10
    private LocalDate paidOn;
    private String method;        // CASH / UPI / CARD

    @Enumerated(EnumType.STRING)
    private Status status;
}
