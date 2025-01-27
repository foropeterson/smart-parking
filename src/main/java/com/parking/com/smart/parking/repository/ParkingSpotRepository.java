package com.parking.com.smart.parking.repository;

import com.parking.com.smart.parking.entities.ParkingSpot;
import com.parking.com.smart.parking.entities.SpotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    Page<ParkingSpot> findByStatus(SpotStatus status, Pageable pageable);
    Page<ParkingSpot> findByStatusAndSpotLocation(SpotStatus status, String location, Pageable pageable);
    Page<ParkingSpot> findBySpotLocation(String location, Pageable pageable);
    Page<ParkingSpot> findAll(Pageable pageable);
    ParkingSpot findBySpotId(Long spotId);

}
