package com.parking.com.smart.parking.service;

import com.parking.com.smart.parking.response.ApiResponse;

public interface FineService {
    ApiResponse findAllFines();
    ApiResponse findFinesByUserId(Long userId);
}
