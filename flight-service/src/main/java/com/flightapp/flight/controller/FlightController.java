package com.flightapp.flight.controller;

import com.flightapp.flight.dto.FlightInventoryRequest;
import com.flightapp.flight.dto.FlightSearchRequest;
import com.flightapp.flight.entity.FlightInventory;
import com.flightapp.flight.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping("/search")
    public Flux<FlightInventory> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        return flightService.searchFlights(request);
    }

    @PostMapping("/add")
    public Mono<ResponseEntity<FlightInventory>> addInventory(@Valid @RequestBody FlightInventoryRequest request) {
        return flightService.addInventory(request)
                .map(inventory -> ResponseEntity.status(HttpStatus.CREATED).body(inventory));
    }

    @GetMapping("/inventory/{id}")
    public Mono<ResponseEntity<FlightInventory>> getFlightById(@PathVariable Long id) {
        return flightService.getFlightById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/inventory/{id}/reduce-seats")
    public Mono<Boolean> reduceSeats(@PathVariable Long id, @RequestParam Integer seats) {
        return flightService.reduceSeats(id, seats);
    }

    @PutMapping("/inventory/{id}/restore-seats")
    public Mono<Boolean> restoreSeats(@PathVariable Long id, @RequestParam Integer seats) {
        return flightService.restoreSeats(id, seats);
    }
}
