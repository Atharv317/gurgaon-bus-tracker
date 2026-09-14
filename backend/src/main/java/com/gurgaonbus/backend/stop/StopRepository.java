package com.gurgaonbus.backend.stop;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StopRepository extends JpaRepository<Stop, Long> {

    Optional<Stop> findByStopCode(String stopCode);

}