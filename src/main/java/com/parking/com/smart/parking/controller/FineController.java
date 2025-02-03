package com.parking.com.smart.parking.controller;

import com.parking.com.smart.parking.response.ApiResponse;
import com.parking.com.smart.parking.service.FineService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fines")
@Tag(name="Fines",description = "Endpoints for managing Fines")
public class FineController {
    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping("/{userId}")
    public ApiResponse getAllUserFines(@PathVariable Long userId) {
        return fineService.findFinesByUserId(userId);
    }
}
