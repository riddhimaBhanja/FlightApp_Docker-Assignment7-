package com.flightapp.booking;

import com.flightapp.booking.dto.*;
import com.flightapp.booking.entity.Booking;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoAndEntityTest {

    @Test
    void testBooking_Builder() {
        LocalDateTime now = LocalDateTime.now();

        Booking booking = Booking.builder()
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
                .bookingDate(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals("1", booking.getId());
        assertEquals("PNR12345678", booking.getPnr());
        assertEquals(1L, booking.getFlightId());
        assertEquals("AI101", booking.getFlightNumber());
        assertEquals("John Doe", booking.getPassengerName());
        assertEquals("john.doe@example.com", booking.getPassengerEmail());
        assertEquals("+919876543210", booking.getPassengerPhone());
        assertEquals(2, booking.getNumberOfSeats());
        assertEquals(10000.0, booking.getTotalAmount());
        assertEquals("CONFIRMED", booking.getStatus());
        assertEquals(now, booking.getBookingDate());
        assertEquals(now, booking.getCreatedAt());
        assertEquals(now, booking.getUpdatedAt());
    }

    @Test
    void testBooking_Setters() {
        Booking booking = new Booking();
        LocalDateTime now = LocalDateTime.now();

        booking.setId("1");
        booking.setPnr("PNR12345678");
        booking.setFlightId(1L);
        booking.setFlightNumber("AI101");
        booking.setPassengerName("John Doe");
        booking.setPassengerEmail("john.doe@example.com");
        booking.setPassengerPhone("+919876543210");
        booking.setNumberOfSeats(2);
        booking.setTotalAmount(10000.0);
        booking.setStatus("CONFIRMED");
        booking.setBookingDate(now);
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);

        assertEquals("1", booking.getId());
        assertEquals("PNR12345678", booking.getPnr());
        assertEquals(1L, booking.getFlightId());
        assertEquals("AI101", booking.getFlightNumber());
    }

    @Test
    void testBookingRequest_Builder() {
        BookingRequest request = BookingRequest.builder()
                .flightId(1L)
                .passengerName("John Doe")
                .passengerEmail("john.doe@example.com")
                .passengerPhone("+919876543210")
                .numberOfSeats(2)
                .build();

        assertEquals(1L, request.getFlightId());
        assertEquals("John Doe", request.getPassengerName());
        assertEquals("john.doe@example.com", request.getPassengerEmail());
        assertEquals("+919876543210", request.getPassengerPhone());
        assertEquals(2, request.getNumberOfSeats());
    }

    @Test
    void testBookingRequest_NoArgsConstructor() {
        BookingRequest request = new BookingRequest();
        request.setFlightId(1L);
        request.setPassengerName("John Doe");
        request.setPassengerEmail("john.doe@example.com");
        request.setPassengerPhone("+919876543210");
        request.setNumberOfSeats(2);

        assertEquals(1L, request.getFlightId());
        assertEquals("John Doe", request.getPassengerName());
    }

    @Test
    void testBookingResponse_Builder() {
        LocalDateTime now = LocalDateTime.now();

        BookingResponse response = BookingResponse.builder()
                .pnr("PNR12345678")
                .flightNumber("AI101")
                .passengerName("John Doe")
                .numberOfSeats(2)
                .totalAmount(10000.0)
                .status("CONFIRMED")
                .bookingDate(now)
                .message("Booking created successfully")
                .build();

        assertEquals("PNR12345678", response.getPnr());
        assertEquals("AI101", response.getFlightNumber());
        assertEquals("John Doe", response.getPassengerName());
        assertEquals(2, response.getNumberOfSeats());
        assertEquals(10000.0, response.getTotalAmount());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(now, response.getBookingDate());
        assertEquals("Booking created successfully", response.getMessage());
    }

    @Test
    void testBookingResponse_NoArgsConstructor() {
        BookingResponse response = new BookingResponse();
        LocalDateTime now = LocalDateTime.now();

        response.setPnr("PNR12345678");
        response.setFlightNumber("AI101");
        response.setPassengerName("John Doe");
        response.setNumberOfSeats(2);
        response.setTotalAmount(10000.0);
        response.setStatus("CONFIRMED");
        response.setBookingDate(now);
        response.setMessage("Booking created successfully");

        assertEquals("PNR12345678", response.getPnr());
        assertEquals("AI101", response.getFlightNumber());
    }

    @Test
    void testFlightInventory_Builder() {
        LocalDateTime departureTime = LocalDateTime.of(2025, 12, 15, 8, 0);
        LocalDateTime arrivalTime = LocalDateTime.of(2025, 12, 15, 10, 30);

        FlightInventory flight = FlightInventory.builder()
                .id(1L)
                .flightNumber("AI101")
                .airline("Air India")
                .origin("DEL")
                .destination("BOM")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .availableSeats(150)
                .price(5000.0)
                .status("ACTIVE")
                .build();

        assertEquals(1L, flight.getId());
        assertEquals("AI101", flight.getFlightNumber());
        assertEquals("Air India", flight.getAirline());
        assertEquals("DEL", flight.getOrigin());
        assertEquals("BOM", flight.getDestination());
        assertEquals(departureTime, flight.getDepartureTime());
        assertEquals(arrivalTime, flight.getArrivalTime());
        assertEquals(150, flight.getAvailableSeats());
        assertEquals(5000.0, flight.getPrice());
        assertEquals("ACTIVE", flight.getStatus());
    }

    @Test
    void testFlightInventory_NoArgsConstructor() {
        FlightInventory flight = new FlightInventory();
        LocalDateTime departureTime = LocalDateTime.of(2025, 12, 15, 8, 0);
        LocalDateTime arrivalTime = LocalDateTime.of(2025, 12, 15, 10, 30);

        flight.setId(1L);
        flight.setFlightNumber("AI101");
        flight.setAirline("Air India");
        flight.setOrigin("DEL");
        flight.setDestination("BOM");
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setAvailableSeats(150);
        flight.setPrice(5000.0);
        flight.setStatus("ACTIVE");

        assertEquals(1L, flight.getId());
        assertEquals("AI101", flight.getFlightNumber());
    }

    @Test
    void testEmailNotification_Builder() {
        EmailNotification notification = EmailNotification.builder()
                .to("john.doe@example.com")
                .subject("Booking Confirmation")
                .body("Your booking is confirmed")
                .pnr("PNR12345678")
                .flightNumber("AI101")
                .passengerName("John Doe")
                .build();

        assertEquals("john.doe@example.com", notification.getTo());
        assertEquals("Booking Confirmation", notification.getSubject());
        assertEquals("Your booking is confirmed", notification.getBody());
        assertEquals("PNR12345678", notification.getPnr());
        assertEquals("AI101", notification.getFlightNumber());
        assertEquals("John Doe", notification.getPassengerName());
    }

    @Test
    void testEmailNotification_NoArgsConstructor() {
        EmailNotification notification = new EmailNotification();
        notification.setTo("john.doe@example.com");
        notification.setSubject("Booking Confirmation");
        notification.setBody("Your booking is confirmed");
        notification.setPnr("PNR12345678");
        notification.setFlightNumber("AI101");
        notification.setPassengerName("John Doe");

        assertEquals("john.doe@example.com", notification.getTo());
        assertEquals("Booking Confirmation", notification.getSubject());
    }
}
