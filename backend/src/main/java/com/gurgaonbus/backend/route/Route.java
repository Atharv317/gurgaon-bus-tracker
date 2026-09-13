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

    @Column(name = "source", length = 200)
    private String source;

    @Column(name = "destination", length = 200)
    private String destination;

    @Column(name = "description")
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Route() {
    }

    public Route(
            Long agencyId,
            String routeCode,
            String source,
            String destination,
            String description,
            OffsetDateTime createdAt
    ) {
        this.agencyId = agencyId;
        this.routeCode = routeCode;
        this.source = source;
        this.destination = destination;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.active = true;

        this.name = routeCode;
    }

    public void updateFromSource(
            String source,
            String destination,
            String description
    ) {
        this.source = source;
        this.destination = destination;
        this.description = description;
        this.updatedAt = OffsetDateTime.now();
        this.active = true;
        this.name = routeCode;
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = OffsetDateTime.now();
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

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}