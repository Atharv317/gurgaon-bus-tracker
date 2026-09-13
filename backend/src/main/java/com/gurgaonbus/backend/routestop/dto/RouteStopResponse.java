package com.gurgaonbus.backend.routestop.dto;

public record RouteStopResponse(
        Long stopId,
        Integer sequence,
        String direction
) {
}