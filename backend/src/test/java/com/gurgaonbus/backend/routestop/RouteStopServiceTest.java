package com.gurgaonbus.backend.routestop;

import com.gurgaonbus.backend.routestop.dto.RouteStopResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteStopServiceTest {

    @Mock
    private RouteStopRepository routeStopRepository;

    @InjectMocks
    private RouteStopService routeStopService;

    @Test
    void shouldReturnStopsForRoute() {
        RouteStop firstStop =
                new RouteStop(1L, 10L, 1, "TOWARDS_GURGAON");

        RouteStop secondStop =
                new RouteStop(1L, 20L, 2, "TOWARDS_GURGAON");

        when(routeStopRepository.findByRouteIdOrderByStopSequence(1L))
                .thenReturn(List.of(firstStop, secondStop));

        List<RouteStopResponse> result =
                routeStopService.getStopsByRoute(1L);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).stopId()).isEqualTo(10L);
        assertThat(result.get(0).sequence()).isEqualTo(1);
        assertThat(result.get(0).direction())
                .isEqualTo("TOWARDS_GURGAON");

        assertThat(result.get(1).stopId()).isEqualTo(20L);
        assertThat(result.get(1).sequence()).isEqualTo(2);
    }
}