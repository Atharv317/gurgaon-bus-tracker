package com.gurgaonbus.backend.routestop;

import com.gurgaonbus.backend.routestop.dto.RouteStopResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteStopService {

    private final RouteStopRepository routeStopRepository;

    public RouteStopService(RouteStopRepository routeStopRepository) {
        this.routeStopRepository = routeStopRepository;
    }

    public List<RouteStopResponse> getStopsByRoute(Long routeId) {
        return routeStopRepository.findByRouteIdOrderByStopSequence(routeId)
                .stream()
                .map(routeStop -> new RouteStopResponse(
                        routeStop.getStopId(),
                        routeStop.getStopSequence(),
                        routeStop.getDirection()
                ))
                .toList();
    }
}