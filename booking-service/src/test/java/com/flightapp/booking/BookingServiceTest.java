package com.flightapp.booking;

import com.flightapp.booking.config.FlightServiceClient;
import com.flightapp.booking.dto.BookingRequest;
import com.flightapp.booking.dto.BookingResponse;
import com.flightapp.booking.dto.FlightInventory;
import com.flightapp.booking.entity.Booking;
import com.flightapp.booking.messaging.EmailService;
import com.flightapp.booking.repository.BookingRepository;
import com.flightapp.booking.service.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightServiceClient flightServiceClient;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingRequest bookingRequest;
    private Booking booking;
    private FlightInventory flightInventory;

    @BeforeEach
    void setUp() {
        bookingRequest = BookingRequest.builder()
                .flightId(1L)
                .passengerName("John Doe")
                .passengerEmail("john@example.com")
                .passengerPhone("1234567890")
                .numberOfSeats(2)
                .build();

        flightInventory = FlightInventory.builder()
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

        booking = Booking.builder()
                .id("1")
                .pnr("PNR12345678")
                .flightId(1L)
                .flightNumber("AI101")
                .passengerName("John Doe")
                .passengerEmail("john@example.com")
                .passengerPhone("1234567890")
                .numberOfSeats(2)
                .totalAmount(10000.0)
                .status("CONFIRMED")
                .bookingDate(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateBookingSuccess() {
        when(flightServiceClient.getFlightById(1L)).thenReturn(flightInventory);
        when(flightServiceClient.reduceSeats(1L, 2)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(booking));
        doNothing().when(emailService).sendBookingConfirmation(any());

        StepVerifier.create(bookingService.createBooking(bookingRequest))
                .expectNextMatches(response ->
                        response.getStatus().equals("CONFIRMED") &&
                        response.getPassengerName().equals("John Doe"))
                .verifyComplete();

        verify(flightServiceClient, times(1)).getFlightById(1L);
        verify(flightServiceClient, times(1)).reduceSeats(1L, 2);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testGetBookingByPnr() {
        when(bookingRepository.findByPnr("PNR12345678"))
                .thenReturn(Mono.just(booking));

        StepVerifier.create(bookingService.getBookingByPnr("PNR12345678"))
                .expectNext(booking)
                .verifyComplete();

        verify(bookingRepository, times(1)).findByPnr("PNR12345678");
    }

    @Test
    void testCancelBooking() {
        when(bookingRepository.findByPnr("PNR12345678"))
                .thenReturn(Mono.just(booking));
        when(flightServiceClient.restoreSeats(1L, 2)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(Mono.just(booking));

        StepVerifier.create(bookingService.cancelBooking("PNR12345678"))
                .expectNextMatches(response ->
                        response.getPnr().equals("PNR12345678"))
                .verifyComplete();

        verify(bookingRepository, times(1)).findByPnr("PNR12345678");
        verify(flightServiceClient, times(1)).restoreSeats(1L, 2);
    }

    @Test
    void testCreateBooking_FlightNotFound() {
        when(flightServiceClient.getFlightById(1L)).thenReturn(null);

        StepVerifier.create(bookingService.createBooking(bookingRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Flight not found"))
                .verify();

        verify(flightServiceClient, times(1)).getFlightById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testCreateBooking_InsufficientSeats() {
        FlightInventory insufficientFlight = FlightInventory.builder()
                .id(1L)
                .flightNumber("AI101")
                .airline("Air India")
                .origin("DEL")
                .destination("BOM")
                .departureTime(LocalDateTime.of(2025, 12, 15, 8, 0))
                .arrivalTime(LocalDateTime.of(2025, 12, 15, 10, 30))
                .availableSeats(1)
                .price(5000.0)
                .status("ACTIVE")
                .build();

        when(flightServiceClient.getFlightById(1L)).thenReturn(insufficientFlight);

        StepVerifier.create(bookingService.createBooking(bookingRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Insufficient seats available"))
                .verify();

        verify(flightServiceClient, times(1)).getFlightById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testCreateBooking_FailedToReduceSeats() {
        when(flightServiceClient.getFlightById(1L)).thenReturn(flightInventory);
        when(flightServiceClient.reduceSeats(1L, 2)).thenReturn(false);

        StepVerifier.create(bookingService.createBooking(bookingRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Failed to reduce seats"))
                .verify();

        verify(flightServiceClient, times(1)).getFlightById(1L);
        verify(flightServiceClient, times(1)).reduceSeats(1L, 2);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testGetBookingByPnr_NotFound() {
        when(bookingRepository.findByPnr("INVALID"))
                .thenReturn(Mono.empty());

        StepVerifier.create(bookingService.getBookingByPnr("INVALID"))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Booking not found"))
                .verify();

        verify(bookingRepository, times(1)).findByPnr("INVALID");
    }

    @Test
    void testCancelBooking_BookingNotFound() {
        when(bookingRepository.findByPnr("INVALID"))
                .thenReturn(Mono.empty());

        StepVerifier.create(bookingService.cancelBooking("INVALID"))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Booking not found"))
                .verify();

        verify(bookingRepository, times(1)).findByPnr("INVALID");
    }

    @Test
    void testCancelBooking_AlreadyCancelled() {
        Booking cancelledBooking = Booking.builder()
                .id("1")
                .pnr("PNR12345678")
                .flightId(1L)
                .flightNumber("AI101")
                .passengerName("John Doe")
                .passengerEmail("john@example.com")
                .passengerPhone("1234567890")
                .numberOfSeats(2)
                .totalAmount(10000.0)
                .status("CANCELLED")
                .bookingDate(LocalDateTime.now())
                .build();

        when(bookingRepository.findByPnr("PNR12345678"))
                .thenReturn(Mono.just(cancelledBooking));

        StepVerifier.create(bookingService.cancelBooking("PNR12345678"))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Booking already cancelled"))
                .verify();

        verify(bookingRepository, times(1)).findByPnr("PNR12345678");
        verify(flightServiceClient, never()).restoreSeats(anyLong(), anyInt());
    }

    @Test
    void testCancelBooking_FailedToRestoreSeats() {
        when(bookingRepository.findByPnr("PNR12345678"))
                .thenReturn(Mono.just(booking));
        when(flightServiceClient.restoreSeats(1L, 2)).thenReturn(false);

        StepVerifier.create(bookingService.cancelBooking("PNR12345678"))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Failed to restore seats"))
                .verify();

        verify(bookingRepository, times(1)).findByPnr("PNR12345678");
        verify(flightServiceClient, times(1)).restoreSeats(1L, 2);
    }
}
