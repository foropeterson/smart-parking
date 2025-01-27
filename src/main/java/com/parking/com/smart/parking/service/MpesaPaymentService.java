package com.parking.com.smart.parking.service;

import com.parking.com.smart.parking.entities.*;
import com.parking.com.smart.parking.repository.BookingRepository;
import com.parking.com.smart.parking.repository.ParkingSpotRepository;
import com.parking.com.smart.parking.repository.PaymentRepository;
import com.parking.com.smart.parking.request.MPaymentRequest;
import com.parking.com.smart.parking.request.PaymentStatusRequest;
import com.parking.com.smart.parking.request.StkPushRequest;
import com.parking.com.smart.parking.response.AccessTokenResponse;
import com.parking.com.smart.parking.response.ApiResponse;
import com.parking.com.smart.parking.response.PaymentStatusResponse;
import com.parking.com.smart.parking.response.StkPushResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

@Service
public class MpesaPaymentService {
    @Value("${mpesa.query.url}")
    private String queryUrlEndpoint;

    @Value("${mpesa.consumerKey}")
    private String consumerKey;

    @Value("${mpesa.consumerSecret}")
    private String consumerSecret;

    @Value("${mpesa.businessShortCode}")
    private String businessShortCode;

    @Value("${mpesa.passkey}")
    private String passkey;

    @Value("${mpesa.stkpush.url}")
    private String stkPushUrlEndpoint;

    @Value("${mpesa.authorization.url}")
    private String authEndpoint;
    private final RestTemplate restTemplate;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final EmailService emailService;

    public MpesaPaymentService(RestTemplate restTemplate, BookingRepository bookingRepository,
                               PaymentRepository paymentRepository, ParkingSpotRepository parkingSpotRepository, EmailService emailService) {
        this.restTemplate = restTemplate;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.emailService = emailService;
    }

    public ApiResponse processMpesaPayment(Long bookingId, MPaymentRequest request) {
        try {
            Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
            if (bookingOptional.isEmpty()) {
                return new ApiResponse("404", "Booking not found", null);
            }

            Booking booking = bookingOptional.get();

            String accessToken = fetchAccessToken();
            System.out.println("accessToken " + accessToken);
            if (accessToken == null) {
                return new ApiResponse("410", "Failed to fetch access token", null);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String password = generatePassword(businessShortCode, passkey, timestamp);

            StkPushRequest stkPushRequest = new StkPushRequest();
            stkPushRequest.setAmount(request.getAmount());
            stkPushRequest.setBusinessShortCode(businessShortCode);
            stkPushRequest.setPartyA(formatPhoneNumber(request.getPhoneNumber()));
            stkPushRequest.setPartyB(businessShortCode);
            stkPushRequest.setPassword(password);
            stkPushRequest.setTimestamp(timestamp);
            stkPushRequest.setPhoneNumber(formatPhoneNumber(request.getPhoneNumber()));
            stkPushRequest.setCallBackURL("https://mydomain.com/pat");
            stkPushRequest.setTransactionType("CustomerPayBillOnline");
            stkPushRequest.setTransactionDesc("Booking-" + bookingId);
            stkPushRequest.setAccountReference("Payment for booking ID " + bookingId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<StkPushRequest> requestEntity = new HttpEntity<>(stkPushRequest, headers);

            ResponseEntity<StkPushResponse> stkPushResponseEntity = restTemplate.postForEntity(
                    stkPushUrlEndpoint,
                    requestEntity,
                    StkPushResponse.class
            );

            StkPushResponse stkPushResponse = stkPushResponseEntity.getBody();
            if (stkPushResponse == null || !"0".equals(stkPushResponse.getResponseCode())) {
                return new ApiResponse("500", "Failed to initiate payment", stkPushResponse);
            }
            Thread.sleep(10000);
            PaymentStatusRequest paymentStatusRequest = new PaymentStatusRequest(
                    businessShortCode,
                    password,
                    timestamp,
                    stkPushResponse.getCheckoutRequestID()
            );
            HttpEntity<PaymentStatusRequest> queryRequestEntity = new HttpEntity<>(paymentStatusRequest, headers);
            ResponseEntity<PaymentStatusResponse> paymentStatusResponseEntity = restTemplate.postForEntity(
                    queryUrlEndpoint,
                    queryRequestEntity,
                    PaymentStatusResponse.class
            );

            PaymentStatusResponse paymentStatusResponse = paymentStatusResponseEntity.getBody();
            if (paymentStatusResponse == null || !"0".equals(paymentStatusResponse.getResultCode())) {
                return new ApiResponse("500", "Payment verification failed", paymentStatusResponse);
            }
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setBookingStatus(BookingStatus.ACTIVE);
            bookingRepository.save(booking);

            ParkingSpot parkingSpot = parkingSpotRepository.findBySpotId(booking.getParkingSpot().getSpotId());
            parkingSpot.setStatus(SpotStatus.BOOKED);
            parkingSpotRepository.save(parkingSpot);

            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setPaymentTime(LocalDateTime.now());
            payment.setPaymentAmount(booking.getAmount());
            payment.setPaymentRef(stkPushResponse.getCheckoutRequestID());
            payment.setPaymentMethod(PaymentMethod.MPESA);
            payment.setPaymentCompleted(true);
            paymentRepository.save(payment);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a"); // 12-hour format with AM/PM

            String formattedDate = booking.getStartTime().format(dateFormatter);
            String formattedStartTime = booking.getStartTime().format(timeFormatter);
            String formattedEndTime = booking.getEndTime().format(timeFormatter);
            Duration duration = Duration.between(booking.getStartTime(), booking.getEndTime());
            double durationInHours = duration.toMinutes() / 60.0;
            emailService.sendPaymentEmail(
                    booking.getUser().getEmail(),
                    "Payment Received - Parking Booking Receipt",
                    booking.getUser().getUserName(),
                    "N/A",
                    String.valueOf(booking.getParkingSpot().getSpotId()),
                    formattedDate,
                    formattedStartTime,
                    formattedEndTime,
                    booking.getVehicleRegistration(),
                    stkPushResponse.getCheckoutRequestID(),
                    booking.getAmount(),
                    booking.getParkingSpot().getSpotLocation(),
                    durationInHours
            );
            return new ApiResponse("200", "Payment processed successfully", payment);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return new ApiResponse(
                    String.valueOf(ex.getStatusCode().value()),
                    "HTTP error occurred: " + ex.getMessage(),
                    null
            );
        } catch (Exception ex) {
            return new ApiResponse("500", "An unexpected error occurred: " + ex.getMessage(), null);
        }
    }

    private String fetchAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(consumerKey, consumerSecret);

            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    authEndpoint,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    AccessTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody().getAccessToken();
            }

            return null;
        } catch (Exception ex) {
            System.err.println("Error fetching access token: " + ex.getMessage());
            return null;
        }
    }

    private String generatePassword(String businessShortCode, String passkey, String timestamp) {
        String dataToEncode = businessShortCode + passkey + timestamp;
        return Base64.getEncoder().encodeToString(dataToEncode.getBytes());
    }

    public String formatPhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.replaceAll("[^\\d]", "");
        if (phoneNumber.length() < 9) {
            return null;
        }
        String last9Digits = phoneNumber.substring(phoneNumber.length() - 9);
        return "254" + last9Digits;
    }
}

