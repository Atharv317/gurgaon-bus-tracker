package com.gurgaonbus.backend.stop;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "stop")

public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stop_code", unique = true, length = 100)
    private String stopCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(
            name = "location",
            nullable = false,
            columnDefinition = "geography(Point,4326)"
    )
    private Point location;

    protected Stop() {
    }
    public Stop(
            String stopCode,
            String name,
            Double latitude,
            Double longitude,
            OffsetDateTime createdAt,
            Point location
    ) {
        this.stopCode = stopCode;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
        this.location = location;
    }

    public void updateFromSource(
            String name,
            Double latitude,
            Double longitude,
            Point location
    ) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
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

    public Point getLocation() {
        return location;
    }
}