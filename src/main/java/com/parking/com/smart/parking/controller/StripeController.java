package com.parking.com.smart.parking.controller;

import com.parking.com.smart.parking.request.CheckoutRequest;
import com.parking.com.smart.parking.response.StripeResponse;
import com.parking.com.smart.parking.service.StripeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@Tag(name="Stripe Payment",description = "Apis for stripe payments")
public class StripeController {
    private final StripeService stripeService;

    public StripeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @Operation(summary = "Stripe Payment", description = "Api to process Stripe payment")
    @PostMapping()
    public ResponseEntity<StripeResponse> checkoutParking(@RequestBody CheckoutRequest checkoutRequest) {
        StripeResponse resp = stripeService.checkoutParking(checkoutRequest);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
