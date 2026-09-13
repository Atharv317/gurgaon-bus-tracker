package com.gurgaonbus.backend.routestop;

import com.gurgaonbus.backend.routestop.dto.RouteStopResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteStopController {

    private final RouteStopService routeStopService;

    public RouteStopController(RouteStopService routeStopService) {
        this.routeStopService = routeStopService;
    }

    @GetMapping("/{routeId}/stops")
    public List<RouteStopResponse> getStopsByRoute(@PathVariable Long routeId) {
        return routeStopService.getStopsByRoute(routeId);
    }
}