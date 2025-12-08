package com.flightapp.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotification implements Serializable {
    private String to;
    private String subject;
    private String body;
    private String pnr;
    private String flightNumber;
    private String passengerName;
}
