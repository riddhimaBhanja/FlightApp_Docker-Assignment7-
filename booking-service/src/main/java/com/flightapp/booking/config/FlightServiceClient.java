package com.flightapp.booking.config;

import com.flightapp.booking.dto.FlightInventory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "flight-service", configuration = FeignClientConfig.class)
public interface FlightServiceClient {

    @GetMapping("/api/flights/inventory/{id}")
    FlightInventory getFlightById(@PathVariable("id") Long id);

    @PutMapping("/api/flights/inventory/{id}/reduce-seats")
    Boolean reduceSeats(@PathVariable("id") Long id, @RequestParam("seats") Integer seats);

    @PutMapping("/api/flights/inventory/{id}/restore-seats")
    Boolean restoreSeats(@PathVariable("id") Long id, @RequestParam("seats") Integer seats);
}
