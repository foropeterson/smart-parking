package com.parking.com.smart.parking.service.impl;

import com.parking.com.smart.parking.entities.*;
import com.parking.com.smart.parking.repository.*;
import com.parking.com.smart.parking.request.BookingRequest;
import com.parking.com.smart.parking.response.ApiPaginatedResponse;
import com.parking.com.smart.parking.response.ApiResponse;
import com.parking.com.smart.parking.service.AuditLogService;
import com.parking.com.smart.parking.service.BookingService;
import com.parking.com.smart.parking.service.EmailService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {
    @Value("${stripe.api_secret_key}")
    private String stripeSecretKey;
    private final PaymentRepository paymentRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final BookingRepository bookingRepository;
    private final FeesRepository feesRepository;
    private final FineRepository fineRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    private static final int GRACE_PERIOD_MINUTES = 10;

    public BookingServiceImpl(PaymentRepository paymentRepository, ParkingSpotRepository parkingSpotRepository, BookingRepository bookingRepository, FeesRepository feesRepository, FineRepository fineRepository, UserRepository userRepository, EmailService emailService, AuditLogService auditLogService) {
        this.paymentRepository = paymentRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.bookingRepository = bookingRepository;
        this.feesRepository = feesRepository;
        this.fineRepository = fineRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    @Override
    public ApiResponse createBooking(BookingRequest bookingRequest) {
        User user = userRepository.findById(bookingRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        ParkingSpot spot = parkingSpotRepository.findById(bookingRequest.getSpotId())
                .orElseThrow(() -> new RuntimeException("Parking spot not found"));
        if (!spot.getStatus().equals(SpotStatus.AVAILABLE)) {
            return new ApiResponse("400", "Parking spot not available", null);
        }
        VehicleType vehicleType;
        try {
            vehicleType = VehicleType.valueOf(bookingRequest.getVehicleType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid vehicle type provided: " + bookingRequest.getVehicleType());
        }
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setParkingSpot(spot);
        booking.setVehicleRegistration(bookingRequest.getVehicleRegistration());
        booking.setVehicleType(vehicleType);
        booking.setStartTime(bookingRequest.getStartTime());
        booking.setEndTime(bookingRequest.getEndTime());
        booking.setAmount(bookingRequest.getAmount());
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setBookingStatus(BookingStatus.ACTIVE);
        bookingRepository.save(booking);
        spot.setStatus(SpotStatus.AVAILABLE);
        parkingSpotRepository.save(spot);
        auditLogService.logAction("Booking", booking.getBookingId(), "CREATE",
                "Booking created for user " + user.getUserId() + " and spot " + spot.getSpotId());

        return new ApiResponse("201", "Booking successful", booking);
    }

    @Override
    public double calculateTotalAmount(Integer userId, String vehicleType, LocalDateTime startTime, LocalDateTime endTime) {
        List<Fine> fines = fineRepository.findByUser_UserIdAndStatus(userId, FineStatus.PENDING);
        double totalFineAmount = fines.stream().mapToDouble(Fine::getAmount).sum();
        VehicleType type;
        try {
            type = VehicleType.valueOf(vehicleType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid vehicle type provided: " + vehicleType);
        }
        Fees fee = feesRepository.findByVehicleType(type);
        if (fee == null) {
            throw new RuntimeException("Fee not found for vehicle type: " + vehicleType);
        }
        long durationInMinutes = Duration.between(startTime, endTime).toMinutes();
        double durationHours = Math.ceil((double) durationInMinutes / 60);
        double bookingFee = fee.getHourlyRate() * durationHours;
        auditLogService.logAction("Booking", null, "CALCULATE_AMOUNT",
                "Calculated amount for user " + userId + " with vehicle type " + vehicleType);

        return bookingFee + totalFineAmount;
    }

    @Override
    public ApiPaginatedResponse getBookings(int page, int size, Integer userId, String paymentStatus) {
        if (page > 0) {
            page = page - 1;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingsPage;
        switch (paymentStatus != null ? paymentStatus.toUpperCase() : "ALL") {
            case "PAID":
                bookingsPage = bookingRepository.findByUser_UserIdAndPaymentStatus(userId, PaymentStatus.PAID, pageable);
                break;
            case "PENDING":
                bookingsPage = bookingRepository.findByUser_UserIdAndPaymentStatus(userId, PaymentStatus.PENDING, pageable);
                break;
            case "INITIATED":
                bookingsPage = bookingRepository.findByUser_UserIdAndPaymentStatus(userId, PaymentStatus.INITIATED, pageable);
                break;
            case "ALL":
            default:
                bookingsPage = bookingRepository.findByUser_UserId(userId, pageable);
                break;
        }
        if (bookingsPage.isEmpty()) {
            return new ApiPaginatedResponse(
                    "404",
                    "No bookings found",
                    null,
                    0,
                    0,
                    page + 1
            );
        }
        // Audit logging
        auditLogService.logAction("Booking", null, "GET_USER_BOOKINGS",
                "Retrieved bookings for user " + userId + " with payment status " + paymentStatus);

        return new ApiPaginatedResponse(
                "200",
                "Bookings retrieved successfully",
                bookingsPage.getContent(),
                bookingsPage.getTotalElements(),
                bookingsPage.getTotalPages(),
                bookingsPage.getNumber() + 1
        );
    }

    @Override
    public ApiPaginatedResponse getAllBookings(int page, int size, String paymentStatus) {
        if (page > 0) {
            page = page - 1;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingsPage;

        switch (paymentStatus != null ? paymentStatus.toUpperCase() : "ALL") {
            case "PAID":
                bookingsPage = bookingRepository.findByPaymentStatus(PaymentStatus.PAID, pageable);
                break;
            case "PENDING":
                bookingsPage = bookingRepository.findByPaymentStatus(PaymentStatus.PENDING, pageable);
                break;
            case "INITIATED":
                bookingsPage = bookingRepository.findByPaymentStatus(PaymentStatus.INITIATED, pageable);
                break;
            case "ALL":
            default:
                bookingsPage = bookingRepository.findAll(pageable);
                break;
        }

        if (bookingsPage.isEmpty()) {
            return new ApiPaginatedResponse(
                    "404",
                    "No bookings found",
                    null,
                    0,
                    0,
                    page + 1
            );
        }
        auditLogService.logAction("Booking", null, "GET_ALL_BOOKINGS",
                "Retrieved all bookings");
        return new ApiPaginatedResponse(
                "200",
                "Bookings retrieved successfully",
                bookingsPage.getContent(),
                bookingsPage.getTotalElements(),
                bookingsPage.getTotalPages(),
                bookingsPage.getNumber() + 1
        );
    }

    // Grace period logic
    @Scheduled(fixedRate = 60000)
    public void handleGracePeriod() {
        System.out.println("------Background Task started ----");
        List<Booking> overdueBookings = bookingRepository.findByEndTimeBeforeAndParkingSpot_StatusAndBookingStatus(LocalDateTime.now(), SpotStatus.BOOKED, BookingStatus.ACTIVE);
        for (Booking booking : overdueBookings) {
            ParkingSpot spot = booking.getParkingSpot();
            User user = booking.getUser();
            if (!booking.isOverdueNotificationSent()) {
                String subject = "Parking Grace Period Notification";
                String gracePeriodMessage = "You have a " + GRACE_PERIOD_MINUTES + "-minute grace period to vacate your parking spot.";
                emailService.sendHtmlEmail(user.getEmail(), subject, user.getUserName(), gracePeriodMessage);
                auditLogService.logAction("Booking", booking.getBookingId(), "SENDING_BOOKING_OVERDUE_EMAIL",
                        "Sent email for the booking that have overstayed the parking");
                booking.setOverdueNotificationSent(true);
                bookingRepository.save(booking);
                auditLogService.logAction("Booking", booking.getBookingId(), "UPDATE_BOOKING",
                        "Update the Booking OverdueNotificationStatusSent to true");
            }
            Fine fine = fineRepository.findByBooking_BookingId(booking.getBookingId());
            if (fine == null) {
                fine = createNewFine(booking, user);
                auditLogService.logAction("Fine", fine.getFineId(), "CREATE_FINE",
                        "Created new fine for booking id:" + booking.getBookingId());
            } else {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime endTime = booking.getEndTime();
                LocalDateTime lastUpdated = fine.getLastUpdated();

                long totalHoursOverdue = Duration.between(endTime, now).toHours();
                Fees fee = feesRepository.findByVehicleType(booking.getVehicleType());
                double totalAmount = totalHoursOverdue * fee.getHourlyRate();
                if (lastUpdated == null || (Duration.between(lastUpdated, now).toHours() >= 1 && totalAmount > 0)) {
                    fine.setAmount(totalAmount);
                    fine.setLastUpdated(now);
                    fineRepository.save(fine);
                    auditLogService.logAction("Fine", fine.getFineId(), "UPDATE_FINE",
                            "Update fine for booking id:" + booking.getBookingId());
                }
            }
            spot.setStatus(SpotStatus.BOOKED);
            parkingSpotRepository.save(spot);
            auditLogService.logAction("PARKING", booking.getBookingId(), "UPDATE_PARKING_SPOT",
                    "Update parking spot status");
        }
    }

    private Fine createNewFine(Booking booking, User user) {
        Fees fee = feesRepository.findByVehicleType(booking.getVehicleType());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = booking.getEndTime();
        long overdueHours = Duration.between(endTime, now).toHours();
        double fineAmount = 0.0;
        if (overdueHours > 0) {
            fineAmount = overdueHours * fee.getHourlyRate();
        }
        Fine fine = new Fine();
        fine.setUser(user);
        fine.setBooking(booking);
        fine.setAmount(fineAmount);
        fine.setIssuedAt(now);
        fine.setDueAt(now.plusDays(1));
        fine.setStatus(FineStatus.PENDING);
        fine.setLastUpdated(now);
        fineRepository.save(fine);
        return fine;
    }

    @Override
    public ApiResponse exitParking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        User user = booking.getUser();
        ParkingSpot spot = booking.getParkingSpot();
        List<Fine> fines = fineRepository.findByUser_UserIdAndStatus(user.getUserId(), FineStatus.PENDING);
        if (!fines.isEmpty()) {
            return new ApiResponse("403", "Unpaid fines detected. Please clear your fines before exiting.", null);
        }
        if (!booking.getPaymentStatus().equals(PaymentStatus.PAID)) {
            return new ApiResponse("403", "Booking payment not completed. Please pay before exiting.", null);
        }
        booking.setBookingStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);
        auditLogService.logAction("Booking", booking.getBookingId(), "UPDATE_BOOKING",
                "Update booking status to expired");
        spot.setStatus(SpotStatus.AVAILABLE);
        parkingSpotRepository.save(spot);
        auditLogService.logAction("PARKINGSPOT", booking.getParkingSpot().getSpotId(), "UPDATE_PARKINGSPOT",
                "Update Parking spot to make it available");

        return new ApiResponse("200", "Exit successful. Parking spot is now available.", null);
    }

    @Scheduled(fixedRate = 60000)
    public void completePayments() {
        System.out.println("---completing payments ---");
        List<Payment> pendingPayments = paymentRepository.findByIsPaymentCompletedFalse();

        for (Payment payment : pendingPayments) {
            String paymentRef = payment.getPaymentRef();
            Stripe.apiKey = stripeSecretKey;
            Session session;
            try {
                session = Session.retrieve(paymentRef);
                if ("complete".equalsIgnoreCase(session.getStatus())) {
                    payment.setPaymentCompleted(true);
                    paymentRepository.save(payment);
                    auditLogService.logAction("PAYMENT", payment.getPaymentId(), "UPDATE_PAYMENT",
                            "Update Payment to completed");

                    Booking booking = payment.getBooking();
                    booking.setPaymentStatus(PaymentStatus.PAID);
                    bookingRepository.save(booking);
                    auditLogService.logAction("BOOKING", booking.getBookingId(), "UPDATE_BOOKING",
                            "Update Booking payment status to Paid");

                    ParkingSpot spot = booking.getParkingSpot();
                    spot.setStatus(SpotStatus.BOOKED);
                    parkingSpotRepository.save(spot);
                    auditLogService.logAction("PARKINGSPOT", booking.getParkingSpot().getSpotId(), "UPDATE_PARKINGSPOT",
                            "Update Parking spot to make it Booked");

                    Fine fine = fineRepository.findByBooking_BookingId(booking.getBookingId());
                    if (fine != null && fine.getStatus() != FineStatus.PAID) {
                        fine.setStatus(FineStatus.PAID);
                        fineRepository.save(fine);
                        auditLogService.logAction("FINE", fine.getFineId(), "UPDATE_FINE",
                                "Update Fine status to Paid");
                    }

                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a"); // 12-hour format with AM/PM

                    String formattedDate = booking.getStartTime().format(dateFormatter);
                    String formattedStartTime = booking.getStartTime().format(timeFormatter);
                    String formattedEndTime = booking.getEndTime().format(timeFormatter);
                    Duration duration = Duration.between(booking.getStartTime(), booking.getEndTime());
                    double durationInHours = duration.toMinutes() / 60.0;

                    // Sending email
                    emailService.sendPaymentEmail(
                            booking.getUser().getEmail(),
                            "Payment Received - Parking Booking Receipt",
                            booking.getUser().getUserName(),
                            "N/A",
                            String.valueOf(spot.getSpotId()),
                            formattedDate,
                            formattedStartTime,
                            formattedEndTime,
                            booking.getVehicleRegistration(),
                            paymentRef,
                            session.getAmountTotal() / 100.0,
                            spot.getSpotLocation(),
                            durationInHours
                    );
                    auditLogService.logAction("BOOKING", booking.getBookingId(), "SEND_EMAIL",
                            "Send email to user for the paid booking");
                }
            } catch (StripeException e) {
                throw new RuntimeException("Failed to retrieve payment status from Stripe: " + e.getMessage());
            }
        }
    }

    @Override
    public ApiResponse getBookingById(Long bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findByBookingId(bookingId);

        if (bookingOptional.isPresent()) {
            auditLogService.logAction("BOOKING", bookingOptional.get().getBookingId(), "GET_BOOKING",
                    "Get a particular booking details");
            return new ApiResponse(
                    "200",
                    "Booking retrieved successfully",
                    bookingOptional.get()
            );
        } else {
            return new ApiResponse(
                    "404",
                    "Booking not found",
                    null
            );
        }
    }

    @Override
    public int getSumOfPaidBookingAmounts() {
        return bookingRepository.getSumOfPaidAmounts();
    }

    @Override
    public int getTotalBookings() {
        return bookingRepository.getTotalBookings();
    }

}
