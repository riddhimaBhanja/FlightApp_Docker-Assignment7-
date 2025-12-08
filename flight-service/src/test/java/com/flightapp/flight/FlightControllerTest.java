package com.flightapp.flight;

import com.flightapp.flight.controller.FlightController;
import com.flightapp.flight.dto.FlightInventoryRequest;
import com.flightapp.flight.dto.FlightSearchRequest;
import com.flightapp.flight.entity.FlightInventory;
import com.flightapp.flight.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebFluxTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private FlightService flightService;

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
    void testSearchFlights_Success() {
        when(flightService.searchFlights(any(FlightSearchRequest.class)))
                .thenReturn(Flux.just(testFlight));

        webTestClient.post()
                .uri("/api/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(searchRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FlightInventory.class)
                .hasSize(1)
                .contains(testFlight);
    }

    @Test
    void testSearchFlights_EmptyResult() {
        when(flightService.searchFlights(any(FlightSearchRequest.class)))
                .thenReturn(Flux.empty());

        webTestClient.post()
                .uri("/api/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(searchRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FlightInventory.class)
                .hasSize(0);
    }

    @Test
    void testAddInventory_Success() {
        when(flightService.addInventory(any(FlightInventoryRequest.class)))
                .thenReturn(Mono.just(testFlight));

        webTestClient.post()
                .uri("/api/flights/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FlightInventory.class)
                .isEqualTo(testFlight);
    }

    @Test
    void testGetFlightById_Success() {
        when(flightService.getFlightById(anyLong()))
                .thenReturn(Mono.just(testFlight));

        webTestClient.get()
                .uri("/api/flights/inventory/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(FlightInventory.class)
                .isEqualTo(testFlight);
    }

    @Test
    void testGetFlightById_NotFound() {
        when(flightService.getFlightById(anyLong()))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/flights/inventory/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testReduceSeats_Success() {
        when(flightService.reduceSeats(anyLong(), anyInt()))
                .thenReturn(Mono.just(true));

        webTestClient.put()
                .uri("/api/flights/inventory/1/reduce-seats?seats=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void testReduceSeats_Failed() {
        when(flightService.reduceSeats(anyLong(), anyInt()))
                .thenReturn(Mono.just(false));

        webTestClient.put()
                .uri("/api/flights/inventory/1/reduce-seats?seats=200")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    void testRestoreSeats_Success() {
        when(flightService.restoreSeats(anyLong(), anyInt()))
                .thenReturn(Mono.just(true));

        webTestClient.put()
                .uri("/api/flights/inventory/1/restore-seats?seats=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void testRestoreSeats_Failed() {
        when(flightService.restoreSeats(anyLong(), anyInt()))
                .thenReturn(Mono.just(false));

        webTestClient.put()
                .uri("/api/flights/inventory/1/restore-seats?seats=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }
}
