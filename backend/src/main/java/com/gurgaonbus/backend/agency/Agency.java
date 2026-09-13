package com.gurgaonbus.backend.agency;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "agency")
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Agency() {
    }
    public Agency(String name, String code, OffsetDateTime createdAt) {
        this.name = name;
        this.code = code;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}