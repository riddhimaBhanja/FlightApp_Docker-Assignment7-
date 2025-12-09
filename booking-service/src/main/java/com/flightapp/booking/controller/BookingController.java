package com.flightapp.booking.controller;

import com.flightapp.booking.dto.BookingRequest;
import com.flightapp.booking.dto.BookingResponse;
import com.flightapp.booking.entity.Booking;
import com.flightapp.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/book")
    public Mono<ResponseEntity<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest request) {
        return bookingService.createBooking(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().body(
                                BookingResponse.builder()
                                        .message(e.getMessage())
                                        .status("FAILED")
                                        .build()
                        )
                ));
    }

    @DeleteMapping("/cancel/{pnr}")
    public Mono<ResponseEntity<BookingResponse>> cancelBooking(@PathVariable String pnr) {
        return bookingService.cancelBooking(pnr)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().body(
                                BookingResponse.builder()
                                        .pnr(pnr)
                                        .message(e.getMessage())
                                        .status("FAILED")
                                        .build()
                        )
                ));
    }

    @GetMapping("/pnr/{pnr}")
    public Mono<ResponseEntity<Booking>> getBookingByPnr(@PathVariable String pnr) {
        return bookingService.getBookingByPnr(pnr)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }
}
