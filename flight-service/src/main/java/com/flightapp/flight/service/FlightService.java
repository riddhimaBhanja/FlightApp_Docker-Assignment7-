package com.flightapp.flight.service;

import com.flightapp.flight.dto.FlightInventoryRequest;
import com.flightapp.flight.dto.FlightSearchRequest;
import com.flightapp.flight.entity.FlightInventory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FlightService {
    Flux<FlightInventory> searchFlights(FlightSearchRequest request);
    Mono<FlightInventory> addInventory(FlightInventoryRequest request);
    Mono<FlightInventory> getFlightById(Long id);
    Mono<Boolean> reduceSeats(Long flightId, Integer seats);
    Mono<Boolean> restoreSeats(Long flightId, Integer seats);
}
