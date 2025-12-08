package com.flightapp.booking.messaging;

import com.flightapp.booking.dto.EmailNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

    @InjectMocks
    private EmailConsumer emailConsumer;

    private EmailNotification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = EmailNotification.builder()
                .to("test@example.com")
                .subject("Booking Confirmation")
                .pnr("PNR12345678")
                .flightNumber("AI101")
                .passengerName("John Doe")
                .body("Your booking is confirmed")
                .build();
    }

    @Test
    void testConsumeEmailNotification_Success() {
        assertDoesNotThrow(() -> emailConsumer.consumeEmailNotification(testNotification));
    }

    @Test
    void testConsumeEmailNotification_NullValues() {
        EmailNotification notificationWithNulls = EmailNotification.builder()
                .to(null)
                .subject(null)
                .pnr(null)
                .build();

        assertDoesNotThrow(() -> emailConsumer.consumeEmailNotification(notificationWithNulls));
    }

    @Test
    void testConsumeEmailNotification_EmptyValues() {
        EmailNotification emptyNotification = EmailNotification.builder()
                .to("")
                .subject("")
                .pnr("")
                .flightNumber("")
                .passengerName("")
                .build();

        assertDoesNotThrow(() -> emailConsumer.consumeEmailNotification(emptyNotification));
    }

    @Test
    void testConsumeEmailNotification_MultipleMessages() {
        assertDoesNotThrow(() -> {
            emailConsumer.consumeEmailNotification(testNotification);
            emailConsumer.consumeEmailNotification(testNotification);
            emailConsumer.consumeEmailNotification(testNotification);
        });
    }

    @Test
    void testConsumeEmailNotification_LongBody() {
        EmailNotification longBodyNotification = EmailNotification.builder()
                .to("test@example.com")
                .subject("Booking Confirmation")
                .pnr("PNR12345678")
                .body("A".repeat(10000))
                .build();

        assertDoesNotThrow(() -> emailConsumer.consumeEmailNotification(longBodyNotification));
    }

    @Test
    void testConsumeEmailNotification_SpecialCharacters() {
        EmailNotification specialCharsNotification = EmailNotification.builder()
                .to("test+special@example.com")
                .subject("Booking: PNR#12345678 - (Confirmed!)")
                .pnr("PNR@12345678")
                .passengerName("John O'Doe")
                .build();

        assertDoesNotThrow(() -> emailConsumer.consumeEmailNotification(specialCharsNotification));
    }
}
