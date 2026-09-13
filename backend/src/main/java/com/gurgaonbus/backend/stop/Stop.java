package com.gurgaonbus.backend.stop;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "stop")
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stop_code", unique = true, length = 50)
    private String stopCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Stop() {
    }

    public Long getId() {
        return id;
    }

    public String getStopCode() {
        return stopCode;
    }

    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}