package com.flightapp.flight;

import com.flightapp.flight.dto.*;
import com.flightapp.flight.entity.FlightInventory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoAndEntityTest {

    @Test
    void testFlightInventory_Builder() {
        LocalDateTime departureTime = LocalDateTime.of(2025, 12, 15, 8, 0);
        LocalDateTime arrivalTime = LocalDateTime.of(2025, 12, 15, 10, 30);
        LocalDateTime now = LocalDateTime.now();

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
                .createdAt(now)
                .updatedAt(now)
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
        assertEquals(now, flight.getCreatedAt());
        assertEquals(now, flight.getUpdatedAt());
    }

    @Test
    void testFlightInventory_Setters() {
        FlightInventory flight = new FlightInventory();
        LocalDateTime departureTime = LocalDateTime.of(2025, 12, 15, 8, 0);
        LocalDateTime arrivalTime = LocalDateTime.of(2025, 12, 15, 10, 30);
        LocalDateTime now = LocalDateTime.now();

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
        flight.setCreatedAt(now);
        flight.setUpdatedAt(now);

        assertEquals(1L, flight.getId());
        assertEquals("AI101", flight.getFlightNumber());
        assertEquals("Air India", flight.getAirline());
    }

    @Test
    void testFlightInventoryRequest_Builder() {
        LocalDateTime departureTime = LocalDateTime.of(2025, 12, 15, 8, 0);
        LocalDateTime arrivalTime = LocalDateTime.of(2025, 12, 15, 10, 30);

        FlightInventoryRequest request = FlightInventoryRequest.builder()
                .flightNumber("AI101")
                .airline("Air India")
                .origin("DEL")
                .destination("BOM")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .availableSeats(150)
                .price(5000.0)
                .build();

        assertEquals("AI101", request.getFlightNumber());
        assertEquals("Air India", request.getAirline());
        assertEquals("DEL", request.getOrigin());
        assertEquals("BOM", request.getDestination());
        assertEquals(departureTime, request.getDepartureTime());
        assertEquals(arrivalTime, request.getArrivalTime());
        assertEquals(150, request.getAvailableSeats());
        assertEquals(5000.0, request.getPrice());
    }

    @Test
    void testFlightSearchRequest_Builder() {
        LocalDate travelDate = LocalDate.of(2025, 12, 15);

        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("DEL")
                .destination("BOM")
                .travelDate(travelDate)
                .build();

        assertEquals("DEL", request.getOrigin());
        assertEquals("BOM", request.getDestination());
        assertEquals(travelDate, request.getTravelDate());
    }

    @Test
    void testFlightSearchRequest_NoArgsConstructor() {
        FlightSearchRequest request = new FlightSearchRequest();
        LocalDate travelDate = LocalDate.of(2025, 12, 15);

        request.setOrigin("DEL");
        request.setDestination("BOM");
        request.setTravelDate(travelDate);

        assertEquals("DEL", request.getOrigin());
        assertEquals("BOM", request.getDestination());
        assertEquals(travelDate, request.getTravelDate());
    }

    @Test
    void testFlightInventoryRequest_NoArgsConstructor() {
        FlightInventoryRequest request = new FlightInventoryRequest();
        LocalDateTime departureTime = LocalDateTime.of(2025, 12, 15, 8, 0);
        LocalDateTime arrivalTime = LocalDateTime.of(2025, 12, 15, 10, 30);

        request.setFlightNumber("AI101");
        request.setAirline("Air India");
        request.setOrigin("DEL");
        request.setDestination("BOM");
        request.setDepartureTime(departureTime);
        request.setArrivalTime(arrivalTime);
        request.setAvailableSeats(150);
        request.setPrice(5000.0);

        assertEquals("AI101", request.getFlightNumber());
        assertEquals("Air India", request.getAirline());
    }
}
