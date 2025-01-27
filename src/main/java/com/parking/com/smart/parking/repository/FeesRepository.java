package com.parking.com.smart.parking.repository;

import com.parking.com.smart.parking.entities.Fees;
import com.parking.com.smart.parking.entities.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeesRepository extends JpaRepository<Fees, Long> {
    Fees findByVehicleType(VehicleType vehicleType);

}