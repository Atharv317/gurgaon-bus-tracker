package com.gurgaonbus.backend.routestop;

import jakarta.persistence.*;

@Entity
@Table(
        name = "route_stop",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_route_stop",
                        columnNames = {"route_id", "direction", "stop_id"}
                )
        }
)
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "stop_id", nullable = false)
    private Long stopId;

    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(name = "direction", length = 50)
    private String direction;

    protected RouteStop() {
    }
    RouteStop(
            Long routeId,
            Long stopId,
            Integer stopSequence,
            String direction
    ) {
        this.routeId = routeId;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.direction = direction;
    }

    public Long getId() {
        return id;
    }

    public Long getRouteId() {
        return routeId;
    }

    public Long getStopId() {
        return stopId;
    }

    public Integer getStopSequence() {
        return stopSequence;
    }

    public String getDirection() {
        return direction;
    }
}