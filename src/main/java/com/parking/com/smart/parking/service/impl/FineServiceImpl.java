package com.parking.com.smart.parking.service.impl;

import com.parking.com.smart.parking.entities.Fine;
import com.parking.com.smart.parking.entities.FineStatus;
import com.parking.com.smart.parking.repository.FineRepository;
import com.parking.com.smart.parking.response.ApiResponse;
import com.parking.com.smart.parking.service.FineService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FineServiceImpl implements FineService {
    private final FineRepository fineRepository;

    public FineServiceImpl(FineRepository fineRepository) {
        this.fineRepository = fineRepository;
    }

    @Override
    public ApiResponse findAllFines() {
        List<Fine> fines = fineRepository.findAll();

        if (fines.isEmpty()) {
            return new ApiResponse("404", "No fine record found", null);
        }
        List<Fine> unpaidFines = fines.stream()
                .filter(fine -> fine.getStatus() == FineStatus.PENDING)
                .collect(Collectors.toList());

        if (unpaidFines.isEmpty()) {
            return new ApiResponse("200", "No pending fines found", unpaidFines);
        }

        return new ApiResponse("200", "Pending fines retrieved successfully", unpaidFines);
    }
    @Override
    public ApiResponse findFinesByUserId(Long userId) {
        List<Fine> userFines = fineRepository.findByUser_UserId(userId);

        if (userFines.isEmpty()) {
            return new ApiResponse("404", "No fines found for this user", null);
        }

        List<Fine> unpaidFines = userFines.stream()
                .filter(fine -> fine.getStatus() == FineStatus.PENDING)
                .collect(Collectors.toList());

        if (unpaidFines.isEmpty()) {
            return new ApiResponse("200", "No pending fines for this user", unpaidFines);
        }

        return new ApiResponse("200", "Pending fines retrieved successfully for user", unpaidFines);
    }
}