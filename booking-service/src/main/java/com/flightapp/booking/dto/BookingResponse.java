package com.flightapp.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String pnr;
    private String flightNumber;
    private String passengerName;
    private Integer numberOfSeats;
    private Double totalAmount;
    private String status;
    private LocalDateTime bookingDate;
    private String message;
}
