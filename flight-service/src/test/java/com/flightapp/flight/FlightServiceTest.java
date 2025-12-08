package com.flightapp.flight;

import com.flightapp.flight.dto.FlightInventoryRequest;
import com.flightapp.flight.dto.FlightSearchRequest;
import com.flightapp.flight.entity.FlightInventory;
import com.flightapp.flight.repository.FlightInventoryRepository;
import com.flightapp.flight.service.FlightServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightInventoryRepository flightInventoryRepository;

    @InjectMocks
    private FlightServiceImpl flightService;

    private FlightInventory testFlight;
    private FlightInventoryRequest testRequest;
    private FlightSearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        testFlight = FlightInventory.builder()
                .id(1L)
                .flightNumber("AI101")
                .airline("Air India")
                .origin("DEL")
                .destination("BOM")
                .departureTime(LocalDateTime.of(2025, 12, 15, 8, 0))
                .arrivalTime(LocalDateTime.of(2025, 12, 15, 10, 30))
                .availableSeats(150)
                .price(5000.0)
                .status("ACTIVE")
                .build();

        testRequest = FlightInventoryRequest.builder()
                .flightNumber("AI101")
                .airline("Air India")
                .origin("DEL")
                .destination("BOM")
                .departureTime(LocalDateTime.of(2025, 12, 15, 8, 0))
                .arrivalTime(LocalDateTime.of(2025, 12, 15, 10, 30))
                .availableSeats(150)
                .price(5000.0)
                .build();

        searchRequest = FlightSearchRequest.builder()
                .origin("DEL")
                .destination("BOM")
                .travelDate(LocalDate.of(2025, 12, 15))
                .build();
    }

    @Test
    void testSearchFlights() {
        when(flightInventoryRepository.findByOriginAndDestinationAndDepartureTimeBetween(
                anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Flux.just(testFlight));

        StepVerifier.create(flightService.searchFlights(searchRequest))
                .expectNext(testFlight)
                .verifyComplete();

        verify(flightInventoryRepository, times(1))
                .findByOriginAndDestinationAndDepartureTimeBetween(
                        anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testAddInventory() {
        when(flightInventoryRepository.save(any(FlightInventory.class)))
                .thenReturn(Mono.just(testFlight));

        StepVerifier.create(flightService.addInventory(testRequest))
                .expectNextMatches(flight ->
                        flight.getFlightNumber().equals("AI101") &&
                        flight.getAirline().equals("Air India"))
                .verifyComplete();

        verify(flightInventoryRepository, times(1)).save(any(FlightInventory.class));
    }

    @Test
    void testGetFlightById() {
        when(flightInventoryRepository.findById(1L))
                .thenReturn(Mono.just(testFlight));

        StepVerifier.create(flightService.getFlightById(1L))
                .expectNext(testFlight)
                .verifyComplete();

        verify(flightInventoryRepository, times(1)).findById(1L);
    }

    @Test
    void testReduceSeats() {
        when(flightInventoryRepository.reduceAvailableSeats(1L, 2))
                .thenReturn(Mono.just(1));

        StepVerifier.create(flightService.reduceSeats(1L, 2))
                .expectNext(true)
                .verifyComplete();

        verify(flightInventoryRepository, times(1)).reduceAvailableSeats(1L, 2);
    }

    @Test
    void testRestoreSeats() {
        when(flightInventoryRepository.increaseAvailableSeats(1L, 2))
                .thenReturn(Mono.just(1));

        StepVerifier.create(flightService.restoreSeats(1L, 2))
                .expectNext(true)
                .verifyComplete();

        verify(flightInventoryRepository, times(1)).increaseAvailableSeats(1L, 2);
    }
}
