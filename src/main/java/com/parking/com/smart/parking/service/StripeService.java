package com.parking.com.smart.parking.service;

import com.parking.com.smart.parking.entities.Booking;
import com.parking.com.smart.parking.entities.Payment;
import com.parking.com.smart.parking.entities.PaymentMethod;
import com.parking.com.smart.parking.entities.PaymentStatus;
import com.parking.com.smart.parking.repository.BookingRepository;
import com.parking.com.smart.parking.repository.PaymentRepository;
import com.parking.com.smart.parking.request.CheckoutRequest;
import com.parking.com.smart.parking.response.StripeResponse;
import com.stripe.Stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StripeService {
    @Value("${stripe.api_secret_key}")
    private String stripeSecretKey;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    @Value("${success.url}")
    private String successUrl;
    private final AuditLogService auditLogService;

    public StripeService(PaymentRepository paymentRepository, BookingRepository bookingRepository, AuditLogService auditLogService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.auditLogService = auditLogService;
    }

    public StripeResponse checkoutParking(CheckoutRequest checkoutRequest) {
        Stripe.apiKey = stripeSecretKey;

        // Validate amount
        if (checkoutRequest.getAmount() == null || checkoutRequest.getAmount() < 50) {
            return StripeResponse.builder()
                    .status("FAILED")
                    .message("Amount must be at least $0.50 USD (or equivalent).")
                    .build();
        }

        // Validate currency
        String currency = checkoutRequest.getCurrency() == null ? "USD" : checkoutRequest.getCurrency().toUpperCase();

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(checkoutRequest.getName()).build();

        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem
                .PriceData.builder()
                .setCurrency(currency)
                .setUnitAmount(checkoutRequest.getAmount())
                .setProductData(productData)
                .build();

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(checkoutRequest.getQuantity())
                .setPriceData(priceData)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl("http://localhost:8081/cancel")
                .addLineItem(lineItem)
                .build();

        try {
            Session session = Session.create(params);

            // Save the payment in the database with INITIATED status
            Payment payment = new Payment();
            payment.setPaymentRef(session.getId());
            payment.setPaymentAmount((double) checkoutRequest.getAmount());
            payment.setPaymentTime(LocalDateTime.now());
            payment.setPaymentMethod(PaymentMethod.CARD);
            payment.setBooking(bookingRepository.findById(checkoutRequest.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found")));
            paymentRepository.save(payment);

            // Log payment creation
            auditLogService.logAction("Payment",payment.getPaymentId(), "CREATE",
                    "Payment initiated with reference: " + payment.getPaymentRef()
            );

            // Update booking payment status to INITIATED
            Booking booking = payment.getBooking();
            booking.setPaymentStatus(PaymentStatus.INITIATED);
            bookingRepository.save(booking);
            // Log booking update
            auditLogService.logAction("Booking", booking.getBookingId(),"UPDATE",
                    "Payment status updated to INITIATED for booking."
            );

            return StripeResponse.builder()
                    .status("SUCCESS")
                    .message("Payment session created")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();
        } catch (StripeException ex) {
            System.err.println("Stripe session creation failed: " + ex.getMessage());
            // Log error
            auditLogService.logAction("Stripe", null, "ERROR",
                    "Stripe session creation failed: " + ex.getMessage()
            );
            return StripeResponse.builder()
                    .status("FAILED")
                    .message("Unable to create Stripe session: " + ex.getMessage())
                    .build();
        }
    }

}
