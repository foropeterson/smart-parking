package com.parking.com.smart.parking.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MPaymentRequest {
    private String phoneNumber;
    private String amount;
}
