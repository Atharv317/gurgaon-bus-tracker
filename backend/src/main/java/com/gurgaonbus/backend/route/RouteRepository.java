package com.gurgaonbus.backend.route;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findByAgencyIdAndRouteCode(
            Long agencyId,
            String routeCode
    );

    List<Route> findAllByAgencyId(Long agencyId);
}