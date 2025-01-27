package com.parking.com.smart.parking.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {
    private Long bookingId;
    private Long amount;
    private Long quantity;
    private String name;
    private String currency;
}
