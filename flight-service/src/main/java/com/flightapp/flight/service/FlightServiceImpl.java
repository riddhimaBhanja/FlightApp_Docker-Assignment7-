package com.flightapp.flight.service;

import com.flightapp.flight.dto.FlightInventoryRequest;
import com.flightapp.flight.dto.FlightSearchRequest;
import com.flightapp.flight.entity.FlightInventory;
import com.flightapp.flight.repository.FlightInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightServiceImpl implements FlightService {

    private final FlightInventoryRepository flightInventoryRepository;

    @Override
    public Flux<FlightInventory> searchFlights(FlightSearchRequest request) {
        LocalDateTime startOfDay = request.getTravelDate().atStartOfDay();
        LocalDateTime endOfDay = request.getTravelDate().atTime(LocalTime.MAX);

        return flightInventoryRepository
                .findByOriginAndDestinationAndDepartureTimeBetween(
                        request.getOrigin(),
                        request.getDestination(),
                        startOfDay,
                        endOfDay)
                .filter(flight -> flight.getAvailableSeats() > 0)
                .doOnNext(flight -> log.info("Found flight: {}", flight.getFlightNumber()));
    }

    @Override
    public Mono<FlightInventory> addInventory(FlightInventoryRequest request) {
        FlightInventory inventory = FlightInventory.builder()
                .flightNumber(request.getFlightNumber())
                .airline(request.getAirline())
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .availableSeats(request.getAvailableSeats())
                .price(request.getPrice())
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return flightInventoryRepository.save(inventory)
                .doOnSuccess(saved -> log.info("Flight inventory added: {}", saved.getFlightNumber()));
    }

    @Override
    public Mono<FlightInventory> getFlightById(Long id) {
        return flightInventoryRepository.findById(id)
                .doOnSuccess(flight -> log.info("Retrieved flight: {}", flight));
    }

    @Override
    public Mono<Boolean> reduceSeats(Long flightId, Integer seats) {
        return flightInventoryRepository.reduceAvailableSeats(flightId, seats)
                .map(updated -> updated > 0)
                .doOnSuccess(success -> log.info("Reduced {} seats for flight {}: {}", seats, flightId, success));
    }

    @Override
    public Mono<Boolean> restoreSeats(Long flightId, Integer seats) {
        return flightInventoryRepository.increaseAvailableSeats(flightId, seats)
                .map(updated -> updated > 0)
                .doOnSuccess(success -> log.info("Restored {} seats for flight {}: {}", seats, flightId, success));
    }
}
