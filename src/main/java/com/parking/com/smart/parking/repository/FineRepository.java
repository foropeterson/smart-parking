package com.parking.com.smart.parking.repository;

import com.parking.com.smart.parking.entities.Fine;
import com.parking.com.smart.parking.entities.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByUser_UserIdAndStatus(Integer userId, FineStatus status);

    Fine findByBooking_BookingId(Long bookingId);

    List<Fine> findByUser_UserId(Long userid);

}
