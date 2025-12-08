package com.flightapp.booking;

import com.flightapp.booking.controller.BookingController;
import com.flightapp.booking.dto.BookingRequest;
import com.flightapp.booking.dto.BookingResponse;
import com.flightapp.booking.entity.Booking;
import com.flightapp.booking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookingService bookingService;

    private BookingRequest bookingRequest;
    private BookingResponse bookingResponse;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingRequest = BookingRequest.builder()
                .flightId(1L)
                .passengerName("John Doe")
                .passengerEmail("john.doe@example.com")
                .passengerPhone("+919876543210")
                .numberOfSeats(2)
                .build();

        bookingResponse = BookingResponse.builder()
                .pnr("PNR12345678")
                .flightNumber("AI101")
                .passengerName("John Doe")
                .numberOfSeats(2)
                .totalAmount(10000.0)
                .status("CONFIRMED")
                .bookingDate(LocalDateTime.now())
                .message("Booking created successfully")
                .build();

        booking = Booking.builder()
                .id("1")
                .pnr("PNR12345678")
                .flightId(1L)
                .flightNumber("AI101")
                .passengerName("John Doe")
                .passengerEmail("john.doe@example.com")
                .passengerPhone("+919876543210")
                .numberOfSeats(2)
                .totalAmount(10000.0)
                .status("CONFIRMED")
                .bookingDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateBooking_Success() {
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenReturn(Mono.just(bookingResponse));

        webTestClient.post()
                .uri("/api/bookings/book")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookingRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookingResponse.class)
                .isEqualTo(bookingResponse);
    }

    @Test
    void testCreateBooking_Failed() {
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Flight not found")));

        webTestClient.post()
                .uri("/api/bookings/book")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookingRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(BookingResponse.class)
                .value(response -> {
                    assert response.getMessage().equals("Flight not found");
                    assert response.getStatus().equals("FAILED");
                });
    }

    @Test
    void testCreateBooking_InsufficientSeats() {
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Insufficient seats available")));

        webTestClient.post()
                .uri("/api/bookings/book")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookingRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(BookingResponse.class)
                .value(response -> {
                    assert response.getMessage().equals("Insufficient seats available");
                    assert response.getStatus().equals("FAILED");
                });
    }

    @Test
    void testCancelBooking_Success() {
        BookingResponse cancelledResponse = BookingResponse.builder()
                .pnr("PNR12345678")
                .flightNumber("AI101")
                .passengerName("John Doe")
                .numberOfSeats(2)
                .totalAmount(10000.0)
                .status("CANCELLED")
                .bookingDate(LocalDateTime.now())
                .message("Booking cancelled successfully")
                .build();

        when(bookingService.cancelBooking(anyString()))
                .thenReturn(Mono.just(cancelledResponse));

        webTestClient.delete()
                .uri("/api/bookings/cancel/PNR12345678")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookingResponse.class)
                .value(response -> {
                    assert response.getStatus().equals("CANCELLED");
                    assert response.getMessage().equals("Booking cancelled successfully");
                });
    }

    @Test
    void testCancelBooking_NotFound() {
        when(bookingService.cancelBooking(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Booking not found with PNR: PNR999")));

        webTestClient.delete()
                .uri("/api/bookings/cancel/PNR999")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(BookingResponse.class)
                .value(response -> {
                    assert response.getStatus().equals("FAILED");
                    assert response.getMessage().contains("Booking not found");
                });
    }

    @Test
    void testCancelBooking_AlreadyCancelled() {
        when(bookingService.cancelBooking(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Booking already cancelled")));

        webTestClient.delete()
                .uri("/api/bookings/cancel/PNR12345678")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(BookingResponse.class)
                .value(response -> {
                    assert response.getStatus().equals("FAILED");
                    assert response.getMessage().equals("Booking already cancelled");
                });
    }

    @Test
    void testGetBookingByPnr_Success() {
        when(bookingService.getBookingByPnr(anyString()))
                .thenReturn(Mono.just(booking));

        webTestClient.get()
                .uri("/api/bookings/pnr/PNR12345678")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Booking.class)
                .isEqualTo(booking);
    }

    @Test
    void testGetBookingByPnr_NotFound() {
        when(bookingService.getBookingByPnr(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Booking not found with PNR: PNR999")));

        webTestClient.get()
                .uri("/api/bookings/pnr/PNR999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
