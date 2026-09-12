package com.gurgaonbus.backend.route;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "route",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_route_agency_code",
                        columnNames = {"agency_id", "route_code"}
                )
        }
)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_id", nullable = false)
    private Long agencyId;

    @Column(name = "route_code", nullable = false, length = 50)
    private String routeCode;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Route() {
    }

    public Long getId() {
        return id;
    }

    public Long getAgencyId() {
        return agencyId;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}