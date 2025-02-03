package com.parking.com.smart.parking.controller;

import com.parking.com.smart.parking.request.MPaymentRequest;
import com.parking.com.smart.parking.response.ApiResponse;
import com.parking.com.smart.parking.service.MpesaPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name="MPESA payment",description = "Apis for managing MPESA payments")
public class MpesaPaymentController {
    private final MpesaPaymentService mpesaPaymentService;

    public MpesaPaymentController(MpesaPaymentService mpesaPaymentService) {
        this.mpesaPaymentService = mpesaPaymentService;
    }
    @Operation(summary = "Process MPESA Payment", description = "APi to process MPESA payments")
    @PostMapping("/process/{bookingId}")
    public ApiResponse processMpesaPayment(@PathVariable Long bookingId, @RequestBody MPaymentRequest request) {
        return mpesaPaymentService.processMpesaPayment(bookingId, request);
    }
}
