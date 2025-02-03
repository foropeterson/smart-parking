package com.parking.com.smart.parking.repository;

import com.parking.com.smart.parking.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByIsPaymentCompletedFalse();
    Optional<Payment> findByBooking_BookingId(Long bookingId);
}
