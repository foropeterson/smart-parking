package com.parking.com.smart.parking.repository;

import com.parking.com.smart.parking.entities.Booking;
import com.parking.com.smart.parking.entities.BookingStatus;
import com.parking.com.smart.parking.entities.PaymentStatus;
import com.parking.com.smart.parking.entities.SpotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByEndTimeBeforeAndParkingSpot_StatusAndBookingStatus(LocalDateTime endTime, SpotStatus status,BookingStatus bookingStatus);

    Page<Booking> findByUser_UserIdAndPaymentStatus(Integer userId, PaymentStatus paymentStatus, Pageable pageable);

    Page<Booking> findByUser_UserId(Integer userId, Pageable pageable);

    Optional<Booking> findByBookingId(Long bookingId);

    Page<Booking> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    Page<Booking> findAll(Pageable pageable);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Booking b WHERE b.paymentStatus = 'PAID'")
    int getSumOfPaidAmounts();

    @Query("SELECT COALESCE(COUNT(b.bookingId), 0) FROM Booking b WHERE b.paymentStatus = 'PAID'")
    int getTotalBookings();
}
