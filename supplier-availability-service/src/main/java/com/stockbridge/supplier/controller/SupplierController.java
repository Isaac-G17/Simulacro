package com.stockbridge.supplier.controller;

import com.stockbridge.supplier.dto.AvailabilityRequest;
import com.stockbridge.supplier.dto.AvailabilityResponse;
import com.stockbridge.supplier.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final AvailabilityService availabilityService;

    public SupplierController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/check-availability")
    public AvailabilityResponse checkAvailability(@Valid @RequestBody AvailabilityRequest request) {
        return availabilityService.check(request);
    }
}
