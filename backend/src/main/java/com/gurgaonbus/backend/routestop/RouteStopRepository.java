package com.gurgaonbus.backend.routestop;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    List<RouteStop> findByRouteIdOrderByStopSequence(Long routeId);

    List<RouteStop> findByRouteIdAndDirectionOrderByStopSequence(
            Long routeId,
            String direction
    );

    List<RouteStop> findByRouteIdOrderByDirectionAscStopSequenceAsc(
            Long routeId
    );

    boolean existsByRouteIdAndDirectionAndStopSequence(
            Long routeId,
            String direction,
            Integer stopSequence
    );
}