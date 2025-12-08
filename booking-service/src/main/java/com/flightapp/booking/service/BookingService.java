package com.flightapp.booking.service;

import com.flightapp.booking.dto.BookingRequest;
import com.flightapp.booking.dto.BookingResponse;
import com.flightapp.booking.entity.Booking;
import reactor.core.publisher.Mono;

public interface BookingService {
    Mono<BookingResponse> createBooking(BookingRequest request);
    Mono<BookingResponse> cancelBooking(String pnr);
    Mono<Booking> getBookingByPnr(String pnr);
}
