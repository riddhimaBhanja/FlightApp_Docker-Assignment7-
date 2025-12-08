package com.flightapp.booking.service;

import com.flightapp.booking.config.FlightServiceClient;
import com.flightapp.booking.dto.BookingRequest;
import com.flightapp.booking.dto.BookingResponse;
import com.flightapp.booking.dto.EmailNotification;
import com.flightapp.booking.dto.FlightInventory;
import com.flightapp.booking.entity.Booking;
import com.flightapp.booking.exception.*;
import com.flightapp.booking.messaging.EmailService;
import com.flightapp.booking.repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightServiceClient flightServiceClient;
    private final EmailService emailService;

    @Override
    @CircuitBreaker(name = "bookingService", fallbackMethod = "createBookingFallback")
    public Mono<BookingResponse> createBooking(BookingRequest request) {
        return Mono.fromCallable(() -> {
            FlightInventory flight = flightServiceClient.getFlightById(request.getFlightId());

            if (flight == null) {
                throw new FlightNotFoundException("Flight not found");
            }

            if (flight.getAvailableSeats() < request.getNumberOfSeats()) {
                throw new InsufficientSeatsException("Insufficient seats available");
            }

            Boolean seatsReduced = flightServiceClient.reduceSeats(
                    request.getFlightId(),
                    request.getNumberOfSeats()
            );

            if (Boolean.FALSE.equals(seatsReduced)) {
                throw new SeatOperationException("Failed to reduce seats");
            }

            String pnr = generatePnr();
            Double totalAmount = flight.getPrice() * request.getNumberOfSeats();

            return Booking.builder()
                    .pnr(pnr)
                    .flightId(request.getFlightId())
                    .flightNumber(flight.getFlightNumber())
                    .passengerName(request.getPassengerName())
                    .passengerEmail(request.getPassengerEmail())
                    .passengerPhone(request.getPassengerPhone())
                    .numberOfSeats(request.getNumberOfSeats())
                    .totalAmount(totalAmount)
                    .status("CONFIRMED")
                    .bookingDate(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(bookingRepository::save)
        .doOnSuccess(booking -> {
            log.info("Booking created successfully with PNR: {}", booking.getPnr());
            sendBookingEmail(booking);
        })
        .map(this::mapToResponse);
    }

    @Override
    @CircuitBreaker(name = "bookingService", fallbackMethod = "cancelBookingFallback")
    public Mono<BookingResponse> cancelBooking(String pnr) {
        return bookingRepository.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new BookingNotFoundException("Booking not found with PNR: " + pnr)))
                .flatMap(booking -> {
                    if ("CANCELLED".equals(booking.getStatus())) {
                        return Mono.error(new BookingAlreadyCancelledException("Booking already cancelled"));
                    }

                    return Mono.fromCallable(() -> {
                        Boolean seatsRestored = flightServiceClient.restoreSeats(
                                booking.getFlightId(),
                                booking.getNumberOfSeats()
                        );

                        if (Boolean.FALSE.equals(seatsRestored)) {
                            throw new SeatOperationException("Failed to restore seats");
                        }

                        booking.setStatus("CANCELLED");
                        booking.setUpdatedAt(LocalDateTime.now());
                        return booking;
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(bookingRepository::save);
                })
                .doOnSuccess(booking -> log.info("Booking cancelled successfully: {}", pnr))
                .map(booking -> BookingResponse.builder()
                        .pnr(booking.getPnr())
                        .flightNumber(booking.getFlightNumber())
                        .passengerName(booking.getPassengerName())
                        .numberOfSeats(booking.getNumberOfSeats())
                        .totalAmount(booking.getTotalAmount())
                        .status(booking.getStatus())
                        .bookingDate(booking.getBookingDate())
                        .message("Booking cancelled successfully")
                        .build());
    }

    @Override
    public Mono<Booking> getBookingByPnr(String pnr) {
        return bookingRepository.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new BookingNotFoundException("Booking not found with PNR: " + pnr)));
    }

    private String generatePnr() {
        return "PNR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .pnr(booking.getPnr())
                .flightNumber(booking.getFlightNumber())
                .passengerName(booking.getPassengerName())
                .numberOfSeats(booking.getNumberOfSeats())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .bookingDate(booking.getBookingDate())
                .message("Booking created successfully")
                .build();
    }

    private void sendBookingEmail(Booking booking) {
        EmailNotification notification = EmailNotification.builder()
                .to(booking.getPassengerEmail())
                .subject("Flight Booking Confirmation - " + booking.getPnr())
                .body(buildEmailBody(booking))
                .pnr(booking.getPnr())
                .flightNumber(booking.getFlightNumber())
                .passengerName(booking.getPassengerName())
                .build();

        emailService.sendBookingConfirmation(notification);
    }

    private String buildEmailBody(Booking booking) {
        return String.format("""
                Dear %s,

                Your flight booking has been confirmed!

                Booking Details:
                PNR: %s
                Flight Number: %s
                Number of Seats: %d
                Total Amount: Rs. %.2f
                Booking Date: %s

                Thank you for choosing our service!

                Best Regards,
                Flight Booking Team""",
                booking.getPassengerName(),
                booking.getPnr(),
                booking.getFlightNumber(),
                booking.getNumberOfSeats(),
                booking.getTotalAmount(),
                booking.getBookingDate()
        );
    }

    private Mono<BookingResponse> createBookingFallback(Exception e) {
        log.error("Circuit breaker activated for createBooking: {}", e.getMessage());
        return Mono.just(BookingResponse.builder()
                .message("Service temporarily unavailable. Please try again later.")
                .status("FAILED")
                .build());
    }

    private Mono<BookingResponse> cancelBookingFallback(String pnr, Exception e) {
        log.error("Circuit breaker activated for cancelBooking: {}", e.getMessage());
        return Mono.just(BookingResponse.builder()
                .pnr(pnr)
                .message("Service temporarily unavailable. Please try again later.")
                .status("FAILED")
                .build());
    }
}
